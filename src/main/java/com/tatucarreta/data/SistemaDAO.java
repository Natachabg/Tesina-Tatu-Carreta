package com.tatucarreta.data;

import com.tatucarreta.config.ConexionSQLite;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Acceso a datos centralizado. Todas las bajas son lógicas. */
public class SistemaDAO {
        private static volatile boolean observacionesMovimientosSincronizadas;

        public record Opcion(int id, String texto) {
                public String toString() {
                        return texto;
                }
        }

        public record Fila(int id, List<String> c) {
        }

        private static Connection c() {
                return ConexionSQLite.conectar();
        }

        public static List<Fila> filas(String sql, String... ps) {
                List<Fila> out = new ArrayList<>();
                try (Connection cn = c(); PreparedStatement p = cn.prepareStatement(sql)) {
                        for (int i = 0; i < ps.length; i++)
                                p.setString(i + 1, ps[i]);
                        try (ResultSet r = p.executeQuery()) {
                                ResultSetMetaData m = r.getMetaData();
                                while (r.next()) {
                                        List<String> v = new ArrayList<>();
                                        int id = 0;
                                        for (int i = 1; i <= m.getColumnCount(); i++) {
                                                Object x = r.getObject(i);
                                                if (i == 1 && x instanceof Number)
                                                        id = ((Number) x).intValue();
                                                v.add(x == null ? "" : String.valueOf(x));
                                        }
                                        out.add(new Fila(id, v));
                                }
                        }
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }
                return out;
        }

        public static int update(String sql, Object... ps) {
                try (Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        for (int i = 0; i < ps.length; i++)
                                p.setObject(i + 1, ps[i]);
                        p.executeUpdate();
                        try (ResultSet r = p.getGeneratedKeys()) {
                                return r.next() ? r.getInt(1) : 0;
                        }
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }
        }

        public static String scalar(String sql) {
                try (Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(sql);
                                ResultSet r = p.executeQuery()) {
                        return r.next() ? r.getString(1) : "0";
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }
        }

        private static boolean existe(String sql, Object... ps) {
                try (Connection cn = c(); PreparedStatement p = cn.prepareStatement(sql)) {
                        for (int i = 0; i < ps.length; i++)
                                p.setObject(i + 1, ps[i]);
                        try (ResultSet r = p.executeQuery()) {
                                return r.next() && r.getInt(1) > 0;
                        }
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }
        }

        public static List<Opcion> opciones(String sql) {
                List<Opcion> l = new ArrayList<>();
                try (Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(sql);
                                ResultSet r = p.executeQuery()) {
                        while (r.next())
                                l.add(new Opcion(r.getInt(1), r.getString(2)));
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }
                return l;
        }

        public static String hoy() {
                return LocalDate.now().toString();
        }

