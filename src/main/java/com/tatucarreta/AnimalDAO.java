package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AnimalDAO {

    // ALTA
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

            System.out.println(e.getMessage());
        }
    }


    // BAJA
    public void eliminar(int idAnimal) {

        String sql = """
                DELETE FROM animales
                WHERE id_animal = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idAnimal
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Animal eliminado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar animal."
            );

            System.out.println(e.getMessage());
        }
    }


    // MODIFICACIÓN
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

            System.out.println(e.getMessage());
        }
    }


    // CONSULTA
    public List<Animal> listar() {

        List<Animal> animales = new ArrayList<>();

        String sql = """
                SELECT
                    id_animal,
                    id_especie,
                    nombre_vulgar,
                    nombre_cientifico,
                    cantidad_actual,
                    origen,
                    estado
                FROM animales
                ORDER BY nombre_vulgar
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Animal animal = new Animal();

                animal.setIdAnimal(
                        resultado.getInt("id_animal")
                );

                animal.setIdEspecie(
                        resultado.getInt("id_especie")
                );

                animal.setNombreVulgar(
                        resultado.getString("nombre_vulgar")
                );

                animal.setNombreCientifico(
                        resultado.getString("nombre_cientifico")
                );

                animal.setCantidadActual(
                        resultado.getInt("cantidad_actual")
                );

                animal.setOrigen(
                        resultado.getString("origen")
                );

                animal.setEstado(
                        resultado.getString("estado")
                );

                animales.add(animal);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar animales."
            );

            System.out.println(e.getMessage());
        }

        return animales;
    }
}