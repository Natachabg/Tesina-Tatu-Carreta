package com.tatucarreta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    private static final String URL = "jdbc:sqlite:tatu_carreta.db";

    public static Connection conectar() {
        Connection conexion = null;

        try {
            conexion = DriverManager.getConnection(URL);
            System.out.println("Conexión a SQLite exitosa.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con SQLite.");
            System.out.println(e.getMessage());
        }

        return conexion;
    }
}