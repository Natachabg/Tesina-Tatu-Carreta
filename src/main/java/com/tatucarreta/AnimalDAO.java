package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AnimalDAO {

    // =========================================================
    // ALTA
    // =========================================================

    public void agregar(Animal animal) {

        String sql = """
                INSERT INTO animales (
                    id_especie,
                    nombre_vulgar,
                    nombre_cientifico,
                    cantidad_actual,
                    origen,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    animal.getIdEspecie()
            );

            sentencia.setString(
                    2,
                    animal.getNombreVulgar()
            );

            sentencia.setString(
                    3,
                    animal.getNombreCientifico()
            );

            sentencia.setInt(
                    4,
                    animal.getCantidadActual()
            );

            sentencia.setString(
                    5,
                    animal.getOrigen()
            );

            sentencia.setString(
                    6,
                    animal.getEstado()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Animal agregado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar animal."
            );

            System.out.println(
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // BAJA
    // =========================================================

    public boolean eliminar(int idAnimal) {

        String sqlVerificar = """
                SELECT
                    (
                        SELECT COUNT(*)
                        FROM detalle_ingreso
                        WHERE id_animal = ?
                    ) AS cantidad_ingresos,

                    (
                        SELECT COUNT(*)
                        FROM plantel_permanente
                        WHERE id_animal = ?
                    ) AS cantidad_plantel,

                    (
                        SELECT COUNT(*)
                        FROM movimientos m
                        INNER JOIN detalle_ingreso d
                            ON d.id_detalle = m.id_detalle
                        WHERE d.id_animal = ?
                    ) AS cantidad_movimientos
                """;


        String sqlEliminar = """
                DELETE FROM animales
                WHERE id_animal = ?
                """;


        try (Connection conexion =
                     ConexionSQLite.conectar()) {


            // =================================================
            // VERIFICAR INFORMACIÓN RELACIONADA
            // =================================================

            try (PreparedStatement sentenciaVerificar =
                         conexion.prepareStatement(
                                 sqlVerificar
                         )) {

                sentenciaVerificar.setInt(
                        1,
                        idAnimal
                );

                sentenciaVerificar.setInt(
                        2,
                        idAnimal
                );

                sentenciaVerificar.setInt(
                        3,
                        idAnimal
                );


                try (ResultSet resultado =
                             sentenciaVerificar.executeQuery()) {

                    if (resultado.next()) {

                        int cantidadIngresos =
                                resultado.getInt(
                                        "cantidad_ingresos"
                                );

                        int cantidadPlantel =
                                resultado.getInt(
                                        "cantidad_plantel"
                                );

                        int cantidadMovimientos =
                                resultado.getInt(
                                        "cantidad_movimientos"
                                );


                        // -------------------------------------
                        // NO PERMITIR ELIMINACIÓN
                        // -------------------------------------

                        if (
                                cantidadIngresos > 0
                                || cantidadPlantel > 0
                                || cantidadMovimientos > 0
                        ) {

                            System.out.println(
                                    "No se puede eliminar el animal."
                            );

                            System.out.println(
                                    "El animal posee información relacionada."
                            );

                            System.out.println(
                                    "Ingresos: "
                                            + cantidadIngresos
                            );

                            System.out.println(
                                    "Plantel permanente: "
                                            + cantidadPlantel
                            );

                            System.out.println(
                                    "Movimientos: "
                                            + cantidadMovimientos
                            );

                            return false;
                        }
                    }
                }
            }


            // =================================================
            // ELIMINAR
            // =================================================

            try (PreparedStatement sentenciaEliminar =
                         conexion.prepareStatement(
                                 sqlEliminar
                         )) {

                sentenciaEliminar.setInt(
                        1,
                        idAnimal
                );

                int filasEliminadas =
                        sentenciaEliminar.executeUpdate();


                if (filasEliminadas > 0) {

                    System.out.println(
                            "Animal eliminado correctamente."
                    );

                    return true;

                } else {

                    System.out.println(
                            "No se encontró el animal."
                    );

                    return false;
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar animal."
            );

            System.out.println(
                    e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // MODIFICACIÓN
    // =========================================================

    public void modificar(Animal animal) {

        String sql = """
                UPDATE animales
                SET
                    id_especie = ?,
                    nombre_vulgar = ?,
                    nombre_cientifico = ?,
                    cantidad_actual = ?,
                    origen = ?,
                    estado = ?
                WHERE id_animal = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    animal.getIdEspecie()
            );

            sentencia.setString(
                    2,
                    animal.getNombreVulgar()
            );

            sentencia.setString(
                    3,
                    animal.getNombreCientifico()
            );

            sentencia.setInt(
                    4,
                    animal.getCantidadActual()
            );

            sentencia.setString(
                    5,
                    animal.getOrigen()
            );

            sentencia.setString(
                    6,
                    animal.getEstado()
            );

            sentencia.setInt(
                    7,
                    animal.getIdAnimal()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Animal modificado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar animal."
            );

            System.out.println(
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // LISTAR TODOS
    // =========================================================

    public List<Animal> listar() {

        List<Animal> animales =
                new ArrayList<>();

        /*
         * La cantidad que se muestra en Animales se calcula así:
         *
         * CANTIDAD INGRESADA
         * -
         * CANTIDAD DE MOVIMIENTOS
         *
         * Si el animal todavía no tiene ingresos,
         * se utiliza cantidad_actual de la tabla animales.
         *
         * De esta manera no se modifica el dato original
         * del animal y se evita duplicar información.
         */

        String sql = """
                SELECT
                    a.id_animal,
                    a.id_especie,
                    a.nombre_vulgar,
                    a.nombre_cientifico,

                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM detalle_ingreso d
                            WHERE d.id_animal = a.id_animal
                        )
                        THEN
                            COALESCE(
                                (
                                    SELECT SUM(d.cantidad)
                                    FROM detalle_ingreso d
                                    WHERE d.id_animal = a.id_animal
                                ),
                                0
                            )
                            -
                            COALESCE(
                                (
                                    SELECT SUM(m.cantidad)
                                    FROM movimientos m
                                    INNER JOIN detalle_ingreso d2
                                        ON d2.id_detalle = m.id_detalle
                                    WHERE d2.id_animal = a.id_animal
                                      AND m.tipo_movimiento IN (
                                          'LIBERACION',
                                          'TRASLADO',
                                          'FALLECIMIENTO'
                                      )
                                ),
                                0
                            )
                        ELSE
                            a.cantidad_actual
                    END AS cantidad_actual,

                    a.origen,
                    a.estado

                FROM animales a

                ORDER BY
                    a.nombre_vulgar COLLATE NOCASE ASC
                """;


        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Animal animal =
                        crearAnimalDesdeResultado(
                                resultado
                        );

                animales.add(animal);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar animales."
            );

            System.out.println(
                    e.getMessage()
            );
        }

        return animales;
    }


    // =========================================================
    // BUSCAR ANIMALES
    // =========================================================

    public List<Animal> buscar(String texto) {

        List<Animal> animales =
                new ArrayList<>();

        String busqueda =
                texto == null
                        ? ""
                        : texto.trim();


        if (busqueda.isBlank()) {

            return listar();
        }


        String sql = """
                SELECT
                    a.id_animal,
                    a.id_especie,
                    a.nombre_vulgar,
                    a.nombre_cientifico,

                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM detalle_ingreso d
                            WHERE d.id_animal = a.id_animal
                        )
                        THEN
                            COALESCE(
                                (
                                    SELECT SUM(d.cantidad)
                                    FROM detalle_ingreso d
                                    WHERE d.id_animal = a.id_animal
                                ),
                                0
                            )
                            -
                            COALESCE(
                                (
                                    SELECT SUM(m.cantidad)
                                    FROM movimientos m
                                    INNER JOIN detalle_ingreso d2
                                        ON d2.id_detalle = m.id_detalle
                                    WHERE d2.id_animal = a.id_animal
                                      AND m.tipo_movimiento IN (
                                          'LIBERACION',
                                          'TRASLADO',
                                          'FALLECIMIENTO'
                                      )
                                ),
                                0
                            )
                        ELSE
                            a.cantidad_actual
                    END AS cantidad_actual,

                    a.origen,
                    a.estado

                FROM animales a

                WHERE
                    LOWER(a.nombre_vulgar)
                        LIKE LOWER(?)

                    OR LOWER(a.nombre_cientifico)
                        LIKE LOWER(?)

                    OR EXISTS (
                        SELECT 1
                        FROM detalle_ingreso d
                        INNER JOIN ingresos i
                            ON i.id_ingreso = d.id_ingreso
                        WHERE
                            d.id_animal = a.id_animal
                            AND LOWER(i.numero_acta)
                                LIKE LOWER(?)
                    )

                ORDER BY
                    a.nombre_vulgar COLLATE NOCASE ASC
                """;


        String criterio =
                "%" + busqueda + "%";


        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    criterio
            );

            sentencia.setString(
                    2,
                    criterio
            );

            sentencia.setString(
                    3,
                    criterio
            );


            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                while (resultado.next()) {

                    Animal animal =
                            crearAnimalDesdeResultado(
                                    resultado
                            );

                    animales.add(animal);
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "Error al buscar animales."
            );

            System.out.println(
                    e.getMessage()
            );
        }


        return animales;
    }


    // =========================================================
    // CREAR OBJETO ANIMAL DESDE RESULTSET
    // =========================================================

    private Animal crearAnimalDesdeResultado(
            ResultSet resultado)
            throws Exception {

        Animal animal =
                new Animal();


        animal.setIdAnimal(
                resultado.getInt(
                        "id_animal"
                )
        );


        animal.setIdEspecie(
                resultado.getInt(
                        "id_especie"
                )
        );


        animal.setNombreVulgar(
                resultado.getString(
                        "nombre_vulgar"
                )
        );


        animal.setNombreCientifico(
                resultado.getString(
                        "nombre_cientifico"
                )
        );


        /*
         * Acá recibimos el valor calculado por el SELECT.
         */
        animal.setCantidadActual(
                resultado.getInt(
                        "cantidad_actual"
                )
        );


        animal.setOrigen(
                resultado.getString(
                        "origen"
                )
        );


        animal.setEstado(
                resultado.getString(
                        "estado"
                )
        );


        return animal;
    }
}