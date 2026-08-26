package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlantelPermanenteDAO {

    // =========================
    // AGREGAR
    // =========================

    public void agregar(PlantelPermanente plantel) {

        String sql = """
                INSERT INTO plantel_permanente (
                    id_animal,
                    cantidad,
                    tipo_ubicacion,
                    id_habitaculo,
                    fecha_ingreso_plantel,
                    estado,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    plantel.getIdAnimal()
            );

            sentencia.setInt(
                    2,
                    plantel.getCantidad()
            );

            sentencia.setString(
                    3,
                    plantel.getTipoUbicacion()
            );

            if (plantel.getIdHabitaculo() != null) {

                sentencia.setInt(
                        4,
                        plantel.getIdHabitaculo()
                );

            } else {

                sentencia.setNull(
                        4,
                        java.sql.Types.INTEGER
                );
            }

            sentencia.setString(
                    5,
                    plantel.getFechaIngresoPlantel()
            );

            sentencia.setString(
                    6,
                    plantel.getEstado()
            );

            sentencia.setString(
                    7,
                    plantel.getObservaciones()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Registro agregado al plantel permanente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar al plantel permanente."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // LISTAR CON NOMBRE
    // DEL ANIMAL Y HABITÁCULO
    // =========================

    public List<PlantelPermanente> listar() {

        List<PlantelPermanente> plantel =
                new ArrayList<>();

        String sql = """
                SELECT
                    p.id_plantel,
                    p.id_animal,
                    a.nombre_vulgar AS nombre_animal,
                    p.cantidad,
                    p.tipo_ubicacion,
                    p.id_habitaculo,
                    h.nombre AS nombre_habitaculo,
                    p.fecha_ingreso_plantel,
                    p.estado,
                    p.observaciones
                FROM plantel_permanente p

                INNER JOIN animales a
                    ON p.id_animal = a.id_animal

                LEFT JOIN habitaculos h
                    ON p.id_habitaculo = h.id_habitaculo

                ORDER BY p.id_plantel
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                PlantelPermanente registro =
                        new PlantelPermanente();

                registro.setIdPlantel(
                        resultado.getInt("id_plantel")
                );

                registro.setIdAnimal(
                        resultado.getInt("id_animal")
                );

                registro.setNombreAnimal(
                        resultado.getString("nombre_animal")
                );

                registro.setCantidad(
                        resultado.getInt("cantidad")
                );

                registro.setTipoUbicacion(
                        resultado.getString("tipo_ubicacion")
                );

                int idHabitaculo =
                        resultado.getInt("id_habitaculo");

                if (resultado.wasNull()) {

                    registro.setIdHabitaculo(null);

                } else {

                    registro.setIdHabitaculo(
                            idHabitaculo
                    );
                }

                registro.setNombreHabitaculo(
                        resultado.getString(
                                "nombre_habitaculo"
                        )
                );

                registro.setFechaIngresoPlantel(
                        resultado.getString(
                                "fecha_ingreso_plantel"
                        )
                );

                registro.setEstado(
                        resultado.getString("estado")
                );

                registro.setObservaciones(
                        resultado.getString("observaciones")
                );

                plantel.add(registro);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar el plantel permanente."
            );

            System.out.println(e.getMessage());
        }

        return plantel;
    }


    // =========================
    // MODIFICAR
    // =========================

    public void modificar(PlantelPermanente plantel) {

        String sql = """
                UPDATE plantel_permanente
                SET
                    id_animal = ?,
                    cantidad = ?,
                    tipo_ubicacion = ?,
                    id_habitaculo = ?,
                    fecha_ingreso_plantel = ?,
                    estado = ?,
                    observaciones = ?
                WHERE id_plantel = ?
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    plantel.getIdAnimal()
            );

            sentencia.setInt(
                    2,
                    plantel.getCantidad()
            );

            sentencia.setString(
                    3,
                    plantel.getTipoUbicacion()
            );

            if (plantel.getIdHabitaculo() != null) {

                sentencia.setInt(
                        4,
                        plantel.getIdHabitaculo()
                );

            } else {

                sentencia.setNull(
                        4,
                        java.sql.Types.INTEGER
                );
            }

            sentencia.setString(
                    5,
                    plantel.getFechaIngresoPlantel()
            );

            sentencia.setString(
                    6,
                    plantel.getEstado()
            );

            sentencia.setString(
                    7,
                    plantel.getObservaciones()
            );

            sentencia.setInt(
                    8,
                    plantel.getIdPlantel()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Registro del plantel modificado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar el plantel permanente."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // ELIMINAR
    // =========================

    public void eliminar(int idPlantel) {

        String sql = """
                DELETE FROM plantel_permanente
                WHERE id_plantel = ?
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idPlantel
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Registro eliminado del plantel permanente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar del plantel permanente."
            );

            System.out.println(e.getMessage());
        }
    }
}