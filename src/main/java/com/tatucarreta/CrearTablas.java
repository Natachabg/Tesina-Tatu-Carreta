package com.tatucarreta;

import java.sql.Connection;
import java.sql.Statement;

public class CrearTablas {

    // =========================
    // TABLA ESPECIES
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

            System.out.println("Tabla especies creada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al crear la tabla especies.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // ACTUALIZAR ESPECIES
    // =========================

    public static void actualizarTablaEspecies() {

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("""
                    CREATE TABLE especies_nueva (
                        id_especie INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL UNIQUE
                    )
                    """);

            sentencia.execute("""
                    INSERT INTO especies_nueva (
                        id_especie,
                        nombre
                    )
                    SELECT
                        id_especie,
                        nombre
                    FROM especies
                    """);

            sentencia.execute("""
                    DROP TABLE especies
                    """);

            sentencia.execute("""
                    ALTER TABLE especies_nueva
                    RENAME TO especies
                    """);

            System.out.println("Tabla especies actualizada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al actualizar la tabla especies.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA ANIMALES
    // =========================

    public static void crearTablaAnimales() {

        String sql = """
                CREATE TABLE IF NOT EXISTS animales (
                    id_animal INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_especie INTEGER NOT NULL,
                    nombre_vulgar TEXT NOT NULL,
                    nombre_cientifico TEXT NOT NULL,
                    estado TEXT NOT NULL,

                    FOREIGN KEY (id_especie)
                    REFERENCES especies(id_especie)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println("Tabla animales creada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al crear la tabla animales.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // ACTUALIZAR ANIMALES
    // =========================

    public static void actualizarTablaAnimales() {

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("""
                    CREATE TABLE animales_nueva (
                        id_animal INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_especie INTEGER NOT NULL,
                        nombre_vulgar TEXT NOT NULL,
                        nombre_cientifico TEXT NOT NULL,
                        estado TEXT NOT NULL,

                        FOREIGN KEY (id_especie)
                        REFERENCES especies(id_especie)
                    )
                    """);

            sentencia.execute("""
                    INSERT INTO animales_nueva (
                        id_animal,
                        id_especie,
                        nombre_vulgar,
                        nombre_cientifico,
                        estado
                    )
                    SELECT
                        id_animal,
                        id_especie,
                        'Sin especificar',
                        'Sin especificar',
                        'Activo'
                    FROM animales
                    """);

            sentencia.execute("""
                    DROP TABLE animales
                    """);

            sentencia.execute("""
                    ALTER TABLE animales_nueva
                    RENAME TO animales
                    """);

            System.out.println("Tabla animales actualizada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al actualizar la tabla animales.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA INGRESOS
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

            System.out.println("Tabla ingresos creada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al crear la tabla ingresos.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA DETALLE INGRESO
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

            System.out.println("Tabla detalle_ingreso creada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al crear la tabla detalle_ingreso.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA HABITACULOS
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

            System.out.println("Tabla habitaculos creada correctamente.");

        } catch (Exception e) {

            System.out.println("Error al crear la tabla habitaculos.");
            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA PLANTEL PERMANENTE
    // =========================

    public static void crearTablaPlantelPermanente() {

        String sql = """
                CREATE TABLE IF NOT EXISTS plantel_permanente (
                    id_plantel INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_detalle INTEGER NOT NULL,
                    cantidad INTEGER NOT NULL,
                    tipo_ubicacion TEXT NOT NULL,
                    id_habitaculo INTEGER,
                    fecha_ingreso_plantel TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    observaciones TEXT,

                    FOREIGN KEY (id_detalle)
                    REFERENCES detalle_ingreso(id_detalle),

                    FOREIGN KEY (id_habitaculo)
                    REFERENCES habitaculos(id_habitaculo)
                )
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute(sql);

            System.out.println(
                    "Tabla plantel_permanente creada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al crear la tabla plantel_permanente."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA MOVIMIENTOS
    // =========================

    public static void crearTablaMovimientos() {

        String sql = """
                CREATE TABLE IF NOT EXISTS movimientos (
                    id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_detalle INTEGER NOT NULL,
                    fecha_movimiento TEXT NOT NULL,
                    tipo_movimiento TEXT NOT NULL,
                    cantidad INTEGER NOT NULL,
                    destino TEXT,
                    observaciones TEXT,

                    FOREIGN KEY (id_detalle)
                    REFERENCES detalle_ingreso(id_detalle)
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
    // ACTUALIZAR MOVIMIENTOS
    // =========================

    public static void actualizarTablaMovimientos() {

        try (Connection conexion = ConexionSQLite.conectar();
             Statement sentencia = conexion.createStatement();
             java.sql.ResultSet columnas =
                     sentencia.executeQuery(
                             "PRAGMA table_info(movimientos)"
                     )) {

            boolean tieneIdDetalle = false;
            boolean tieneIdAnimal = false;

            while (columnas.next()) {

                String nombreColumna =
                        columnas.getString("name");

                if ("id_detalle".equalsIgnoreCase(nombreColumna)) {
                    tieneIdDetalle = true;
                }

                if ("id_animal".equalsIgnoreCase(nombreColumna)) {
                    tieneIdAnimal = true;
                }
            }

            // Ya está actualizada
            if (tieneIdDetalle) {

                System.out.println(
                        "La tabla movimientos ya está actualizada."
                );

                return;
            }

            // Tenemos la tabla vieja
            if (tieneIdAnimal) {

                int cantidadRegistros = 0;

                try (java.sql.ResultSet resultado =
                             sentencia.executeQuery(
                                     "SELECT COUNT(*) AS total FROM movimientos"
                             )) {

                    if (resultado.next()) {

                        cantidadRegistros =
                                resultado.getInt("total");
                    }
                }

                // En nuestra base actual sabemos que son 0.
                // No hay movimientos que perder.
                if (cantidadRegistros == 0) {

                    sentencia.execute(
                            "DROP TABLE movimientos"
                    );

                    String sqlNueva = """
                            CREATE TABLE movimientos (
                                id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                                id_detalle INTEGER NOT NULL,
                                fecha_movimiento TEXT NOT NULL,
                                tipo_movimiento TEXT NOT NULL,
                                cantidad INTEGER NOT NULL,
                                destino TEXT,
                                observaciones TEXT,

                                FOREIGN KEY (id_detalle)
                                REFERENCES detalle_ingreso(id_detalle)
                            )
                            """;

                    sentencia.execute(sqlNueva);

                    System.out.println(
                            "Tabla movimientos actualizada correctamente."
                    );

                } else {

                    System.out.println(
                            "ATENCIÓN: la tabla movimientos tiene "
                            + cantidadRegistros
                            + " registros y no se modificó."
                    );

                }

            } else {

                System.out.println(
                        "La tabla movimientos no tiene la estructura esperada."
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al actualizar la tabla movimientos."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // TABLA IDENTIFICACIONES
    // =========================

    public static void crearTablaIdentificaciones() {

        String sql = """
                CREATE TABLE IF NOT EXISTS identificaciones (
                    id_identificacion INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_detalle INTEGER NOT NULL,
                    tipo_identificacion TEXT NOT NULL,
                    numero_identificacion TEXT NOT NULL,
                    fecha_identificacion TEXT,
                    observaciones TEXT,

                    FOREIGN KEY (id_detalle)
                    REFERENCES detalle_ingreso(id_detalle)
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
    // TABLA USUARIOS
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
    // =========================================================
// ACTUALIZAR PLANTEL PERMANENTE
// Permite registros de carga inicial sin id_detalle
// =========================================================

public static void actualizarTablaPlantelPermanente() {

    try (Connection conexion = ConexionSQLite.conectar();
         Statement sentencia = conexion.createStatement()) {

        // Verificamos si id_detalle permite NULL.
        // SQLite no permite modificar directamente una columna NOT NULL,
        // por eso, si todavía es NOT NULL, recreamos la tabla conservando
        // los datos existentes.

        boolean idDetalleEsObligatorio = false;

        try (java.sql.ResultSet columnas =
                     sentencia.executeQuery(
                             "PRAGMA table_info(plantel_permanente)"
                     )) {

            while (columnas.next()) {

                String nombre =
                        columnas.getString("name");

                if ("id_detalle".equalsIgnoreCase(nombre)) {

                    int notNull =
                            columnas.getInt("notnull");

                    idDetalleEsObligatorio =
                            notNull == 1;

                    break;
                }
            }
        }

        // Ya permite NULL. No hacemos nada.
        if (!idDetalleEsObligatorio) {

            System.out.println(
                    "La tabla plantel_permanente ya permite carga inicial."
            );

            return;
        }

        // -------------------------------------------------
        // Crear tabla nueva
        // -------------------------------------------------

        sentencia.execute("""
                CREATE TABLE plantel_permanente_nueva (
                    id_plantel INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_detalle INTEGER,
                    cantidad INTEGER NOT NULL,
                    tipo_ubicacion TEXT NOT NULL,
                    id_habitaculo INTEGER,
                    fecha_ingreso_plantel TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    observaciones TEXT,

                    FOREIGN KEY (id_detalle)
                    REFERENCES detalle_ingreso(id_detalle),

                    FOREIGN KEY (id_habitaculo)
                    REFERENCES habitaculos(id_habitaculo)
                )
                """);

        // -------------------------------------------------
        // Copiar los datos existentes
        // -------------------------------------------------

        sentencia.execute("""
                INSERT INTO plantel_permanente_nueva (
                    id_plantel,
                    id_detalle,
                    cantidad,
                    tipo_ubicacion,
                    id_habitaculo,
                    fecha_ingreso_plantel,
                    estado,
                    observaciones
                )
                SELECT
                    id_plantel,
                    id_detalle,
                    cantidad,
                    tipo_ubicacion,
                    id_habitaculo,
                    fecha_ingreso_plantel,
                    estado,
                    observaciones
                FROM plantel_permanente
                """);

        // -------------------------------------------------
        // Reemplazar tabla
        // -------------------------------------------------

        sentencia.execute(
                "DROP TABLE plantel_permanente"
        );

        sentencia.execute("""
                ALTER TABLE plantel_permanente_nueva
                RENAME TO plantel_permanente
                """);

        System.out.println(
                "Tabla plantel_permanente actualizada correctamente."
        );

    } catch (Exception e) {

        System.out.println(
                "Error al actualizar plantel_permanente."
        );

        System.out.println(
                e.getMessage()
        );
    }
}


// =========================================================
// ACTUALIZAR MOVIMIENTOS
// Agrega referencia opcional al plantel permanente
// =========================================================

public static void actualizarTablaMovimientosPlantel() {

    try (Connection conexion = ConexionSQLite.conectar();
         Statement sentencia = conexion.createStatement()) {

        boolean tieneIdPlantel = false;

        try (java.sql.ResultSet columnas =
                     sentencia.executeQuery(
                             "PRAGMA table_info(movimientos)"
                     )) {

            while (columnas.next()) {

                String nombre =
                        columnas.getString("name");

                if ("id_plantel".equalsIgnoreCase(nombre)) {

                    tieneIdPlantel = true;
                    break;
                }
            }
        }

        // Si ya existe, no hacemos nada.
        if (tieneIdPlantel) {

            System.out.println(
                    "La tabla movimientos ya tiene id_plantel."
            );

            return;
        }

        // -------------------------------------------------
        // Agregar nueva referencia opcional
        // -------------------------------------------------

        sentencia.execute("""
                ALTER TABLE movimientos
                ADD COLUMN id_plantel INTEGER
                """);

        System.out.println(
                "Columna id_plantel agregada a movimientos."
        );

    } catch (Exception e) {

        System.out.println(
                "Error al actualizar movimientos."
        );

        System.out.println(
                e.getMessage()
        );
    }
}
}