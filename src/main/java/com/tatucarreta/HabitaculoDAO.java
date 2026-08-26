package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HabitaculoDAO {

    // =========================
    // AGREGAR
    // =========================

    public void agregar(Habitaculo habitaculo) {

        String sql = """
                INSERT INTO habitaculos (
                    nombre,
                    sector,
                    capacidad,
                    estado,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    habitaculo.getNombre()
            );

            sentencia.setString(
                    2,
                    habitaculo.getSector()
            );

            if (habitaculo.getCapacidad() != null) {

                sentencia.setInt(
                        3,
                        habitaculo.getCapacidad()
                );

            } else {

                sentencia.setNull(
                        3,
                        java.sql.Types.INTEGER
                );
            }

            sentencia.setString(
                    4,
                    habitaculo.getEstado()
            );

            sentencia.setString(
                    5,
                    habitaculo.getObservaciones()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Habitáculo agregado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar el habitáculo."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // LISTAR
    // =========================

    public List<Habitaculo> listar() {

        List<Habitaculo> habitaculos =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_habitaculo,
                    nombre,
                    sector,
                    capacidad,
                    estado,
                    observaciones
                FROM habitaculos
                ORDER BY nombre
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Habitaculo habitaculo =
                        new Habitaculo();

                habitaculo.setIdHabitaculo(
                        resultado.getInt(
                                "id_habitaculo"
                        )
                );

                habitaculo.setNombre(
                        resultado.getString(
                                "nombre"
                        )
                );

                habitaculo.setSector(
                        resultado.getString(
                                "sector"
                        )
                );

                int capacidad =
                        resultado.getInt(
                                "capacidad"
                        );

                if (resultado.wasNull()) {

                    habitaculo.setCapacidad(null);

                } else {

                    habitaculo.setCapacidad(
                            capacidad
                    );
                }

                habitaculo.setEstado(
                        resultado.getString(
                                "estado"
                        )
                );

                habitaculo.setObservaciones(
                        resultado.getString(
                                "observaciones"
                        )
                );

                habitaculos.add(habitaculo);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar los habitáculos."
            );

            System.out.println(e.getMessage());
        }

        return habitaculos;
    }


    // =========================
    // MODIFICAR
    // =========================

    public void modificar(Habitaculo habitaculo) {

        String sql = """
                UPDATE habitaculos
                SET
                    nombre = ?,
                    sector = ?,
                    capacidad = ?,
                    estado = ?,
                    observaciones = ?
                WHERE id_habitaculo = ?
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    habitaculo.getNombre()
            );

            sentencia.setString(
                    2,
                    habitaculo.getSector()
            );

            if (habitaculo.getCapacidad() != null) {

                sentencia.setInt(
                        3,
                        habitaculo.getCapacidad()
                );

            } else {

                sentencia.setNull(
                        3,
                        java.sql.Types.INTEGER
                );
            }

            sentencia.setString(
                    4,
                    habitaculo.getEstado()
            );

            sentencia.setString(
                    5,
                    habitaculo.getObservaciones()
            );

            sentencia.setInt(
                    6,
                    habitaculo.getIdHabitaculo()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Habitáculo modificado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar el habitáculo."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // ELIMINAR
    // =========================

    public void eliminar(int idHabitaculo) {

        String sql = """
                DELETE FROM habitaculos
                WHERE id_habitaculo = ?
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idHabitaculo);

            sentencia.executeUpdate();

            System.out.println(
                    "Habitáculo eliminado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar el habitáculo."
            );

            System.out.println(e.getMessage());
        }
    }
}