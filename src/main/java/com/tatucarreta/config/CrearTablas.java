package com.tatucarreta.config;

import java.sql.*;

/**
 * Inicialización y migración segura de la base SQLite. Nunca elimina
 * información del usuario.
 */
public final class CrearTablas {
    private CrearTablas() {
    }

    public static void crearTablas() {
        try (Connection c = ConexionSQLite.conectar(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS usuarios (id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,nombre_usuario TEXT NOT NULL,usuario TEXT NOT NULL UNIQUE,password TEXT NOT NULL,rol TEXT NOT NULL DEFAULT 'Usuario',estado TEXT NOT NULL DEFAULT 'Activo')");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS especies (id_especie INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT NOT NULL UNIQUE,clasificacion TEXT NOT NULL DEFAULT 'Autóctonos',clase TEXT NOT NULL DEFAULT 'Sin clasificar',estado_conservacion TEXT NOT NULL DEFAULT 'Sin evaluar',estado TEXT NOT NULL DEFAULT 'Activo')");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS animales (id_animal INTEGER PRIMARY KEY AUTOINCREMENT,id_especie INTEGER NOT NULL,nombre_vulgar TEXT NOT NULL,nombre_cientifico TEXT,cantidad_actual INTEGER NOT NULL DEFAULT 0,origen TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_especie) REFERENCES especies(id_especie))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS habitaculos (id_habitaculo INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT NOT NULL,sector TEXT NOT NULL,capacidad INTEGER NOT NULL,estado TEXT NOT NULL DEFAULT 'Activo',observaciones TEXT)");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS ingresos (id_ingreso INTEGER PRIMARY KEY AUTOINCREMENT,numero_acta TEXT NOT NULL,fecha_ingreso TEXT NOT NULL,organismo_procedencia TEXT, responsable_entrega TEXT,recibe_reserva TEXT,procedencia TEXT,motivo_ingreso TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo')");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS detalle_ingreso (" +
                            "id_detalle INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "id_ingreso INTEGER NOT NULL," +
                            "id_animal INTEGER NOT NULL," +
                            "id_identificacion INTEGER," +
                            "cantidad INTEGER NOT NULL," +
                            "sexo TEXT," +
                            "edad TEXT," +
                            "peso REAL," +
                            "estado_ingreso TEXT," +
                            "observaciones TEXT," +
                            "estado TEXT NOT NULL DEFAULT 'Activo'," +
                            "FOREIGN KEY(id_ingreso) REFERENCES ingresos(id_ingreso)," +
                            "FOREIGN KEY(id_animal) REFERENCES animales(id_animal)" +
                            ")");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS plantel_permanente (id_plantel INTEGER PRIMARY KEY AUTOINCREMENT,id_animal INTEGER NOT NULL,cantidad INTEGER NOT NULL DEFAULT 1,tipo_ubicacion TEXT,id_habitaculo INTEGER,fecha_ingreso_plantel TEXT,estado TEXT NOT NULL DEFAULT 'Activo',observaciones TEXT,FOREIGN KEY(id_animal) REFERENCES animales(id_animal),FOREIGN KEY(id_habitaculo) REFERENCES habitaculos(id_habitaculo))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS identificaciones (id_identificacion INTEGER PRIMARY KEY AUTOINCREMENT,id_animal INTEGER NOT NULL,tipo_identificacion TEXT NOT NULL,numero_identificacion TEXT NOT NULL,fecha_identificacion TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_animal) REFERENCES animales(id_animal))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS movimientos (id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,id_detalle INTEGER,id_plantel INTEGER,fecha_movimiento TEXT NOT NULL,tipo_movimiento TEXT NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,destino TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_detalle) REFERENCES detalle_ingreso(id_detalle),FOREIGN KEY(id_plantel) REFERENCES plantel_permanente(id_plantel))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS nacimientos (id_nacimiento INTEGER PRIMARY KEY AUTOINCREMENT,id_animal INTEGER NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,fecha_nacimiento TEXT NOT NULL,id_habitaculo INTEGER,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_animal) REFERENCES animales(id_animal),FOREIGN KEY(id_habitaculo) REFERENCES habitaculos(id_habitaculo))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS papelera (id_papelera INTEGER PRIMARY KEY AUTOINCREMENT,tipo_registro TEXT NOT NULL,id_registro INTEGER NOT NULL,datos TEXT NOT NULL,fecha_eliminacion TEXT NOT NULL)");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS reportes_plantel (id_reporte INTEGER PRIMARY KEY AUTOINCREMENT,periodo TEXT NOT NULL,fecha_generacion TEXT NOT NULL,UNIQUE(periodo))");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS reportes_plantel_detalle (id_detalle_reporte INTEGER PRIMARY KEY AUTOINCREMENT,id_reporte INTEGER NOT NULL,id_animal INTEGER,nombre_vulgar TEXT,nombre_cientifico TEXT,especie TEXT,clase TEXT,clasificacion TEXT,cantidad INTEGER,FOREIGN KEY(id_reporte) REFERENCES reportes_plantel(id_reporte))");

            ensureColumn(c, "usuarios", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "especies", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "especies", "clasificacion", "TEXT NOT NULL DEFAULT 'Autóctonos'");
            ensureColumn(c, "especies", "clase", "TEXT NOT NULL DEFAULT 'Sin clasificar'");
            ensureColumn(c, "especies", "estado_conservacion", "TEXT NOT NULL DEFAULT 'Sin evaluar'");
            ensureColumn(c, "animales", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "habitaculos", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "ingresos", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "ingresos", "recibe_reserva", "TEXT");
            ensureColumn(c, "ingresos", "tipo_ingreso", "TEXT NOT NULL DEFAULT 'INDIVIDUAL'");

            // Acta lógica permanente que representa la existencia viva actual
            // del Plantel Permanente. No tiene detalles propios: su contenido
            // se obtiene directamente del Plantel activo.
            s.execute(
                    "UPDATE ingresos SET tipo_ingreso='INDIVIDUAL' " +
                    "WHERE tipo_ingreso IS NULL OR TRIM(tipo_ingreso)=''");
            s.execute(
                    "INSERT INTO ingresos(" +
                    "numero_acta,fecha_ingreso,organismo_procedencia,responsable_entrega," +
                    "recibe_reserva,procedencia,motivo_ingreso,observaciones,estado,tipo_ingreso" +
                    ") " +
                    "SELECT 'ACTA GENERAL PLANTEL',date('now'),'Reserva Natural','Sistema'," +
                    "'Reserva Natural','Plantel Permanente','Ingreso General del Plantel'," +
                    "'Acta general del Plantel Permanente. Representa únicamente los ejemplares vivos que actualmente permanecen en la Reserva.'," +
                    "'Activo','PLANTEL_GENERAL' " +
                    "WHERE NOT EXISTS (" +
                    "SELECT 1 FROM ingresos WHERE tipo_ingreso='PLANTEL_GENERAL' AND estado='Activo'" +
                    ")");
            ensureColumn(c, "detalle_ingreso", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "plantel_permanente", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "identificaciones", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "identificaciones", "fecha_identificacion", "TEXT");
            if (tieneColumna(c, "identificaciones", "fecha_asignacion"))
                s.execute(
                        "UPDATE identificaciones SET fecha_identificacion=COALESCE(fecha_identificacion,fecha_asignacion)");
            ensureColumn(c, "movimientos", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "movimientos", "sexo", "TEXT");
            s.execute("UPDATE movimientos SET tipo_movimiento='Traslado' WHERE LOWER(TRIM(tipo_movimiento))='egreso'");
            ensureColumn(c, "nacimientos", "estado", "TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c, "nacimientos", "sexo", "TEXT");

            if (columnaEsNotNull(c, "movimientos", "id_detalle"))
                migrarMovimientos(c);

            s.execute("CREATE INDEX IF NOT EXISTS idx_animales_estado ON animales(estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_ingresos_estado ON ingresos(estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_ingresos_tipo ON ingresos(tipo_ingreso)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_movimientos_estado ON movimientos(estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_plantel_estado ON plantel_permanente(estado)");
            normalizarAdministrador(c);
            migrarTablasDuplicadas(c);
            normalizarReferenciasPlantel(c);
            migrarMovimientosActaAPlantel(c);
            deduplicarRegistrosActivos(c);
            normalizarReferenciasMovimientosPlantel(c);
            crearRestriccionesUnicasActivas(c);
            normalizarCantidades(c);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo inicializar la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Unifica tablas heredadas que quedaron de versiones anteriores. No se
     * mantienen copias operativas.
     */
    private static void migrarTablasDuplicadas(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            // identificaciones_plantel es una tabla antigua. Sus datos pasan a la tabla
            // canónica.
            if (tablaExiste(c, "identificaciones_plantel")) {
                try (ResultSet r = s.executeQuery(
                        "SELECT id_animal,tipo_identificacion,numero_identificacion,fecha_identificacion,observaciones FROM identificaciones_plantel")) {
                    while (r.next()) {
                        int animal = r.getInt(1);
                        String tipo = r.getString(2);
                        String numero = r.getString(3);
                        if (!existe(c,
                                "SELECT 1 FROM identificaciones WHERE id_animal=? AND LOWER(TRIM(tipo_identificacion))=LOWER(TRIM(?)) AND LOWER(TRIM(numero_identificacion))=LOWER(TRIM(?))",
                                animal, tipo, numero)) {
                            try (PreparedStatement p = c.prepareStatement(
                                    "INSERT INTO identificaciones(id_animal,tipo_identificacion,numero_identificacion,fecha_identificacion,observaciones,estado) VALUES(?,?,?,?,?,'Activo')")) {
                                p.setInt(1, animal);
                                p.setString(2, tipo);
                                p.setString(3, numero);
                                p.setString(4, r.getString(4));
                                p.setString(5, r.getString(5));
                                p.executeUpdate();
                            }
                        }
                    }
                }
                s.execute("DROP TABLE identificaciones_plantel");
            }
            if (tablaExiste(c, "identificaciones_nueva"))
                s.execute("DROP TABLE identificaciones_nueva");
            if (tablaExiste(c, "plantel_permanente_nueva"))
                s.execute("DROP TABLE plantel_permanente_nueva");
        }
    }

    private static boolean tablaExiste(Connection c, String tabla) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            p.setString(1, tabla);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    private static boolean existe(Connection c, String sql, Object... ps) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < ps.length; i++)
                p.setObject(i + 1, ps[i]);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    private static boolean columnaEsNotNull(Connection c, String table, String column) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("PRAGMA table_info(" + table + ")");
                ResultSet r = p.executeQuery()) {
            while (r.next())
                if (column.equalsIgnoreCase(r.getString("name")))
                    return r.getInt("notnull") == 1;
        }
        return false;
    }

    private static void migrarMovimientos(Connection c) throws SQLException {
        boolean estado = tieneColumna(c, "movimientos", "estado"), sexo = tieneColumna(c, "movimientos", "sexo");
        String es = estado ? "estado" : "'Activo'", sx = sexo ? "sexo" : "NULL";
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=OFF");
            s.execute(
                    "CREATE TABLE movimientos_nuevo (id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,id_detalle INTEGER,id_plantel INTEGER,fecha_movimiento TEXT NOT NULL,tipo_movimiento TEXT NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,destino TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_detalle) REFERENCES detalle_ingreso(id_detalle),FOREIGN KEY(id_plantel) REFERENCES plantel_permanente(id_plantel))");
            s.execute(
                    "INSERT INTO movimientos_nuevo(id_movimiento,id_detalle,id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) SELECT id_movimiento,id_detalle,id_plantel,fecha_movimiento,tipo_movimiento,cantidad,"
                            + sx + ",destino,observaciones," + es + " FROM movimientos");
            s.execute("DROP TABLE movimientos");
            s.execute("ALTER TABLE movimientos_nuevo RENAME TO movimientos");
            s.execute("CREATE INDEX IF NOT EXISTS idx_movimientos_estado ON movimientos(estado)");
        } finally {
            try (Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys=ON");
            }
        }
    }

    private static boolean tieneColumna(Connection c, String table, String column) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("PRAGMA table_info(" + table + ")");
                ResultSet r = p.executeQuery()) {
            while (r.next())
                if (column.equalsIgnoreCase(r.getString("name")))
                    return true;
        }
        return false;
    }

    private static void ensureColumn(Connection c, String table, String column, String definition) throws SQLException {
        if (tieneColumna(c, table, column))
            return;
        try (Statement s = c.createStatement()) {
            s.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    /**
     * Conserva como inactivos los registros históricos de Plantel que ya no tienen
     * animal maestro asociado.
     */
    private static void normalizarReferenciasPlantel(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(
                    "UPDATE plantel_permanente SET estado='Inactivo',observaciones=CASE WHEN trim(COALESCE(observaciones,''))='' THEN 'Registro histórico sin animal asociado; conservado como inactivo.' ELSE observaciones END WHERE estado='Activo' AND id_animal NOT IN (SELECT id_animal FROM animales)");
        }
    }

    /**
     * Evita que una misma carga operativa quede registrada dos veces.
     * No se borran filas: los duplicados activos pasan a Inactivo y se conserva su
     * historial.
     */
    private static void deduplicarRegistrosActivos(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            // Un acta es única por número. Se conserva la primera registrada.
            s.executeUpdate(
                    "UPDATE ingresos SET estado='Inactivo' WHERE estado='Activo' AND id_ingreso NOT IN (SELECT MIN(id_ingreso) FROM ingresos WHERE estado='Activo' GROUP BY TRIM(numero_acta))");

            // Un animal de catálogo se identifica por especie + nombre vulgar + nombre
            // científico.
            s.executeUpdate(
                    "UPDATE animales SET estado='Inactivo' WHERE estado='Activo' AND id_animal NOT IN (SELECT MIN(id_animal) FROM animales WHERE estado='Activo' GROUP BY id_especie,LOWER(TRIM(nombre_vulgar)),LOWER(TRIM(COALESCE(nombre_cientifico,''))))");

            // Habitáculos: mismo nombre dentro del mismo sector no se duplica.
            s.executeUpdate(
                    "UPDATE habitaculos SET estado='Inactivo' WHERE estado='Activo' AND id_habitaculo NOT IN (SELECT MIN(id_habitaculo) FROM habitaculos WHERE estado='Activo' GROUP BY LOWER(TRIM(nombre)),LOWER(TRIM(sector)))");

            // Identificaciones: mismo animal + tipo + número no se registra dos veces.
            s.executeUpdate(
                    "UPDATE identificaciones SET estado='Inactivo' WHERE estado='Activo' AND id_identificacion NOT IN (SELECT MIN(id_identificacion) FROM identificaciones WHERE estado='Activo' GROUP BY id_animal,LOWER(TRIM(tipo_identificacion)),LOWER(TRIM(numero_identificacion)))");

            // Detalle repetido dentro de una misma acta: se consolidan las cantidades.
            try (ResultSet r = s.executeQuery(
                    "SELECT id_ingreso,id_animal,MIN(id_detalle) AS conservar,SUM(cantidad) AS total,COUNT(*) AS n FROM detalle_ingreso WHERE estado='Activo' GROUP BY id_ingreso,id_animal HAVING COUNT(*)>1")) {
                while (r.next()) {
                    int ingreso = r.getInt(1), animal = r.getInt(2), conservar = r.getInt(3), total = r.getInt(4);
                    try (PreparedStatement u = c
                            .prepareStatement("UPDATE detalle_ingreso SET cantidad=? WHERE id_detalle=?")) {
                        u.setInt(1, total);
                        u.setInt(2, conservar);
                        u.executeUpdate();
                    }
                    try (PreparedStatement b = c.prepareStatement(
                            "UPDATE detalle_ingreso SET estado='Inactivo' WHERE id_ingreso=? AND id_animal=? AND estado='Activo' AND id_detalle<>?")) {
                        b.setInt(1, ingreso);
                        b.setInt(2, animal);
                        b.setInt(3, conservar);
                        b.executeUpdate();
                    }
                }
            }

            // Plantel repetido para el mismo animal y ubicación: se consolidan cantidades.
            try (ResultSet r = s.executeQuery(
                    "SELECT id_animal,COALESCE(id_habitaculo,0),LOWER(TRIM(COALESCE(tipo_ubicacion,''))),MIN(id_plantel) AS conservar,SUM(cantidad) AS total,COUNT(*) AS n FROM plantel_permanente WHERE estado='Activo' GROUP BY id_animal,COALESCE(id_habitaculo,0),LOWER(TRIM(COALESCE(tipo_ubicacion,''))) HAVING COUNT(*)>1")) {
                while (r.next()) {
                    int animal = r.getInt(1), hab = r.getInt(2), conservar = r.getInt(4), total = r.getInt(5);
                    try (PreparedStatement u = c
                            .prepareStatement("UPDATE plantel_permanente SET cantidad=? WHERE id_plantel=?")) {
                        u.setInt(1, total);
                        u.setInt(2, conservar);
                        u.executeUpdate();
                    }
                    try (PreparedStatement b = c.prepareStatement(
                            "UPDATE plantel_permanente SET estado='Inactivo' WHERE id_animal=? AND COALESCE(id_habitaculo,0)=? AND LOWER(TRIM(COALESCE(tipo_ubicacion,'')))=(SELECT LOWER(TRIM(COALESCE(tipo_ubicacion,''))) FROM plantel_permanente WHERE id_plantel=?) AND estado='Activo' AND id_plantel<>?")) {
                        b.setInt(1, animal);
                        b.setInt(2, hab);
                        b.setInt(3, conservar);
                        b.setInt(4, conservar);
                        b.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Cuando la normalización consolida dos registros de Plantel, los movimientos
     * históricos deben apuntar al registro activo que quedó como canónico.
     */
    private static void normalizarReferenciasMovimientosPlantel(Connection c) throws SQLException {
        String sql = "UPDATE movimientos SET id_plantel=("
                + "SELECT MIN(p2.id_plantel) "
                + "FROM plantel_permanente p1 "
                + "JOIN plantel_permanente p2 "
                + "ON p2.id_animal=p1.id_animal "
                + "AND LOWER(TRIM(COALESCE(p2.tipo_ubicacion,'')))="
                + "LOWER(TRIM(COALESCE(p1.tipo_ubicacion,''))) "
                + "AND ((p2.id_habitaculo IS NULL AND p1.id_habitaculo IS NULL) "
                + "OR p2.id_habitaculo=p1.id_habitaculo) "
                + "AND p2.estado='Activo' "
                + "WHERE p1.id_plantel=movimientos.id_plantel"
                + ") "
                + "WHERE id_plantel IS NOT NULL "
                + "AND EXISTS (SELECT 1 FROM plantel_permanente p "
                + "WHERE p.id_plantel=movimientos.id_plantel AND p.estado='Inactivo')";

        try (Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    private static void crearRestriccionesUnicasActivas(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS ux_ingreso_acta_activo ON ingresos(TRIM(numero_acta)) WHERE estado='Activo'");
            s.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS ux_animal_catalogo_activo ON animales(id_especie,LOWER(TRIM(nombre_vulgar)),LOWER(TRIM(COALESCE(nombre_cientifico,'')))) WHERE estado='Activo'");
            s.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS ux_habitaculo_activo ON habitaculos(LOWER(TRIM(nombre)),LOWER(TRIM(sector))) WHERE estado='Activo'");
            s.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS ux_identificacion_activa ON identificaciones(id_animal,LOWER(TRIM(tipo_identificacion)),LOWER(TRIM(numero_identificacion))) WHERE estado='Activo'");
            s.execute(
                    "CREATE INDEX IF NOT EXISTS idx_detalle_ingreso_animal ON detalle_ingreso(id_ingreso,id_animal,estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_plantel_animal ON plantel_permanente(id_animal,estado)");
        }
    }

    /**
     * Corrige movimientos históricos que antes mostraban "Campo Abierto" o
     * "Plantel Permanente" pero no generaban un registro en Plantel Permanente.
     *
     * No elimina movimientos ni altera su cantidad consumida del acta: solamente
     * reconstruye la ubicación actual que faltaba y vincula el movimiento con ella.
     */
    private static void migrarMovimientosActaAPlantel(Connection c) throws SQLException {
        String sql = "SELECT m.id_movimiento,m.id_detalle,m.fecha_movimiento,m.tipo_movimiento,"
                + "m.cantidad,m.destino,m.observaciones,d.id_animal "
                + "FROM movimientos m "
                + "JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle "
                + "WHERE m.estado='Activo' "
                + "AND m.id_detalle IS NOT NULL "
                + "AND m.id_plantel IS NULL "
                + "AND LOWER(TRIM(m.tipo_movimiento))='traslado' "
                + "AND (LOWER(TRIM(COALESCE(m.destino,''))) LIKE 'campo abierto%' "
                + "OR LOWER(TRIM(COALESCE(m.destino,''))) LIKE 'plantel permanente%') "
                + "ORDER BY m.id_movimiento";

        try (PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int movimiento = rs.getInt("id_movimiento");
                int animal = rs.getInt("id_animal");
                int cantidad = rs.getInt("cantidad");
                String fecha = rs.getString("fecha_movimiento");
                String destino = rs.getString("destino");
                String observaciones = rs.getString("observaciones");

                if (animal <= 0 || cantidad <= 0) {
                    continue;
                }

                Integer habitaculo = null;
                String tipoUbicacion = "Campo abierto";

                String destinoNormalizado = destino == null
                        ? ""
                        : destino.trim().replaceAll("\\s+", " ");

                String destinoLower = destinoNormalizado.toLowerCase(java.util.Locale.ROOT);

                if (destinoLower.startsWith("plantel permanente")
                        && destinoLower.contains("habitáculo:")) {

                    String nombreHabitaculo = destinoNormalizado.substring(
                            destinoLower.indexOf("habitáculo:") + "habitáculo:".length()).trim();

                    if (!nombreHabitaculo.isBlank()) {
                        try (PreparedStatement ph = c.prepareStatement(
                                "SELECT id_habitaculo FROM habitaculos "
                                        + "WHERE estado='Activo' "
                                        + "AND LOWER(TRIM(nombre))=LOWER(TRIM(?)) "
                                        + "LIMIT 1")) {
                            ph.setString(1, nombreHabitaculo);

                            try (ResultSet rh = ph.executeQuery()) {
                                if (rh.next()) {
                                    habitaculo = rh.getInt(1);
                                    tipoUbicacion = "Habitáculo";
                                }
                            }
                        }
                    }
                }

                int plantel = 0;

                String sqlExistente = "SELECT id_plantel FROM plantel_permanente "
                        + "WHERE id_animal=? AND estado='Activo' "
                        + "AND LOWER(TRIM(COALESCE(tipo_ubicacion,'')))=LOWER(TRIM(?)) "
                        + "AND ((id_habitaculo IS NULL AND ? IS NULL) OR id_habitaculo=?) "
                        + "ORDER BY id_plantel LIMIT 1";

                try (PreparedStatement pe = c.prepareStatement(sqlExistente)) {
                    pe.setInt(1, animal);
                    pe.setString(2, tipoUbicacion);
                    if (habitaculo == null) {
                        pe.setNull(3, Types.INTEGER);
                        pe.setNull(4, Types.INTEGER);
                    } else {
                        pe.setInt(3, habitaculo);
                        pe.setInt(4, habitaculo);
                    }

                    try (ResultSet re = pe.executeQuery()) {
                        if (re.next()) {
                            plantel = re.getInt(1);
                        }
                    }
                }

                if (plantel > 0) {
                    try (PreparedStatement pu = c.prepareStatement(
                            "UPDATE plantel_permanente "
                                    + "SET cantidad=cantidad+?,fecha_ingreso_plantel=? "
                                    + "WHERE id_plantel=?")) {
                        pu.setInt(1, cantidad);
                        pu.setString(2, fecha);
                        pu.setInt(3, plantel);
                        pu.executeUpdate();
                    }
                } else {
                    try (PreparedStatement pi = c.prepareStatement(
                            "INSERT INTO plantel_permanente("
                                    + "id_animal,cantidad,tipo_ubicacion,id_habitaculo,"
                                    + "fecha_ingreso_plantel,estado,observaciones) "
                                    + "VALUES(?,?,?,?,?,'Activo',?)",
                            Statement.RETURN_GENERATED_KEYS)) {

                        pi.setInt(1, animal);
                        pi.setInt(2, cantidad);
                        pi.setString(3, tipoUbicacion);

                        if (habitaculo == null) {
                            pi.setNull(4, Types.INTEGER);
                        } else {
                            pi.setInt(4, habitaculo);
                        }

                        pi.setString(5, fecha);
                        pi.setString(6, observaciones == null ? "" : observaciones.trim());
                        pi.executeUpdate();

                        try (ResultSet generated = pi.getGeneratedKeys()) {
                            if (generated.next()) {
                                plantel = generated.getInt(1);
                            }
                        }
                    }
                }

                if (plantel <= 0) {
                    continue;
                }

                try (PreparedStatement pm = c.prepareStatement(
                        "UPDATE movimientos SET id_plantel=? WHERE id_movimiento=?")) {
                    pm.setInt(1, plantel);
                    pm.setInt(2, movimiento);
                    pm.executeUpdate();
                }

                String animalNombre = "";
                try (PreparedStatement pa = c.prepareStatement(
                        "SELECT COALESCE(nombre_vulgar,'') FROM animales WHERE id_animal=?")) {
                    pa.setInt(1, animal);

                    try (ResultSet ra = pa.executeQuery()) {
                        if (ra.next()) {
                            animalNombre = ra.getString(1);
                        }
                    }
                }

                String historial = formatearFecha(fecha) +
                        " : Traslado de " +
                        cantidad +
                        " ejemplar(es) de " +
                        animalNombre +
                        " → " +
                        destinoNormalizado +
                        " (Movimiento #" +
                        movimiento +
                        ")";

                String observacionMovimiento = observaciones == null
                        ? ""
                        : observaciones.trim();

                if (!observacionMovimiento.contains(historial)) {
                    observacionMovimiento = observacionMovimiento.isBlank()
                            ? historial
                            : observacionMovimiento + "\n" + historial;

                    try (PreparedStatement po = c.prepareStatement(
                            "UPDATE movimientos SET observaciones=? WHERE id_movimiento=?")) {
                        po.setString(1, observacionMovimiento);
                        po.setInt(2, movimiento);
                        po.executeUpdate();
                    }
                }

                agregarObservacionPlantel(c, plantel, historial);
            }
        }
    }

    private static String formatearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return "";
        }

        try {
            return java.time.LocalDate.parse(fecha.trim())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return fecha.trim();
        }
    }

    private static void agregarObservacionPlantel(
            Connection c,
            int idPlantel,
            String observacion) throws SQLException {

        if (idPlantel <= 0 || observacion == null || observacion.isBlank()) {
            return;
        }

        String actual = "";

        try (PreparedStatement ps = c.prepareStatement(
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

        if (actual.contains(observacion.trim())) {
            return;
        }

        String nuevo = actual.isBlank()
                ? observacion.trim()
                : actual + "\n" + observacion.trim();

        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE plantel_permanente SET observaciones=? WHERE id_plantel=?")) {
            ps.setString(1, nuevo);
            ps.setInt(2, idPlantel);
            ps.executeUpdate();
        }
    }

    private static void normalizarCantidades(Connection c) throws SQLException {
        String sql = "UPDATE animales SET cantidad_actual="
                + "COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p "
                + "WHERE p.id_animal=animales.id_animal AND p.estado='Activo' AND p.cantidad>0),0)"
                + "+"
                + "COALESCE((SELECT SUM(MAX(d.cantidad-COALESCE(("
                + "SELECT SUM(m.cantidad) FROM movimientos m "
                + "WHERE m.id_detalle=d.id_detalle AND m.estado='Activo' "
                + "AND m.tipo_movimiento<>'Ingreso'"
                + "),0),0)) "
                + "FROM detalle_ingreso d "
                + "JOIN ingresos i ON i.id_ingreso=d.id_ingreso "
                + "WHERE d.id_animal=animales.id_animal "
                + "AND d.estado='Activo' AND i.estado='Activo'),0)";

        try (Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    private static void normalizarAdministrador(Connection c) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE usuario='admin'");
                ResultSet r = p.executeQuery()) {
            if (r.next() && r.getInt(1) == 0)
                try (PreparedStatement i = c.prepareStatement(
                        "INSERT INTO usuarios(nombre_usuario,usuario,password,rol,estado) VALUES(?,?,?,?,?)")) {
                    i.setString(1, "Administrador");
                    i.setString(2, "admin");
                    i.setString(3, "admin");
                    i.setString(4, "Administrador");
                    i.setString(5, "Activo");
                    i.executeUpdate();
                }
        }
        try (PreparedStatement u = c
                .prepareStatement("UPDATE usuarios SET password='admin',estado='Activo' WHERE usuario='admin'")) {
            u.executeUpdate();
        }
    }
}
