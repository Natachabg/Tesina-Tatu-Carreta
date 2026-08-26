package com.tatucarreta;

import java.sql.Connection;
import java.sql.Statement;

public class CrearTablas {

    // =========================
    // ESPECIES
    // =========================

    public static void crearTablaEspecies() {

        String sql = """
                CREATE TABLE IF NOT EXISTS especies (
                    id_especie INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla especies creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla especies."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // ANIMALES
    // =========================

    public static void crearTablaAnimales() {

        String sql = """
                CREATE TABLE IF NOT EXISTS animales (
                    id_animal INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_especie INTEGER NOT NULL,
                    nombre_vulgar TEXT NOT NULL,
                    nombre_cientifico TEXT NOT NULL,
                    cantidad_actual INTEGER NOT NULL DEFAULT 1,
                    origen TEXT NOT NULL,
                    estado TEXT NOT NULL,

                    FOREIGN KEY (id_especie)
                    REFERENCES especies(id_especie)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla animales creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla animales."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // INGRESOS / ACTAS
    // =========================

    public static void crearTablaIngresos() {

        String sql = """
                CREATE TABLE IF NOT EXISTS ingresos (
                    id_ingreso INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero_acta TEXT NOT NULL,
                    fecha_ingreso TEXT NOT NULL,
                    organismo_procedencia TEXT NOT NULL,
                    responsable_entrega TEXT,
                    procedencia TEXT NOT NULL,
                    motivo_ingreso TEXT NOT NULL,
                    documentacion TEXT,
                    observaciones TEXT
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla ingresos creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla ingresos."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // DETALLE INGRESO
    // =========================

    public static void crearTablaDetalleIngreso() {

        String sql = """
                CREATE TABLE IF NOT EXISTS detalle_ingreso (
                    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_ingreso INTEGER NOT NULL,
                    id_animal INTEGER NOT NULL,
                    cantidad INTEGER NOT NULL,
                    sexo TEXT,
                    edad TEXT,
                    peso REAL,
                    estado_ingreso TEXT,
                    observaciones TEXT,

                    FOREIGN KEY (id_ingreso)
                    REFERENCES ingresos(id_ingreso),

                    FOREIGN KEY (id_animal)
                    REFERENCES animales(id_animal)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla detalle_ingreso creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla detalle_ingreso."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // HABITÁCULOS
    // =========================

    public static void crearTablaHabitaculos() {

        String sql = """
                CREATE TABLE IF NOT EXISTS habitaculos (
                    id_habitaculo INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    sector TEXT NOT NULL,
                    capacidad INTEGER,
                    estado TEXT NOT NULL,
                    observaciones TEXT
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla habitaculos creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla habitaculos."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // PLANTEL PERMANENTE
    // =========================

    public static void crearTablaPlantelPermanente() {

        String sql = """
                CREATE TABLE IF NOT EXISTS plantel_permanente (
                    id_plantel INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_animal INTEGER NOT NULL,
                    cantidad INTEGER NOT NULL,
                    tipo_ubicacion TEXT NOT NULL,
                    id_habitaculo INTEGER,
                    fecha_ingreso_plantel TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    observaciones TEXT,

                    FOREIGN KEY (id_animal)
                    REFERENCES animales(id_animal),

                    FOREIGN KEY (id_habitaculo)
                    REFERENCES habitaculos(id_habitaculo)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla plantel permanente creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla plantel permanente."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // MOVIMIENTOS
    // =========================

    public static void crearTablaMovimientos() {

        String sql = """
                CREATE TABLE IF NOT EXISTS movimientos (
                    id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_animal INTEGER NOT NULL,
                    id_ingreso INTEGER,
                    fecha_movimiento TEXT NOT NULL,
                    tipo_movimiento TEXT NOT NULL,
                    cantidad INTEGER NOT NULL,
                    destino TEXT,
                    observaciones TEXT,

                    FOREIGN KEY (id_animal)
                    REFERENCES animales(id_animal),

                    FOREIGN KEY (id_ingreso)
                    REFERENCES ingresos(id_ingreso)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla movimientos creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla movimientos."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // IDENTIFICACIONES
    // =========================

    public static void crearTablaIdentificaciones() {

        String sql = """
                CREATE TABLE IF NOT EXISTS identificaciones (
                    id_identificacion INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_animal INTEGER NOT NULL,
                    tipo_identificacion TEXT NOT NULL,
                    numero_identificacion TEXT NOT NULL,
                    fecha_identificacion TEXT,
                    observaciones TEXT,

                    FOREIGN KEY (id_animal)
                    REFERENCES animales(id_animal)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla identificaciones creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla identificaciones."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // USUARIOS
    // =========================

    public static void crearTablaUsuarios() {

        String sql = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_usuario TEXT NOT NULL,
                    usuario TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    rol TEXT NOT NULL,
                    estado TEXT NOT NULL
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla usuarios creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla usuarios."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // ACTUALIZAR MOVIMIENTOS
    // SOLO PARA BASES VIEJAS
    // =========================

    public static void actualizarTablaMovimientosNueva() {

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("""
                    CREATE TABLE IF NOT EXISTS movimientos_nueva (
                        id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_animal INTEGER NOT NULL,
                        id_ingreso INTEGER,
                        fecha_movimiento TEXT NOT NULL,
                        tipo_movimiento TEXT NOT NULL,
                        cantidad INTEGER NOT NULL,
                        destino TEXT,
                        observaciones TEXT,

                        FOREIGN KEY (id_animal)
                        REFERENCES animales(id_animal),

                        FOREIGN KEY (id_ingreso)
                        REFERENCES ingresos(id_ingreso)
                    )
                    """);

            sentencia.execute("""
                    INSERT INTO movimientos_nueva (
                        id_movimiento,
                        id_animal,
                        fecha_movimiento,
                        tipo_movimiento,
                        cantidad,
                        destino,
                        observaciones
                    )
                    SELECT
                        id_movimiento,
                        id_animal,
                        fecha_movimiento,
                        tipo_movimiento,
                        cantidad,
                        destino,
                        observaciones
                    FROM movimientos
                    """);

            sentencia.execute("""
                    DROP TABLE movimientos
                    """);

            sentencia.execute("""
                    ALTER TABLE movimientos_nueva
                    RENAME TO movimientos
                    """);

            System.out.println(
                    "Tabla movimientos actualizada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al actualizar tabla movimientos."
            );

            System.out.println(e.getMessage());
        }
    }
}