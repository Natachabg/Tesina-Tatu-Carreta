package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void agregar(Usuario usuario) {

        String sql = """
                INSERT INTO usuarios (
                    nombre_usuario,
                    usuario,
                    password,
                    rol,
                    estado
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    usuario.getNombreUsuario()
            );

            sentencia.setString(
                    2,
                    usuario.getUsuario()
            );

            sentencia.setString(
                    3,
                    usuario.getPassword()
            );

            sentencia.setString(
                    4,
                    usuario.getRol()
            );

            sentencia.setString(
                    5,
                    usuario.getEstado()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Usuario agregado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar usuario."
            );

            System.out.println(e.getMessage());
        }
    }

    public List<Usuario> listar() {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = """
                SELECT
                    id_usuario,
                    nombre_usuario,
                    usuario,
                    password,
                    rol,
                    estado
                FROM usuarios
                ORDER BY nombre_usuario
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(
                        resultado.getInt("id_usuario")
                );

                usuario.setNombreUsuario(
                        resultado.getString("nombre_usuario")
                );

                usuario.setUsuario(
                        resultado.getString("usuario")
                );

                usuario.setPassword(
                        resultado.getString("password")
                );

                usuario.setRol(
                        resultado.getString("rol")
                );

                usuario.setEstado(
                        resultado.getString("estado")
                );

                usuarios.add(usuario);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar usuarios."
            );

            System.out.println(e.getMessage());
        }

        return usuarios;
    }

    public void modificar(Usuario usuario) {

        String sql = """
                UPDATE usuarios
                SET
                    nombre_usuario = ?,
                    usuario = ?,
                    password = ?,
                    rol = ?,
                    estado = ?
                WHERE id_usuario = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    usuario.getNombreUsuario()
            );

            sentencia.setString(
                    2,
                    usuario.getUsuario()
            );

            sentencia.setString(
                    3,
                    usuario.getPassword()
            );

            sentencia.setString(
                    4,
                    usuario.getRol()
            );

            sentencia.setString(
                    5,
                    usuario.getEstado()
            );

            sentencia.setInt(
                    6,
                    usuario.getIdUsuario()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Usuario modificado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar usuario."
            );

            System.out.println(e.getMessage());
        }
    }

    public void eliminar(int idUsuario) {

        String sql = """
                DELETE FROM usuarios
                WHERE id_usuario = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idUsuario
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Usuario eliminado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar usuario."
            );

            System.out.println(e.getMessage());
        }
    }
    public Usuario iniciarSesion(
        String nombreUsuario,
        String password) {

    String sql = """
            SELECT
                id_usuario,
                nombre_usuario,
                usuario,
                password,
                rol,
                estado
            FROM usuarios
            WHERE usuario = ?
            AND password = ?
            AND estado = 'Activo'
            """;

    try (Connection conexion = ConexionSQLite.conectar();
         PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

        sentencia.setString(1, nombreUsuario);
        sentencia.setString(2, password);

        ResultSet resultado =
                sentencia.executeQuery();

        if (resultado.next()) {

            Usuario usuario = new Usuario();

            usuario.setIdUsuario(
                    resultado.getInt("id_usuario")
            );

            usuario.setNombreUsuario(
                    resultado.getString("nombre_usuario")
            );

            usuario.setUsuario(
                    resultado.getString("usuario")
            );

            usuario.setPassword(
                    resultado.getString("password")
            );

            usuario.setRol(
                    resultado.getString("rol")
            );

            usuario.setEstado(
                    resultado.getString("estado")
            );

            return usuario;
        }

    } catch (Exception e) {

        System.out.println(
                "Error al iniciar sesión."
        );

        System.out.println(e.getMessage());
    }

    return null;
}
}
