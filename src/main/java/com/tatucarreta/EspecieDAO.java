package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EspecieDAO {

    public void agregar(Especie especie) {

        String sql = """
                INSERT INTO especies (nombre)
                VALUES (?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    especie.getNombre()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Especie agregada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar especie."
            );

            System.out.println(e.getMessage());
        }
    }

    public List<Especie> listar() {

        List<Especie> especies = new ArrayList<>();

        String sql = """
                SELECT id_especie, nombre
                FROM especies
                ORDER BY nombre
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Especie especie = new Especie();

                especie.setIdEspecie(
                        resultado.getInt("id_especie")
                );

                especie.setNombre(
                        resultado.getString("nombre")
                );

                especies.add(especie);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar especies."
            );

            System.out.println(e.getMessage());
        }

        return especies;
    }

    public void modificar(Especie especie) {

        String sql = """
                UPDATE especies
                SET nombre = ?
                WHERE id_especie = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    especie.getNombre()
            );

            sentencia.setInt(
                    2,
                    especie.getIdEspecie()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Especie modificada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar especie."
            );

            System.out.println(e.getMessage());
        }
    }

    public void eliminar(int idEspecie) {

        String sql = """
                DELETE FROM especies
                WHERE id_especie = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idEspecie
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Especie eliminada correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar especie."
            );

            System.out.println(e.getMessage());
        }
    }
}