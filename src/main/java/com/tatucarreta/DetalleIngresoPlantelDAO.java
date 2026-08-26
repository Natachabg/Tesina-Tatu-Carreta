package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DetalleIngresoPlantelDAO {

    public List<DetalleIngresoPlantel> listar() {

        List<DetalleIngresoPlantel> lista =
                new ArrayList<>();

        String sql = """
                SELECT
                    d.id_detalle,
                    a.nombre_vulgar,
                    d.id_ingreso,
                    d.cantidad
                FROM detalle_ingreso d
                INNER JOIN animales a
                    ON d.id_animal = a.id_animal
                ORDER BY d.id_ingreso DESC
                """;

        try (Connection conexion =
                     ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                DetalleIngresoPlantel detalle =
                        new DetalleIngresoPlantel(
                                resultado.getInt(
                                        "id_detalle"
                                ),
                                resultado.getString(
                                        "nombre_vulgar"
                                ),
                                resultado.getInt(
                                        "id_ingreso"
                                ),
                                resultado.getInt(
                                        "cantidad"
                                )
                        );

                lista.add(detalle);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar animales ingresados."
            );

            System.out.println(e.getMessage());
        }

        return lista;
    }
}