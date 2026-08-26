package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DetalleIngresoDAO {

    public void agregar(DetalleIngreso detalle) {

        String sql = """
                INSERT INTO detalle_ingreso (
                    id_ingreso,
                    id_animal,
                    cantidad,
                    sexo,
                    edad,
                    peso,
                    estado_ingreso,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    detalle.getIdIngreso()
            );

            sentencia.setInt(
                    2,
                    detalle.getIdAnimal()
            );

            sentencia.setInt(
                    3,
                    detalle.getCantidad()
            );

            sentencia.setString(
                    4,
                    detalle.getSexo()
            );

            sentencia.setString(
                    5,
                    detalle.getEdad()
            );

            sentencia.setDouble(
                    6,
                    detalle.getPeso()
            );

            sentencia.setString(
                    7,
                    detalle.getEstadoIngreso()
            );

            sentencia.setString(
                    8,
                    detalle.getObservaciones()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Detalle de ingreso agregado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar detalle de ingreso."
            );

            System.out.println(e.getMessage());
        }
    }

    public List<DetalleIngreso> listarPorIngreso(int idIngreso) {

        List<DetalleIngreso> detalles = new ArrayList<>();

        String sql = """
                SELECT
                    id_detalle,
                    id_ingreso,
                    id_animal,
                    cantidad,
                    sexo,
                    edad,
                    peso,
                    estado_ingreso,
                    observaciones
                FROM detalle_ingreso
                WHERE id_ingreso = ?
                ORDER BY id_detalle
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idIngreso
            );

            try (ResultSet resultado =
                         sentencia.executeQuery()) {

                while (resultado.next()) {

                    DetalleIngreso detalle =
                            new DetalleIngreso();

                    detalle.setIdDetalle(
                            resultado.getInt("id_detalle")
                    );

                    detalle.setIdIngreso(
                            resultado.getInt("id_ingreso")
                    );

                    detalle.setIdAnimal(
                            resultado.getInt("id_animal")
                    );

                    detalle.setCantidad(
                            resultado.getInt("cantidad")
                    );

                    detalle.setSexo(
                            resultado.getString("sexo")
                    );

                    detalle.setEdad(
                            resultado.getString("edad")
                    );

                    detalle.setPeso(
                            resultado.getDouble("peso")
                    );

                    detalle.setEstadoIngreso(
                            resultado.getString(
                                    "estado_ingreso"
                            )
                    );

                    detalle.setObservaciones(
                            resultado.getString(
                                    "observaciones"
                            )
                    );

                    detalles.add(detalle);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar detalles de ingreso."
            );

            System.out.println(e.getMessage());
        }

        return detalles;
    }

    public void modificar(DetalleIngreso detalle) {

        String sql = """
                UPDATE detalle_ingreso
                SET
                    id_animal = ?,
                    cantidad = ?,
                    sexo = ?,
                    edad = ?,
                    peso = ?,
                    estado_ingreso = ?,
                    observaciones = ?
                WHERE id_detalle = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    detalle.getIdAnimal()
            );

            sentencia.setInt(
                    2,
                    detalle.getCantidad()
            );

            sentencia.setString(
                    3,
                    detalle.getSexo()
            );

            sentencia.setString(
                    4,
                    detalle.getEdad()
            );

            sentencia.setDouble(
                    5,
                    detalle.getPeso()
            );

            sentencia.setString(
                    6,
                    detalle.getEstadoIngreso()
            );

            sentencia.setString(
                    7,
                    detalle.getObservaciones()
            );

            sentencia.setInt(
                    8,
                    detalle.getIdDetalle()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Detalle de ingreso modificado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar detalle de ingreso."
            );

            System.out.println(e.getMessage());
        }
    }

    public void eliminar(int idDetalle) {

        String sql = """
                DELETE FROM detalle_ingreso
                WHERE id_detalle = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idDetalle
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Detalle de ingreso eliminado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar detalle de ingreso."
            );

            System.out.println(e.getMessage());
        }
    }
}