        /**
         * Revierte un movimiento activo sin eliminarlo del historial.
         *
         * - Si el movimiento salió del Plantel, devuelve la cantidad al Plantel.
         * - Si el movimiento ingresó al Plantel desde un Acta, descuenta la
         * cantidad que ese movimiento había agregado.
         * - El movimiento queda en estado Inactivo y conserva su registro,
         * agregando una marca de REVERTIDO en sus observaciones.
         * - Se reconstruyen las observaciones de los habitáculos afectados.
         */
        public static void revertirMovimiento(int idMovimiento) {

                if (idMovimiento <= 0) {
                        throw new IllegalArgumentException(
                                        "El movimiento seleccionado no es válido.");
                }

                Connection cn = null;

                try {
                        cn = c();
                        cn.setAutoCommit(false);

                        int idDetalle = 0;
                        int idPlantel = 0;
                        int cantidad = 0;
                        int animal = 0;
                        int estadoActivo = 0;
                        String tipo = "";
                        String fecha = "";

                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT COALESCE(m.id_detalle,0), " +
                                                        "COALESCE(m.id_plantel,0), " +
                                                        "COALESCE(m.cantidad,0), " +
                                                        "COALESCE(p.id_animal,0), " +
                                                        "COALESCE(m.tipo_movimiento,''), " +
                                                        "COALESCE(m.fecha_movimiento,''), " +
                                                        "CASE WHEN m.estado='Activo' THEN 1 ELSE 0 END " +
                                                        "FROM movimientos m " +
                                                        "LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                        "WHERE m.id_movimiento=?")) {

                                ps.setInt(1, idMovimiento);

                                try (ResultSet rs = ps.executeQuery()) {
                                        if (!rs.next()) {
                                                throw new IllegalArgumentException(
                                                                "El movimiento seleccionado no existe.");
                                        }

                                        idDetalle = rs.getInt(1);
                                        idPlantel = rs.getInt(2);
                                        cantidad = rs.getInt(3);
                                        animal = rs.getInt(4);
                                        tipo = rs.getString(5);
                                        fecha = rs.getString(6);
                                        estadoActivo = rs.getInt(7);
                                }
                        }

                        if (estadoActivo != 1) {
                                throw new IllegalArgumentException(
                                                "El movimiento seleccionado ya no está activo y no puede revertirse nuevamente.");
                        }

                        if (cantidad <= 0) {
                                throw new IllegalArgumentException(
                                                "El movimiento no tiene una cantidad válida para revertir.");
                        }

                        int habitaculoAfectado = 0;

                        if (idPlantel > 0) {
                                try (PreparedStatement ps = cn.prepareStatement(
                                                "SELECT COALESCE(id_habitaculo,0) " +
                                                                "FROM plantel_permanente WHERE id_plantel=?")) {
                                        ps.setInt(1, idPlantel);
                                        try (ResultSet rs = ps.executeQuery()) {
                                                if (rs.next()) {
                                                        habitaculoAfectado = rs.getInt(1);
                                                }
                                        }
                                }
                        }

                        /*
                         * Un movimiento con id_detalle pertenece a un Acta.
                         * Cuando además tiene id_plantel, ese Plantel es el destino
                         * creado/actualizado por el traslado hacia Plantel Permanente.
                         * Por eso, al revertirlo, se descuenta la cantidad ingresada.
                         *
                         * Un movimiento sin id_detalle y con id_plantel corresponde
                         * al movimiento realizado desde un Plantel existente. En
                         * ese caso hay que devolver la cantidad al origen.
                         */
                        if (idPlantel > 0) {

                                if (idDetalle > 0) {

                                        int actual = cantidadPlantelConConexion(
                                                        cn,
                                                        idPlantel);

                                        if (actual < cantidad) {
                                                throw new IllegalArgumentException(
                                                                "No se puede revertir el movimiento porque el Plantel ya no conserva la cantidad necesaria para deshacerlo.");
                                        }

                                        updateConConexion(
                                                        cn,
                                                        "UPDATE plantel_permanente SET " +
                                                                        "cantidad=MAX(cantidad-?,0), " +
                                                                        "estado=CASE WHEN cantidad-?<=0 THEN 'Inactivo' ELSE 'Activo' END "
                                                                        +
                                                                        "WHERE id_plantel=?",
                                                        cantidad,
                                                        cantidad,
                                                        idPlantel);

                                } else {

                                        updateConConexion(
                                                        cn,
                                                        "UPDATE plantel_permanente SET " +
                                                                        "cantidad=cantidad+?, estado='Activo' " +
                                                                        "WHERE id_plantel=?",
                                                        cantidad,
                                                        idPlantel);
                                }
                        }

                        String marcaReversion = formatearFechaMovimiento(fecha) +
                                        " - REVERTIDO - Movimiento #" +
                                        idMovimiento;

                        String observacionMovimiento = "";

                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT COALESCE(observaciones,'') FROM movimientos WHERE id_movimiento=?")) {
                                ps.setInt(1, idMovimiento);
                                try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                                observacionMovimiento = rs.getString(1);
                                        }
                                }
                        }

                        if (observacionMovimiento == null) {
                                observacionMovimiento = "";
                        }

                        if (!observacionMovimiento.contains("REVERTIDO - Movimiento #" + idMovimiento)) {
                                observacionMovimiento = observacionMovimiento.isBlank()
                                                ? marcaReversion
                                                : observacionMovimiento.trim() + "\n" + marcaReversion;
                        }

                        updateConConexion(
                                        cn,
                                        "UPDATE movimientos SET estado='Inactivo', observaciones=? WHERE id_movimiento=?",
                                        observacionMovimiento,
                                        idMovimiento);

                        // El Acta individual conserva la trazabilidad de la reversión.
                        if (idDetalle > 0) {
                                int ingreso = ingresoDeDetalleConConexion(cn, idDetalle);

                                if (ingreso > 0) {
                                        agregarObservacionActaConConexion(
                                                        cn,
                                                        ingreso,
                                                        marcaReversion);
                                }
                        }

                        // El Acta General conserva también la trazabilidad de la reversión.
                        agregarObservacionActaGeneralConConexion(
                                        cn,
                                        idMovimiento,
                                        fecha,
                                        "REVERTIDO",
                                        cantidad,
                                        animal > 0
                                                        ? nombreAnimalConConexion(cn, animal)
                                                        : "",
                                        "");

                        if (habitaculoAfectado > 0) {
                                actualizarObservacionHabitaculoConConexion(
                                                cn,
                                                habitaculoAfectado);
                        }

                        if (animal > 0) {
                                recalcularCantidadAnimalConConexion(
                                                cn,
                                                animal);
                        }

                        cn.commit();

                } catch (Exception e) {

                        if (cn != null) {
                                try {
                                        cn.rollback();
                                } catch (Exception ignored) {
                                }
                        }

                        if (e instanceof IllegalArgumentException) {
                                throw (IllegalArgumentException) e;
                        }

                        throw new RuntimeException(
                                        "No se pudo revertir el movimiento: " + e.getMessage(),
                                        e);

                } finally {

                        if (cn != null) {
                                try {
                                        cn.setAutoCommit(true);
                                } catch (Exception ignored) {
                                }

                                try {
                                        cn.close();
                                } catch (Exception ignored) {
                                }
                        }
                }
        }

        public static void baja(String tabla, String pk, int id) {
                update("UPDATE " + tabla + " SET estado='Inactivo' WHERE " + pk + "=?", id);
        }

        public static void activar(String tabla, String pk, int id) {

                // Tratamiento especial para los habitáculos.
                if ("habitaculos".equalsIgnoreCase(tabla)) {

                        String observaciones = scalar(
                                        "SELECT COALESCE(observaciones,'') FROM "
                                                        + tabla
                                                        + " WHERE "
                                                        + pk
                                                        + "="
                                                        + id);

                        if (observaciones != null && !"0".equals(observaciones)) {

                                StringBuilder observacionesFinales = new StringBuilder();

                                for (String linea : observaciones.split("\\R")) {

                                        if (linea.trim().startsWith(
                                                        "Motivo de inactivación (")) {
                                                continue;
                                        }

                                        if (observacionesFinales.length() > 0) {
                                                observacionesFinales.append("\n");
                                        }

                                        observacionesFinales.append(linea);
                                }

                                update(
                                                "UPDATE " + tabla
                                                                + " SET estado='Activo', observaciones=? WHERE "
                                                                + pk + "=?",
                                                observacionesFinales.toString().trim(),
                                                id);

                                return;
                        }
                }

                // Comportamiento original para las demás entidades.
                update(
                                "UPDATE " + tabla + " SET estado='Activo' WHERE " + pk + "=?",
                                id);
        }

        // ESPECIES

        public static List<Fila> especies(String q) {

                return filas(
                                "SELECT id_especie,nombre,estado " +
                                                "FROM especies " +
                                                "WHERE estado='Activo' " +
                                                "AND nombre LIKE ? " +
                                                "ORDER BY nombre",
                                "%" + q + "%");
        }

        public static List<Fila> especiesInactivas(String q) {

                return filas(
                                "SELECT id_especie,nombre,estado " +
                                                "FROM especies " +
                                                "WHERE estado='Inactivo' " +
                                                "AND nombre LIKE ? " +
                                                "ORDER BY nombre",
                                "%" + q + "%");
        }

        // Compatibilidad con las vistas existentes que solo actualizan el nombre.
        // En altas se crea Activa; en modificaciones se conserva el estado actual.
        public static int guardarEspecie(int id, String nombre) {
                String estadoActual = id <= 0
                                ? "Activo"
                                : scalar("SELECT COALESCE(estado,'Activo') FROM especies WHERE id_especie=" + id);
                return guardarEspecie(id, nombre, estadoActual);
        }

        public static int guardarEspecie(int id, String nombre, String estado) {
                if (nombre == null || nombre.isBlank()) {
                        throw new IllegalArgumentException("El nombre de la especie es obligatorio.");
                }
                String estadoFinal = estado == null ? "" : estado.trim();
                if (!"Activo".equals(estadoFinal) && !"Inactivo".equals(estadoFinal)) {
                        throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
                }
                if ("Inactivo".equals(estadoFinal) && id > 0 && !especiePuedeEliminar(id)) {
                        throw new IllegalArgumentException(
                                        "No se puede inactivar esta especie porque tiene animales asociados.");
                }
                if (existe(
                                "SELECT 1 FROM especies WHERE LOWER(TRIM(nombre))=LOWER(TRIM(?)) AND id_especie<>?",
                                nombre.trim(),
                                id)) {
                        throw new IllegalArgumentException("Ya existe una especie con ese nombre.");
                }
                if (id == 0) {
                        return update("INSERT INTO especies(nombre,estado) VALUES(?,?)", nombre.trim(), estadoFinal);
                }
                update("UPDATE especies SET nombre=?, estado=? WHERE id_especie=?", nombre.trim(), estadoFinal, id);
                return id;
        }

        public static boolean especiePuedeEliminar(int id) {

                return Integer.parseInt(
                                scalar(
                                                "SELECT COUNT(*) " +
                                                                "FROM animales " +
                                                                "WHERE id_especie=" + id)) == 0;
        }

        // ANIMALES
        public static List<Fila> animales(String q) {

                return filas(
                                "SELECT " +
                                                "a.id_animal," +
                                                "a.nombre_vulgar," +
                                                "COALESCE(e.nombre,'')," +
                                                "COALESCE(a.nombre_cientifico,'')," +

                                                /*
                                                 * CANTIDAD ACTUAL DENTRO DE LA RESERVA
                                                 *
                                                 * 1. Cantidad activa del Plantel Permanente.
                                                 *
                                                 * 2. Cantidad todavía disponible de los ingresos
                                                 * mediante actas.
                                                 *
                                                 * Los movimientos de acta distintos de 'Ingreso'
                                                 * descuentan la cantidad disponible.
                                                 */
                                                "(" +

                                                "COALESCE((" +
                                                "SELECT SUM(p.cantidad) " +
                                                "FROM plantel_permanente p " +
                                                "WHERE p.id_animal=a.id_animal " +
                                                "AND p.estado='Activo' " +
                                                "AND p.cantidad>0" +
                                                "),0) " +

                                                "+" +

                                                "COALESCE((" +
                                                "SELECT SUM(" +
                                                "MAX(" +
                                                "d.cantidad - COALESCE((" +
                                                "SELECT SUM(m.cantidad) " +
                                                "FROM movimientos m " +
                                                "WHERE m.id_detalle=d.id_detalle " +
                                                "AND m.estado='Activo' " +
                                                "AND m.tipo_movimiento<>'Ingreso'" +
                                                "),0)," +
                                                "0)" +
                                                ") " +
                                                "FROM detalle_ingreso d " +
                                                "JOIN ingresos i " +
                                                "ON i.id_ingreso=d.id_ingreso " +
                                                "WHERE d.id_animal=a.id_animal " +
                                                "AND d.estado='Activo' " +
                                                "AND i.estado='Activo'" +
                                                "),0)" +

                                                ")," +

                                                "COALESCE(a.origen,'')," +
                                                "a.estado " +

                                                "FROM animales a " +

                                                "LEFT JOIN especies e " +
                                                "ON e.id_especie=a.id_especie " +

                                                "WHERE a.estado='Activo' " +

                                                "AND (" +

                                                "a.nombre_vulgar LIKE ? " +
                                                "OR COALESCE(e.nombre,'') LIKE ? " +
                                                "OR COALESCE(a.nombre_cientifico,'') LIKE ?" +

                                                ") " +

                                                /*
                                                 * Solo mostramos animales que realmente
                                                 * tienen existencia dentro de la reserva.
                                                 */
                                                "AND (" +

                                                "COALESCE((" +
                                                "SELECT SUM(p.cantidad) " +
                                                "FROM plantel_permanente p " +
                                                "WHERE p.id_animal=a.id_animal " +
                                                "AND p.estado='Activo' " +
                                                "AND p.cantidad>0" +
                                                "),0) " +

                                                "+" +

                                                "COALESCE((" +
                                                "SELECT SUM(" +
                                                "MAX(" +
                                                "d.cantidad - COALESCE((" +
                                                "SELECT SUM(m.cantidad) " +
                                                "FROM movimientos m " +
                                                "WHERE m.id_detalle=d.id_detalle " +
                                                "AND m.estado='Activo' " +
                                                "AND m.tipo_movimiento<>'Ingreso'" +
                                                "),0)," +
                                                "0)" +
                                                ") " +
                                                "FROM detalle_ingreso d " +
                                                "JOIN ingresos i " +
                                                "ON i.id_ingreso=d.id_ingreso " +
                                                "WHERE d.id_animal=a.id_animal " +
                                                "AND d.estado='Activo' " +
                                                "AND i.estado='Activo'" +
                                                "),0)" +

                                                ") > 0 " +

                                                "ORDER BY a.nombre_vulgar",

                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%");
        }

        public static List<Fila> animalesInactivos(String q) {
                return filas(
                                "SELECT a.id_animal,a.nombre_vulgar,COALESCE(e.nombre,''),COALESCE(a.nombre_cientifico,''),a.cantidad_actual,COALESCE(a.origen,''),a.estado FROM animales a LEFT JOIN especies e ON e.id_especie=a.id_especie WHERE a.estado='Inactivo' AND (a.nombre_vulgar LIKE ? OR COALESCE(e.nombre,'') LIKE ?) ORDER BY a.nombre_vulgar",
                                "%" + q + "%", "%" + q + "%");
        }

        public static int guardarAnimal(int id, int esp, String vul, String cient, String origen) {
                if (esp <= 0 || !existe("SELECT 1 FROM especies WHERE id_especie=? AND estado='Activo'", esp)) {
                        throw new IllegalArgumentException("Seleccioná una especie activa.");
                }
                if (vul == null || vul.isBlank()) {
                        throw new IllegalArgumentException("El nombre vulgar del animal es obligatorio.");
                }
                if (cient == null || cient.isBlank()) {
                        throw new IllegalArgumentException("El nombre científico del animal es obligatorio.");
                }
                if (existe(
                                "SELECT 1 FROM animales WHERE estado='Activo' AND id_especie=? " +
                                                "AND LOWER(TRIM(nombre_vulgar))=LOWER(TRIM(?)) " +
                                                "AND LOWER(TRIM(nombre_cientifico))=LOWER(TRIM(?)) AND id_animal<>?",
                                esp, vul.trim(), cient.trim(), id)) {
                        throw new IllegalArgumentException(
                                        "Ya existe ese animal en el catálogo para la especie seleccionada.");
                }
                if (id == 0) {
                        return update(
                                        "INSERT INTO animales(id_especie,nombre_vulgar,nombre_cientifico,cantidad_actual,origen,estado) "
                                                        +
                                                        "VALUES(?,?,?,0,?,'Activo')",
                                        esp, vul.trim(), cient.trim(), origen);
                }
                update(
                                "UPDATE animales SET id_especie=?,nombre_vulgar=?,nombre_cientifico=?,origen=? WHERE id_animal=?",
                                esp, vul.trim(), cient.trim(), origen, id);
                return id;
        }

        public static boolean animalPuedeEliminar(int id) {
                return Integer.parseInt(scalar("SELECT COUNT(*) FROM detalle_ingreso WHERE id_animal=" + id))
                                + Integer.parseInt(scalar(
                                                "SELECT COUNT(*) FROM plantel_permanente WHERE id_animal=" + id)) == 0;
        }

        public static List<Opcion> animalesOpciones() {
                return opciones("SELECT id_animal,nombre_vulgar FROM animales WHERE estado='Activo' ORDER BY nombre_vulgar");
        }

        public static List<Opcion> especiesOpciones() {
                return opciones("SELECT id_especie,nombre FROM especies WHERE estado='Activo' ORDER BY nombre");
        }

        // HABITACULOS
        public static List<Fila> habitaculos(String q) {
                sincronizarObservacionesHabitaculos();
                return filas(
                                "SELECT id_habitaculo,nombre,COALESCE(sector,''),COALESCE(capacidad,''),estado,COALESCE(observaciones,'') FROM habitaculos WHERE estado='Activo' AND (nombre LIKE ? OR sector LIKE ?) ORDER BY nombre",
                                "%" + q + "%", "%" + q + "%");
        }

        private static void sincronizarObservacionesHabitaculos() {
                try (Connection cn = c()) {
                        List<Integer> ids = new ArrayList<>();
                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT id_habitaculo FROM habitaculos WHERE estado='Activo'");
                                        ResultSet rs = ps.executeQuery()) {
                                while (rs.next())
                                        ids.add(rs.getInt(1));
                        }
                        for (int id : ids)
                                actualizarObservacionHabitaculoConConexion(cn, id);
                } catch (SQLException e) {
                        throw new RuntimeException("No se pudieron actualizar las observaciones de los habitáculos.",
                                        e);
                }
        }

        public static List<Fila> habitaculosInactivos(String q) {
                return filas(
                                "SELECT id_habitaculo,nombre,COALESCE(sector,''),COALESCE(capacidad,''),estado,COALESCE(observaciones,'') FROM habitaculos WHERE estado='Inactivo' AND (nombre LIKE ? OR sector LIKE ?) ORDER BY nombre",
                                "%" + q + "%", "%" + q + "%");
        }

        public static int guardarHabitaculo(
                        int id,
                        String n,
                        String s,
                        int cap,
                        String obs) {

                String nombre = n == null
                                ? ""
                                : n.trim();

                String sector = s == null
                                ? ""
                                : s.trim();

                String observaciones = obs == null
                                ? ""
                                : obs.trim();

                if (nombre.isBlank()) {

                        throw new IllegalArgumentException(
                                        "El nombre del habitáculo es obligatorio.");
                }

                if (sector.isBlank()) {

                        throw new IllegalArgumentException(
                                        "El sector es obligatorio.");
                }

                if (cap <= 0) {

                        throw new IllegalArgumentException(
                                        "La capacidad debe ser mayor que cero.");
                }

                // ============================================================
                // SECTOR ÚNICO
                // ============================================================

                if (existe(
                                "SELECT 1 " +
                                                "FROM habitaculos " +
                                                "WHERE estado='Activo' " +
                                                "AND LOWER(TRIM(sector))=" +
                                                "LOWER(TRIM(?)) " +
                                                "AND id_habitaculo<>?",
                                sector,
                                id)) {

                        throw new IllegalArgumentException(
                                        "Ya existe un habitáculo activo en ese sector.");
                }

                // ============================================================
                // AL EDITAR, NO PERMITIR CAPACIDAD MENOR A LA OCUPACIÓN
                // ============================================================

                if (id > 0) {

                        int ocupacion = Integer.parseInt(
                                        scalar(
                                                        "SELECT COALESCE(SUM(cantidad),0) " +
                                                                        "FROM plantel_permanente " +
                                                                        "WHERE id_habitaculo=" +
                                                                        id +
                                                                        " AND estado='Activo' " +
                                                                        " AND cantidad>0"));

                        if (cap < ocupacion) {

                                throw new IllegalArgumentException(
                                                "La capacidad no puede ser menor que la ocupación actual.\n\n" +
                                                                "Ocupación actual: " +
                                                                ocupacion +
                                                                "\nNueva capacidad: " +
                                                                cap);
                        }
                }

                if (id == 0) {

                        int habitaculosRegistrados = Integer.parseInt(
                                        scalar("SELECT COUNT(*) FROM habitaculos"));
                        if (habitaculosRegistrados >= 10) {
                                throw new IllegalArgumentException(
                                                "No se pueden crear más de 10 habitáculos.");
                        }

                        return update(
                                        "INSERT INTO habitaculos(" +
                                                        "nombre," +
                                                        "sector," +
                                                        "capacidad," +
                                                        "estado," +
                                                        "observaciones" +
                                                        ") VALUES(?,?,?,'Activo',?)",

                                        nombre,
                                        sector,
                                        cap,
                                        observaciones);

                }

                update(
                                "UPDATE habitaculos SET " +
                                                "nombre=?," +
                                                "sector=?," +
                                                "capacidad=?," +
                                                "observaciones=? " +
                                                "WHERE id_habitaculo=?",

                                nombre,
                                sector,
                                cap,
                                observaciones,
                                id);

                return id;
        }

        public static void inactivarHabitaculoConMotivo(int id, String motivo) {
                if (id <= 0) {
                        throw new IllegalArgumentException("El habitáculo seleccionado no es válido.");
                }
                String motivoFinal = motivo == null ? "" : motivo.trim();
                if (motivoFinal.isBlank()) {
                        throw new IllegalArgumentException("El motivo de inactivación es obligatorio.");
                }

                Connection cn = null;
                try {
                        cn = c();
                        cn.setAutoCommit(false);

                        int ocupacion = Integer.parseInt(scalarConConexion(
                                        cn,
                                        "SELECT COUNT(*) FROM plantel_permanente " +
                                                        "WHERE id_habitaculo=? AND estado='Activo' AND cantidad>0",
                                        id));
                        if (ocupacion > 0) {
                                throw new IllegalArgumentException(
                                                "No se puede inactivar este habitáculo porque conserva animales asociados.");
                        }

                        String observaciones = scalarConConexion(
                                        cn,
                                        "SELECT COALESCE(observaciones,'') FROM habitaculos " +
                                                        "WHERE id_habitaculo=? AND estado='Activo'",
                                        id);
                        if (observaciones == null || "0".equals(observaciones)) {
                                throw new IllegalArgumentException("El habitáculo no existe o ya está inactivo.");
                        }

                        String nota = "Motivo de inactivación (" + LocalDate.now() + "): " + motivoFinal;
                        String nuevasObservaciones = observaciones.isBlank()
                                        || "Disponible.".equalsIgnoreCase(observaciones.trim())
                                                        ? nota
                                                        : observaciones.trim() + "\n" + nota;
                        updateConConexion(
                                        cn,
                                        "UPDATE habitaculos SET observaciones=?, estado='Inactivo' " +
                                                        "WHERE id_habitaculo=? AND estado='Activo'",
                                        nuevasObservaciones,
                                        id);
                        cn.commit();
                } catch (Exception e) {
                        if (cn != null) {
                                try {
                                        cn.rollback();
                                } catch (Exception ignored) {
                                }
                        }
                        if (e instanceof IllegalArgumentException) {
                                throw (IllegalArgumentException) e;
                        }
                        throw new RuntimeException("No se pudo guardar el motivo de inactivación: " + e.getMessage(),
                                        e);
                } finally {
                        if (cn != null) {
                                try {
                                        cn.setAutoCommit(true);
                                } catch (Exception ignored) {
                                }
                                try {
                                        cn.close();
                                } catch (Exception ignored) {
                                }
                        }
                }
        }

        public static boolean habitaculoPuedeEliminar(
                        int id) {

                return Integer.parseInt(
                                scalar(
                                                "SELECT COUNT(*) " +
                                                                "FROM plantel_permanente " +
                                                                "WHERE id_habitaculo=" +
                                                                id +
                                                                " AND estado='Activo' " +
                                                                " AND cantidad>0")) == 0;
        }

        public static List<Opcion> habitaculoOpciones() {
                return opciones("SELECT id_habitaculo,nombre FROM habitaculos WHERE estado='Activo' ORDER BY nombre");
        }

        public static int ocupacionHabitaculo(
                        int id,
                        int excluirPlantel) {

                return Integer.parseInt(
                                scalar(
                                                "SELECT COALESCE(SUM(p.cantidad),0) " +
                                                                "FROM plantel_permanente p " +
                                                                "JOIN animales a " +
                                                                "ON a.id_animal=p.id_animal " +
                                                                "WHERE p.id_habitaculo=" +
                                                                id +
                                                                " AND p.estado='Activo' " +
                                                                " AND p.cantidad>0 " +
                                                                " AND p.id_plantel<>" +
                                                                excluirPlantel));
        }

        public static int capacidadHabitaculo(int id) {
                return Integer.parseInt(
                                scalar("SELECT COALESCE(capacidad,0) FROM habitaculos WHERE id_habitaculo=" + id));
        }

        // INGRESOS / ACTAS
        public static List<Fila> ingresos(String q) {
                return filas(
                                "SELECT id_ingreso,numero_acta,fecha_ingreso,COALESCE(organismo_procedencia,''),COALESCE(responsable_entrega,''),COALESCE(recibe_reserva,''),COALESCE(procedencia,''),COALESCE(motivo_ingreso,''),estado FROM ingresos WHERE estado='Activo' AND COALESCE(tipo_ingreso,'INDIVIDUAL')='INDIVIDUAL' AND (numero_acta LIKE ? OR organismo_procedencia LIKE ? OR procedencia LIKE ?) ORDER BY fecha_ingreso DESC",
                                "%" + q + "%", "%" + q + "%", "%" + q + "%");
        }

        public static List<Fila> ingresosInactivos(String q) {
                return filas(
                                "SELECT id_ingreso,numero_acta,fecha_ingreso,COALESCE(organismo_procedencia,''),COALESCE(responsable_entrega,''),COALESCE(recibe_reserva,''),COALESCE(procedencia,''),COALESCE(motivo_ingreso,''),estado FROM ingresos WHERE estado='Inactivo' AND COALESCE(tipo_ingreso,'INDIVIDUAL')='INDIVIDUAL' AND (numero_acta LIKE ? OR organismo_procedencia LIKE ? OR procedencia LIKE ?) ORDER BY fecha_ingreso DESC",
                                "%" + q + "%", "%" + q + "%", "%" + q + "%");
        }

        public static int guardarIngreso(int id, String acta, String fecha, String org, String resp, String recibe,
                        String proc, String mot, String obs) {
                if (resp == null || resp.isBlank())
                        throw new IllegalArgumentException("El responsable de entrega es obligatorio.");
                if (recibe == null || recibe.isBlank())
                        throw new IllegalArgumentException("La persona que recibe en la Reserva es obligatoria.");
                if (existe("SELECT 1 FROM ingresos WHERE estado='Activo' AND COALESCE(tipo_ingreso,'INDIVIDUAL')='INDIVIDUAL' AND TRIM(numero_acta)=TRIM(?) AND id_ingreso<>?",
                                acta,
                                id))
                        throw new IllegalArgumentException("Ya existe un acta activa con ese número.");
                if (id == 0)
                        return update(
                                        "INSERT INTO ingresos(numero_acta,fecha_ingreso,organismo_procedencia,responsable_entrega,recibe_reserva,procedencia,motivo_ingreso,observaciones,estado) VALUES(?,?,?,?,?,?,?, ?,'Activo')",
                                        acta, fecha, org, resp, recibe, proc, mot, obs);
                update("UPDATE ingresos SET numero_acta=?,fecha_ingreso=?,organismo_procedencia=?,responsable_entrega=?,recibe_reserva=?,procedencia=?,motivo_ingreso=?,observaciones=? WHERE id_ingreso=?",
                                acta, fecha, org, resp, recibe, proc, mot, obs, id);
                return id;
        }

        public static boolean ingresoPuedeBaja(int id) {
                return Integer.parseInt(scalar("SELECT COUNT(*) FROM detalle_ingreso WHERE id_ingreso=" + id)) == 0;
        }

        /**
         * Devuelve el resumen REAL de movimientos de un Acta individual.
         *
         * IMPORTANTE:
         * - Solo considera movimientos con estado='Activo'.
         * - Suma CANTIDADES, no cantidad de registros.
         * - Un movimiento revertido queda Inactivo y vuelve a liberar
         * automáticamente su cantidad disponible.
         *
         * Resultado:
         * [0] Traslados
         * [1] Fallecimientos
         * [2] Nacimientos
         * [3] Liberaciones
         *
         * Los nacimientos no se registran desde un Acta individual
         * (la propia lógica de movimientos los impide), por lo que
         * normalmente este valor será 0 aquí. Se mantiene en el resumen
         * para conservar la estructura de las tarjetas y permitir la
         * evolución del sistema sin romper la Vista.
         */
        public static int[] resumenMovimientosActa(int ingreso) {

                if (ingreso <= 0) {
                        return new int[] { 0, 0, 0, 0 };
                }

                int trasladados = 0;
                int fallecimientos = 0;
                int nacimientos = 0;
                int liberaciones = 0;

                String sql = "SELECT " +
                                "COALESCE(SUM(CASE " +
                                "WHEN LOWER(TRIM(m.tipo_movimiento))='traslado' " +
                                "THEN m.cantidad ELSE 0 END),0), " +
                                "COALESCE(SUM(CASE " +
                                "WHEN LOWER(TRIM(m.tipo_movimiento))='fallecimiento' " +
                                "THEN m.cantidad ELSE 0 END),0), " +
                                "COALESCE(SUM(CASE " +
                                "WHEN LOWER(TRIM(m.tipo_movimiento))='nacimiento' " +
                                "THEN m.cantidad ELSE 0 END),0), " +
                                "COALESCE(SUM(CASE " +
                                "WHEN LOWER(TRIM(m.tipo_movimiento)) IN " +
                                "('liberación','liberacion') " +
                                "THEN m.cantidad ELSE 0 END),0) " +
                                "FROM movimientos m " +
                                "JOIN detalle_ingreso d " +
                                "ON d.id_detalle=m.id_detalle " +
                                "WHERE d.id_ingreso=? " +
                                "AND m.estado='Activo' " +
                                "AND d.estado='Activo'";

                try (Connection cn = c();
                                PreparedStatement ps = cn.prepareStatement(sql)) {

                        ps.setInt(1, ingreso);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (rs.next()) {
                                        trasladados = rs.getInt(1);
                                        fallecimientos = rs.getInt(2);
                                        nacimientos = rs.getInt(3);
                                        liberaciones = rs.getInt(4);
                                }

                        }

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "No se pudo calcular el resumen de movimientos del acta.",
                                        e);
                }

                return new int[] {
                                trasladados,
                                fallecimientos,
                                nacimientos,
                                liberaciones
                };
        }

        public static List<Fila> detallesIngreso(int ingreso) {
                return filas(
                                "SELECT d.id_detalle,a.nombre_vulgar,d.cantidad,COALESCE(d.sexo,''),COALESCE(d.edad,''),CASE WHEN d.peso IS NULL OR d.peso<=0 THEN '' ELSE printf('%.2f',d.peso) END,COALESCE(d.estado_ingreso,''),d.estado FROM detalle_ingreso d JOIN animales a ON a.id_animal=d.id_animal WHERE d.id_ingreso=? ORDER BY d.id_detalle",
                                String.valueOf(ingreso));
        }

        public static int guardarDetalle(
                        int id,
                        int ingreso,
                        int animal,
                        int cant,
                        String sexo,
                        String edad,
                        String peso,
                        String estado,
                        String obs,
                        Integer idIdentificacion) {

                if (existe(
                                "SELECT 1 FROM detalle_ingreso " +
                                                "WHERE estado='Activo' " +
                                                "AND id_ingreso=? " +
                                                "AND id_animal=? " +
                                                "AND id_detalle<>?",
                                ingreso,
                                animal,
                                id)) {

                        throw new IllegalArgumentException(
                                        "Ese animal ya está cargado en esta acta. " +
                                                        "Modificá el registro existente si necesitás cambiar la cantidad.");
                }

                Double pv = peso == null || peso.isBlank()
                                ? null
                                : Double.parseDouble(peso);

                // ============================================================
                // NUEVO DETALLE
                // ============================================================

                if (id == 0) {

                        int nuevo = update(
                                        "INSERT INTO detalle_ingreso(" +
                                                        "id_ingreso," +
                                                        "id_animal," +
                                                        "id_identificacion," +
                                                        "cantidad," +
                                                        "sexo," +
                                                        "edad," +
                                                        "peso," +
                                                        "estado_ingreso," +
                                                        "observaciones," +
                                                        "estado" +
                                                        ") VALUES(?,?,?,?,?,?,?,?,?,'Activo')",

                                        ingreso,
                                        animal,
                                        idIdentificacion,
                                        cant,
                                        sexo,
                                        edad,
                                        pv,
                                        estado,
                                        obs);

                        int movimientoIngreso = update(
                                        "INSERT INTO movimientos(" +
                                                        "id_detalle," +
                                                        "fecha_movimiento," +
                                                        "tipo_movimiento," +
                                                        "cantidad," +
                                                        "sexo," +
                                                        "destino," +
                                                        "observaciones," +
                                                        "estado" +
                                                        ") " +
                                                        "SELECT ?," +
                                                        "fecha_ingreso," +
                                                        "'Ingreso'," +
                                                        "?," +
                                                        "?," +
                                                        "?," +
                                                        "?," +
                                                        "'Activo' " +
                                                        "FROM ingresos " +
                                                        "WHERE id_ingreso=?",

                                        nuevo,
                                        cant,
                                        sexo,
                                        "Reserva Natural",
                                        obs,
                                        ingreso);

                        String animalNombreIngreso = scalar(
                                        "SELECT COALESCE(nombre_vulgar,'') " +
                                                        "FROM animales WHERE id_animal=" + animal);

                        String observacionIngreso = formatearFechaMovimiento(
                                        scalar("SELECT fecha_ingreso FROM ingresos WHERE id_ingreso=" + ingreso))
                                        + " - Ingreso de "
                                        + cant
                                        + " ejemplar(es) de "
                                        + animalNombreIngreso
                                        + " → Reserva Natural - Movimiento #"
                                        + movimientoIngreso;

                        agregarObservacionActa(
                                        ingreso,
                                        observacionIngreso);

                        recalcularCantidadAnimal(animal);

                        return nuevo;
                }

                // ============================================================
                // MODIFICAR DETALLE EXISTENTE
                // ============================================================

                int oldAnimal = 0;

                try (
                                Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(
                                                "SELECT id_animal " +
                                                                "FROM detalle_ingreso " +
                                                                "WHERE id_detalle=?")) {

                        p.setInt(1, id);

                        try (ResultSet r = p.executeQuery()) {

                                if (r.next()) {
                                        oldAnimal = r.getInt("id_animal");
                                }
                        }

                } catch (SQLException e) {

                        throw new RuntimeException(e);
                }

                update(
                                "UPDATE detalle_ingreso SET " +
                                                "id_animal=?," +
                                                "id_identificacion=?," +
                                                "cantidad=?," +
                                                "sexo=?," +
                                                "edad=?," +
                                                "peso=?," +
                                                "estado_ingreso=?," +
                                                "observaciones=? " +
                                                "WHERE id_detalle=?",

                                animal,
                                idIdentificacion,
                                cant,
                                sexo,
                                edad,
                                pv,
                                estado,
                                obs,
                                id);

                // Si cambió el animal, recalculamos ambos.
                if (oldAnimal != animal) {

                        recalcularCantidadAnimal(oldAnimal);
                        recalcularCantidadAnimal(animal);

                } else {

                        recalcularCantidadAnimal(animal);
                }

                // Actualizamos también el movimiento de ingreso.
                update(
                                "UPDATE movimientos SET " +
                                                "cantidad=?," +
                                                "sexo=?," +
                                                "observaciones=? " +
                                                "WHERE id_detalle=? " +
                                                "AND tipo_movimiento='Ingreso' " +
                                                "AND estado='Activo'",

                                cant,
                                sexo,
                                obs,
                                id);

                return id;
        }

        public static boolean puedeDesactivarDetalle(int id) {
                return true;
        }

        public static void bajaDetalle(int id) {
                int animal = 0;
                try (Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(
                                                "SELECT id_animal FROM detalle_ingreso WHERE id_detalle=? AND estado='Activo'")) {
                        p.setInt(1, id);
                        try (ResultSet r = p.executeQuery()) {
                                if (r.next())
                                        animal = r.getInt(1);
                        }
                } catch (SQLException e) {
                        throw new RuntimeException(e);
                }
                update("UPDATE detalle_ingreso SET estado='Inactivo' WHERE id_detalle=?", id);
                update("UPDATE movimientos SET estado='Inactivo' WHERE id_detalle=? AND tipo_movimiento='Ingreso'", id);
                if (animal > 0)
                        recalcularCantidadAnimal(animal);
        }

        public static List<Opcion> detallesOpciones() {
                return opciones(
                                "SELECT d.id_detalle,a.nombre_vulgar || ' · Acta ' || i.numero_acta FROM detalle_ingreso d JOIN animales a ON a.id_animal=d.id_animal JOIN ingresos i ON i.id_ingreso=d.id_ingreso WHERE d.estado='Activo' AND i.estado='Activo' AND COALESCE(i.tipo_ingreso,'INDIVIDUAL')='INDIVIDUAL' ORDER BY i.fecha_ingreso DESC");
        }

        // PLANTEl
        /**
         * Compatibilidad con movimientos internos antiguos guardados desde
         * VistaMovimientos: esos movimientos descontaban el origen, pero no
         * materializaban la ubicación de destino en plantel_permanente.
         * Se incorporan una sola vez; el número de movimiento queda anotado
         * en observaciones del registro destino para evitar duplicados.
         */
        private static void sincronizarTrasladosInternosPendientes() {
                List<Fila> movimientosPendientes = filas(
                                "SELECT m.id_movimiento, m.id_plantel, p.id_animal, m.cantidad, " +
                                                "COALESCE(m.fecha_movimiento,''), COALESCE(m.destino,''), " +
                                                "COALESCE(a.nombre_vulgar,'') " +
                                                "FROM movimientos m " +
                                                "JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                "JOIN animales a ON a.id_animal=p.id_animal " +
                                                "WHERE m.estado='Activo' AND LOWER(TRIM(m.tipo_movimiento))='traslado' "
                                                +
                                                "AND COALESCE(m.destino,'')<>'' " +
                                                "ORDER BY m.id_movimiento");

                for (Fila movimiento : movimientosPendientes) {
                        if (movimiento.c().size() < 7)
                                continue;
                        int idMovimiento = Integer.parseInt(movimiento.c().get(0));
                        int animal = Integer.parseInt(movimiento.c().get(2));
                        int cantidad = Integer.parseInt(movimiento.c().get(3));
                        String fecha = movimiento.c().get(4);
                        String destino = movimiento.c().get(5).trim();
                        String animalNombre = movimiento.c().get(6);
                        if (cantidad <= 0 || !destinoEsInternoPlantel(destino))
                                continue;
                        // The new Acta flow already creates the destination row.
                        // A generic legacy destination carries no habitat and
                        // must not be guessed as a habitat transfer.
                        if (destino.equalsIgnoreCase("Plantel Permanente"))
                                continue;

                        String tipoDestino;
                        Integer idHabitaculo = null;
                        if (destino.equalsIgnoreCase("Campo abierto")) {
                                tipoDestino = "Campo Abierto";
                        } else {
                                tipoDestino = "Habitáculo";
                                String nombreHab = extraerNombreHabitaculoDestino(destino);
                                if (nombreHab.isBlank())
                                        continue;
                                String idHab = scalar(
                                                "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                                + nombreHab.replace("'", "''") + "')) LIMIT 1");
                                if (idHab == null || idHab.isBlank())
                                        continue;
                                idHabitaculo = Integer.parseInt(idHab);
                        }

                        String condicionHab = idHabitaculo == null
                                        ? "id_habitaculo IS NULL"
                                        : "id_habitaculo=" + idHabitaculo;
                        String marca = "Movimiento #" + idMovimiento;
                        String yaSincronizado = scalar(
                                        "SELECT COUNT(*) FROM plantel_permanente WHERE id_animal=" + animal
                                                        + " AND LOWER(TRIM(tipo_ubicacion))=LOWER(TRIM('"
                                                        + tipoDestino.replace("'", "''") + "')) AND " + condicionHab
                                                        + " AND COALESCE(observaciones,'') LIKE '%"
                                                        + marca + "%' ");
                        if (yaSincronizado != null && Integer.parseInt(yaSincronizado) > 0)
                                continue;

                        String linea = formatearFechaMovimiento(fecha)
                                        + " - Traslado interno de " + cantidad + " ejemplar(es)"
                                        + (animalNombre.isBlank() ? "" : " de " + animalNombre)
                                        + " → " + destino + " - " + marca;
                        String destinoExistente = scalar(
                                        "SELECT id_plantel FROM plantel_permanente WHERE id_animal=" + animal
                                                        + " AND estado='Activo' AND LOWER(TRIM(tipo_ubicacion))=LOWER(TRIM('"
                                                        + tipoDestino.replace("'", "''") + "')) AND " + condicionHab
                                                        + " ORDER BY id_plantel LIMIT 1");
                        // scalar() devuelve "0" cuando la consulta no encuentra
                        // filas; ese valor debe llevar al INSERT, no al UPDATE id=0.
                        if (destinoExistente != null && !destinoExistente.isBlank()
                                        && !"0".equals(destinoExistente.trim())) {
                                int idPlantelDestino = Integer.parseInt(destinoExistente);
                                update("UPDATE plantel_permanente SET cantidad=cantidad+?, estado='Activo' WHERE id_plantel=?",
                                                cantidad, idPlantelDestino);
                                agregarObservacionPlantel(idPlantelDestino, linea);
                        } else {
                                int idPlantelDestino = update(
                                                "INSERT INTO plantel_permanente(id_animal,cantidad,tipo_ubicacion,id_habitaculo,fecha_ingreso_plantel,estado,observaciones) "
                                                                +
                                                                "VALUES(?,?,?,?,?,'Activo',?)",
                                                animal, cantidad, tipoDestino, idHabitaculo, fecha, linea);
                        }
                        if (idHabitaculo != null) {
                                try (Connection cn = c()) {
                                        actualizarObservacionHabitaculoConConexion(cn, idHabitaculo);
                                } catch (SQLException ex) {
                                        throw new RuntimeException(ex);
                                }
                        }
                }
        }

        public static List<Fila> plantel(String q) {

                sincronizarTrasladosInternosPendientes();
                String filtro = "%" + q + "%";
                /*
                 * La cantidad actual de cada ubicación se toma exclusivamente de
                 * plantel_permanente. Los movimientos son historial y permanecen
                 * ligados al Plantel de origen; usarlos para calcular el stock
                 * actual volvía a contar ejemplares que ya habían sido trasladados.
                 *
                 * Se conserva una fila por registro/ubicación para que el total
                 * mostrado coincida con p.cantidad y no dependa de los movimientos.
                 */
                return filas(
                                "SELECT p.id_plantel, " +
                                                "COALESCE(a.nombre_vulgar,'⚠ Sin animal asociado'), " +
                                                "COALESCE((SELECT GROUP_CONCAT(DISTINCT i.tipo_identificacion || ': ' || i.numero_identificacion) "
                                                +
                                                "FROM movimientos m " +
                                                "JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle " +
                                                "JOIN identificaciones i ON i.id_identificacion=d.id_identificacion " +
                                                "WHERE m.id_plantel=p.id_plantel AND m.estado='Activo' " +
                                                "AND d.estado='Activo' AND i.estado='Activo' " +
                                                "AND m.tipo_movimiento IN ('Traslado','Ingreso a Plantel') " +
                                                "AND d.id_animal=p.id_animal), 'Sin identificación'), " +
                                                "COALESCE(e.nombre,'Sin especie'), " +
                                                "p.cantidad, " +
                                                "COALESCE(p.tipo_ubicacion,''), " +
                                                "COALESCE(h.nombre,''), " +
                                                "COALESCE(p.fecha_ingreso_plantel,''), " +
                                                "p.estado " +
                                                "FROM plantel_permanente p " +
                                                "LEFT JOIN animales a ON a.id_animal=p.id_animal " +
                                                "LEFT JOIN especies e ON e.id_especie=a.id_especie " +
                                                "LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo " +
                                                "WHERE p.estado='Activo' AND p.cantidad>0 " +
                                                "AND (COALESCE(a.nombre_vulgar,'') LIKE ? " +
                                                "OR COALESCE(e.nombre,'') LIKE ? " +
                                                "OR COALESCE(h.nombre,'') LIKE ?) " +
                                                "ORDER BY a.nombre_vulgar, p.tipo_ubicacion, h.nombre",
                                filtro, filtro, filtro);
        }

        public static List<Fila> plantelInactivos(String q) {
                return filas(
                                "SELECT p.id_plantel,COALESCE(a.nombre_vulgar,'⚠ Sin animal asociado'),COALESCE((SELECT GROUP_CONCAT(i.tipo_identificacion || ': ' || i.numero_identificacion, ' | ') FROM identificaciones i WHERE i.id_animal=p.id_animal AND i.estado='Activo'),'Sin identificación'),COALESCE(e.nombre,'Sin especie'),p.cantidad,COALESCE(p.tipo_ubicacion,''),COALESCE(h.nombre,''),COALESCE(p.fecha_ingreso_plantel,''),p.estado FROM plantel_permanente p LEFT JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo WHERE p.estado='Inactivo' AND (COALESCE(a.nombre_vulgar,'') LIKE ? OR COALESCE(e.nombre,'') LIKE ? OR COALESCE(h.nombre,'') LIKE ? OR a.id_animal IS NULL) ORDER BY a.nombre_vulgar",
                                "%" + q + "%", "%" + q + "%", "%" + q + "%");
        }

        public static int guardarPlantel(int id, int animal, int cantidad, String tipo, Integer hab, String fecha,
                        String obs) {

                if (animal <= 0) {
                        throw new IllegalArgumentException(
                                        "El animal seleccionado no es válido.");
                }

                if (cantidad <= 0) {
                        throw new IllegalArgumentException(
                                        "La cantidad en Plantel debe ser mayor que cero.");
                }

                if (fecha == null || fecha.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La fecha de ingreso al Plantel es obligatoria.");
                }

                try {
                        java.time.LocalDate.parse(fecha.trim());
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "La fecha de ingreso al Plantel no es válida.");
                }

                String tipoFinal = tipo == null
                                ? ""
                                : tipo.trim();

                if (!"Habitáculo".equalsIgnoreCase(tipoFinal)
                                && !"Campo abierto".equalsIgnoreCase(tipoFinal)) {
                        throw new IllegalArgumentException(
                                        "La ubicación del Plantel debe ser Habitáculo o Campo abierto.");
                }

                boolean esHabitaculo = "Habitáculo".equalsIgnoreCase(tipoFinal);

                if (esHabitaculo && (hab == null || hab <= 0)) {
                        throw new IllegalArgumentException(
                                        "Debés seleccionar el habitáculo.");
                }

                if (!esHabitaculo) {
                        hab = null;
                        tipoFinal = "Campo abierto";
                } else {
                        tipoFinal = "Habitáculo";
                }

                if (!existe(
                                "SELECT 1 FROM animales WHERE id_animal=? AND estado='Activo'",
                                animal)) {
                        throw new IllegalArgumentException(
                                        "El animal seleccionado no existe o está inactivo.");
                }

                if (id > 0 && !existe(
                                "SELECT 1 FROM plantel_permanente WHERE id_plantel=?",
                                id)) {
                        throw new IllegalArgumentException(
                                        "El registro de Plantel seleccionado no existe.");
                }

                if (hab != null && !existe(
                                "SELECT 1 FROM habitaculos WHERE id_habitaculo=? AND estado='Activo'",
                                hab)) {
                        throw new IllegalArgumentException(
                                        "El habitáculo seleccionado no existe o está inactivo.");
                }

                if (hab != null) {
                        int ocup = ocupacionHabitaculo(hab, id);
                        int cap = capacidadHabitaculo(hab);
                        if (cap > 0 && ocup + cantidad > cap)
                                throw new IllegalArgumentException(
                                                "El habitáculo seleccionado tiene capacidad para " + cap
                                                                + " ejemplares y ya hay " + ocup
                                                                + " asignados. No se puede superar la capacidad.");
                }
                if (id == 0) {
                        int nuevo = update(
                                        "INSERT INTO plantel_permanente(id_animal,cantidad,tipo_ubicacion,id_habitaculo,fecha_ingreso_plantel,estado,observaciones) VALUES(?,?,?,?,?,'Activo',?)",
                                        animal, cantidad, tipoFinal, hab, fecha, obs);

                        // ========================================================
                        // EL ALTA DEL PLANTEL TAMBIÉN ES UN MOVIMIENTO
                        //
                        // Se mantiene el registro original en plantel_permanente
                        // y además se genera su movimiento asociado.
                        // No se elimina ni se reemplaza la lógica anterior.
                        // ========================================================

                        int movimientoId = update(
                                        "INSERT INTO movimientos(" +
                                                        "id_detalle," +
                                                        "id_plantel," +
                                                        "fecha_movimiento," +
                                                        "tipo_movimiento," +
                                                        "cantidad," +
                                                        "sexo," +
                                                        "destino," +
                                                        "observaciones," +
                                                        "estado" +
                                                        ") VALUES(NULL,?,?,?,?,?,?,?, 'Activo')",
                                        nuevo,
                                        fecha,
                                        "Ingreso a Plantel",
                                        cantidad,
                                        null,
                                        "Plantel Permanente",
                                        obs == null ? "" : obs.trim());

                        String animalNombre = scalar(
                                        "SELECT COALESCE(nombre_vulgar,'') FROM animales WHERE id_animal=" + animal);

                        String observacionMovimiento = formatearFechaMovimiento(fecha) +
                                        " - Ingreso a Plantel de " +
                                        cantidad +
                                        " ejemplar(es) de " +
                                        animalNombre +
                                        " → Plantel Permanente - Movimiento #" +
                                        movimientoId;

                        if (hab != null) {
                                agregarObservacionHabitaculo(
                                                hab,
                                                formatearFechaMovimiento(fecha)
                                                                + " - INGRESO AL HABITÁCULO: "
                                                                + cantidad + " ejemplar(es) de " + animalNombre
                                                                + " - Movimiento #" + movimientoId);
                        }

                        String observacionFinal = obs == null
                                        ? ""
                                        : obs.trim();

                        if (!observacionFinal.isBlank()) {
                                observacionFinal += "\n" + observacionMovimiento;
                        } else {
                                observacionFinal = observacionMovimiento;
                        }

                        update(
                                        "UPDATE movimientos SET observaciones=? WHERE id_movimiento=?",
                                        observacionFinal,
                                        movimientoId);

                        agregarObservacionPlantel(
                                        nuevo,
                                        formatearFechaMovimiento(fecha) +
                                                        " - Ingreso a Plantel de " +
                                                        cantidad +
                                                        " ejemplar(es) de " +
                                                        animalNombre +
                                                        " → " +
                                                        tipo +
                                                        " - Movimiento #" +
                                                        movimientoId);

                        agregarObservacionActaGeneral(
                                        movimientoId,
                                        fecha,
                                        "Ingreso a Plantel",
                                        cantidad,
                                        animalNombre,
                                        "Plantel Permanente");

                        recalcularCantidadAnimal(animal);
                        return nuevo;
                }
                int viejoAnimal = Integer
                                .parseInt(scalar("SELECT id_animal FROM plantel_permanente WHERE id_plantel=" + id));
                update("UPDATE plantel_permanente SET id_animal=?,cantidad=?,tipo_ubicacion=?,id_habitaculo=?,fecha_ingreso_plantel=?,observaciones=? WHERE id_plantel=?",
                                animal, cantidad, tipoFinal, hab, fecha, obs, id);
                recalcularCantidadAnimal(viejoAnimal);
                recalcularCantidadAnimal(animal);
                return id;
        }

        public static int guardarPlantelIndividual(
                        int animal,
                        String tipo,
                        Integer hab,
                        String fecha,
                        String obs,
                        String peso,
                        String edad,
                        String estado,
                        Integer idIdentificacion) {

                if (animal <= 0) {
                        throw new IllegalArgumentException(
                                        "El animal seleccionado no es válido.");
                }

                if (fecha == null || fecha.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La fecha de ingreso al Plantel es obligatoria.");
                }

                try {
                        java.time.LocalDate.parse(fecha.trim());
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "La fecha de ingreso al Plantel no es válida.");
                }

                String tipoFinal = tipo == null
                                ? ""
                                : tipo.trim();

                if (!"Habitáculo".equalsIgnoreCase(tipoFinal)
                                && !"Campo abierto".equalsIgnoreCase(tipoFinal)) {

                        throw new IllegalArgumentException(
                                        "La ubicación del Plantel debe ser Habitáculo o Campo abierto.");
                }

                boolean esHabitaculo = "Habitáculo".equalsIgnoreCase(tipoFinal);

                if (esHabitaculo && (hab == null || hab <= 0)) {
                        throw new IllegalArgumentException(
                                        "Debés seleccionar el habitáculo.");
                }

                if (!esHabitaculo) {
                        hab = null;
                        tipoFinal = "Campo abierto";
                } else {
                        tipoFinal = "Habitáculo";
                }

                if (!existe(
                                "SELECT 1 FROM animales " +
                                                "WHERE id_animal=? " +
                                                "AND estado='Activo'",
                                animal)) {

                        throw new IllegalArgumentException(
                                        "El animal seleccionado no existe o está inactivo.");
                }

                if (hab != null && !existe(
                                "SELECT 1 FROM habitaculos " +
                                                "WHERE id_habitaculo=? " +
                                                "AND estado='Activo'",
                                hab)) {

                        throw new IllegalArgumentException(
                                        "El habitáculo seleccionado no existe o está inactivo.");
                }

                /*
                 * ============================================================
                 * VALIDACIÓN DEL PESO
                 * ============================================================
                 */

                Double pesoFinal = null;

                if (peso != null && !peso.isBlank()) {
                        try {
                                pesoFinal = Double.parseDouble(
                                                peso.trim().replace(",", "."));
                        } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(
                                                "El peso ingresado no es válido.");
                        }

                        if (pesoFinal < 0) {
                                throw new IllegalArgumentException(
                                                "El peso no puede ser negativo.");
                        }
                }

                /*
                 * ============================================================
                 * VALIDACIÓN DEL ESTADO
                 * ============================================================
                 */

                String estadoFinal = estado == null
                                ? ""
                                : estado.trim();

                /*
                 * ============================================================
                 * VALIDACIÓN DE LA IDENTIFICACIÓN
                 * ============================================================
                 */

                if (idIdentificacion != null) {

                        if (!existe(
                                        "SELECT 1 FROM identificaciones " +
                                                        "WHERE id_identificacion=? " +
                                                        "AND id_animal=? " +
                                                        "AND estado='Activo'",
                                        idIdentificacion,
                                        animal)) {

                                throw new IllegalArgumentException(
                                                "La identificación seleccionada no pertenece al animal.");
                        }
                }

                /*
                 * ============================================================
                 * CAPACIDAD DEL HABITÁCULO
                 * ============================================================
                 */

                int plantelExistente = 0;

                String sqlPlantel = "SELECT id_plantel " +
                                "FROM plantel_permanente " +
                                "WHERE estado='Activo' " +
                                "AND id_animal=? " +
                                "AND LOWER(TRIM(COALESCE(tipo_ubicacion,'')))=" +
                                "LOWER(TRIM(?)) " +
                                "AND ((id_habitaculo IS NULL AND ? IS NULL) " +
                                "OR id_habitaculo=?) " +
                                "ORDER BY id_plantel " +
                                "LIMIT 1";

                List<Fila> planteles = filas(
                                sqlPlantel,
                                String.valueOf(animal),
                                tipoFinal,
                                hab == null ? null : String.valueOf(hab),
                                hab == null ? null : String.valueOf(hab));

                if (!planteles.isEmpty()) {
                        plantelExistente = planteles.get(0).id();
                }

                if (hab != null) {

                        int ocupacion = ocupacionHabitaculo(
                                        hab,
                                        plantelExistente);

                        int capacidad = capacidadHabitaculo(hab);

                        if (capacidad > 0 && ocupacion + 1 > capacidad) {
                                throw new IllegalArgumentException(
                                                "El habitáculo seleccionado tiene capacidad para "
                                                                + capacidad
                                                                + " ejemplares y ya hay "
                                                                + ocupacion
                                                                + " asignados. No se puede superar la capacidad.");
                        }
                }

                /*
                 * ============================================================
                 * CREAR INGRESO TÉCNICO INTERNO
                 *
                 * No se modifica guardarIngreso().
                 * Este ingreso solamente existe para poder respetar
                 * la relación obligatoria:
                 *
                 * ingresos
                 * ↓
                 * detalle_ingreso
                 * ↓
                 * movimientos
                 * ↓
                 * plantel_permanente
                 * ============================================================
                 */

                String numeroActaTecnica = "PLANTEL-" +
                                System.currentTimeMillis();

                int idIngresoTecnico = update(
                                "INSERT INTO ingresos(" +
                                                "numero_acta," +
                                                "fecha_ingreso," +
                                                "organismo_procedencia," +
                                                "responsable_entrega," +
                                                "recibe_reserva," +
                                                "procedencia," +
                                                "motivo_ingreso," +
                                                "observaciones," +
                                                "estado" +
                                                ") VALUES(?,?,?,?,?,?,?,?,'Activo')",

                                numeroActaTecnica,
                                fecha,
                                "Alta directa de Plantel",
                                "Sistema",
                                "Plantel Permanente",
                                "Alta directa",
                                "Alta de ejemplar individual",
                                obs == null ? "" : obs.trim());

                /*
                 * ============================================================
                 * CREAR DETALLE DEL EJEMPLAR
                 * ============================================================
                 */

                int idDetalle = update(
                                "INSERT INTO detalle_ingreso(" +
                                                "id_ingreso," +
                                                "id_animal," +
                                                "id_identificacion," +
                                                "cantidad," +
                                                "sexo," +
                                                "edad," +
                                                "peso," +
                                                "estado_ingreso," +
                                                "observaciones," +
                                                "estado" +
                                                ") VALUES(?,?,?,?,?,?,?,?,?,'Activo')",

                                idIngresoTecnico,
                                animal,
                                idIdentificacion,
                                1,
                                null,
                                edad == null ? "" : edad.trim(),
                                pesoFinal,
                                estadoFinal,
                                obs == null ? "" : obs.trim());

                /*
                 * ============================================================
                 * OBTENER O CREAR PLANTEL
                 * ============================================================
                 */

                int idPlantel;

                if (plantelExistente > 0) {

                        idPlantel = plantelExistente;

                        update(
                                        "UPDATE plantel_permanente SET " +
                                                        "cantidad=cantidad+1," +
                                                        "fecha_ingreso_plantel=?," +
                                                        "estado='Activo' " +
                                                        "WHERE id_plantel=?",
                                        fecha,
                                        idPlantel);

                } else {

                        idPlantel = update(
                                        "INSERT INTO plantel_permanente(" +
                                                        "id_animal," +
                                                        "cantidad," +
                                                        "tipo_ubicacion," +
                                                        "id_habitaculo," +
                                                        "fecha_ingreso_plantel," +
                                                        "estado," +
                                                        "observaciones" +
                                                        ") VALUES(?,?,?,?,?,'Activo',?)",

                                        animal,
                                        1,
                                        tipoFinal,
                                        hab,
                                        fecha,
                                        obs == null ? "" : obs.trim());
                }

                /*
                 * ============================================================
                 * CREAR MOVIMIENTO
                 *
                 * ESTA VEZ sí queda relacionado al detalle individual.
                 * ============================================================
                 */

                int movimientoId = update(
                                "INSERT INTO movimientos(" +
                                                "id_detalle," +
                                                "id_plantel," +
                                                "fecha_movimiento," +
                                                "tipo_movimiento," +
                                                "cantidad," +
                                                "sexo," +
                                                "destino," +
                                                "observaciones," +
                                                "estado" +
                                                ") VALUES(?,?,?,?,?,?,?,?, 'Activo')",

                                idDetalle,
                                idPlantel,
                                fecha,
                                "Ingreso a Plantel",
                                1,
                                null,
                                "Plantel Permanente",
                                obs == null ? "" : obs.trim());

                /*
                 * ============================================================
                 * FORMATEAR OBSERVACIÓN DEL MOVIMIENTO
                 * ============================================================
                 */

                String animalNombre = scalar(
                                "SELECT COALESCE(nombre_vulgar,'') " +
                                                "FROM animales " +
                                                "WHERE id_animal=" + animal);

                String observacionMovimiento = formatearFechaMovimiento(fecha)
                                + " - Ingreso de 1 ejemplar de "
                                + animalNombre
                                + " → "
                                + tipoFinal
                                + " - Movimiento #"
                                + movimientoId;

                if (hab != null) {
                        agregarObservacionHabitaculo(
                                        hab,
                                        formatearFechaMovimiento(fecha)
                                                        + " - INGRESO AL HABITÁCULO: 1 ejemplar de "
                                                        + animalNombre
                                                        + " - Movimiento #" + movimientoId);
                }

                String observacionFinal = obs == null
                                ? ""
                                : obs.trim();

                if (!observacionFinal.isBlank()) {
                        observacionFinal += "\n"
                                        + observacionMovimiento;
                } else {
                        observacionFinal = observacionMovimiento;
                }

                update(
                                "UPDATE movimientos SET " +
                                                "observaciones=? " +
                                                "WHERE id_movimiento=?",
                                observacionFinal,
                                movimientoId);

                /*
                 * ============================================================
                 * AGREGAR OBSERVACIÓN AL PLANTEL
                 * ============================================================
                 */

                agregarObservacionPlantel(
                                idPlantel,
                                observacionMovimiento);

                agregarObservacionActaGeneral(
                                movimientoId,
                                fecha,
                                "Ingreso a Plantel",
                                1,
                                animalNombre,
                                "Plantel Permanente");

                /*
                 * ============================================================
                 * RECALCULAR CANTIDAD DEL ANIMAL
                 * ============================================================
                 */

                recalcularCantidadAnimal(animal);

                return idPlantel;
        }

        private static void recalcularCantidadAnimal(int animal) {

                /*
                 * ============================================================
                 * CANTIDAD EN PLANTEL PERMANENTE
                 * ============================================================
                 */

                int plantel = Integer.parseInt(
                                scalar(
                                                "SELECT COALESCE(SUM(cantidad),0) " +
                                                                "FROM plantel_permanente " +
                                                                "WHERE id_animal=" + animal +
                                                                " AND estado='Activo' " +
                                                                " AND cantidad>0"));

                /*
                 * ============================================================
                 * CANTIDAD DISPONIBLE DE ACTAS
                 *
                 * Cada detalle comienza con d.cantidad.
                 *
                 * Los movimientos distintos de 'Ingreso' consumen
                 * disponibilidad.
                 * ============================================================
                 */

                int ingresos = Integer.parseInt(
                                scalar(
                                                "SELECT COALESCE(SUM(" +
                                                                "MAX(" +
                                                                "d.cantidad - COALESCE((" +
                                                                "SELECT SUM(m.cantidad) " +
                                                                "FROM movimientos m " +
                                                                "WHERE m.id_detalle=d.id_detalle " +
                                                                "AND m.estado='Activo' " +
                                                                "AND m.tipo_movimiento<>'Ingreso'" +
                                                                "),0)," +
                                                                "0)" +
                                                                "),0) " +

                                                                "FROM detalle_ingreso d " +

                                                                "JOIN ingresos i " +
                                                                "ON i.id_ingreso=d.id_ingreso " +

                                                                "WHERE d.id_animal=" + animal +
                                                                " AND d.estado='Activo' " +
                                                                " AND i.estado='Activo'"));

                /*
                 * ============================================================
                 * TOTAL REAL DENTRO DE LA RESERVA
                 * ============================================================
                 */

                int total = plantel + ingresos;

                /*
                 * cantidad_actual ahora representa realmente:
                 *
                 * PLANTEL + ACTAS DISPONIBLES
                 *
                 * Nunca puede ser negativa.
                 */

                total = Math.max(total, 0);

                update(
                                "UPDATE animales " +
                                                "SET cantidad_actual=? " +
                                                "WHERE id_animal=?",
                                total,
                                animal);
        }

        public static int animalPorPlantel(int id) {
                return Integer.parseInt(scalar("SELECT id_animal FROM plantel_permanente WHERE id_plantel=" + id));
        }

        public static int cantidadPlantel(int id) {
                return Integer.parseInt(
                                scalar("SELECT COALESCE(cantidad,0) FROM plantel_permanente WHERE id_plantel=" + id));
        }

        public static void trasladarPlantel(
                        int plantel,
                        int cantidad,
                        String fecha,
                        String sexo,
                        String destino,
                        String observaciones) {
                trasladarPlantel(plantel, cantidad, fecha, sexo, destino, observaciones, null, null);
        }

        public static void trasladarPlantel(
                        int plantel,
                        int cantidad,
                        String fecha,
                        String sexo,
                        String destino,
                        String observaciones,
                        String ubicacionDestinoSeleccionada,
                        Integer idHabitaculoDestinoSeleccionado) {

                // ============================================================
                // VALIDACIONES EXISTENTES - NO SE QUITAN
                // ============================================================

                if (cantidad <= 0)
                        throw new IllegalArgumentException(
                                        "La cantidad a trasladar debe ser mayor que cero.");

                if (fecha == null || fecha.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La fecha del traslado es obligatoria.");
                }

                try {
                        java.time.LocalDate.parse(fecha.trim());
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "La fecha del traslado no es válida.");
                }

                int actual = cantidadPlantel(plantel);

                if (actual <= 0)
                        throw new IllegalArgumentException(
                                        "El ejemplar seleccionado ya no tiene existencia activa en el Plantel.");

                if (cantidad > actual)
                        throw new IllegalArgumentException(
                                        "La cantidad no puede superar la existencia actual (" + actual + ").");

                if (destino == null || destino.isBlank())
                        throw new IllegalArgumentException(
                                        "Indicá el centro o institución de destino.");

                // ============================================================
                // DATOS DEL PLANTEL DE ORIGEN
                // ============================================================

                int animal = animalPorPlantel(plantel);

                int habitaculoOrigen = 0;
                try {
                        habitaculoOrigen = Integer.parseInt(scalar(
                                        "SELECT COALESCE(id_habitaculo,0) FROM plantel_permanente WHERE id_plantel="
                                                        + plantel));
                } catch (Exception ignored) {
                }

                String tipoOrigen = scalar(
                                "SELECT COALESCE(tipo_ubicacion,'') " +
                                                "FROM plantel_permanente " +
                                                "WHERE id_plantel=" + plantel);

                String observacionesFinales = observaciones == null
                                ? ""
                                : observaciones.trim();

                String destinoFinal = destino.trim();

                // Validar el destino usando el ID elegido en la interfaz.
                // Se conserva la lectura del texto para los llamadores anteriores.
                boolean destinoInterno = ubicacionDestinoSeleccionada != null
                                || destinoEsInternoPlantel(destinoFinal);
                Integer idHabValidar = idHabitaculoDestinoSeleccionado;
                if (destinoInterno && idHabValidar == null
                                && !destinoFinal.equalsIgnoreCase("Campo abierto")) {
                        String nombreDestino = extraerNombreHabitaculoDestino(destinoFinal);
                        if (!nombreDestino.isBlank()) {
                                String idDestino = scalar(
                                                "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                                + nombreDestino.replace("'", "''")
                                                                + "')) AND estado='Activo' LIMIT 1");
                                if (idDestino != null && !idDestino.isBlank()) {
                                        idHabValidar = Integer.parseInt(idDestino);
                                }
                        }
                }
                if (destinoInterno && !destinoFinal.equalsIgnoreCase("Campo abierto") && idHabValidar == null) {
                        throw new IllegalArgumentException("No se pudo identificar el habitáculo de destino.");
                }
                if (idHabValidar != null) {
                        int ocupacionDestino = ocupacionHabitaculo(idHabValidar, plantel);
                        int capacidadDestino = capacidadHabitaculo(idHabValidar);
                        if (capacidadDestino > 0 && ocupacionDestino + cantidad > capacidadDestino) {
                                throw new IllegalArgumentException(
                                                "El habitáculo seleccionado tiene capacidad para "
                                                                + capacidadDestino + " ejemplares y ya hay "
                                                                + ocupacionDestino
                                                                + " asignados. No se puede superar la capacidad.");
                        }
                }

                // ============================================================
                // MOVIMIENTO - SE CONSERVA LA LÓGICA EXISTENTE
                // ============================================================

                int movimientoId = update(
                                "INSERT INTO movimientos(" +
                                                "id_plantel," +
                                                "fecha_movimiento," +
                                                "tipo_movimiento," +
                                                "cantidad," +
                                                "sexo," +
                                                "destino," +
                                                "observaciones," +
                                                "estado" +
                                                ") VALUES(?,?,?,?,?,?,?, 'Activo')",
                                plantel,
                                fecha,
                                "Traslado",
                                cantidad,
                                sexo,
                                destinoFinal,
                                observacionesFinales);

                // ============================================================
                // OBSERVACIÓN DEL MOVIMIENTO
                // ============================================================

                String animalNombre = scalar(
                                "SELECT COALESCE(nombre_vulgar,'') " +
                                                "FROM animales " +
                                                "WHERE id_animal=" + animal);

                String observacionMovimiento = formatearFechaMovimiento(fecha) +
                                " - Traslado de " +
                                cantidad +
                                " ejemplar(es) de " +
                                animalNombre +
                                " → " +
                                destinoFinal +
                                " - Movimiento #" +
                                movimientoId;

                if (!observacionesFinales.isBlank()) {
                        observacionesFinales += "\n" + observacionMovimiento;
                } else {
                        observacionesFinales = observacionMovimiento;
                }

                update(
                                "UPDATE movimientos SET observaciones=? WHERE id_movimiento=?",
                                observacionesFinales,
                                movimientoId);

                String historialTraslado = formatearFechaMovimiento(fecha) +
                                " - Traslado de " +
                                cantidad +
                                " ejemplar(es) de " +
                                animalNombre +
                                " → " +
                                destinoFinal +
                                " - Movimiento #" +
                                movimientoId;

                agregarObservacionPlantel(
                                plantel,
                                historialTraslado);

                agregarObservacionActaGeneral(
                                movimientoId,
                                fecha,
                                "Traslado",
                                cantidad,
                                animalNombre,
                                destinoFinal);

                // ============================================================
                // NUEVA LÓGICA:
                // SI EL DESTINO SIGUE SIENDO DENTRO DEL PLANTEL,
                // LOS EJEMPLARES SE REGISTRAN EN EL DESTINO.
                // ============================================================

                boolean quedaEnPlantel = ubicacionDestinoSeleccionada != null || destinoEsInternoPlantel(destinoFinal);

                if (quedaEnPlantel) {

                        String tipoDestino;
                        Integer habitaculoDestino = null;

                        if (ubicacionDestinoSeleccionada != null) {
                                if ("Campo abierto".equalsIgnoreCase(ubicacionDestinoSeleccionada)) {
                                        tipoDestino = "Campo Abierto";
                                } else if ("Habitáculo".equalsIgnoreCase(ubicacionDestinoSeleccionada)) {
                                        tipoDestino = "Habitáculo";
                                        if (idHabitaculoDestinoSeleccionado == null
                                                        || idHabitaculoDestinoSeleccionado <= 0) {
                                                throw new IllegalArgumentException(
                                                                "Seleccioná un habitáculo de destino válido.");
                                        }
                                        habitaculoDestino = idHabitaculoDestinoSeleccionado;
                                } else {
                                        throw new IllegalArgumentException("La ubicación de destino no es válida.");
                                }
                        } else if (destinoFinal.equalsIgnoreCase("Campo abierto")
                                        || destinoFinal.equalsIgnoreCase("Campo Abierto")) {
                                tipoDestino = "Campo Abierto";
                        } else {
                                tipoDestino = "Habitáculo";
                                String nombreHabitaculo = extraerNombreHabitaculoDestino(destinoFinal);
                                if (!nombreHabitaculo.isBlank()) {
                                        String idHabitaculo = scalar(
                                                        "SELECT id_habitaculo FROM habitaculos " +
                                                                        "WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                                        + nombreHabitaculo.replace("'", "''") + "')) " +
                                                                        "AND estado='Activo' LIMIT 1");
                                        if (idHabitaculo != null && !idHabitaculo.isBlank()) {
                                                habitaculoDestino = Integer.parseInt(idHabitaculo);
                                        }
                                }
                        }

                        // ----------------------------------------------------
                        // SI ES HABITÁCULO, VALIDAMOS CAPACIDAD
                        // ----------------------------------------------------

                        if (habitaculoDestino != null) {

                                int ocupacion = ocupacionHabitaculo(
                                                habitaculoDestino,
                                                plantel);

                                int capacidad = capacidadHabitaculo(
                                                habitaculoDestino);

                                if (capacidad > 0
                                                && ocupacion + cantidad > capacidad) {

                                        throw new IllegalArgumentException(
                                                        "El habitáculo seleccionado tiene capacidad para "
                                                                        + capacidad
                                                                        + " ejemplares y ya hay "
                                                                        + ocupacion
                                                                        + " asignados. No se puede superar la capacidad.");
                                }
                        }

                        // ----------------------------------------------------
                        // BUSCAMOS SI YA EXISTE UN PLANTEL DEL MISMO ANIMAL
                        // EN EL MISMO DESTINO
                        // ----------------------------------------------------

                        String condicionHabitaculo;

                        if (habitaculoDestino == null) {
                                condicionHabitaculo = "id_habitaculo IS NULL";
                        } else {
                                condicionHabitaculo = "id_habitaculo=" + habitaculoDestino;
                        }

                        String existente = scalar(
                                        "SELECT id_plantel " +
                                                        "FROM plantel_permanente " +
                                                        "WHERE id_animal=" + animal
                                                        + " AND estado='Activo'"
                                                        + " AND LOWER(tipo_ubicacion)=LOWER('"
                                                        + tipoDestino.replace("'", "''")
                                                        + "')"
                                                        + " AND " + condicionHabitaculo
                                                        + " LIMIT 1");

                        // ----------------------------------------------------
                        // SI EXISTE, SUMAMOS LA CANTIDAD
                        // ----------------------------------------------------

                        // scalar() usa "0" como valor por defecto cuando no existe
                        // una ubicación destino. En ese caso hay que crear la fila.
                        if (existente != null
                                        && !existente.isBlank()
                                        && !"0".equals(existente.trim())) {

                                int plantelDestino = Integer.parseInt(existente);

                                update(
                                                "UPDATE plantel_permanente " +
                                                                "SET cantidad=cantidad+?, " +
                                                                "fecha_ingreso_plantel=? " +
                                                                "WHERE id_plantel=?",
                                                cantidad,
                                                fecha,
                                                plantelDestino);

                                agregarObservacionPlantel(
                                                plantelDestino,
                                                historialTraslado);

                        } else {

                                // ------------------------------------------------
                                // SI NO EXISTE, CREAMOS UN NUEVO PLANTEL
                                // ------------------------------------------------

                                int plantelDestino = update(
                                                "INSERT INTO plantel_permanente(" +
                                                                "id_animal," +
                                                                "cantidad," +
                                                                "tipo_ubicacion," +
                                                                "id_habitaculo," +
                                                                "fecha_ingreso_plantel," +
                                                                "estado," +
                                                                "observaciones" +
                                                                ") VALUES(?,?,?,?,?,'Activo',?)",
                                                animal,
                                                cantidad,
                                                tipoDestino,
                                                habitaculoDestino,
                                                fecha,
                                                observacionesFinales);

                                agregarObservacionPlantel(
                                                plantelDestino,
                                                historialTraslado);
                        }
                }

                // ============================================================
                // ACTUALIZACIÓN DEL PLANTEL DE ORIGEN
                // LÓGICA EXISTENTE - NO SE QUITA
                // ============================================================

                update(
                                "UPDATE plantel_permanente " +
                                                "SET cantidad=MAX(cantidad-?,0), " +
                                                "estado=CASE " +
                                                "WHEN cantidad-?<=0 THEN 'Inactivo' " +
                                                "ELSE estado END " +
                                                "WHERE id_plantel=?",
                                cantidad,
                                cantidad,
                                plantel);

                // ============================================================
                // RECALCULAR EXISTENCIA DEL ANIMAL
                // LÓGICA EXISTENTE - NO SE QUITA
                // ============================================================

                if (habitaculoOrigen > 0) {
                        try (Connection cn = c()) {
                                actualizarObservacionHabitaculoConConexion(cn, habitaculoOrigen);
                        } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                        }
                }

                recalcularCantidadAnimal(animal);
        }

        public static List<Fila> resumenActa(int ingreso) {
                return filas(
                                "SELECT d.id_detalle,a.nombre_vulgar,d.cantidad,COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p WHERE p.id_animal=d.id_animal AND p.estado='Activo'),0),COALESCE((SELECT SUM(m.cantidad) FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE p.id_animal=d.id_animal AND m.tipo_movimiento='Fallecimiento' AND m.estado='Activo'),0),COALESCE((SELECT SUM(m.cantidad) FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE p.id_animal=d.id_animal AND m.tipo_movimiento='Traslado' AND m.estado='Activo'),0),COALESCE((SELECT GROUP_CONCAT(DISTINCT m.destino) FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE p.id_animal=d.id_animal AND m.tipo_movimiento='Traslado' AND m.estado='Activo' AND COALESCE(m.destino,'')<>''),'') FROM detalle_ingreso d JOIN animales a ON a.id_animal=d.id_animal WHERE d.id_ingreso=? AND d.estado='Activo' ORDER BY a.nombre_vulgar",
                                String.valueOf(ingreso));
        }

        public static void bajaPlantel(int id) {
                int animal = animalPorPlantel(id);
                baja("plantel_permanente", "id_plantel", id);
                recalcularCantidadAnimal(animal);
        }

        public static List<Opcion> plantelOpciones() {
                return opciones(
                                "SELECT p.id_plantel," +
                                                "a.nombre_vulgar || ' · ' || " +
                                                "COALESCE(e.nombre,'Sin especie') || ' · ' || " +
                                                "p.cantidad || ' disponibles · ' || " +
                                                "CASE " +
                                                "WHEN LOWER(TRIM(COALESCE(p.tipo_ubicacion,'')))='campo abierto' " +
                                                "THEN 'Campo Abierto' " +
                                                "WHEN LOWER(TRIM(COALESCE(p.tipo_ubicacion,'')))='plantel permanente' "
                                                +
                                                "THEN 'Plantel Permanente' " +
                                                "WHEN COALESCE(TRIM(p.tipo_ubicacion),'')<>'' " +
                                                "THEN p.tipo_ubicacion " +
                                                "ELSE 'Plantel Permanente' " +
                                                "END " +
                                                "FROM plantel_permanente p " +
                                                "JOIN animales a ON a.id_animal=p.id_animal " +
                                                "LEFT JOIN especies e ON e.id_especie=a.id_especie " +
                                                "WHERE p.estado='Activo' " +
                                                "AND p.cantidad>0 " +
                                                "ORDER BY a.nombre_vulgar");
        }

        // MOVIMIENTOS
        // MOVIMIENTOS

        public record DetalleMovimiento(
                        int idDetalle,
                        int idAnimal,
                        String animal,
                        int cantidadOriginal,
                        int cantidadDisponible,
                        String sexo,
                        String edad,
                        String peso) {
        }

        /**
         * Devuelve los movimientos activos.
         */
        public static List<Fila> movimientos(String q) {

                sincronizarObservacionesMovimientos();

                return filas(
                                "SELECT " +
                                                "m.id_movimiento," +
                                                "COALESCE(a.nombre_vulgar,'Plantel')," +
                                                "COALESCE(NULLIF(TRIM(i.numero_acta),''),CASE WHEN m.id_plantel IS NOT NULL THEN 'Plantel Permanente' ELSE '' END),"
                                                +
                                                "m.fecha_movimiento," +
                                                "m.tipo_movimiento," +
                                                "m.cantidad," +
                                                "COALESCE(m.sexo,'')," +
                                                "COALESCE((" +
                                                "SELECT GROUP_CONCAT(" +
                                                "ix.tipo_identificacion || ': ' || ix.numero_identificacion," +
                                                "' | ') " +
                                                "FROM identificaciones ix " +
                                                "WHERE ix.id_animal=a.id_animal " +
                                                "AND ix.estado='Activo'" +
                                                "),'Sin identificación')," +
                                                "COALESCE(m.destino,'')," +
                                                "m.estado " +

                                                "FROM movimientos m " +

                                                "LEFT JOIN detalle_ingreso d " +
                                                "ON d.id_detalle=m.id_detalle " +

                                                "LEFT JOIN ingresos i " +
                                                "ON i.id_ingreso=d.id_ingreso " +

                                                "LEFT JOIN plantel_permanente p " +
                                                "ON p.id_plantel=m.id_plantel " +

                                                "LEFT JOIN animales a " +
                                                "ON a.id_animal=COALESCE(d.id_animal,p.id_animal) " +

                                                "WHERE m.estado='Activo' " +

                                                "AND (" +
                                                "COALESCE(a.nombre_vulgar,'') LIKE ? " +
                                                "OR COALESCE(NULLIF(TRIM(i.numero_acta),''),CASE WHEN m.id_plantel IS NOT NULL THEN 'Plantel Permanente' ELSE '' END) LIKE ? "
                                                +
                                                "OR m.tipo_movimiento LIKE ? " +
                                                "OR COALESCE(m.destino,'') LIKE ?" +
                                                ") " +

                                                "ORDER BY m.fecha_movimiento DESC",

                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%");
        }

        /**
         * Devuelve los movimientos inactivos.
         */
        public static List<Fila> movimientosInactivos(String q) {

                sincronizarObservacionesMovimientos();

                return filas(
                                "SELECT " +
                                                "m.id_movimiento," +
                                                "COALESCE(a.nombre_vulgar,'Plantel')," +
                                                "COALESCE(NULLIF(TRIM(i.numero_acta),''),CASE WHEN m.id_plantel IS NOT NULL THEN 'Plantel Permanente' ELSE '' END),"
                                                +
                                                "m.fecha_movimiento," +
                                                "m.tipo_movimiento," +
                                                "m.cantidad," +
                                                "COALESCE(m.sexo,'')," +
                                                "COALESCE((" +
                                                "SELECT GROUP_CONCAT(" +
                                                "ix.tipo_identificacion || ': ' || ix.numero_identificacion," +
                                                "' | ') " +
                                                "FROM identificaciones ix " +
                                                "WHERE ix.id_animal=a.id_animal " +
                                                "AND ix.estado='Activo'" +
                                                "),'Sin identificación')," +
                                                "COALESCE(m.destino,'')," +
                                                "m.estado " +

                                                "FROM movimientos m " +

                                                "LEFT JOIN detalle_ingreso d " +
                                                "ON d.id_detalle=m.id_detalle " +

                                                "LEFT JOIN ingresos i " +
                                                "ON i.id_ingreso=d.id_ingreso " +

                                                "LEFT JOIN plantel_permanente p " +
                                                "ON p.id_plantel=m.id_plantel " +

                                                "LEFT JOIN animales a " +
                                                "ON a.id_animal=COALESCE(d.id_animal,p.id_animal) " +

                                                "WHERE m.estado='Inactivo' " +

                                                "AND (" +
                                                "COALESCE(a.nombre_vulgar,'') LIKE ? " +
                                                "OR COALESCE(NULLIF(TRIM(i.numero_acta),''),CASE WHEN m.id_plantel IS NOT NULL THEN 'Plantel Permanente' ELSE '' END) LIKE ? "
                                                +
                                                "OR m.tipo_movimiento LIKE ? " +
                                                "OR COALESCE(m.destino,'') LIKE ?" +
                                                ") " +

                                                "ORDER BY m.fecha_movimiento DESC",

                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%");
        }

        private static int ingresoDeDetalle(int detalle) {
                if (detalle <= 0) {
                        return 0;
                }

                return Integer.parseInt(
                                scalar(
                                                "SELECT COALESCE(id_ingreso,0) " +
                                                                "FROM detalle_ingreso " +
                                                                "WHERE id_detalle=" + detalle));
        }

        /**
         * Devuelve el acta asociada a un movimiento.
         */
        public static int ingresoDeMovimiento(int movimiento) {

                return Integer.parseInt(
                                scalar(
                                                "SELECT COALESCE(i.id_ingreso,0) " +
                                                                "FROM movimientos m " +
                                                                "LEFT JOIN detalle_ingreso d " +
                                                                "ON d.id_detalle=m.id_detalle " +
                                                                "LEFT JOIN ingresos i " +
                                                                "ON i.id_ingreso=d.id_ingreso " +
                                                                "WHERE m.id_movimiento=" +
                                                                movimiento));
        }

        /**
         * Devuelve las actas activas que todavía tienen animales disponibles.
         *
         * El movimiento inicial "Ingreso" NO consume disponibilidad.
         */
        public static List<Opcion> actasConDisponibilidad() {

                return opciones(
                                "SELECT " +
                                                "i.id_ingreso," +
                                                "i.numero_acta || ' · ' || i.fecha_ingreso " +

                                                "FROM ingresos i " +

                                                "WHERE i.estado='Activo' " +

                                                "AND EXISTS (" +

                                                "SELECT 1 " +
                                                "FROM detalle_ingreso d " +

                                                "WHERE d.id_ingreso=i.id_ingreso " +
                                                "AND d.estado='Activo' " +

                                                "AND d.cantidad > (" +

                                                "SELECT COALESCE(SUM(m.cantidad),0) " +
                                                "FROM movimientos m " +

                                                "WHERE m.id_detalle=d.id_detalle " +
                                                "AND m.estado='Activo' " +
                                                "AND m.tipo_movimiento<>'Ingreso'" +

                                                ")" +

                                                ") " +

                                                "ORDER BY i.fecha_ingreso DESC");
        }

        /**
         * Devuelve todos los detalles del acta.
         *
         * IMPORTANTE:
         *
         * cantidadOriginal = cantidad cargada originalmente en el acta.
         *
         * cantidadDisponible =
         * cantidadOriginal - movimientos activos,
         * excluyendo el movimiento inicial "Ingreso".
         *
         * Sexo, edad y peso pertenecen al detalle del acta.
         * No son datos editables del movimiento.
         */
        public static List<DetalleMovimiento> detallesMovimiento(int ingreso) {

                List<DetalleMovimiento> out = new ArrayList<>();

                String sql = "SELECT " +
                                "d.id_detalle," +
                                "d.id_animal," +
                                "a.nombre_vulgar," +
                                "d.cantidad," +

                                "(" +
                                "d.cantidad - " +
                                "COALESCE((" +
                                "SELECT SUM(m.cantidad) " +
                                "FROM movimientos m " +
                                "WHERE m.id_detalle=d.id_detalle " +
                                "AND m.estado='Activo' " +
                                "AND m.tipo_movimiento<>'Ingreso'" +
                                "),0)" +
                                ") AS disponible," +

                                "COALESCE(d.sexo,'')," +
                                "COALESCE(d.edad,'')," +

                                "CASE " +
                                "WHEN d.peso IS NULL OR d.peso<=0 THEN '' " +
                                "ELSE printf('%.2f',d.peso) " +
                                "END " +

                                "FROM detalle_ingreso d " +

                                "JOIN animales a " +
                                "ON a.id_animal=d.id_animal " +

                                "WHERE d.id_ingreso=? " +
                                "AND d.estado='Activo' " +

                                "ORDER BY a.nombre_vulgar";

                try (
                                Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(sql)) {

                        p.setInt(1, ingreso);

                        try (ResultSet r = p.executeQuery()) {

                                while (r.next()) {

                                        int disponible = Math.max(
                                                        r.getInt(5),
                                                        0);

                                        out.add(
                                                        new DetalleMovimiento(
                                                                        r.getInt(1),
                                                                        r.getInt(2),
                                                                        r.getString(3),
                                                                        r.getInt(4),
                                                                        disponible,
                                                                        r.getString(6),
                                                                        r.getString(7),
                                                                        r.getString(8)));
                                }
                        }

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "No se pudieron obtener los detalles del acta.",
                                        e);
                }

                return out;
        }

        /**
         * Devuelve la cantidad disponible actualmente para un detalle.
         */
        public static int cantidadDisponibleDetalle(int detalle) {

                String sql = "SELECT " +
                                "COALESCE(d.cantidad,0) - " +

                                "COALESCE((" +
                                "SELECT SUM(m.cantidad) " +
                                "FROM movimientos m " +
                                "WHERE m.id_detalle=d.id_detalle " +
                                "AND m.estado='Activo' " +
                                "AND m.tipo_movimiento<>'Ingreso'" +
                                "),0) " +

                                "FROM detalle_ingreso d " +

                                "WHERE d.id_detalle=? " +
                                "AND d.estado='Activo'";

                try (
                                Connection cn = c();
                                PreparedStatement p = cn.prepareStatement(sql)) {

                        p.setInt(1, detalle);

                        try (ResultSet r = p.executeQuery()) {

                                if (!r.next()) {

                                        throw new IllegalArgumentException(
                                                        "El detalle del acta no existe o se encuentra inactivo.");
                                }

                                return Math.max(
                                                r.getInt(1),
                                                0);
                        }

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "No se pudo consultar la disponibilidad.",
                                        e);
                }
        }

        public static int guardarMovimientoActa(
                        int ingreso,
                        int detalle,
                        String fecha,
                        String tipo,
                        int cantidad,
                        String sexo,
                        String edad,
                        String peso,
                        String destino,
                        Integer idHabitaculo,
                        String observaciones) {

                if (ingreso <= 0) {
                        throw new IllegalArgumentException(
                                        "El acta seleccionada no es válida.");
                }

                if (detalle <= 0) {
                        throw new IllegalArgumentException(
                                        "El detalle seleccionado no es válido.");
                }

                if (fecha == null || fecha.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La fecha del movimiento es obligatoria.");
                }

                try {
                        java.time.LocalDate.parse(fecha.trim());
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "La fecha del movimiento no es válida.");
                }

                if (tipo == null || tipo.isBlank()) {
                        throw new IllegalArgumentException(
                                        "El tipo de movimiento es obligatorio.");
                }

                if ("Nacimiento".equalsIgnoreCase(tipo.trim())) {
                        throw new IllegalArgumentException(
                                        "Los nacimientos no se registran desde un acta.");
                }

                if (cantidad <= 0) {
                        throw new IllegalArgumentException(
                                        "La cantidad debe ser mayor que cero.");
                }

                String tipoFinal = tipo.trim();

                if (!java.util.Set.of(
                                "Traslado",
                                "Liberación",
                                "Fallecimiento",
                                "Otro")
                                .contains(tipoFinal)) {
                        throw new IllegalArgumentException(
                                        "El tipo de movimiento seleccionado no es válido.");
                }

                String destinoFinal = destino == null
                                ? ""
                                : destino.trim();

                boolean esTraslado = "Traslado".equalsIgnoreCase(tipoFinal);
                boolean vaAPlantel = destinoEsPlantel(destinoFinal)
                                || (esTraslado && destinoEsInternoPlantel(destinoFinal));

                if (esTraslado && vaAPlantel && idHabitaculo == null
                                && destinoFinal.toLowerCase(java.util.Locale.ROOT).startsWith("habitáculo")) {
                        String nombreHab = extraerNombreHabitaculoDestino(destinoFinal);
                        String idHab = scalar(
                                        "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                        + nombreHab.replace("'", "''")
                                                        + "')) AND estado='Activo' LIMIT 1");
                        if (idHab != null && !idHab.isBlank() && !"0".equals(idHab.trim())) {
                                idHabitaculo = Integer.parseInt(idHab);
                        }
                }

                if (("Traslado".equalsIgnoreCase(tipoFinal)
                                || "Liberación".equalsIgnoreCase(tipoFinal))
                                && destinoFinal.isBlank()) {

                        throw new IllegalArgumentException(
                                        "El destino es obligatorio para este tipo de movimiento.");
                }

                if (vaAPlantel &&
                                !"Traslado".equalsIgnoreCase(tipoFinal)) {
                        throw new IllegalArgumentException(
                                        "El Plantel Permanente solo puede ser destino de un traslado.");
                }

                if (vaAPlantel &&
                                idHabitaculo != null &&
                                idHabitaculo <= 0) {
                        idHabitaculo = null;
                }

                Connection cn = null;

                try {

                        cn = c();
                        cn.setAutoCommit(false);

                        int animal;
                        int cantidadOriginal;
                        String animalNombre;

                        // ========================================================
                        // OBTENER DETALLE DEL ACTA
                        // ========================================================

                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT " +
                                                        "d.id_animal," +
                                                        "d.cantidad," +
                                                        "COALESCE(a.nombre_vulgar,'') " +
                                                        "FROM detalle_ingreso d " +
                                                        "JOIN ingresos i " +
                                                        "ON i.id_ingreso=d.id_ingreso " +
                                                        "JOIN animales a " +
                                                        "ON a.id_animal=d.id_animal " +
                                                        "WHERE d.id_detalle=? " +
                                                        "AND d.id_ingreso=? " +
                                                        "AND d.estado='Activo' " +
                                                        "AND i.estado='Activo'")) {

                                ps.setInt(1, detalle);
                                ps.setInt(2, ingreso);

                                try (ResultSet rs = ps.executeQuery()) {

                                        if (!rs.next()) {

                                                throw new IllegalArgumentException(
                                                                "El animal seleccionado no pertenece al acta indicada o el acta está inactiva.");
                                        }

                                        animal = rs.getInt(1);
                                        cantidadOriginal = rs.getInt(2);
                                        animalNombre = rs.getString(3);
                                }
                        }

                        // ========================================================
                        // CANTIDAD YA UTILIZADA
                        // ========================================================

                        int cantidadUsada;

                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT COALESCE(SUM(cantidad),0) " +
                                                        "FROM movimientos " +
                                                        "WHERE id_detalle=? " +
                                                        "AND estado='Activo' " +
                                                        "AND tipo_movimiento<>'Ingreso'")) {

                                ps.setInt(1, detalle);

                                try (ResultSet rs = ps.executeQuery()) {

                                        cantidadUsada = rs.next()
                                                        ? rs.getInt(1)
                                                        : 0;
                                }
                        }

                        int disponible = Math.max(
                                        cantidadOriginal - cantidadUsada,
                                        0);

                        if (cantidad > disponible) {

                                throw new IllegalArgumentException(
                                                "No hay suficiente cantidad disponible en el acta.\n\n" +
                                                                "Disponible: " +
                                                                disponible +
                                                                "\nSolicitado: " +
                                                                cantidad);
                        }

                        // ========================================================
                        // VALIDAR HABITÁCULO
                        // ========================================================

                        if (vaAPlantel && idHabitaculo != null) {

                                validarCapacidadHabitaculo(
                                                cn,
                                                idHabitaculo,
                                                0,
                                                cantidad);
                        }

                        // ========================================================
                        // CREAR MOVIMIENTO
                        // ========================================================

                        int movimientoId;

                        String sqlMovimiento = "INSERT INTO movimientos(" +
                                        "id_detalle," +
                                        "id_plantel," +
                                        "fecha_movimiento," +
                                        "tipo_movimiento," +
                                        "cantidad," +
                                        "sexo," +
                                        "destino," +
                                        "observaciones," +
                                        "estado" +
                                        ") VALUES(?,?,?,?,?,?,?,?, 'Activo')";

                        try (PreparedStatement ps = cn.prepareStatement(sqlMovimiento)) {

                                ps.setInt(1, detalle);

                                ps.setNull(
                                                2,
                                                java.sql.Types.INTEGER);

                                ps.setString(
                                                3,
                                                fecha);

                                ps.setString(
                                                4,
                                                tipoFinal);

                                ps.setInt(
                                                5,
                                                cantidad);

                                if (sexo == null || sexo.isBlank()) {

                                        ps.setNull(
                                                        6,
                                                        java.sql.Types.VARCHAR);

                                } else {

                                        ps.setString(
                                                        6,
                                                        sexo.trim());
                                }

                                if (destinoFinal.isBlank()) {

                                        ps.setNull(
                                                        7,
                                                        java.sql.Types.VARCHAR);

                                } else {

                                        ps.setString(
                                                        7,
                                                        destinoFinal);
                                }

                                if (observaciones == null ||
                                                observaciones.isBlank()) {

                                        ps.setNull(
                                                        8,
                                                        java.sql.Types.VARCHAR);

                                } else {

                                        ps.setString(
                                                        8,
                                                        observaciones.trim());
                                }

                                ps.executeUpdate();
                        }

                        movimientoId = obtenerLastInsertId(cn);

                        // ========================================================
                        // SI ES TRASLADO A PLANTEL
                        // CREAR EL PLANTEL Y VINCULARLO AL MOVIMIENTO
                        // ========================================================

                        if (vaAPlantel) {

                                int idPlantel = crearPlantelDesdeMovimiento(
                                                cn,
                                                animal,
                                                cantidad,
                                                idHabitaculo,
                                                fecha,
                                                observaciones);

                                updateConConexion(
                                                cn,
                                                "UPDATE movimientos " +
                                                                "SET id_plantel=? " +
                                                                "WHERE id_movimiento=?",
                                                idPlantel,
                                                movimientoId);
                        }

                        // ========================================================
                        // OBSERVACIÓN DEL MOVIMIENTO
                        // ========================================================

                        String observacionMovimiento = formatearFechaMovimiento(fecha) +
                                        " - " +
                                        tipoFinal +
                                        " de " +
                                        cantidad +
                                        " ejemplar(es) de " +
                                        animalNombre +
                                        (destinoFinal.isBlank()
                                                        ? ""
                                                        : " → " + destinoFinal)
                                        +
                                        " - Movimiento #" +
                                        movimientoId;

                        String observacionMovimientoFinal = reemplazarLineaMovimiento(
                                        observaciones == null ? "" : observaciones.trim(),
                                        observacionMovimiento,
                                        movimientoId);

                        updateConConexion(
                                        cn,
                                        "UPDATE movimientos " +
                                                        "SET observaciones=? " +
                                                        "WHERE id_movimiento=?",
                                        observacionMovimientoFinal,
                                        movimientoId);

                        if (vaAPlantel) {
                                Integer idPlantelMovimiento = null;
                                try (PreparedStatement ps = cn.prepareStatement(
                                                "SELECT id_plantel FROM movimientos WHERE id_movimiento=?")) {
                                        ps.setInt(1, movimientoId);
                                        try (ResultSet rs = ps.executeQuery()) {
                                                if (rs.next()) {
                                                        idPlantelMovimiento = rs.getInt(1);
                                                }
                                        }
                                }

                                if (idPlantelMovimiento != null && idPlantelMovimiento > 0) {
                                        agregarObservacionPlantelConConexion(
                                                        cn,
                                                        idPlantelMovimiento,
                                                        observacionMovimiento);

                                        agregarObservacionActaGeneralConConexion(
                                                        cn,
                                                        movimientoId,
                                                        fecha,
                                                        tipoFinal,
                                                        cantidad,
                                                        animalNombre,
                                                        destinoFinal);
                                }
                        }

                        // ========================================================
                        // SIEMPRE REGISTRAR EL MOVIMIENTO EN EL ACTA
                        //
                        // FORMATO OBLIGATORIO:
                        //
                        // fecha : acción de X ejemplar(es) de animal
                        // → destino (Movimiento #ID)
                        //
                        // Esto se hace para TODOS los movimientos:
                        // Traslado
                        // Liberación
                        // Fallecimiento
                        // Otro
                        // etc.
                        // ========================================================

                        String observacionActa = formatearFechaMovimiento(fecha) +
                                        " - " +
                                        tipoFinal +
                                        " de " +
                                        cantidad +
                                        " ejemplar(es) de " +
                                        animalNombre +
                                        (destinoFinal.isBlank()
                                                        ? ""
                                                        : " → " + destinoFinal)
                                        +
                                        " - Movimiento #" +
                                        movimientoId;

                        agregarObservacionActaConConexion(
                                        cn,
                                        ingreso,
                                        observacionActa);

                        // ========================================================
                        // ACTUALIZAR ESTADO DEL ACTA
                        //
                        // Si quedó sin animales:
                        //
                        // ACTA ACTUAL SIN ANIMALES
                        //
                        // Si posteriormente vuelve a tener disponibilidad,
                        // este método elimina automáticamente ese mensaje.
                        // ========================================================

                        actualizarEstadoObservacionActaConConexion(
                                        cn,
                                        ingreso);

                        // ========================================================
                        // ACTUALIZAR OBSERVACIÓN DEL HABITÁCULO
                        //
                        // SOLAMENTE:
                        //
                        // Traslado -> Plantel Permanente
                        //
                        // No se modifica la observación del habitáculo para:
                        //
                        // Liberación
                        // Fallecimiento
                        // Otro
                        // Campo Abierto
                        // etc.
                        // ========================================================

                        if (esTraslado &&
                                        vaAPlantel &&
                                        idHabitaculo != null &&
                                        idHabitaculo > 0) {

                                actualizarObservacionHabitaculoConConexion(
                                                cn,
                                                idHabitaculo);
                        }

                        // ========================================================
                        // RECALCULAR STOCK DEL ANIMAL
                        // ========================================================

                        recalcularCantidadAnimalConConexion(
                                        cn,
                                        animal);

                        // ========================================================
                        // CONFIRMAR TRANSACCIÓN
                        // ========================================================

                        cn.commit();

                        return movimientoId;

                } catch (Exception e) {

                        if (cn != null) {

                                try {
                                        cn.rollback();
                                } catch (Exception ignored) {
                                }
                        }

                        if (e instanceof IllegalArgumentException) {
                                throw (IllegalArgumentException) e;
                        }

                        throw new RuntimeException(
                                        "No se pudo guardar el movimiento: " +
                                                        e.getMessage(),
                                        e);

                } finally {

                        if (cn != null) {

                                try {
                                        cn.setAutoCommit(true);
                                } catch (Exception ignored) {
                                }

                                try {
                                        cn.close();
                                } catch (Exception ignored) {
                                }
                        }
                }
        }

        // ============================================================
        // ACTA GENERAL DEL PLANTEL PERMANENTE
        // ============================================================

        /**
         * El Acta General del Plantel es una "acta lógica" dentro de ingresos.
         * No se usa para calcular stock ni aparece en la VistaIngresos.
         * Su contenido vivo se obtiene directamente de plantel_permanente.
         */
        public static int actaGeneralPlantelId() {
                return Integer.parseInt(
                                scalar(
                                                "SELECT id_ingreso " +
                                                                "FROM ingresos " +
                                                                "WHERE tipo_ingreso='PLANTEL_GENERAL' " +
                                                                "AND estado='Activo' " +
                                                                "ORDER BY id_ingreso " +
                                                                "LIMIT 1"));
        }

        /**
         * Agrega al Acta General la trazabilidad de un movimiento que afecta
         * al Plantel Permanente.
         */
        private static void agregarObservacionActaGeneralConConexion(
                        Connection cn,
                        int movimientoId,
                        String fecha,
                        String tipo,
                        int cantidad,
                        String animalNombre,
                        String destino) throws SQLException {

                if (movimientoId <= 0) {
                        return;
                }

                int actaGeneral = 0;

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT id_ingreso FROM ingresos " +
                                                "WHERE tipo_ingreso='PLANTEL_GENERAL' " +
                                                "AND estado='Activo' " +
                                                "ORDER BY id_ingreso LIMIT 1")) {

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        actaGeneral = rs.getInt(1);
                                }
                        }
                }

                if (actaGeneral <= 0) {
                        return;
                }

                agregarMovimientoObservacionActaConConexion(
                                cn,
                                actaGeneral,
                                movimientoId,
                                fecha,
                                tipo,
                                cantidad,
                                animalNombre,
                                destino);
        }

        private static void agregarObservacionActa(
                        int ingreso,
                        String observacion) {

                if (ingreso <= 0 || observacion == null || observacion.isBlank()) {
                        return;
                }

                try (Connection cn = c()) {
                        agregarObservacionActaConConexion(
                                        cn,
                                        ingreso,
                                        observacion);
                } catch (SQLException e) {
                        throw new RuntimeException(
                                        "No se pudo actualizar la observación del acta.",
                                        e);
                }
        }

        private static void agregarObservacionActaGeneral(
                        int movimientoId,
                        String fecha,
                        String tipo,
                        int cantidad,
                        String animalNombre,
                        String destino) {

                try (Connection cn = c()) {
                        agregarObservacionActaGeneralConConexion(
                                        cn,
                                        movimientoId,
                                        fecha,
                                        tipo,
                                        cantidad,
                                        animalNombre,
                                        destino);
                } catch (SQLException e) {
                        throw new RuntimeException(
                                        "No se pudo actualizar el Acta General del Plantel.",
                                        e);
                }
        }

        /**
         * Registros que actualmente componen el Acta General.
         * Se consulta el Plantel activo para que un fallecimiento o un traslado
         * fuera de la Reserva desaparezca automáticamente del acta.
         */
        public static List<Fila> actaGeneralPlantel(String q) {
                sincronizarTrasladosInternosPendientes();
                return filas(
                                "SELECT " +
                                                "p.id_plantel," +
                                                "COALESCE(a.nombre_vulgar,'')," +
                                                "COALESCE((SELECT GROUP_CONCAT(i.tipo_identificacion || ': ' || i.numero_identificacion,' | ') "
                                                +
                                                "FROM identificaciones i WHERE i.id_animal=p.id_animal AND i.estado='Activo'),'Sin identificación'),"
                                                +
                                                "COALESCE(e.nombre,'Sin especie')," +
                                                "p.cantidad," +
                                                "COALESCE(p.tipo_ubicacion,'')," +
                                                "COALESCE(h.nombre,'')," +
                                                "COALESCE(p.fecha_ingreso_plantel,'')," +
                                                "p.estado " +
                                                "FROM plantel_permanente p " +
                                                "JOIN animales a ON a.id_animal=p.id_animal " +
                                                "LEFT JOIN especies e ON e.id_especie=a.id_especie " +
                                                "LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo " +
                                                "WHERE p.estado='Activo' AND p.cantidad>0 " +
                                                "AND (COALESCE(a.nombre_vulgar,'') LIKE ? " +
                                                "OR COALESCE(e.nombre,'') LIKE ? " +
                                                "OR COALESCE(h.nombre,'') LIKE ?) " +
                                                "ORDER BY a.nombre_vulgar, p.id_plantel",
                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%");
        }

        /**
         * Fallecimientos ocurridos mientras el ejemplar pertenecía al Plantel.
         * Un fallecimiento directamente desde un Acta individual no tiene
         * id_plantel y por eso queda fuera de este listado.
         */
        public static List<Fila> fallecimientosPlantel(String q) {
                return filas(
                                "SELECT " +
                                                "m.id_movimiento," +
                                                "m.fecha_movimiento," +
                                                "COALESCE(a.nombre_vulgar,'')," +
                                                "m.cantidad," +
                                                "COALESCE(m.sexo,'')," +
                                                "COALESCE(m.observaciones,'')," +
                                                "COALESCE(p.id_plantel,0) " +
                                                "FROM movimientos m " +
                                                "JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                "LEFT JOIN animales a ON a.id_animal=p.id_animal " +
                                                "WHERE m.estado='Activo' " +
                                                "AND m.id_plantel IS NOT NULL " +
                                                "AND m.tipo_movimiento='Fallecimiento' " +
                                                "AND (COALESCE(a.nombre_vulgar,'') LIKE ? " +
                                                "OR COALESCE(m.observaciones,'') LIKE ?) " +
                                                "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC",
                                "%" + q + "%",
                                "%" + q + "%");
        }

        /**
         * Traslados que efectivamente salen de la Reserva desde el Plantel.
         * Los cambios internos entre habitáculo y campo abierto no aparecen.
         */
        public static List<Fila> trasladosPlantel(String q) {
                return filas(
                                "SELECT " +
                                                "m.id_movimiento," +
                                                "m.fecha_movimiento," +
                                                "COALESCE(a.nombre_vulgar,'')," +
                                                "m.cantidad," +
                                                "COALESCE(m.destino,'')," +
                                                "COALESCE(m.observaciones,'')," +
                                                "COALESCE(p.id_plantel,0) " +
                                                "FROM movimientos m " +
                                                "JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                "LEFT JOIN animales a ON a.id_animal=p.id_animal " +
                                                "WHERE m.estado='Activo' " +
                                                "AND m.id_plantel IS NOT NULL " +
                                                "AND m.tipo_movimiento='Traslado' " +
                                                "AND NOT (" +
                                                "LOWER(TRIM(COALESCE(m.destino,'')))='campo abierto' " +
                                                "OR LOWER(TRIM(COALESCE(m.destino,''))) LIKE 'habitáculo:%' " +
                                                "OR LOWER(TRIM(COALESCE(m.destino,''))) LIKE 'habitáculo ·%' " +
                                                "OR LOWER(TRIM(COALESCE(m.destino,''))) LIKE 'habitáculo -%' " +
                                                "OR LOWER(TRIM(COALESCE(m.destino,''))) LIKE 'plantel permanente%' " +
                                                ") " +
                                                "AND (COALESCE(a.nombre_vulgar,'') LIKE ? " +
                                                "OR COALESCE(m.destino,'') LIKE ? " +
                                                "OR COALESCE(m.observaciones,'') LIKE ?) " +
                                                "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC",
                                "%" + q + "%",
                                "%" + q + "%",
                                "%" + q + "%");
        }

        /**
         * Identifica correctamente los destinos internos del Plantel.
         * Acepta las variantes usadas por la interfaz actual.
         */
        private static boolean destinoEsInternoPlantel(String destino) {
                if (destino == null) {
                        return false;
                }

                String d = destino.trim().toLowerCase();

                return d.equals("campo abierto")
                                || d.startsWith("habitáculo:")
                                || d.startsWith("habitáculo ·")
                                || d.startsWith("habitáculo -")
                                || d.startsWith("plantel permanente");
        }

        private static String extraerNombreHabitaculoDestino(String destino) {
                if (destino == null) {
                        return "";
                }

                String d = destino.trim();

                int pos = d.indexOf(':');

                if (pos >= 0) {
                        return d.substring(pos + 1).trim();
                }

                pos = d.indexOf('·');

                if (pos >= 0) {
                        return d.substring(pos + 1).trim();
                }

                pos = d.indexOf('-');

                if (pos >= 0) {
                        return d.substring(pos + 1).trim();
                }

                return "";
        }

        private static String formatearFechaMovimiento(String fecha) {
                try {
                        return java.time.LocalDate
                                        .parse(fecha)
                                        .format(
                                                        java.time.format.DateTimeFormatter.ofPattern(
                                                                        "dd/MM/yyyy"));
                } catch (Exception e) {
                        return fecha == null ? "" : fecha;
                }
        }

        /**
         * Obtiene el detalle de un movimiento.
         */
        public record MovimientoDetalle(
                        int idMovimiento,
                        int idIngreso,
                        int idDetalle,
                        String fecha,
                        String tipo,
                        int cantidad,
                        String sexo,
                        String destino,
                        String observaciones) {
        }

        public static MovimientoDetalle detalleMovimiento(
                        int idMovimiento) {

                String sql = "SELECT " +
                                "m.id_movimiento," +
                                "COALESCE(i.id_ingreso,0)," +
                                "COALESCE(m.id_detalle,0)," +
                                "m.fecha_movimiento," +
                                "m.tipo_movimiento," +
                                "m.cantidad," +
                                "COALESCE(m.sexo,'')," +
                                "COALESCE(m.destino,'')," +
                                "COALESCE(m.observaciones,'') " +

                                "FROM movimientos m " +

                                "LEFT JOIN detalle_ingreso d " +
                                "ON d.id_detalle=m.id_detalle " +

                                "LEFT JOIN ingresos i " +
                                "ON i.id_ingreso=d.id_ingreso " +

                                "LEFT JOIN plantel_permanente p " +
                                "ON p.id_plantel=m.id_plantel " +

                                "WHERE m.id_movimiento=?";

                try (
                                Connection cn = c();
                                PreparedStatement ps = cn.prepareStatement(sql)) {

                        ps.setInt(1, idMovimiento);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (!rs.next()) {

                                        throw new IllegalArgumentException(
                                                        "El movimiento no existe.");
                                }

                                return new MovimientoDetalle(
                                                rs.getInt(1),
                                                rs.getInt(2),
                                                rs.getInt(3),
                                                rs.getString(4),
                                                rs.getString(5),
                                                rs.getInt(6),
                                                rs.getString(7),
                                                rs.getString(8),
                                                rs.getString(9));
                        }

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "No se pudo obtener el movimiento.",
                                        e);
                }
        }

        /**
         * Modifica un movimiento proveniente de un Acta.
         *
         * Reglas:
         *
         * - El movimiento "Ingreso" no se puede modificar.
         * - El movimiento no puede cambiar de Acta.
         * - Siempre se registra una observación en el Acta.
         * - La observación del Acta siempre contiene:
         *
         * fecha : acción de X ejemplar(es) de animal → destino (Movimiento #ID)
         *
         * - Si el movimiento va al Plantel Permanente, se actualiza
         * también la observación del habitáculo.
         * - Si se cambia de habitáculo, se actualiza tanto el anterior
         * como el nuevo.
         */
        public static void actualizarMovimientoActa(
                        int idMovimiento,
                        int idIngreso,
                        int idDetalle,
                        String fecha,
                        String tipo,
                        int cantidad,
                        String sexo,
                        String edad,
                        String peso,
                        String destino,
                        Integer idHabitaculo,
                        String observaciones) {

                if (idMovimiento <= 0) {
                        throw new IllegalArgumentException(
                                        "El movimiento seleccionado no es válido.");
                }

                if (idIngreso <= 0) {
                        throw new IllegalArgumentException(
                                        "El acta seleccionada no es válida.");
                }

                if (idDetalle <= 0) {
                        throw new IllegalArgumentException(
                                        "El detalle seleccionado no es válido.");
                }

                if (fecha == null || fecha.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La fecha es obligatoria.");
                }

                try {
                        java.time.LocalDate.parse(fecha.trim());
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "La fecha del movimiento no es válida.");
                }

                if (tipo == null || tipo.isBlank()) {
                        throw new IllegalArgumentException(
                                        "El tipo de movimiento es obligatorio.");
                }

                if (cantidad <= 0) {
                        throw new IllegalArgumentException(
                                        "La cantidad debe ser mayor que cero.");
                }

                String tipoFinal = tipo.trim();

                if (!java.util.Set.of(
                                "Traslado",
                                "Liberación",
                                "Fallecimiento",
                                "Otro")
                                .contains(tipoFinal)) {
                        throw new IllegalArgumentException(
                                        "El tipo de movimiento seleccionado no es válido.");
                }

                String destinoFinal = destino == null
                                ? ""
                                : destino.trim();

                boolean vaAPlantel = destinoEsPlantel(destinoFinal)
                                || ("Traslado".equalsIgnoreCase(tipoFinal)
                                                && destinoEsInternoPlantel(destinoFinal));

                if ("Traslado".equalsIgnoreCase(tipoFinal) && vaAPlantel && idHabitaculo == null
                                && destinoFinal.toLowerCase(java.util.Locale.ROOT).startsWith("habitáculo")) {
                        String nombreHab = extraerNombreHabitaculoDestino(destinoFinal);
                        String idHab = scalar(
                                        "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                        + nombreHab.replace("'", "''")
                                                        + "')) AND estado='Activo' LIMIT 1");
                        try {
                                if (idHab != null && !idHab.isBlank() && !"0".equals(idHab.trim())) {
                                        idHabitaculo = Integer.parseInt(idHab);
                                }
                        } catch (Exception ignored) {
                        }
                }

                if (("Traslado".equalsIgnoreCase(tipoFinal)
                                || "Liberación".equalsIgnoreCase(tipoFinal))
                                && destinoFinal.isBlank()) {
                        throw new IllegalArgumentException(
                                        "El destino es obligatorio para este tipo de movimiento.");
                }

                if (vaAPlantel &&
                                !"Traslado".equalsIgnoreCase(tipoFinal)) {
                        throw new IllegalArgumentException(
                                        "El Plantel Permanente solo puede ser destino de un traslado.");
                }

                if (vaAPlantel &&
                                idHabitaculo != null &&
                                idHabitaculo <= 0) {
                        idHabitaculo = null;
                }

                Connection cn = null;

                try {

                        cn = c();
                        cn.setAutoCommit(false);

                        // ========================================================
                        // LEER MOVIMIENTO ANTERIOR
                        // ========================================================

                        int detalleAnterior;
                        int plantelAnterior;
                        int cantidadAnterior;
                        int animalAnterior;
                        int ingresoAnterior;
                        int habitaculoAnterior;
                        String tipoAnterior;

                        String sqlAnterior = "SELECT " +
                                        "m.id_detalle," +
                                        "COALESCE(m.id_plantel,0)," +
                                        "m.cantidad," +
                                        "d.id_animal," +
                                        "d.id_ingreso," +
                                        "m.tipo_movimiento," +
                                        "COALESCE(p.id_habitaculo,0) " +
                                        "FROM movimientos m " +
                                        "JOIN detalle_ingreso d " +
                                        "ON d.id_detalle=m.id_detalle " +
                                        "JOIN animales a " +
                                        "ON a.id_animal=d.id_animal " +
                                        "LEFT JOIN plantel_permanente p " +
                                        "ON p.id_plantel=m.id_plantel " +
                                        "WHERE m.id_movimiento=? " +
                                        "AND m.estado='Activo'";

                        try (PreparedStatement ps = cn.prepareStatement(sqlAnterior)) {

                                ps.setInt(1, idMovimiento);

                                try (ResultSet rs = ps.executeQuery()) {

                                        if (!rs.next()) {
                                                throw new IllegalArgumentException(
                                                                "El movimiento seleccionado no existe o está inactivo.");
                                        }

                                        detalleAnterior = rs.getInt(1);
                                        plantelAnterior = rs.getInt(2);
                                        cantidadAnterior = rs.getInt(3);
                                        animalAnterior = rs.getInt(4);
                                        ingresoAnterior = rs.getInt(5);
                                        tipoAnterior = rs.getString(6);

                                        // IMPORTANTE:
                                        // ahora sí obtenemos el habitáculo anterior.
                                        habitaculoAnterior = rs.getInt(7);
                                }
                        }

                        // ========================================================
                        // EL MOVIMIENTO NO PUEDE CAMBIARSE DE ACTA
                        // ========================================================

                        if (ingresoAnterior != idIngreso) {
                                throw new IllegalArgumentException(
                                                "El movimiento no puede cambiarse a otra acta.");
                        }

                        // ========================================================
                        // EL INGRESO ORIGINAL NO SE MODIFICA
                        // ========================================================

                        if ("Ingreso".equalsIgnoreCase(tipoAnterior)) {
                                throw new IllegalArgumentException(
                                                "Un movimiento de ingreso no puede modificarse desde esta pantalla.");
                        }

                        // ========================================================
                        // VALIDAR NUEVO DETALLE
                        // ========================================================

                        int animalNuevo;
                        int cantidadOriginalNueva;
                        String animalNuevoNombre;

                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT " +
                                                        "d.id_animal," +
                                                        "d.cantidad," +
                                                        "COALESCE(a.nombre_vulgar,'') " +
                                                        "FROM detalle_ingreso d " +
                                                        "JOIN ingresos i " +
                                                        "ON i.id_ingreso=d.id_ingreso " +
                                                        "JOIN animales a " +
                                                        "ON a.id_animal=d.id_animal " +
                                                        "WHERE d.id_detalle=? " +
                                                        "AND d.id_ingreso=? " +
                                                        "AND d.estado='Activo' " +
                                                        "AND i.estado='Activo'")) {

                                ps.setInt(1, idDetalle);
                                ps.setInt(2, idIngreso);

                                try (ResultSet rs = ps.executeQuery()) {

                                        if (!rs.next()) {
                                                throw new IllegalArgumentException(
                                                                "El animal seleccionado no pertenece al acta indicada.");
                                        }

                                        animalNuevo = rs.getInt(1);
                                        cantidadOriginalNueva = rs.getInt(2);
                                        animalNuevoNombre = rs.getString(3);
                                }
                        }

                        // ========================================================
                        // DISPONIBILIDAD DEL NUEVO DETALLE
                        // EXCLUYENDO EL MOVIMIENTO ACTUAL
                        // ========================================================

                        int cantidadUsadaNueva;

                        try (PreparedStatement ps = cn.prepareStatement(
                                        "SELECT COALESCE(SUM(cantidad),0) " +
                                                        "FROM movimientos " +
                                                        "WHERE id_detalle=? " +
                                                        "AND id_movimiento<>? " +
                                                        "AND estado='Activo' " +
                                                        "AND tipo_movimiento<>'Ingreso'")) {

                                ps.setInt(1, idDetalle);
                                ps.setInt(2, idMovimiento);

                                try (ResultSet rs = ps.executeQuery()) {

                                        cantidadUsadaNueva = rs.next()
                                                        ? rs.getInt(1)
                                                        : 0;
                                }
                        }

                        int disponibleNuevo = Math.max(
                                        cantidadOriginalNueva - cantidadUsadaNueva,
                                        0);

                        if (cantidad > disponibleNuevo) {

                                throw new IllegalArgumentException(
                                                "No hay suficiente cantidad disponible en el acta.\n\n" +
                                                                "Disponible: " + disponibleNuevo +
                                                                "\nSolicitado: " + cantidad);
                        }

                        // ========================================================
                        // VALIDAR HABITÁCULO
                        // ========================================================

                        if (vaAPlantel && idHabitaculo != null) {

                                int capacidad = capacidadHabitaculo(
                                                idHabitaculo);

                                if (capacidad <= 0) {
                                        throw new IllegalArgumentException(
                                                        "El habitáculo seleccionado no tiene una capacidad válida.");
                                }

                                int ocupacion = ocupacionHabitaculo(
                                                idHabitaculo,
                                                0);

                                // La cantidad anterior ya ocupa el habitáculo.
                                // La descontamos solamente para validar el nuevo
                                // valor del mismo movimiento.
                                if (habitaculoAnterior > 0 &&
                                                habitaculoAnterior == idHabitaculo) {
                                        ocupacion = Math.max(
                                                        ocupacion - cantidadAnterior,
                                                        0);
                                }

                                if (ocupacion + cantidad > capacidad) {
                                        throw new IllegalArgumentException(
                                                        "El habitáculo seleccionado no tiene suficiente capacidad.\n\n"
                                                                        + "Capacidad: " + capacidad
                                                                        + "\nOcupación disponible para este movimiento: "
                                                                        + ocupacion
                                                                        + "\nSolicitado: " + cantidad);
                                }
                        }

                        // ========================================================
                        // QUITAR SOLAMENTE LA CANTIDAD DEL MOVIMIENTO ANTERIOR
                        //
                        // Un mismo Plantel puede estar vinculado a más de un
                        // movimiento de Acta. Nunca se debe poner a cero todo el
                        // registro al editar uno de ellos.
                        // ========================================================

                        if (plantelAnterior > 0) {

                                updateConConexion(
                                                cn,
                                                "UPDATE plantel_permanente SET " +
                                                                "cantidad=MAX(cantidad-?,0), " +
                                                                "estado=CASE " +
                                                                "WHEN cantidad-?<=0 THEN 'Inactivo' " +
                                                                "ELSE 'Activo' END " +
                                                                "WHERE id_plantel=?",
                                                cantidadAnterior,
                                                cantidadAnterior,
                                                plantelAnterior);
                        }

                        // ========================================================
                        // ACTUALIZAR MOVIMIENTO
                        // ========================================================

                        updateConConexion(
                                        cn,
                                        "UPDATE movimientos SET " +
                                                        "id_detalle=?," +
                                                        "id_plantel=NULL," +
                                                        "fecha_movimiento=?," +
                                                        "tipo_movimiento=?," +
                                                        "cantidad=?," +
                                                        "sexo=?," +
                                                        "destino=?," +
                                                        "observaciones=? " +
                                                        "WHERE id_movimiento=?",

                                        idDetalle,
                                        fecha,
                                        tipoFinal,
                                        cantidad,

                                        sexo == null || sexo.isBlank()
                                                        ? null
                                                        : sexo.trim(),

                                        destinoFinal.isBlank()
                                                        ? null
                                                        : destinoFinal,

                                        observaciones == null ||
                                                        observaciones.isBlank()
                                                                        ? null
                                                                        : observaciones.trim(),

                                        idMovimiento);

                        // ========================================================
                        // SI EL NUEVO DESTINO ES PLANTEL
                        //
                        // Se crea o reutiliza el registro de ubicación correspondiente.
                        // Esto permite que varios movimientos del mismo animal compartan
                        // correctamente un Plantel sin perder cantidades al editar.
                        // ========================================================

                        if (vaAPlantel) {

                                int idPlantel = crearPlantelDesdeMovimiento(
                                                cn,
                                                animalNuevo,
                                                cantidad,
                                                idHabitaculo,
                                                fecha,
                                                observaciones);

                                updateConConexion(
                                                cn,
                                                "UPDATE movimientos " +
                                                                "SET id_plantel=? " +
                                                                "WHERE id_movimiento=?",
                                                idPlantel,
                                                idMovimiento);
                        }

                        // ========================================================
                        // OBSERVACIÓN DEL MOVIMIENTO
                        // ========================================================

                        String observacionMovimiento = formatearFechaMovimiento(fecha) +
                                        " - " +
                                        tipoFinal +
                                        " de " +
                                        cantidad +
                                        " ejemplar(es) de " +
                                        animalNuevoNombre +
                                        (destinoFinal.isBlank()
                                                        ? ""
                                                        : " → " + destinoFinal)
                                        +
                                        " - Movimiento #" +
                                        idMovimiento;

                        String observacionFinal = reemplazarLineaMovimiento(
                                        observaciones == null ? "" : observaciones.trim(),
                                        observacionMovimiento,
                                        idMovimiento);

                        updateConConexion(
                                        cn,
                                        "UPDATE movimientos " +
                                                        "SET observaciones=? " +
                                                        "WHERE id_movimiento=?",
                                        observacionFinal,
                                        idMovimiento);

                        int plantelObservacionId = 0;

                        if (vaAPlantel) {
                                String idPlantelTexto = scalarConConexion(
                                                cn,
                                                "SELECT COALESCE(id_plantel,0) FROM movimientos WHERE id_movimiento=?",
                                                idMovimiento);

                                try {
                                        plantelObservacionId = Integer.parseInt(idPlantelTexto);
                                } catch (Exception ignored) {
                                        plantelObservacionId = 0;
                                }
                        } else if (plantelAnterior > 0) {
                                plantelObservacionId = plantelAnterior;
                        }

                        if (plantelObservacionId > 0) {
                                agregarObservacionPlantelConConexion(
                                                cn,
                                                plantelObservacionId,
                                                observacionMovimiento);

                                agregarObservacionActaGeneralConConexion(
                                                cn,
                                                idMovimiento,
                                                fecha,
                                                tipoFinal,
                                                cantidad,
                                                animalNuevoNombre,
                                                destinoFinal);
                        }

                        // ========================================================
                        // SIEMPRE ACTUALIZAR LA OBSERVACIÓN DEL ACTA
                        //
                        // FORMATO OBLIGATORIO:
                        //
                        // fecha : acción de X ejemplar(es) de animal
                        // → destino (Movimiento #ID)
                        // ========================================================

                        String observacionActa = formatearFechaMovimiento(fecha) +
                                        " - " +
                                        tipoFinal +
                                        " de " +
                                        cantidad +
                                        " ejemplar(es) de " +
                                        animalNuevoNombre +
                                        (destinoFinal.isBlank()
                                                        ? ""
                                                        : " → " + destinoFinal)
                                        +
                                        " - Movimiento #" +
                                        idMovimiento;

                        actualizarMovimientoObservacionActaConConexion(
                                        cn,
                                        idIngreso,
                                        idMovimiento,
                                        observacionActa);

                        // ========================================================
                        // ACTUALIZAR ESTADO DEL ACTA
                        // ========================================================

                        actualizarEstadoObservacionActaConConexion(
                                        cn,
                                        idIngreso);

                        // ========================================================
                        // ACTUALIZAR HABITÁCULO ANTERIOR
                        //
                        // Si el movimiento salió de Plantel o cambió de
                        // habitáculo, hay que reconstruir su observación.
                        // ========================================================

                        if (habitaculoAnterior > 0 &&
                                        (!vaAPlantel ||
                                                        idHabitaculo == null ||
                                                        habitaculoAnterior != idHabitaculo)) {

                                actualizarObservacionHabitaculoConConexion(
                                                cn,
                                                habitaculoAnterior);
                        }

                        // ========================================================
                        // ACTUALIZAR HABITÁCULO NUEVO
                        // ========================================================

                        if (vaAPlantel &&
                                        idHabitaculo != null &&
                                        idHabitaculo > 0) {

                                actualizarObservacionHabitaculoConConexion(
                                                cn,
                                                idHabitaculo);
                        }

                        // ========================================================
                        // RECALCULAR STOCK DEL ANIMAL ANTERIOR
                        // ========================================================

                        recalcularCantidadAnimalConConexion(
                                        cn,
                                        animalAnterior);

                        // ========================================================
                        // RECALCULAR STOCK DEL NUEVO ANIMAL
                        // ========================================================

                        if (animalNuevo != animalAnterior) {

                                recalcularCantidadAnimalConConexion(
                                                cn,
                                                animalNuevo);
                        }

                        cn.commit();

                } catch (Exception e) {

                        if (cn != null) {

                                try {
                                        cn.rollback();
                                } catch (Exception ignored) {
                                }
                        }

                        if (e instanceof IllegalArgumentException) {
                                throw (IllegalArgumentException) e;
                        }

                        throw new RuntimeException(
                                        "No se pudo modificar el movimiento: " +
                                                        e.getMessage(),
                                        e);

                } finally {

                        if (cn != null) {

                                try {
                                        cn.setAutoCommit(true);
                                } catch (Exception ignored) {
                                }

                                try {
                                        cn.close();
                                } catch (Exception ignored) {
                                }
                        }
                }
        }

        public static int guardarMovimiento(
                        int id,
                        Integer detalle,
                        Integer plantel,
                        String fecha,
                        String tipo,
                        int cant,
                        String sexo,
                        String destino,
                        String obs) {

                if (cant <= 0) {
                        throw new IllegalArgumentException(
                                        "La cantidad del movimiento debe ser mayor que cero.");
                }

                // ========================================================
                // NUEVO MOVIMIENTO
                // ========================================================

                if (id == 0) {

                        int habitaculoOrigen = plantel != null && plantel > 0
                                        ? Integer.parseInt(scalar(
                                                        "SELECT COALESCE(id_habitaculo,0) FROM plantel_permanente WHERE id_plantel="
                                                                        + plantel))
                                        : 0;

                        // ------------------------------------------------
                        // VALIDAR CANTIDAD DEL PLANTEL
                        // ------------------------------------------------

                        if (plantel != null && plantel > 0) {

                                int actual = cantidadPlantel(plantel);

                                if (actual <= 0) {
                                        throw new IllegalArgumentException(
                                                        "El ejemplar seleccionado ya no tiene existencia activa en el Plantel.");
                                }

                                if (cant > actual) {
                                        throw new IllegalArgumentException(
                                                        "La cantidad no puede superar la existencia actual ("
                                                                        + actual + ").");
                                }
                        }

                        // ------------------------------------------------
                        int animalTrasladoInterno = 0;
                        boolean trasladoInterno = plantel != null && plantel > 0
                                        && "Traslado".equalsIgnoreCase(tipo)
                                        && destinoEsInternoPlantel(destino);
                        Integer habitaculoDestinoInterno = null;
                        String tipoUbicacionDestinoInterno = null;
                        if (trasladoInterno) {
                                animalTrasladoInterno = animalPorPlantel(plantel);
                                String destinoNormalizado = destino.trim();
                                if (destinoNormalizado.equalsIgnoreCase("Campo abierto")) {
                                        tipoUbicacionDestinoInterno = "Campo Abierto";
                                } else {
                                        tipoUbicacionDestinoInterno = "Habitáculo";
                                        String nombreHab = extraerNombreHabitaculoDestino(destinoNormalizado);
                                        if (nombreHab.isBlank()) {
                                                throw new IllegalArgumentException(
                                                                "Seleccioná el habitáculo de destino.");
                                        }
                                        String idHab = scalar(
                                                        "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                                        + nombreHab.replace("'", "''")
                                                                        + "')) AND estado='Activo' LIMIT 1");
                                        if (idHab == null || idHab.isBlank()) {
                                                throw new IllegalArgumentException(
                                                                "El habitáculo de destino no existe o está inactivo.");
                                        }
                                        habitaculoDestinoInterno = Integer.parseInt(idHab);
                                        int ocupacionDestino = ocupacionHabitaculo(habitaculoDestinoInterno, plantel);
                                        int capacidadDestino = capacidadHabitaculo(habitaculoDestinoInterno);
                                        if (capacidadDestino > 0 && ocupacionDestino + cant > capacidadDestino) {
                                                throw new IllegalArgumentException(
                                                                "El habitáculo seleccionado tiene capacidad para "
                                                                                + capacidadDestino
                                                                                + " ejemplares y ya hay "
                                                                                + ocupacionDestino + " asignados.");
                                        }
                                }
                        }
                        // GUARDAR MOVIMIENTO
                        // ------------------------------------------------

                        int nuevo = update(
                                        "INSERT INTO movimientos(" +
                                                        "id_detalle," +
                                                        "id_plantel," +
                                                        "fecha_movimiento," +
                                                        "tipo_movimiento," +
                                                        "cantidad," +
                                                        "sexo," +
                                                        "destino," +
                                                        "observaciones," +
                                                        "estado" +
                                                        ") VALUES(?,?,?,?,?,?,?,?, 'Activo')",

                                        detalle,
                                        plantel,
                                        fecha,
                                        tipo,
                                        cant,
                                        sexo,
                                        destino,
                                        obs);

                        // ------------------------------------------------
                        // ------------------------------------------------
                        // El traslado interno suma en destino y luego descuenta origen.
                        // ------------------------------------------------
                        if (trasladoInterno) {
                                Integer idHabDestino = habitaculoDestinoInterno;
                                String condicionHab = idHabDestino == null
                                                ? "id_habitaculo IS NULL"
                                                : "id_habitaculo=" + idHabDestino;
                                String idDestinoExistente = scalar(
                                                "SELECT id_plantel FROM plantel_permanente "
                                                                + "WHERE id_animal=" + animalTrasladoInterno
                                                                + " AND estado='Activo' AND LOWER(TRIM(tipo_ubicacion))=LOWER(TRIM('"
                                                                + tipoUbicacionDestinoInterno.replace("'", "''") + "'))"
                                                                + " AND " + condicionHab + " LIMIT 1");
                                String lineaHistorial = formatearFechaMovimiento(fecha)
                                                + " - Traslado interno de " + cant
                                                + " ejemplar(es) → " + destino.trim()
                                                + " - Movimiento #" + nuevo;
                                if (idDestinoExistente != null && !idDestinoExistente.isBlank()) {
                                        int idPlantelDestino = Integer.parseInt(idDestinoExistente);
                                        update("UPDATE plantel_permanente SET cantidad=cantidad+?, estado='Activo', fecha_ingreso_plantel=? WHERE id_plantel=?",
                                                        cant, fecha, idPlantelDestino);
                                        agregarObservacionPlantel(idPlantelDestino, lineaHistorial);
                                } else {
                                        update(
                                                        "INSERT INTO plantel_permanente(id_animal,cantidad,tipo_ubicacion,id_habitaculo,fecha_ingreso_plantel,estado,observaciones) VALUES(?,?,?,?,?,'Activo',?)",
                                                        animalTrasladoInterno, cant, tipoUbicacionDestinoInterno,
                                                        idHabDestino, fecha, lineaHistorial);
                                }
                        }
                        // DESCONTAR DEL PLANTEL
                        // ------------------------------------------------

                        if (plantel != null && plantel > 0) {

                                int animal = animalPorPlantel(plantel);

                                update(
                                                "UPDATE plantel_permanente SET " +
                                                                "cantidad=MAX(cantidad-?,0)," +
                                                                "estado=CASE " +
                                                                "WHEN cantidad-?<=0 THEN 'Inactivo' " +
                                                                "ELSE 'Activo' END " +
                                                                "WHERE id_plantel=?",

                                                cant,
                                                cant,
                                                plantel);

                                recalcularCantidadAnimal(animal);
                        }

                        // ========================================================
                        // TRAZABILIDAD: EL MOVIMIENTO QUEDA REFERENCIADO
                        // EN SU ACTA Y/O EN EL ACTA GENERAL DEL PLANTEL.
                        // ========================================================

                        int ingresoMovimiento = detalle == null
                                        ? 0
                                        : ingresoDeDetalle(detalle);

                        String animalMovimiento = "";

                        if (detalle != null && detalle > 0) {
                                animalMovimiento = scalar(
                                                "SELECT COALESCE(a.nombre_vulgar,'') " +
                                                                "FROM detalle_ingreso d " +
                                                                "JOIN animales a ON a.id_animal=d.id_animal " +
                                                                "WHERE d.id_detalle=" + detalle);
                        } else if (plantel != null && plantel > 0) {
                                animalMovimiento = scalar(
                                                "SELECT COALESCE(a.nombre_vulgar,'') " +
                                                                "FROM plantel_permanente p " +
                                                                "JOIN animales a ON a.id_animal=p.id_animal " +
                                                                "WHERE p.id_plantel=" + plantel);
                        }

                        String observacionMovimiento = formatearFechaMovimiento(fecha) +
                                        " - " +
                                        tipo +
                                        " de " +
                                        cant +
                                        " ejemplar(es)" +
                                        (animalMovimiento == null || animalMovimiento.isBlank()
                                                        ? ""
                                                        : " de " + animalMovimiento)
                                        +
                                        (destino == null || destino.isBlank()
                                                        ? ""
                                                        : " → " + destino)
                                        +
                                        " - Movimiento #" +
                                        nuevo;

                        if (ingresoMovimiento > 0) {
                                agregarObservacionActa(
                                                ingresoMovimiento,
                                                observacionMovimiento);
                        }

                        if (plantel != null && plantel > 0) {
                                agregarObservacionActaGeneral(
                                                nuevo,
                                                fecha,
                                                tipo,
                                                cant,
                                                animalMovimiento,
                                                destino);
                        }

                        if (trasladoInterno) {
                                try (Connection cn = c()) {
                                        if (habitaculoOrigen > 0) {
                                                actualizarObservacionHabitaculoConConexion(cn, habitaculoOrigen);
                                        }
                                        if (habitaculoDestinoInterno != null && habitaculoDestinoInterno > 0
                                                        && habitaculoDestinoInterno != habitaculoOrigen) {
                                                actualizarObservacionHabitaculoConConexion(cn,
                                                                habitaculoDestinoInterno);
                                        }
                                } catch (SQLException ex) {
                                        throw new RuntimeException(
                                                        "No se pudo actualizar la observación del habitáculo.", ex);
                                }
                        }

                        return nuevo;
                }

                // ========================================================
                // EDITAR MOVIMIENTO EXISTENTE
                // ========================================================

                int plantelAnterior = 0;
                int cantidadAnterior = 0;
                int animalAnterior = 0;
                int habitaculoOrigenEdicion = 0;
                int habitaculoDestinoAnteriorEdicion = 0;
                int habitaculoDestinoEdicion = 0;
                int plantelDestinoAnteriorEdicion = 0;
                String destinoAnterior = "";
                String tipoAnteriorEdicion = "";

                // --------------------------------------------------------
                // OBTENER DATOS ANTERIORES
                // --------------------------------------------------------

                String sqlAnterior = "SELECT " +
                                "COALESCE(m.id_plantel,0)," +
                                "m.cantidad," +
                                "COALESCE(p.id_animal,0)," +
                                "COALESCE(m.destino,'')," +
                                "COALESCE(p.id_habitaculo,0)," +
                                "COALESCE(m.tipo_movimiento,'') " +
                                "FROM movimientos m " +
                                "LEFT JOIN plantel_permanente p " +
                                "ON p.id_plantel=m.id_plantel " +
                                "WHERE m.id_movimiento=?";

                try (
                                Connection cn = c();
                                PreparedStatement ps = cn.prepareStatement(sqlAnterior)) {

                        ps.setInt(1, id);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (rs.next()) {

                                        plantelAnterior = rs.getInt(1);
                                        cantidadAnterior = rs.getInt(2);
                                        animalAnterior = rs.getInt(3);
                                        destinoAnterior = rs.getString(4);
                                        habitaculoOrigenEdicion = rs.getInt(5);
                                        tipoAnteriorEdicion = rs.getString(6);
                                }
                        }

                } catch (SQLException e) {

                        throw new RuntimeException(
                                        "No se pudo obtener el movimiento anterior.",
                                        e);
                }

                // Deshacer el efecto anterior en el destino antes de aplicar
                // la edición. El movimiento de Plantel conserva id_plantel en
                // el origen, por eso el destino se resuelve por animal y ubicación.
                if (plantelAnterior > 0 && "Traslado".equalsIgnoreCase(tipoAnteriorEdicion)
                                && destinoEsInternoPlantel(destinoAnterior)) {
                        int animalOrigen = animalAnterior > 0 ? animalAnterior : animalPorPlantel(plantelAnterior);
                        String tipoUbicacionAnterior;
                        Integer habAnterior = null;
                        if ("Campo abierto".equalsIgnoreCase(destinoAnterior.trim())) {
                                tipoUbicacionAnterior = "Campo Abierto";
                        } else {
                                tipoUbicacionAnterior = "Habitáculo";
                                String nombreHabAnterior = extraerNombreHabitaculoDestino(destinoAnterior);
                                String idHabAnterior = nombreHabAnterior.isBlank() ? "0"
                                                : scalar(
                                                                "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                                                + nombreHabAnterior.replace("'", "''")
                                                                                + "')) LIMIT 1");
                                try {
                                        habAnterior = Integer.parseInt(idHabAnterior);
                                } catch (Exception ignored) {
                                }
                        }
                        if (habAnterior != null)
                                habitaculoDestinoAnteriorEdicion = habAnterior;
                        String filtroHabAnterior = habAnterior == null || habAnterior <= 0
                                        ? "id_habitaculo IS NULL"
                                        : "id_habitaculo=" + habAnterior;
                        String idDestinoAnterior = scalar(
                                        "SELECT id_plantel FROM plantel_permanente WHERE id_animal=" + animalOrigen
                                                        + " AND id_plantel<>" + plantelAnterior
                                                        + " AND LOWER(TRIM(tipo_ubicacion))=LOWER(TRIM('"
                                                        + tipoUbicacionAnterior
                                                        + "')) AND " + filtroHabAnterior
                                                        + " ORDER BY id_plantel LIMIT 1");
                        try {
                                plantelDestinoAnteriorEdicion = Integer.parseInt(idDestinoAnterior);
                        } catch (Exception ignored) {
                                plantelDestinoAnteriorEdicion = 0;
                        }
                }

                // ========================================================
                // VALIDAR NUEVO PLANTEL
                // ========================================================

                if (plantel != null && plantel > 0) {

                        int actual = cantidadPlantel(plantel);

                        // Si estamos editando el mismo Plantel,
                        // primero consideramos que la cantidad anterior
                        // volverá a estar disponible.
                        if (plantelAnterior == plantel) {
                                actual += cantidadAnterior;
                        }

                        if (actual <= 0) {

                                throw new IllegalArgumentException(
                                                "El ejemplar seleccionado ya no tiene existencia activa en el Plantel.");
                        }

                        if (cant > actual) {

                                throw new IllegalArgumentException(
                                                "La cantidad no puede superar la existencia disponible ("
                                                                + actual + ").");
                        }
                }

                // ========================================================
                // DEVOLVER LA CANTIDAD ANTERIOR AL PLANTEL
                // ========================================================

                if (plantelAnterior > 0) {

                        update(
                                        "UPDATE plantel_permanente SET " +
                                                        "cantidad=cantidad+?," +
                                                        "estado='Activo' " +
                                                        "WHERE id_plantel=?",

                                        cantidadAnterior,
                                        plantelAnterior);
                }

                // ========================================================
                // SI CAMBIÓ DE PLANTEL
                // VALIDAR NUEVO PLANTEL DESPUÉS DE DEVOLVER EL ANTERIOR
                // ========================================================

                if (plantel != null &&
                                plantel > 0 &&
                                plantelAnterior != plantel) {

                        int actualNuevo = cantidadPlantel(plantel);

                        if (actualNuevo <= 0) {

                                throw new IllegalArgumentException(
                                                "El nuevo Plantel seleccionado no tiene existencia activa.");
                        }

                        if (cant > actualNuevo) {

                                throw new IllegalArgumentException(
                                                "La cantidad no puede superar la existencia disponible ("
                                                                + actualNuevo + ").");
                        }
                }

                // ========================================================
                // ACTUALIZAR MOVIMIENTO
                //
                // IMPORTANTE:
                // NO TOCAMOS LAS OBSERVACIONES.
                // ========================================================

                String observacionEdicion = formatearFechaMovimiento(fecha) +
                                " - " +
                                tipo +
                                " de " +
                                cant +
                                " ejemplar(es)" +
                                (destino == null || destino.isBlank()
                                                ? ""
                                                : " → " + destino)
                                +
                                " - Movimiento #" +
                                id;

                String observacionesActualizadas = reemplazarLineaMovimiento(
                                obs == null ? "" : obs.trim(),
                                observacionEdicion,
                                id);

                update(
                                "UPDATE movimientos SET " +
                                                "id_detalle=?," +
                                                "id_plantel=?," +
                                                "fecha_movimiento=?," +
                                                "tipo_movimiento=?," +
                                                "cantidad=?," +
                                                "sexo=?," +
                                                "destino=?," +
                                                "observaciones=? " +
                                                "WHERE id_movimiento=?",

                                detalle,
                                plantel,
                                fecha,
                                tipo,
                                cant,
                                sexo,
                                destino,
                                observacionesActualizadas,
                                id);

                // ========================================================
                // APLICAR LA NUEVA CANTIDAD AL PLANTEL
                //
                // Si el destino sigue siendo interno, el ejemplar permanece
                // en el Acta General y se mueve de ubicación.
                // ========================================================

                boolean destinoInterno = plantel != null &&
                                plantel > 0 &&
                                destinoEsInternoPlantel(destino);

                int plantelDestino = 0;

                if (destinoInterno) {

                        int animalDestino = animalAnterior > 0
                                        ? animalAnterior
                                        : animalPorPlantel(plantel);

                        String tipoDestino;
                        Integer habitaculoDestino = null;

                        if ("Campo abierto".equalsIgnoreCase(
                                        destino.trim())) {

                                tipoDestino = "Campo Abierto";

                        } else {

                                tipoDestino = "Habitáculo";

                                String nombreHabitaculo = extraerNombreHabitaculoDestino(destino);

                                if (nombreHabitaculo.isBlank()) {
                                        throw new IllegalArgumentException(
                                                        "No se pudo determinar el habitáculo de destino.");
                                }

                                String idHabitaculoDestino = scalar(
                                                "SELECT id_habitaculo " +
                                                                "FROM habitaculos " +
                                                                "WHERE LOWER(TRIM(nombre))=LOWER(TRIM('" +
                                                                nombreHabitaculo.replace("'", "''") +
                                                                "')) " +
                                                                "AND estado='Activo' " +
                                                                "LIMIT 1");

                                if (idHabitaculoDestino == null ||
                                                idHabitaculoDestino.isBlank() ||
                                                "0".equals(idHabitaculoDestino)) {

                                        throw new IllegalArgumentException(
                                                        "El habitáculo de destino no existe o está inactivo.");
                                }

                                habitaculoDestino = Integer.parseInt(idHabitaculoDestino);
                        }

                        if (plantelDestino == 0) {

                                String condicionHabitaculo = habitaculoDestino == null
                                                ? "id_habitaculo IS NULL"
                                                : "id_habitaculo=" +
                                                                habitaculoDestino;

                                String idDestino = scalar(
                                                "SELECT id_plantel " +
                                                                "FROM plantel_permanente " +
                                                                "WHERE id_animal=" + animalDestino +
                                                                " AND estado='Activo' " +
                                                                "AND LOWER(TRIM(COALESCE(tipo_ubicacion,'')))=LOWER(TRIM('"
                                                                +
                                                                tipoDestino.replace("'", "''") +
                                                                "')) " +
                                                                "AND " + condicionHabitaculo +
                                                                " AND id_plantel<>" + plantel +
                                                                " LIMIT 1");

                                if (idDestino != null &&
                                                !idDestino.isBlank() &&
                                                !"0".equals(idDestino)) {

                                        plantelDestino = Integer.parseInt(idDestino);
                                }
                        }

                        // Si el destino es exactamente el mismo registro,
                        // no se debe descontar ni duplicar cantidad.
                        if (plantelDestino == 0) {

                                String tipoOrigen = scalar(
                                                "SELECT LOWER(TRIM(COALESCE(tipo_ubicacion,''))) " +
                                                                "FROM plantel_permanente " +
                                                                "WHERE id_plantel=" + plantel);

                                String habOrigen = scalar(
                                                "SELECT COALESCE(id_habitaculo,0) " +
                                                                "FROM plantel_permanente " +
                                                                "WHERE id_plantel=" + plantel);

                                boolean mismaUbicacion = tipoOrigen.equalsIgnoreCase(
                                                tipoDestino.trim().toLowerCase()) &&
                                                ((habitaculoDestino == null
                                                                && ("0".equals(habOrigen) || habOrigen.isBlank()))
                                                                || (habitaculoDestino != null &&
                                                                                String.valueOf(habitaculoDestino)
                                                                                                .equals(habOrigen)));

                                if (mismaUbicacion) {
                                        plantelDestino = plantel;
                                }
                        }

                        if (plantelDestino != plantel) {

                                if (habitaculoDestino != null) {

                                        int ocupacion = ocupacionHabitaculo(
                                                        habitaculoDestino,
                                                        plantel);

                                        int capacidad = capacidadHabitaculo(
                                                        habitaculoDestino);

                                        if (plantelDestinoAnteriorEdicion > 0
                                                        && plantelDestinoAnteriorEdicion == plantelDestino) {
                                                ocupacion = Math.max(0, ocupacion - cantidadAnterior);
                                        }

                                        if (capacidad > 0 &&
                                                        ocupacion + cant > capacidad) {

                                                throw new IllegalArgumentException(
                                                                "El habitáculo de destino no tiene suficiente capacidad.");
                                        }
                                }

                                if (plantelDestinoAnteriorEdicion > 0) {
                                        String obsDestinoAnterior = scalar(
                                                        "SELECT COALESCE(observaciones,'') FROM plantel_permanente WHERE id_plantel="
                                                                        + plantelDestinoAnteriorEdicion);
                                        update("UPDATE plantel_permanente SET observaciones=? WHERE id_plantel=?",
                                                        reemplazarLineaMovimiento(obsDestinoAnterior, "", id),
                                                        plantelDestinoAnteriorEdicion);
                                        update("UPDATE plantel_permanente SET cantidad=MAX(cantidad-?,0),estado=CASE WHEN cantidad-?<=0 THEN 'Inactivo' ELSE 'Activo' END WHERE id_plantel=?",
                                                        cantidadAnterior, cantidadAnterior,
                                                        plantelDestinoAnteriorEdicion);
                                }

                                int diferenciaOrigen = ("Traslado".equalsIgnoreCase(tipoAnteriorEdicion)
                                                ? cantidadAnterior
                                                : 0)
                                                - ("Traslado".equalsIgnoreCase(tipo) ? cant : 0);
                                update(
                                                "UPDATE plantel_permanente SET " +
                                                                "cantidad=MAX(cantidad+?,0)," +
                                                                "estado=CASE " +
                                                                "WHEN cantidad+?<=0 THEN 'Inactivo' " +
                                                                "ELSE 'Activo' END " +
                                                                "WHERE id_plantel=?",
                                                diferenciaOrigen,
                                                diferenciaOrigen,
                                                plantel);

                                if (plantelDestino > 0) {

                                        update(
                                                        "UPDATE plantel_permanente SET " +
                                                                        "cantidad=cantidad+?," +
                                                                        "fecha_ingreso_plantel=?," +
                                                                        "estado='Activo' " +
                                                                        "WHERE id_plantel=?",
                                                        cant,
                                                        fecha,
                                                        plantelDestino);

                                } else {

                                        plantelDestino = update(
                                                        "INSERT INTO plantel_permanente(" +
                                                                        "id_animal,cantidad,tipo_ubicacion,id_habitaculo,"
                                                                        +
                                                                        "fecha_ingreso_plantel,estado,observaciones" +
                                                                        ") VALUES(?,?,?,?,?,'Activo',?)",
                                                        animalDestino,
                                                        cant,
                                                        tipoDestino,
                                                        habitaculoDestino,
                                                        fecha,
                                                        obs);
                                }

                        }

                        recalcularCantidadAnimal(animalDestino);

                        if (animalAnterior != animalDestino &&
                                        animalAnterior > 0) {

                                recalcularCantidadAnimal(animalAnterior);
                        }

                } else if (plantel != null && plantel > 0) {

                        if (plantelDestinoAnteriorEdicion > 0) {
                                String obsDestinoAnterior = scalar(
                                                "SELECT COALESCE(observaciones,'') FROM plantel_permanente WHERE id_plantel="
                                                                + plantelDestinoAnteriorEdicion);
                                update("UPDATE plantel_permanente SET observaciones=? WHERE id_plantel=?",
                                                reemplazarLineaMovimiento(obsDestinoAnterior, "", id),
                                                plantelDestinoAnteriorEdicion);
                                update("UPDATE plantel_permanente SET cantidad=MAX(cantidad-?,0),estado=CASE WHEN cantidad-?<=0 THEN 'Inactivo' ELSE 'Activo' END WHERE id_plantel=?",
                                                cantidadAnterior, cantidadAnterior, plantelDestinoAnteriorEdicion);
                        }

                        int animalNuevo = animalPorPlantel(plantel);

                        int diferenciaOrigen = ("Traslado".equalsIgnoreCase(tipoAnteriorEdicion)
                                        ? cantidadAnterior
                                        : 0)
                                        - ("Traslado".equalsIgnoreCase(tipo) ? cant : 0);
                        update(
                                        "UPDATE plantel_permanente SET " +
                                                        "cantidad=MAX(cantidad+?,0)," +
                                                        "estado=CASE " +
                                                        "WHEN cantidad+?<=0 THEN 'Inactivo' " +
                                                        "ELSE 'Activo' END " +
                                                        "WHERE id_plantel=?",
                                        diferenciaOrigen,
                                        diferenciaOrigen,
                                        plantel);

                        recalcularCantidadAnimal(animalNuevo);

                        if (animalAnterior != animalNuevo &&
                                        animalAnterior > 0) {

                                recalcularCantidadAnimal(animalAnterior);
                        }

                } else {

                        // Si el movimiento dejó de salir del Plantel,
                        // el animal anterior recupera su stock.

                        if (animalAnterior > 0) {
                                recalcularCantidadAnimal(animalAnterior);
                        }
                }

                String animalObservacion = "";

                if (plantel != null && plantel > 0) {
                        animalObservacion = scalar(
                                        "SELECT COALESCE(a.nombre_vulgar,'') " +
                                                        "FROM plantel_permanente p " +
                                                        "JOIN animales a ON a.id_animal=p.id_animal " +
                                                        "WHERE p.id_plantel=" + plantel);
                } else if (animalAnterior > 0) {
                        animalObservacion = scalar(
                                        "SELECT COALESCE(nombre_vulgar,'') " +
                                                        "FROM animales WHERE id_animal=" + animalAnterior);
                }

                if ((plantel != null && plantel > 0) || plantelAnterior > 0) {
                        int plantelParaObservacion = plantel != null && plantel > 0
                                        ? plantel
                                        : plantelAnterior;
                        String lineaHistorial = observacionMovimiento(
                                        fecha,
                                        tipo,
                                        cant,
                                        animalObservacion,
                                        destino,
                                        id);
                        if (plantelParaObservacion > 0) {
                                agregarObservacionPlantel(
                                                plantelParaObservacion,
                                                lineaHistorial);
                        }
                        if (destinoInterno && plantelDestino > 0 && plantelDestino != plantelParaObservacion) {
                                agregarObservacionPlantel(plantelDestino, lineaHistorial);
                        }
                        agregarObservacionActaGeneral(
                                        id,
                                        fecha,
                                        tipo,
                                        cant,
                                        animalObservacion,
                                        destino);
                }

                if (destinoInterno) {
                        String nuevoDestino = destino == null ? "" : destino.trim();
                        if (nuevoDestino.toLowerCase(java.util.Locale.ROOT).startsWith("habitáculo")) {
                                String nombreHab = extraerNombreHabitaculoDestino(nuevoDestino);
                                String idHabNuevo = nombreHab.isBlank() ? "0"
                                                : scalar(
                                                                "SELECT id_habitaculo FROM habitaculos WHERE LOWER(TRIM(nombre))=LOWER(TRIM('"
                                                                                + nombreHab.replace("'", "''")
                                                                                + "')) LIMIT 1");
                                try {
                                        habitaculoDestinoEdicion = Integer.parseInt(idHabNuevo);
                                } catch (Exception ignored) {
                                }
                        }
                }
                try (Connection cn = c()) {
                        int[] afectados = { habitaculoOrigenEdicion, habitaculoDestinoAnteriorEdicion,
                                        habitaculoDestinoEdicion };
                        java.util.Set<Integer> unicos = new java.util.LinkedHashSet<>();
                        for (int habAfectado : afectados)
                                if (habAfectado > 0)
                                        unicos.add(habAfectado);
                        for (int habAfectado : unicos)
                                actualizarObservacionHabitaculoConConexion(cn, habAfectado);
                } catch (SQLException e) {
                        throw new RuntimeException("No se pudieron actualizar las observaciones de los habitáculos.",
                                        e);
                }

                return id;
        }

        private static int obtenerLastInsertId(
                        Connection cn) throws SQLException {

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT last_insert_rowid()")) {

                        try (ResultSet rs = ps.executeQuery()) {

                                if (!rs.next()) {
                                        throw new SQLException(
                                                        "No se pudo obtener el ID generado.");
                                }

                                return rs.getInt(1);
                        }
                }
        }

        private static String scalarConConexion(
                        Connection cn,
                        String sql,
                        Object... params) throws SQLException {

                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                        for (int i = 0; i < params.length; i++) {
                                if (params[i] == null) {
                                        ps.setNull(i + 1, Types.NULL);
                                } else {
                                        ps.setObject(i + 1, params[i]);
                                }
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() ? rs.getString(1) : "0";
                        }
                }
        }

        private static void updateConConexion(
                        Connection cn,
                        String sql,
                        Object... params) throws SQLException {

                try (PreparedStatement ps = cn.prepareStatement(sql)) {

                        for (int i = 0; i < params.length; i++) {

                                Object value = params[i];

                                if (value == null) {

                                        ps.setNull(
                                                        i + 1,
                                                        Types.NULL);

                                } else {

                                        ps.setObject(
                                                        i + 1,
                                                        value);
                                }
                        }

                        ps.executeUpdate();
                }
        }

        private static void validarCapacidadHabitaculo(
                        Connection cn,
                        Integer idHabitaculo,
                        int excluirPlantel,
                        int cantidad) throws SQLException {

                if (idHabitaculo == null ||
                                idHabitaculo <= 0) {

                        throw new IllegalArgumentException(
                                        "El habitáculo seleccionado no es válido.");
                }

                int capacidad;

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(capacidad,0) " +
                                                "FROM habitaculos " +
                                                "WHERE id_habitaculo=? " +
                                                "AND estado='Activo'")) {

                        ps.setInt(
                                        1,
                                        idHabitaculo);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (!rs.next()) {

                                        throw new IllegalArgumentException(
                                                        "El habitáculo seleccionado no existe o está inactivo.");
                                }

                                capacidad = rs.getInt(1);
                        }
                }

                if (capacidad <= 0) {

                        throw new IllegalArgumentException(
                                        "El habitáculo seleccionado no tiene una capacidad válida.");
                }

                int ocupacion;

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(SUM(cantidad),0) " +
                                                "FROM plantel_permanente " +
                                                "WHERE id_habitaculo=? " +
                                                "AND estado='Activo' " +
                                                "AND cantidad>0 " +
                                                "AND id_plantel<>?")) {

                        ps.setInt(
                                        1,
                                        idHabitaculo);

                        ps.setInt(
                                        2,
                                        excluirPlantel);

                        try (ResultSet rs = ps.executeQuery()) {

                                ocupacion = rs.next()
                                                ? rs.getInt(1)
                                                : 0;
                        }
                }

                if (ocupacion + cantidad > capacidad) {

                        throw new IllegalArgumentException(
                                        "El habitáculo seleccionado no tiene suficiente capacidad.\n\n" +
                                                        "Capacidad: " +
                                                        capacidad +
                                                        "\nOcupación actual: " +
                                                        ocupacion +
                                                        "\nCantidad solicitada: " +
                                                        cantidad);
                }
        }

        private static boolean destinoEsPlantel(String destino) {
                if (destino == null) {
                        return false;
                }

                String normalizado = destino.trim()
                                .replaceAll("\\s+", " ")
                                .toLowerCase(java.util.Locale.ROOT);

                return normalizado.equals("plantel permanente")
                                || normalizado.startsWith("plantel permanente ·")
                                || normalizado.startsWith("plantel permanente -")
                                || normalizado.startsWith("plantel permanente:");
        }

        private static void agregarObservacionPlantelConConexion(
                        Connection cn,
                        int idPlantel,
                        String observacion) throws SQLException {

                if (idPlantel <= 0 || observacion == null || observacion.isBlank()) {
                        return;
                }

                String actual = "";

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(observaciones,'') FROM plantel_permanente WHERE id_plantel=?")) {
                        ps.setInt(1, idPlantel);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        actual = rs.getString(1);
                                }
                        }
                }

                if (actual == null) {
                        actual = "";
                }

                int idMovimiento = idMovimientoEnObservacion(observacion);
                String nueva = idMovimiento > 0
                                ? reemplazarLineaMovimiento(actual, observacion, idMovimiento)
                                : (actual.contains(observacion.trim()) ? actual
                                                : actual.isBlank() ? observacion.trim()
                                                                : actual + "\n" + observacion.trim());

                if (nueva.equals(actual))
                        return;

                updateConConexion(
                                cn,
                                "UPDATE plantel_permanente SET observaciones=? WHERE id_plantel=?",
                                nueva,
                                idPlantel);
        }

        private static void agregarObservacionPlantel(
                        int idPlantel,
                        String observacion) {

                if (idPlantel <= 0 || observacion == null || observacion.isBlank()) {
                        return;
                }

                String actual = scalar(
                                "SELECT COALESCE(observaciones,'') FROM plantel_permanente WHERE id_plantel="
                                                + idPlantel);

                String textoActual = actual == null ? "" : actual;
                int idMovimiento = idMovimientoEnObservacion(observacion);
                String nueva = idMovimiento > 0
                                ? reemplazarLineaMovimiento(textoActual, observacion, idMovimiento)
                                : (textoActual.contains(observacion.trim()) ? textoActual
                                                : textoActual.isBlank() ? observacion.trim()
                                                                : textoActual + "\n" + observacion.trim());

                if (nueva.equals(textoActual))
                        return;

                update(
                                "UPDATE plantel_permanente SET observaciones=? WHERE id_plantel=?",
                                nueva,
                                idPlantel);
        }

        private static int crearPlantelDesdeMovimiento(
                        Connection cn,
                        int animal,
                        int cantidad,
                        Integer idHabitaculo,
                        String fecha,
                        String observaciones) throws SQLException {

                if (animal <= 0) {
                        throw new IllegalArgumentException(
                                        "El animal asociado al movimiento no es válido.");
                }

                if (cantidad <= 0) {
                        throw new IllegalArgumentException(
                                        "La cantidad para el Plantel debe ser mayor que cero.");
                }

                String tipoUbicacion = idHabitaculo == null
                                ? "Campo abierto"
                                : "Habitáculo";

                // Un mismo animal puede llegar al mismo destino en más de un
                // movimiento. El Plantel representa la existencia acumulada de esa
                // ubicación, por lo que reutilizamos el registro activo.
                String existenteSql = "SELECT id_plantel FROM plantel_permanente " +
                                "WHERE id_animal=? " +
                                "AND estado='Activo' " +
                                "AND LOWER(TRIM(COALESCE(tipo_ubicacion,'')))=LOWER(TRIM(?)) " +
                                "AND ((id_habitaculo IS NULL AND ? IS NULL) OR id_habitaculo=?) " +
                                "ORDER BY id_plantel LIMIT 1";

                try (PreparedStatement pe = cn.prepareStatement(existenteSql)) {

                        pe.setInt(1, animal);
                        pe.setString(2, tipoUbicacion);

                        if (idHabitaculo == null) {
                                pe.setNull(3, Types.INTEGER);
                                pe.setNull(4, Types.INTEGER);
                        } else {
                                pe.setInt(3, idHabitaculo);
                                pe.setInt(4, idHabitaculo);
                        }

                        try (ResultSet re = pe.executeQuery()) {

                                if (re.next()) {
                                        int idPlantel = re.getInt(1);

                                        updateConConexion(
                                                        cn,
                                                        "UPDATE plantel_permanente SET " +
                                                                        "cantidad=cantidad+?," +
                                                                        "fecha_ingreso_plantel=?," +
                                                                        "estado='Activo' " +
                                                                        "WHERE id_plantel=?",
                                                        cantidad,
                                                        fecha,
                                                        idPlantel);

                                        if (observaciones != null &&
                                                        !observaciones.isBlank()) {
                                                agregarObservacionPlantelConConexion(
                                                                cn,
                                                                idPlantel,
                                                                observaciones.trim());
                                        }

                                        return idPlantel;
                                }
                        }
                }

                String sql = "INSERT INTO plantel_permanente(" +
                                "id_animal," +
                                "cantidad," +
                                "tipo_ubicacion," +
                                "id_habitaculo," +
                                "fecha_ingreso_plantel," +
                                "estado," +
                                "observaciones" +
                                ") VALUES(?,?,?,?,?,'Activo',?)";

                try (PreparedStatement ps = cn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, animal);
                        ps.setInt(2, cantidad);
                        ps.setString(3, tipoUbicacion);

                        if (idHabitaculo == null) {
                                ps.setNull(4, Types.INTEGER);
                        } else {
                                ps.setInt(4, idHabitaculo);
                        }

                        ps.setString(5, fecha);

                        if (observaciones == null ||
                                        observaciones.isBlank()) {
                                ps.setNull(6, Types.VARCHAR);
                        } else {
                                ps.setString(6, observaciones.trim());
                        }

                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                                if (!rs.next()) {
                                        throw new SQLException(
                                                        "No se pudo obtener el Plantel generado.");
                                }

                                return rs.getInt(1);
                        }
                }
        }

        private static int cantidadDisponibleActaConConexion(
                        Connection cn,
                        int ingreso) throws SQLException {

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(SUM(" +
                                                "MAX(" +
                                                "d.cantidad - " +
                                                "COALESCE((" +
                                                "SELECT SUM(m.cantidad) " +
                                                "FROM movimientos m " +
                                                "WHERE m.id_detalle=d.id_detalle " +
                                                "AND m.estado='Activo' " +
                                                "AND m.tipo_movimiento<>'Ingreso'" +
                                                "),0)" +
                                                ",0)" +
                                                "),0) " +
                                                "FROM detalle_ingreso d " +
                                                "JOIN ingresos i " +
                                                "ON i.id_ingreso=d.id_ingreso " +
                                                "WHERE d.id_ingreso=? " +
                                                "AND d.estado='Activo' " +
                                                "AND i.estado='Activo'")) {

                        ps.setInt(
                                        1,
                                        ingreso);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (!rs.next()) {
                                        return 0;
                                }

                                return Math.max(
                                                rs.getInt(1),
                                                0);
                        }
                }
        }

        /**
         * Garantiza que los movimientos tengan su referencia en las observaciones
         * del acta correspondiente. También repara referencias faltantes de datos
         * históricos al abrir la vista de Movimientos. La operación es idempotente:
         * cada movimiento conserva una sola línea vigente por su ID.
         */
        private static void sincronizarObservacionesMovimientos() {
                if (observacionesMovimientosSincronizadas)
                        return;
                synchronized (SistemaDAO.class) {
                        if (observacionesMovimientosSincronizadas)
                                return;

                        List<Fila> movimientos = filas(
                                        "SELECT m.id_movimiento,COALESCE(d.id_ingreso,0),COALESCE(m.id_plantel,0)," +
                                                        "COALESCE(m.fecha_movimiento,''),COALESCE(m.tipo_movimiento,''),"
                                                        +
                                                        "COALESCE(m.cantidad,0),COALESCE(a.nombre_vulgar,''),COALESCE(m.destino,'') "
                                                        +
                                                        "FROM movimientos m " +
                                                        "LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle " +
                                                        "LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                        "LEFT JOIN animales a ON a.id_animal=COALESCE(d.id_animal,p.id_animal) "
                                                        +
                                                        "ORDER BY m.id_movimiento");

                        for (Fila fila : movimientos) {
                                List<String> celdas = fila.c();
                                if (celdas.size() < 8)
                                        continue;

                                int idMovimiento = enteroSeguro(celdas.get(0));
                                int idIngreso = enteroSeguro(celdas.get(1));
                                int idPlantel = enteroSeguro(celdas.get(2));
                                String fecha = celdas.get(3);
                                String tipo = celdas.get(4);
                                int cantidad = enteroSeguro(celdas.get(5));
                                String animal = celdas.get(6);
                                String destino = celdas.get(7);
                                if (idMovimiento <= 0 || cantidad <= 0)
                                        continue;

                                String linea = observacionMovimiento(fecha, tipo, cantidad, animal, destino,
                                                idMovimiento);
                                if (idIngreso > 0) {
                                        agregarObservacionActa(idIngreso, linea);
                                }
                                if (idPlantel > 0) {
                                        agregarObservacionPlantel(idPlantel, linea);
                                        agregarObservacionActaGeneral(idMovimiento, fecha, tipo, cantidad, animal,
                                                        destino);
                                }
                        }
                        observacionesMovimientosSincronizadas = true;
                }
        }

        private static int enteroSeguro(String valor) {
                try {
                        return Integer.parseInt(valor == null ? "0" : valor.trim());
                } catch (NumberFormatException e) {
                        return 0;
                }
        }

        private static String observacionMovimiento(
                        String fecha, String tipo, int cantidad, String animal, String destino, int idMovimiento) {
                return formatearFechaMovimiento(fecha) + " - " +
                                (tipo == null || tipo.isBlank() ? "Movimiento" : tipo.trim()) +
                                " de " + cantidad + " ejemplar(es)" +
                                (animal == null || animal.isBlank() ? "" : " de " + animal.trim()) +
                                (destino == null || destino.isBlank() ? "" : " → " + destino.trim()) +
                                " - Movimiento #" + idMovimiento;
        }

        /**
         * Sustituye solamente la línea del movimiento indicado y conserva el resto del
         * historial.
         */
        private static String reemplazarLineaMovimiento(String actual, String nuevaLinea, int idMovimiento) {
                String texto = actual == null ? "" : actual;
                String patron = "(?i).*\\bMovimiento\\s*#\\s*" + idMovimiento + "\\b.*";
                StringBuilder resultado = new StringBuilder();
                for (String linea : texto.split("\\R")) {
                        if (!linea.matches(patron) && !linea.isBlank()) {
                                if (resultado.length() > 0)
                                        resultado.append("\n");
                                resultado.append(linea);
                        }
                }
                if (nuevaLinea != null && !nuevaLinea.isBlank()) {
                        if (resultado.length() > 0)
                                resultado.append("\n");
                        resultado.append(nuevaLinea.trim());
                }
                return resultado.toString();
        }

        private static int idMovimientoEnObservacion(String observacion) {
                if (observacion == null)
                        return 0;
                java.util.regex.Matcher matcher = java.util.regex.Pattern
                                .compile("(?i)\\bMovimiento\\s*#\\s*(\\d+)")
                                .matcher(observacion);
                return matcher.find() ? enteroSeguro(matcher.group(1)) : 0;
        }

        private static void agregarObservacionActaConConexion(
                        Connection cn,
                        int ingreso,
                        String observacion) throws SQLException {

                if (ingreso <= 0 ||
                                observacion == null ||
                                observacion.isBlank()) {

                        return;
                }

                String actual = "";

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(observaciones,'') " +
                                                "FROM ingresos " +
                                                "WHERE id_ingreso=?")) {

                        ps.setInt(1, ingreso);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (rs.next()) {
                                        actual = rs.getString(1);
                                }
                        }
                }

                if (actual == null) {
                        actual = "";
                }

                int idMovimiento = idMovimientoEnObservacion(observacion);
                String nuevo = idMovimiento > 0
                                ? reemplazarLineaMovimiento(actual, observacion, idMovimiento)
                                : (actual.contains(observacion) ? actual
                                                : actual.isBlank() ? observacion : actual + "\n" + observacion);
                if (nuevo.equals(actual))
                        return;

                updateConConexion(
                                cn,
                                "UPDATE ingresos " +
                                                "SET observaciones=? " +
                                                "WHERE id_ingreso=?",
                                nuevo,
                                ingreso);
        }

        private static void recalcularCantidadAnimalConConexion(
                        Connection cn,
                        int animal) throws SQLException {

                if (animal <= 0) {
                        return;
                }

                int plantel;

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(SUM(cantidad),0) " +
                                                "FROM plantel_permanente " +
                                                "WHERE id_animal=? " +
                                                "AND estado='Activo' " +
                                                "AND cantidad>0")) {

                        ps.setInt(
                                        1,
                                        animal);

                        try (ResultSet rs = ps.executeQuery()) {

                                plantel = rs.next()
                                                ? rs.getInt(1)
                                                : 0;
                        }
                }

                int ingresos;

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(SUM(" +
                                                "MAX(" +
                                                "d.cantidad - " +
                                                "COALESCE((" +
                                                "SELECT SUM(m.cantidad) " +
                                                "FROM movimientos m " +
                                                "WHERE m.id_detalle=d.id_detalle " +
                                                "AND m.estado='Activo' " +
                                                "AND m.tipo_movimiento<>'Ingreso'" +
                                                "),0)" +
                                                ",0)" +
                                                "),0) " +
                                                "FROM detalle_ingreso d " +
                                                "JOIN ingresos i " +
                                                "ON i.id_ingreso=d.id_ingreso " +
                                                "WHERE d.id_animal=? " +
                                                "AND d.estado='Activo' " +
                                                "AND i.estado='Activo'")) {

                        ps.setInt(
                                        1,
                                        animal);

                        try (ResultSet rs = ps.executeQuery()) {

                                ingresos = rs.next()
                                                ? rs.getInt(1)
                                                : 0;
                        }
                }

                int total = Math.max(
                                plantel + ingresos,
                                0);

                updateConConexion(
                                cn,
                                "UPDATE animales " +
                                                "SET cantidad_actual=? " +
                                                "WHERE id_animal=?",
                                total,
                                animal);
        }

        private static void agregarMovimientoObservacionActaConConexion(
                        Connection cn,
                        int ingreso,
                        int movimientoId,
                        String fecha,
                        String tipo,
                        int cantidad,
                        String animalNombre,
                        String destino) throws SQLException {

                if (ingreso <= 0 || movimientoId <= 0) {
                        return;
                }

                String fechaTexto = fecha == null || fecha.isBlank()
                                ? ""
                                : fecha.trim();

                String tipoTexto = tipo == null
                                ? ""
                                : tipo.trim();

                String animalTexto = animalNombre == null
                                ? ""
                                : animalNombre.trim();

                String destinoTexto = destino == null
                                ? ""
                                : destino.trim();

                String observacion = observacionMovimiento(
                                fechaTexto,
                                tipoTexto,
                                cantidad,
                                animalTexto,
                                destinoTexto,
                                movimientoId);

                agregarObservacionActaConConexion(
                                cn,
                                ingreso,
                                observacion);
        }

        private static void actualizarMovimientoObservacionActaConConexion(
                        Connection cn,
                        int ingreso,
                        int movimientoId,
                        String nuevaObservacion) throws SQLException {

                if (ingreso <= 0 ||
                                movimientoId <= 0 ||
                                nuevaObservacion == null ||
                                nuevaObservacion.isBlank()) {
                        return;
                }

                String actual = "";

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(observaciones,'') " +
                                                "FROM ingresos " +
                                                "WHERE id_ingreso=?")) {

                        ps.setInt(1, ingreso);

                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        actual = rs.getString(1);
                                }
                        }
                }

                if (actual == null) {
                        actual = "";
                }

                String nueva = nuevaObservacion.trim();
                int idEnLinea = idMovimientoEnObservacion(nueva);
                String resultado = idEnLinea > 0
                                ? reemplazarLineaMovimiento(actual, nueva, idEnLinea)
                                : (actual.contains(nueva) ? actual
                                                : actual.isBlank() ? nueva : actual + "\n" + nueva);

                if (resultado.equals(actual))
                        return;

                updateConConexion(
                                cn,
                                "UPDATE ingresos " +
                                                "SET observaciones=? " +
                                                "WHERE id_ingreso=?",
                                resultado,
                                ingreso);
        }

        /**
         * Mantiene correctamente el mensaje
         * "ACTA ACTUAL SIN ANIMALES".
         *
         * Primero elimina cualquier mensaje anterior
         * y luego lo vuelve a agregar solamente si
         * realmente no quedan animales disponibles.
         */
        private static void actualizarEstadoObservacionActaConConexion(
                        Connection cn,
                        int ingreso) throws SQLException {

                if (ingreso <= 0) {
                        return;
                }

                String actual = "";

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(observaciones,'') " +
                                                "FROM ingresos " +
                                                "WHERE id_ingreso=?")) {

                        ps.setInt(1, ingreso);

                        try (ResultSet rs = ps.executeQuery()) {

                                if (rs.next()) {
                                        actual = rs.getString(1);
                                }
                        }
                }

                if (actual == null) {
                        actual = "";
                }

                String[] lineas = actual.split("\\R");

                StringBuilder resultado = new StringBuilder();

                for (String linea : lineas) {

                        if (linea.trim().equalsIgnoreCase(
                                        "ACTA ACTUAL SIN ANIMALES")) {

                                continue;
                        }

                        if (!linea.isBlank()) {

                                if (resultado.length() > 0) {
                                        resultado.append("\n");
                                }

                                resultado.append(linea);
                        }
                }

                int disponibles = cantidadDisponibleActaConConexion(
                                cn,
                                ingreso);

                if (disponibles <= 0) {

                        if (resultado.length() > 0) {
                                resultado.append("\n");
                        }

                        resultado.append(
                                        formatearFechaMovimiento(
                                                        LocalDate.now().toString())
                                                        + " - ACTA ACTUAL SIN ANIMALES");
                }

                updateConConexion(
                                cn,
                                "UPDATE ingresos " +
                                                "SET observaciones=? " +
                                                "WHERE id_ingreso=?",
                                resultado.toString(),
                                ingreso);
        }

        /**
         * Reconstruye completamente la observación de un habitáculo
         * utilizando únicamente la ocupación actual.
         *
         * Ejemplo:
         *
         * Ocupado por el Acta ACT-025 con 3 animales.
         * Ocupado por el Acta ACT-031 con 2 animales.
         *
         * Si no queda ningún animal proveniente de un Acta:
         *
         * Disponible.
         */
        private static int cantidadPlantelConConexion(
                        Connection cn,
                        int idPlantel) throws SQLException {

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(cantidad,0) FROM plantel_permanente WHERE id_plantel=?")) {
                        ps.setInt(1, idPlantel);
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() ? Math.max(rs.getInt(1), 0) : 0;
                        }
                }
        }

        private static int ingresoDeDetalleConConexion(
                        Connection cn,
                        int idDetalle) throws SQLException {

                if (idDetalle <= 0) {
                        return 0;
                }

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(id_ingreso,0) FROM detalle_ingreso WHERE id_detalle=?")) {
                        ps.setInt(1, idDetalle);
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() ? rs.getInt(1) : 0;
                        }
                }
        }

        private static String nombreAnimalConConexion(
                        Connection cn,
                        int idAnimal) throws SQLException {

                if (idAnimal <= 0) {
                        return "";
                }

                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(nombre_vulgar,'') FROM animales WHERE id_animal=?")) {
                        ps.setInt(1, idAnimal);
                        try (ResultSet rs = ps.executeQuery()) {
                                return rs.next() ? rs.getString(1) : "";
                        }
                }
        }

        private static void agregarObservacionHabitaculo(int idHabitaculo, String linea) {
                if (idHabitaculo <= 0 || linea == null || linea.isBlank())
                        return;
                String actual = scalar("SELECT COALESCE(observaciones,'') FROM habitaculos WHERE id_habitaculo="
                                + idHabitaculo);
                if (actual == null || actual.isBlank() || "0".equals(actual)
                                || "Disponible.".equalsIgnoreCase(actual.trim())) {
                        actual = linea.trim();
                } else {
                        actual = actual.trim() + "\n" + linea.trim();
                }
                update("UPDATE habitaculos SET observaciones=? WHERE id_habitaculo=?", actual, idHabitaculo);
        }

        private static void agregarObservacionNacimientoHabitaculo(int idHabitaculo, String linea) {
                if (idHabitaculo <= 0 || linea == null || linea.isBlank())
                        return;
                String actual = scalar("SELECT COALESCE(observaciones,'') FROM habitaculos WHERE id_habitaculo="
                                + idHabitaculo);
                if (actual == null || actual.isBlank() || "Disponible.".equalsIgnoreCase(actual.trim())) {
                        actual = linea.trim();
                } else {
                        actual = actual.trim() + "\n" + linea.trim();
                }
                update("UPDATE habitaculos SET observaciones=? WHERE id_habitaculo=?", actual, idHabitaculo);
        }

        private static void actualizarObservacionHabitaculoConConexion(Connection cn, int idHabitaculo)
                        throws SQLException {
                if (idHabitaculo <= 0)
                        return;
                String nombre = "";
                String anteriores = "";
                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT COALESCE(nombre,''),COALESCE(observaciones,'') FROM habitaculos WHERE id_habitaculo=?")) {
                        ps.setInt(1, idHabitaculo);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (!rs.next())
                                        return;
                                nombre = rs.getString(1);
                                anteriores = rs.getString(2);
                        }
                }

                List<String> notas = new ArrayList<>();
                String destinoLike = "%" + nombre.toLowerCase(java.util.Locale.ROOT) + "%";
                try (PreparedStatement ps = cn.prepareStatement(
                                "SELECT p.id_plantel,p.id_animal,p.cantidad,COALESCE(a.nombre_vulgar,'') FROM plantel_permanente p JOIN animales a ON a.id_animal=p.id_animal WHERE p.id_habitaculo=? AND p.estado='Activo' AND p.cantidad>0 ORDER BY p.id_plantel")) {
                        ps.setInt(1, idHabitaculo);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        int plantel = rs.getInt(1), animal = rs.getInt(2), ocupacion = rs.getInt(3);
                                        String nombreAnimal = rs.getString(4);
                                        List<String> eventos = new ArrayList<>();
                                        List<Integer> ids = new ArrayList<>(), cantidades = new ArrayList<>();
                                        List<String> fechas = new ArrayList<>(), tipos = new ArrayList<>();
                                        String sqlEntradas = "SELECT m.id_movimiento,m.fecha_movimiento,m.tipo_movimiento,m.cantidad "
                                                        +
                                                        "FROM movimientos m LEFT JOIN plantel_permanente origen ON origen.id_plantel=m.id_plantel "
                                                        +
                                                        "LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle " +
                                                        "WHERE m.estado='Activo' AND COALESCE(origen.id_animal,d.id_animal)=? AND ("
                                                        +
                                                        "((LOWER(m.tipo_movimiento) LIKE '%ingreso%' OR LOWER(m.tipo_movimiento) LIKE '%nacimiento%') AND m.id_plantel=?) OR "
                                                        +
                                                        "(LOWER(m.tipo_movimiento)='traslado' AND LOWER(COALESCE(m.destino,'')) LIKE ?)) "
                                                        +
                                                        "ORDER BY m.id_movimiento";
                                        try (PreparedStatement pe = cn.prepareStatement(sqlEntradas)) {
                                                pe.setInt(1, animal);
                                                pe.setInt(2, plantel);
                                                pe.setString(3, destinoLike);
                                                try (ResultSet re = pe.executeQuery()) {
                                                        while (re.next()) {
                                                                ids.add(re.getInt(1));
                                                                fechas.add(re.getString(2));
                                                                tipos.add(re.getString(3));
                                                                cantidades.add(re.getInt(4));
                                                        }
                                                }
                                        }
                                        int salidas = 0;
                                        String sqlSalidas = "SELECT COALESCE(SUM(m.cantidad),0) FROM movimientos m WHERE m.id_plantel=? AND m.estado='Activo' AND (LOWER(m.tipo_movimiento) LIKE '%fallecimiento%' OR LOWER(m.tipo_movimiento) LIKE '%liberación%' OR (LOWER(m.tipo_movimiento)='traslado' AND LOWER(COALESCE(m.destino,'')) NOT LIKE ?))";
                                        try (PreparedStatement psOut = cn.prepareStatement(sqlSalidas)) {
                                                psOut.setInt(1, plantel);
                                                psOut.setString(2, destinoLike);
                                                try (ResultSet ro = psOut.executeQuery()) {
                                                        if (ro.next())
                                                                salidas = ro.getInt(1);
                                                }
                                        }
                                        for (int i = cantidades.size() - 1; i >= 0 && salidas > 0; i--) {
                                                int descuento = Math.min(salidas, cantidades.get(i));
                                                cantidades.set(i, cantidades.get(i) - descuento);
                                                salidas -= descuento;
                                        }
                                        int suma = cantidades.stream().mapToInt(Integer::intValue).sum();
                                        int diferencia = ocupacion - suma;
                                        if (diferencia > 0 && !cantidades.isEmpty())
                                                cantidades.set(cantidades.size() - 1,
                                                                cantidades.get(cantidades.size() - 1) + diferencia);
                                        if (diferencia < 0) {
                                                int quitar = -diferencia;
                                                for (int i = cantidades.size() - 1; i >= 0 && quitar > 0; i--) {
                                                        int d = Math.min(quitar, cantidades.get(i));
                                                        cantidades.set(i, cantidades.get(i) - d);
                                                        quitar -= d;
                                                }
                                        }
                                        for (int i = 0; i < ids.size(); i++) {
                                                if (cantidades.get(i) > 0)
                                                        notas.add(formatearFechaMovimiento(fechas.get(i)) + " - "
                                                                        + tipos.get(i) + " de " + cantidades.get(i)
                                                                        + " ejemplar(es) de " + nombreAnimal
                                                                        + " - Movimiento #" + ids.get(i));
                                        }
                                }
                        }
                }
                // Conservar texto manual y descartar líneas automáticas viejas.
                if (anteriores != null)
                        for (String linea : anteriores.split("\\R")) {
                                String t = linea.trim();
                                if (!t.isBlank() && !t.equalsIgnoreCase("Disponible.")
                                                && !t.matches(".*Movimiento #[0-9]+.*"))
                                        notas.add(t);
                        }
                updateConConexion(cn, "UPDATE habitaculos SET observaciones=? WHERE id_habitaculo=?",
                                String.join("\n", notas), idHabitaculo);
        }

        // USUARIOS
        public static List<Fila> usuarios(String q) {
                return filas(
                                "SELECT id_usuario,nombre_usuario,usuario,rol,estado FROM usuarios WHERE estado='Activo' AND (nombre_usuario LIKE ? OR usuario LIKE ? OR rol LIKE ?) ORDER BY nombre_usuario",
                                "%" + q + "%", "%" + q + "%", "%" + q + "%");
        }

        public static List<Fila> usuariosInactivos(String q) {
                return filas(
                                "SELECT id_usuario,nombre_usuario,usuario,rol,estado FROM usuarios WHERE estado='Inactivo' AND (nombre_usuario LIKE ? OR usuario LIKE ? OR rol LIKE ?) ORDER BY nombre_usuario",
                                "%" + q + "%", "%" + q + "%", "%" + q + "%");
        }

        public static int guardarUsuario(int id, String n, String u, String p, String rol) {
                if (id == 0)
                        return update("INSERT INTO usuarios(nombre_usuario,usuario,password,rol,estado) VALUES(?,?,?,?, 'Activo')",
                                        n, u, p, rol);
                if (p == null || p.isBlank())
                        update("UPDATE usuarios SET nombre_usuario=?,usuario=?,rol=? WHERE id_usuario=?", n, u, rol,
                                        id);
                else
                        update("UPDATE usuarios SET nombre_usuario=?,usuario=?,password=?,rol=? WHERE id_usuario=?", n,
                                        u, p, rol,
                                        id);
                return id;
        }

        public static boolean puedeDesactivarUsuario(int id) {
                String u = scalar("SELECT usuario FROM usuarios WHERE id_usuario=" + id);
                if ("admin".equalsIgnoreCase(u))
                        return false;
                return true;
        }

        // IDENTIFICACIONES

        public static List<Fila> identificacionesTodas() {
                /*
                 * IDENTIFICACIONES DEL PLANTEL PERMANENTE
                 *
                 * Solo se muestran identificaciones activas cuyo animal
                 * permanece actualmente en el Plantel Permanente.
                 *
                 * Se usa EXISTS en lugar de JOIN para evitar duplicar una
                 * identificación cuando el mismo animal posee más de un
                 * registro activo en plantel_permanente.
                 */
                return filas(
                                "SELECT i.id_identificacion," +
                                                "i.tipo_identificacion," +
                                                "i.numero_identificacion," +
                                                "COALESCE(i.fecha_identificacion,'')," +
                                                "COALESCE(i.observaciones,'')," +
                                                "i.estado," +
                                                "COALESCE(a.nombre_vulgar,'') " +
                                                "FROM identificaciones i " +
                                                "LEFT JOIN animales a ON a.id_animal=i.id_animal " +
                                                "WHERE i.estado='Activo' " +
                                                "AND EXISTS (" +
                                                "SELECT 1 FROM plantel_permanente p " +
                                                "WHERE p.id_animal=i.id_animal " +
                                                "AND p.estado='Activo' " +
                                                "AND COALESCE(p.cantidad,0)>0" +
                                                ") " +
                                                "ORDER BY a.nombre_vulgar,i.tipo_identificacion,i.numero_identificacion");
        }

        public static int animalPorIdentificacion(int idIdentificacion) {
                return Integer.parseInt(scalar(
                                "SELECT COALESCE(id_animal,0) FROM identificaciones WHERE id_identificacion="
                                                + idIdentificacion));
        }

        public static List<Fila> identificaciones(int animal) {
                return filas(
                                "SELECT id_identificacion," +
                                                "tipo_identificacion," +
                                                "numero_identificacion," +
                                                "COALESCE(fecha_identificacion,'')," +
                                                "COALESCE(observaciones,'')," +
                                                "estado " +
                                                "FROM identificaciones " +
                                                "WHERE id_animal=? " +
                                                "AND estado='Activo' " +
                                                "ORDER BY tipo_identificacion",
                                String.valueOf(animal));
        }

        public static int guardarIdentificacion(
                        int id,
                        int animal,
                        String tipo,
                        String numero,
                        String fecha,
                        String obs) {

                // ============================================================
                // VALIDACIONES
                // ============================================================

                if (tipo == null || tipo.trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Seleccioná el tipo de identificación.");
                }

                if (numero == null || numero.trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Ingresá el número de identificación.");
                }

                // ============================================================
                // UNA IDENTIFICACIÓN NO PUEDE REPETIRSE EN NINGÚN ANIMAL
                // ============================================================

                if (existe(
                                "SELECT 1 FROM identificaciones " +
                                                "WHERE estado='Activo' " +
                                                "AND LOWER(TRIM(tipo_identificacion))=LOWER(TRIM(?)) " +
                                                "AND LOWER(TRIM(numero_identificacion))=LOWER(TRIM(?)) " +
                                                "AND id_identificacion<>?",
                                tipo.trim(),
                                numero.trim(),
                                id)) {

                        throw new IllegalArgumentException(
                                        "Esa identificación ya está registrada para otro ejemplar.");
                }

                // ============================================================
                // NUEVA IDENTIFICACIÓN
                // ============================================================

                if (id == 0) {

                        return update(
                                        "INSERT INTO identificaciones(" +
                                                        "id_animal," +
                                                        "tipo_identificacion," +
                                                        "numero_identificacion," +
                                                        "fecha_identificacion," +
                                                        "observaciones," +
                                                        "estado" +
                                                        ") VALUES(?,?,?,?,?,'Activo')",
                                        animal,
                                        tipo.trim(),
                                        numero.trim(),
                                        fecha,
                                        obs);
                }

                // ============================================================
                // MODIFICAR IDENTIFICACIÓN EXISTENTE
                // ============================================================

                update(
                                "UPDATE identificaciones SET " +
                                                "tipo_identificacion=?," +
                                                "numero_identificacion=?," +
                                                "fecha_identificacion=?," +
                                                "observaciones=? " +
                                                "WHERE id_identificacion=?",
                                tipo.trim(),
                                numero.trim(),
                                fecha,
                                obs,
                                id);

                return id;
        }

        public static void bajaIdentificacion(int id) {
                baja("identificaciones", "id_identificacion", id);
        }

        public static Integer obtenerIdIdentificacionDetalle(int idDetalle) {

                String sql = "SELECT id_identificacion " +
                                "FROM detalle_ingreso " +
                                "WHERE id_detalle=" + idDetalle;

                String valor = scalar(sql);

                if (valor == null || valor.isBlank()) {
                        return null;
                }

                try {
                        return Integer.valueOf(valor);
                } catch (NumberFormatException e) {
                        return null;
                }
        }

        public static Fila identificacionPorId(int idIdentificacion) {

                List<Fila> filas = filas(
                                "SELECT " +
                                                "id_identificacion," +
                                                "tipo_identificacion," +
                                                "numero_identificacion," +
                                                "fecha_identificacion," +
                                                "observaciones," +
                                                "estado " +
                                                "FROM identificaciones " +
                                                "WHERE id_identificacion=" + idIdentificacion);

                return filas.isEmpty()
                                ? null
                                : filas.get(0);
        }

        // NACIMIENTOS / FALLECIMIENTOS
        private static void validarMovimientoPlantelBasico(
                        int plantel,
                        int animal,
                        int cant,
                        String sexo,
                        String fecha) {

                if (plantel <= 0 || animal <= 0) {
                        throw new IllegalArgumentException(
                                        "El Plantel y el animal asociados al movimiento no son válidos.");
                }

                if (cant <= 0) {
                        throw new IllegalArgumentException(
                                        "La cantidad debe ser mayor que cero.");
                }

                if (sexo == null || sexo.isBlank()) {
                        throw new IllegalArgumentException(
                                        "El sexo es obligatorio para este movimiento.");
                }

                if (fecha == null || fecha.isBlank()) {
                        throw new IllegalArgumentException(
                                        "La fecha es obligatoria.");
                }

                try {
                        java.time.LocalDate.parse(fecha.trim());
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "La fecha del movimiento no es válida.");
                }

                int actual = cantidadPlantel(plantel);

                if (actual <= 0) {
                        throw new IllegalArgumentException(
                                        "El registro seleccionado no tiene existencia activa en el Plantel.");
                }
        }

        public static void registrarNacimiento(int plantel, int animal, int cant, String sexo, String fecha,
                        String obs) {
                validarMovimientoPlantelBasico(plantel, animal, cant, sexo, fecha);
                int hab = 0;
                try {
                        hab = Integer.parseInt(
                                        scalar("SELECT COALESCE(id_habitaculo,0) FROM plantel_permanente WHERE id_plantel="
                                                        + plantel));
                } catch (Exception ignored) {
                }

                update("INSERT INTO nacimientos(id_animal,cantidad,sexo,fecha_nacimiento,id_habitaculo,observaciones,estado) VALUES(?,?,?,?,?,?, 'Activo')",
                                animal, cant, sexo, fecha, hab == 0 ? null : hab, obs);
                update("UPDATE plantel_permanente SET cantidad=cantidad+? WHERE id_plantel=? AND estado='Activo'", cant,
                                plantel);
                int movimientoId = update(
                                "INSERT INTO movimientos(id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) VALUES(?,?,?,?,?,?,?, 'Activo')",
                                plantel, fecha, "Nacimiento", cant, sexo, "Plantel Permanente", obs);

                String animalNombre = scalar(
                                "SELECT COALESCE(nombre_vulgar,'') FROM animales WHERE id_animal=" + animal);

                String historial = formatearFechaMovimiento(fecha) +
                                " - Nacimiento de " +
                                cant +
                                " ejemplar(es) de " +
                                animalNombre +
                                " - Movimiento #" +
                                movimientoId;

                String obsMovimiento = obs == null ? "" : obs.trim();
                if (!obsMovimiento.isBlank()) {
                        obsMovimiento += "\n" + historial;
                } else {
                        obsMovimiento = historial;
                }

                update(
                                "UPDATE movimientos SET observaciones=? WHERE id_movimiento=?",
                                obsMovimiento,
                                movimientoId);

                agregarObservacionPlantel(plantel, historial);

                if (hab > 0) {
                        agregarObservacionNacimientoHabitaculo(
                                        hab,
                                        formatearFechaMovimiento(fecha) + " - NACIMIENTO: "
                                                        + cant + " ejemplar(es) de " + animalNombre
                                                        + (sexo == null || sexo.isBlank() ? ""
                                                                        : " (" + sexo.trim() + ")")
                                                        + (obs == null || obs.isBlank() ? "" : " - " + obs.trim()));
                        try (Connection cn = c()) {
                                actualizarObservacionHabitaculoConConexion(cn, hab);
                        } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                        }
                }

                agregarObservacionActaGeneral(
                                movimientoId,
                                fecha,
                                "Nacimiento",
                                cant,
                                animalNombre,
                                "Plantel Permanente");

                recalcularCantidadAnimal(animal);
        }

        public static void registrarFallecimiento(int plantel, int animal, int cant, String sexo, String fecha,
                        String obs) {
                validarMovimientoPlantelBasico(plantel, animal, cant, sexo, fecha);

                int habitaculoAntes = 0;
                try {
                        habitaculoAntes = Integer.parseInt(scalar(
                                        "SELECT COALESCE(id_habitaculo,0) FROM plantel_permanente WHERE id_plantel="
                                                        + plantel));
                } catch (Exception ignored) {
                }

                int actual = cantidadPlantel(plantel);
                if (cant > actual) {
                        throw new IllegalArgumentException(
                                        "La cantidad fallecida no puede superar la existencia actual ("
                                                        + actual + ").");
                }

                int movimientoId = update(
                                "INSERT INTO movimientos(id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) VALUES(?,?,?,?,?,?,?, 'Activo')",
                                plantel, fecha, "Fallecimiento", cant, sexo, "Baja por fallecimiento", obs);

                String animalNombre = scalar(
                                "SELECT COALESCE(nombre_vulgar,'') FROM animales WHERE id_animal=" + animal);

                String historial = formatearFechaMovimiento(fecha) +
                                " - Fallecimiento de " +
                                cant +
                                " ejemplar(es) de " +
                                animalNombre +
                                " - Movimiento #" +
                                movimientoId;

                String obsMovimiento = obs == null ? "" : obs.trim();
                if (!obsMovimiento.isBlank()) {
                        obsMovimiento += "\n" + historial;
                } else {
                        obsMovimiento = historial;
                }

                update(
                                "UPDATE movimientos SET observaciones=? WHERE id_movimiento=?",
                                obsMovimiento,
                                movimientoId);

                agregarObservacionPlantel(plantel, historial);

                agregarObservacionActaGeneral(
                                movimientoId,
                                fecha,
                                "Fallecimiento",
                                cant,
                                animalNombre,
                                "Baja por fallecimiento");

                update("UPDATE plantel_permanente SET cantidad=MAX(cantidad-?,0),estado=CASE WHEN cantidad-?<=0 THEN 'Inactivo' ELSE estado END WHERE id_plantel=?",
                                cant, cant, plantel);

                if (habitaculoAntes > 0) {
                        try (Connection cn = c()) {
                                actualizarObservacionHabitaculoConConexion(cn, habitaculoAntes);
                        } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                        }
                }

                recalcularCantidadAnimal(animal);
        }

        public static List<Fila> movimientosPlantelPorTipo(String tipo) {
                if (tipo == null || tipo.isBlank()) {
                        return new ArrayList<>();
                }

                return filas(
                                "SELECT m.id_movimiento," +
                                                "m.fecha_movimiento," +
                                                "COALESCE(a.nombre_vulgar,'')," +
                                                "m.cantidad," +
                                                "COALESCE(m.sexo,'')," +
                                                "COALESCE(m.destino,'')," +
                                                "COALESCE(m.observaciones,'')," +
                                                "COALESCE(p.tipo_ubicacion,'')," +
                                                "COALESCE(h.nombre,'') " +
                                                "FROM movimientos m " +
                                                "LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel " +
                                                "LEFT JOIN animales a ON a.id_animal=COALESCE(p.id_animal,(SELECT d.id_animal FROM detalle_ingreso d WHERE d.id_detalle=m.id_detalle)) "
                                                +
                                                "LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo " +
                                                "WHERE m.estado='Activo' " +
                                                "AND LOWER(TRIM(m.tipo_movimiento))=LOWER(TRIM(?)) " +
                                                "AND m.id_plantel IS NOT NULL " +
                                                "ORDER BY m.fecha_movimiento DESC,m.id_movimiento DESC",
                                tipo.trim());
        }

        public static List<Fila> movimientosPlantelPorTipo(String tipo, String desde, String hasta) {
                if (tipo == null || tipo.isBlank() || desde == null || hasta == null) {
                        return new ArrayList<>();
                }
                return filas(
                                "SELECT m.id_movimiento,m.fecha_movimiento,COALESCE(a.nombre_vulgar,''),m.cantidad,COALESCE(m.sexo,''),COALESCE(m.destino,''),COALESCE(m.observaciones,''),COALESCE(p.tipo_ubicacion,''),COALESCE(h.nombre,'') "
                                                +
                                                "FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel "
                                                +
                                                "LEFT JOIN animales a ON a.id_animal=COALESCE(p.id_animal,(SELECT d.id_animal FROM detalle_ingreso d WHERE d.id_detalle=m.id_detalle)) "
                                                +
                                                "LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo " +
                                                "WHERE m.estado='Activo' AND LOWER(TRIM(m.tipo_movimiento))=LOWER(TRIM(?)) "
                                                +
                                                "AND m.id_plantel IS NOT NULL AND date(m.fecha_movimiento)>=date(?) AND date(m.fecha_movimiento)<=date(?) "
                                                +
                                                "ORDER BY m.fecha_movimiento DESC,m.id_movimiento DESC",
                                tipo.trim(), desde, hasta);
        }

        // DASHBOARD
        public static int ejemplaresPlantel() {
                return Integer.parseInt(scalar(
                                "SELECT COALESCE(SUM(p.cantidad),0) FROM plantel_permanente p WHERE p.estado='Activo' AND p.cantidad>0"));
        }

        public static int animalesCatalogados() {
                return Integer.parseInt(scalar("SELECT COUNT(*) FROM animales WHERE estado='Activo'"));
        }

        public static int especiesActivas() {
                return Integer.parseInt(scalar("SELECT COUNT(*) FROM especies WHERE estado='Activo'"));
        }

        public static int actasActivas() {
                return Integer.parseInt(scalar(
                                "SELECT COUNT(*) FROM ingresos WHERE estado='Activo' AND COALESCE(tipo_ingreso,'INDIVIDUAL')='INDIVIDUAL'"));
        }

        public static int habitaculosActivos() {
                return Integer.parseInt(scalar("SELECT COUNT(*) FROM habitaculos WHERE estado='Activo'"));
        }

        public static int capacidadTotal() {
                return Integer.parseInt(
                                scalar("SELECT COALESCE(SUM(capacidad),0) FROM habitaculos WHERE estado='Activo'"));
        }

        public static int ocupacionTotal() {
                return Integer.parseInt(scalar(
                                "SELECT COALESCE(SUM(p.cantidad),0) FROM plantel_permanente p WHERE p.estado='Activo' AND p.id_habitaculo IS NOT NULL"));
        }

        public static int movimientosPeriodo(int animal, String inicio, String fin, String tipo) {
                String t = tipo.replace("'", "''");
                String sql = "SELECT COALESCE(SUM(m.cantidad),0) FROM movimientos m LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE (d.id_animal="
                                + animal + " OR p.id_animal=" + animal + ") AND m.tipo_movimiento='" + t
                                + "' AND m.estado='Activo' AND m.fecha_movimiento>='" + inicio
                                + "' AND m.fecha_movimiento<='" + fin
                                + "'";
                return Integer.parseInt(scalar(sql));
        }

        public static void guardarSnapshot(String periodo, String fecha, List<Fila> filas) {
                int rid = update(
                                "INSERT INTO reportes_plantel(periodo,fecha_generacion) VALUES(?,?) ON CONFLICT(periodo) DO UPDATE SET fecha_generacion=excluded.fecha_generacion",
                                periodo, fecha);
                rid = Integer.parseInt(
                                scalar("SELECT id_reporte FROM reportes_plantel WHERE periodo='"
                                                + periodo.replace("'", "''") + "'"));
                update("DELETE FROM reportes_plantel_detalle WHERE id_reporte=?", rid);
                for (Fila f : filas) {
                        List<String> c = f.c();
                        update("INSERT INTO reportes_plantel_detalle(id_reporte,id_animal,nombre_vulgar,nombre_cientifico,especie,clase,clasificacion,cantidad) VALUES(?,?,?,?,?,?,?,?)",
                                        rid, f.id(), get(c, 1), get(c, 2), get(c, 3), get(c, 4), get(c, 5),
                                        toInt(get(c, 6)));
                }
        }

        public static int snapshotAnterior(int animal) {
                return Integer.parseInt(scalar(
                                "SELECT COALESCE((SELECT d.cantidad FROM reportes_plantel_detalle d JOIN reportes_plantel r ON r.id_reporte=d.id_reporte WHERE d.id_animal="
                                                + animal + " ORDER BY r.fecha_generacion DESC LIMIT 1),-1)"));
        }

        private static String get(List<String> c, int i) {
                return c.size() > i && c.get(i) != null ? c.get(i) : "";
        }

        private static int toInt(String s) {
                try {
                        return Integer.parseInt(s);
                } catch (Exception e) {
                        return 0;
                }
        }

        // REPORTE
        public static List<Fila> reportePlantelMensual() {
                return filas(
                                "SELECT p.id_plantel,a.nombre_vulgar,COALESCE(a.nombre_cientifico,''),COALESCE(e.nombre,''),'Sin clasificar','Autóctonos',p.cantidad,COALESCE(h.nombre,''),COALESCE(p.tipo_ubicacion,''),COALESCE(p.fecha_ingreso_plantel,''),COALESCE(p.observaciones,''),COALESCE(a.origen,'') FROM plantel_permanente p JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo WHERE p.estado='Activo' AND p.cantidad>0 ORDER BY e.nombre,a.nombre_vulgar");
        }

        public static List<Fila> reportePlantelMensual(String desde, String hasta) {
                return filas(
                                "SELECT p.id_plantel,a.nombre_vulgar,COALESCE(a.nombre_cientifico,''),COALESCE(e.nombre,''),'Sin clasificar','Autóctonos',p.cantidad,COALESCE(h.nombre,''),COALESCE(p.tipo_ubicacion,''),COALESCE(p.fecha_ingreso_plantel,''),COALESCE(p.observaciones,''),COALESCE(a.origen,'') "
                                                +
                                                "FROM plantel_permanente p JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo "
                                                +
                                                "WHERE p.estado='Activo' AND p.cantidad>0 AND date(p.fecha_ingreso_plantel)>=date(?) AND date(p.fecha_ingreso_plantel)<=date(?) ORDER BY e.nombre,a.nombre_vulgar",
                                desde, hasta);
        }

        // ANIMALES - VISTA MAESTRA
        /**
         * Devuelve TODOS los animales activos que tienen existencia real dentro
         * de la Reserva.
         *
         * Regla de presentación:
         * - cada identificación activa representa un ejemplar individual y se
         * muestra en una fila propia;
         * - los ejemplares sin identificación se agrupan en una única fila por
         * animal;
         * - nunca se inventa una identificación ni se modifica el animal.
         */
        public static List<Fila> animalesPorUbicacion(String q, String filtro) {
                String texto = q == null ? "" : q.trim();
                String filtroSeguro = filtro == null ? "TODOS" : filtro.trim().toUpperCase(Locale.ROOT);

                String actas = "COALESCE((SELECT SUM(CASE WHEN disponible>0 THEN disponible ELSE 0 END) " +
                                "FROM (" +
                                "SELECT d.cantidad-COALESCE((SELECT SUM(m.cantidad) FROM movimientos m " +
                                "WHERE m.id_detalle=d.id_detalle AND m.estado='Activo' " +
                                "AND m.tipo_movimiento<>'Ingreso'),0) AS disponible " +
                                "FROM detalle_ingreso d JOIN ingresos i ON i.id_ingreso=d.id_ingreso " +
                                "WHERE d.id_animal=a.id_animal AND d.estado='Activo' AND i.estado='Activo'" +
                                ")" +
                                "),0)";

                String plantel = "COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p " +
                                "WHERE p.id_animal=a.id_animal AND p.estado='Activo' AND p.cantidad>0),0)";

                String existencia = "(" + actas + "+" + plantel + ")";

                String sql = "SELECT a.id_animal,a.nombre_vulgar,COALESCE(e.nombre,'')," +
                                "COALESCE(a.nombre_cientifico,'')," + actas + "," + plantel + "," +
                                "COALESCE(a.origen,''),a.estado " +
                                "FROM animales a LEFT JOIN especies e ON e.id_especie=a.id_especie " +
                                "WHERE a.estado='Activo' AND " + existencia + ">0 " +
                                "AND (a.nombre_vulgar LIKE ? OR COALESCE(a.nombre_cientifico,'') LIKE ? " +
                                "OR COALESCE(e.nombre,'') LIKE ? OR EXISTS (" +
                                "SELECT 1 FROM identificaciones i WHERE i.id_animal=a.id_animal " +
                                "AND i.estado='Activo' AND (i.tipo_identificacion LIKE ? OR i.numero_identificacion LIKE ?))) ";

                if ("ACTAS".equals(filtroSeguro)) {
                        sql += "AND " + actas + ">0 ";
                } else if ("PLANTEL".equals(filtroSeguro)) {
                        sql += "AND " + plantel + ">0 ";
                }

                sql += "ORDER BY a.nombre_vulgar";

                String like = "%" + texto + "%";
                List<Fila> animales = filas(sql, like, like, like, like, like);
                List<Fila> resultado = new ArrayList<>();

                for (Fila animal : animales) {
                        List<String> c = animal.c();

                        int cantidadActas = enteroSeguro(c, 4);
                        int cantidadPlantel = enteroSeguro(c, 5);
                        int cantidadTotal = cantidadActas + cantidadPlantel;

                        if (cantidadTotal <= 0) {
                                continue;
                        }

                        String nombre = valorSeguro(c, 1);
                        String especie = valorSeguro(c, 2);
                        String cientifico = valorSeguro(c, 3);
                        String origen = valorSeguro(c, 6);
                        String estado = valorSeguro(c, 7);

                        List<Fila> ids = identificaciones(animal.id());
                        int individuales = Math.min(ids.size(), cantidadTotal);

                        // Cada identificación activa representa un ejemplar
                        // individual. Nunca se agrupan dos identificaciones.
                        for (int i = 0; i < individuales; i++) {
                                Fila ident = ids.get(i);
                                String tipo = valorSeguro(ident.c(), 1);
                                String numero = valorSeguro(ident.c(), 2);

                                resultado.add(new Fila(
                                                animal.id(),
                                                List.of(
                                                                String.valueOf(animal.id()),
                                                                nombre,
                                                                tipo + ": " + numero,
                                                                especie,
                                                                cientifico,
                                                                "1",
                                                                origen,
                                                                estado)));
                        }

                        // Lo que queda sin identificación se muestra agrupado
                        // para ese animal.
                        int sinIdentificacion = cantidadTotal - individuales;

                        if (sinIdentificacion > 0) {
                                resultado.add(new Fila(
                                                animal.id(),
                                                List.of(
                                                                String.valueOf(animal.id()),
                                                                nombre,
                                                                "Sin identificación",
                                                                especie,
                                                                cientifico,
                                                                String.valueOf(sinIdentificacion),
                                                                origen,
                                                                estado)));
                        }
                }

                return resultado;
        }

        public static Fila animalInformacion(int idAnimal) {
                String actas = "COALESCE((SELECT SUM(CASE WHEN disponible>0 THEN disponible ELSE 0 END) " +
                                "FROM (" +
                                "SELECT d.cantidad-COALESCE((SELECT SUM(m.cantidad) FROM movimientos m " +
                                "WHERE m.id_detalle=d.id_detalle AND m.estado='Activo' " +
                                "AND m.tipo_movimiento<>'Ingreso'),0) AS disponible " +
                                "FROM detalle_ingreso d JOIN ingresos i ON i.id_ingreso=d.id_ingreso " +
                                "WHERE d.id_animal=a.id_animal AND d.estado='Activo' AND i.estado='Activo'" +
                                ")" +
                                "),0)";

                String plantel = "COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p " +
                                "WHERE p.id_animal=a.id_animal AND p.estado='Activo' AND p.cantidad>0),0)";

                return filas(
                                "SELECT a.id_animal,a.nombre_vulgar,COALESCE(e.nombre,'')," +
                                                "COALESCE(a.nombre_cientifico,''),COALESCE(a.origen,'')," +
                                                plantel + "," + actas + "," +
                                                "COALESCE((SELECT group_concat(x.identificacion,' · ') FROM (" +
                                                "SELECT DISTINCT i.tipo_identificacion || ': ' || i.numero_identificacion AS identificacion "
                                                +
                                                "FROM identificaciones i WHERE i.id_animal=a.id_animal AND i.estado='Activo' "
                                                +
                                                "ORDER BY i.tipo_identificacion,i.numero_identificacion) x),'Sin identificación'),a.estado "
                                                +
                                                "FROM animales a LEFT JOIN especies e ON e.id_especie=a.id_especie " +
                                                "WHERE a.id_animal=?",
                                String.valueOf(idAnimal))
                                .stream()
                                .findFirst()
                                .orElse(null);
        }

        private static int enteroSeguro(List<String> valores, int indice) {
                try {
                        return valores.size() > indice ? Integer.parseInt(valores.get(indice)) : 0;
                } catch (Exception e) {
                        return 0;
                }
        }

        private static String valorSeguro(List<String> valores, int indice) {
                return valores.size() > indice && valores.get(indice) != null
                                ? valores.get(indice)
                                : "";
        }

}