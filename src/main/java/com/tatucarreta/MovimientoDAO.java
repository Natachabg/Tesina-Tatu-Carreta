package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    // =========================================================
    // AGREGAR MOVIMIENTO
    // =========================================================

    public void agregar(Movimiento movimiento) {

        String sql = """
                INSERT INTO movimientos (
                    id_detalle,
                    id_plantel,
                    fecha_movimiento,
                    tipo_movimiento,
                    cantidad,
                    destino,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    movimiento.getIdDetalle()
            );

            if (movimiento.getIdPlantel() != null) {

                sentencia.setInt(
                        2,
                        movimiento.getIdPlantel()
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

            System.out.println(
                    e.getMessage()
            );
        }
    }


    // =========================================================
    // LISTAR MOVIMIENTOS
    // Compatible con la estructura actual de la base
    // =========================================================

    public List<Movimiento> listar() {

        List<Movimiento> movimientos =
                new ArrayList<>();

        String sql = """
                SELECT
                    m.id_movimiento,
                    m.id_detalle,
                    m.id_plantel,
                    m.fecha_movimiento,
                    m.tipo_movimiento,
                    m.cantidad,
                    m.destino,
                    m.observaciones,

                    a.nombre_vulgar AS nombre_animal,
                    e.nombre AS especie,
                    i.numero_acta AS numero_acta

                FROM movimientos m

                INNER JOIN detalle_ingreso d
                    ON m.id_detalle = d.id_detalle

                INNER JOIN animales a
                    ON d.id_animal = a.id_animal

                INNER JOIN especies e
                    ON a.id_especie = e.id_especie

                INNER JOIN ingresos i
                    ON d.id_ingreso = i.id_ingreso

                ORDER BY m.id_movimiento DESC
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

                movimiento.setIdDetalle(
                        resultado.getInt(
                                "id_detalle"
                        )
                );

                int idPlantel =
                        resultado.getInt(
                                "id_plantel"
                        );

                if (resultado.wasNull()) {

                    movimiento.setIdPlantel(null);

                } else {

                    movimiento.setIdPlantel(
                            idPlantel
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

                movimiento.setNombreAnimal(
                        resultado.getString(
                                "nombre_animal"
                        )
                );

                movimiento.setEspecie(
                        resultado.getString(
                                "especie"
                        )
                );

                movimiento.setNumeroActa(
                        resultado.getString(
                                "numero_acta"
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

            System.out.println(
                    e.getMessage()
            );
        }

        return movimientos;
    }


    // =========================================================
    // ELIMINAR MOVIMIENTO
    // =========================================================

    public void eliminar(
            int idMovimiento) {

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

            System.out.println(
                    e.getMessage()
            );
        }
    }
}