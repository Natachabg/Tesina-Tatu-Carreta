package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    // =========================
    // AGREGAR
    // =========================

    public void agregar(Movimiento movimiento) {

        String sql = """
                INSERT INTO movimientos (
                    id_animal,
                    id_ingreso,
                    fecha_movimiento,
                    tipo_movimiento,
                    cantidad,
                    destino,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    movimiento.getIdAnimal()
            );

            // ID INGRESO OPCIONAL
            if (movimiento.getIdIngreso() != null) {

                sentencia.setInt(
                        2,
                        movimiento.getIdIngreso()
                );

            } else {

                sentencia.setNull(
                        2,
                        java.sql.Types.INTEGER
                );
            }

            sentencia.setString(
                    3,
                    movimiento.getFechaMovimiento()
            );

            sentencia.setString(
                    4,
                    movimiento.getTipoMovimiento()
            );

            sentencia.setInt(
                    5,
                    movimiento.getCantidad()
            );

            sentencia.setString(
                    6,
                    movimiento.getDestino()
            );

            sentencia.setString(
                    7,
                    movimiento.getObservaciones()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Movimiento registrado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al registrar el movimiento."
            );

            System.out.println(e.getMessage());
        }
    }


    // =========================
    // LISTAR
    // =========================

    public List<Movimiento> listar() {

        List<Movimiento> movimientos =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_movimiento,
                    id_animal,
                    id_ingreso,
                    fecha_movimiento,
                    tipo_movimiento,
                    cantidad,
                    destino,
                    observaciones
                FROM movimientos
                ORDER BY fecha_movimiento DESC
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Movimiento movimiento =
                        new Movimiento();

                movimiento.setIdMovimiento(
                        resultado.getInt(
                                "id_movimiento"
                        )
                );

                movimiento.setIdAnimal(
                        resultado.getInt(
                                "id_animal"
                        )
                );

                int idIngreso =
                        resultado.getInt(
                                "id_ingreso"
                        );

                if (resultado.wasNull()) {

                    movimiento.setIdIngreso(null);

                } else {

                    movimiento.setIdIngreso(
                            idIngreso
                    );
                }

                movimiento.setFechaMovimiento(
                        resultado.getString(
                                "fecha_movimiento"
                        )
                );

                movimiento.setTipoMovimiento(
                        resultado.getString(
                                "tipo_movimiento"
                        )
                );

                movimiento.setCantidad(
                        resultado.getInt(
                                "cantidad"
                        )
                );

                movimiento.setDestino(
                        resultado.getString(
                                "destino"
                        )
                );

                movimiento.setObservaciones(
                        resultado.getString(
                                "observaciones"
                        )
                );

                movimientos.add(
                        movimiento
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar movimientos."
            );

            System.out.println(e.getMessage());
        }

        return movimientos;
    }


    // =========================
    // ELIMINAR
    // =========================

    public void eliminar(int idMovimiento) {

        String sql = """
                DELETE FROM movimientos
                WHERE id_movimiento = ?
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idMovimiento
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Movimiento eliminado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar movimiento."
            );

            System.out.println(e.getMessage());
        }
    }
}
