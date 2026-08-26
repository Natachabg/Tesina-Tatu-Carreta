package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class IngresoDAO {

    public void agregar(Ingreso ingreso) {

        String sql = """
                INSERT INTO ingresos (
                    numero_acta,
                    fecha_ingreso,
                    organismo_procedencia,
                    responsable_entrega,
                    procedencia,
                    motivo_ingreso,
                    documentacion,
                    observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    ingreso.getNumeroActa()
            );

            sentencia.setString(
                    2,
                    ingreso.getFechaIngreso()
            );

            sentencia.setString(
                    3,
                    ingreso.getOrganismoProcedencia()
            );

            sentencia.setString(
                    4,
                    ingreso.getResponsableEntrega()
            );

            sentencia.setString(
                    5,
                    ingreso.getProcedencia()
            );

            sentencia.setString(
                    6,
                    ingreso.getMotivoIngreso()
            );

            sentencia.setString(
                    7,
                    ingreso.getDocumentacion()
            );

            sentencia.setString(
                    8,
                    ingreso.getObservaciones()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Ingreso agregado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al agregar ingreso."
            );

            System.out.println(e.getMessage());
        }
    }

    public List<Ingreso> listar() {

        List<Ingreso> ingresos = new ArrayList<>();

        String sql = """
                SELECT
                    id_ingreso,
                    numero_acta,
                    fecha_ingreso,
                    organismo_procedencia,
                    responsable_entrega,
                    procedencia,
                    motivo_ingreso,
                    documentacion,
                    observaciones
                FROM ingresos
                ORDER BY id_ingreso DESC
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Ingreso ingreso = new Ingreso();

                ingreso.setIdIngreso(
                        resultado.getInt("id_ingreso")
                );

                ingreso.setNumeroActa(
                        resultado.getString("numero_acta")
                );

                ingreso.setFechaIngreso(
                        resultado.getString("fecha_ingreso")
                );

                ingreso.setOrganismoProcedencia(
                        resultado.getString(
                                "organismo_procedencia"
                        )
                );

                ingreso.setResponsableEntrega(
                        resultado.getString(
                                "responsable_entrega"
                        )
                );

                ingreso.setProcedencia(
                        resultado.getString("procedencia")
                );

                ingreso.setMotivoIngreso(
                        resultado.getString("motivo_ingreso")
                );

                ingreso.setDocumentacion(
                        resultado.getString("documentacion")
                );

                ingreso.setObservaciones(
                        resultado.getString("observaciones")
                );

                ingresos.add(ingreso);
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al listar ingresos."
            );

            System.out.println(e.getMessage());
        }

        return ingresos;
    }

    public void modificar(Ingreso ingreso) {

        String sql = """
                UPDATE ingresos
                SET
                    numero_acta = ?,
                    fecha_ingreso = ?,
                    organismo_procedencia = ?,
                    responsable_entrega = ?,
                    procedencia = ?,
                    motivo_ingreso = ?,
                    documentacion = ?,
                    observaciones = ?
                WHERE id_ingreso = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    ingreso.getNumeroActa()
            );

            sentencia.setString(
                    2,
                    ingreso.getFechaIngreso()
            );

            sentencia.setString(
                    3,
                    ingreso.getOrganismoProcedencia()
            );

            sentencia.setString(
                    4,
                    ingreso.getResponsableEntrega()
            );

            sentencia.setString(
                    5,
                    ingreso.getProcedencia()
            );

            sentencia.setString(
                    6,
                    ingreso.getMotivoIngreso()
            );

            sentencia.setString(
                    7,
                    ingreso.getDocumentacion()
            );

            sentencia.setString(
                    8,
                    ingreso.getObservaciones()
            );

            sentencia.setInt(
                    9,
                    ingreso.getIdIngreso()
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Ingreso modificado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al modificar ingreso."
            );

            System.out.println(e.getMessage());
        }
    }

    public void eliminar(int idIngreso) {

        String sql = """
                DELETE FROM ingresos
                WHERE id_ingreso = ?
                """;

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(
                    1,
                    idIngreso
            );

            sentencia.executeUpdate();

            System.out.println(
                    "Ingreso eliminado correctamente."
            );

        } catch (Exception e) {

            System.out.println(
                    "Error al eliminar ingreso."
            );

            System.out.println(e.getMessage());
        }
    }
    public List<Ingreso> listarPorAnimal(int idAnimal) {

    List<Ingreso> ingresos = new ArrayList<>();

    String sql = """
            SELECT DISTINCT
                i.id_ingreso,
                i.numero_acta,
                i.fecha_ingreso,
                i.organismo_procedencia,
                i.responsable_entrega,
                i.procedencia,
                i.motivo_ingreso,
                i.documentacion,
                i.observaciones
            FROM ingresos i
            INNER JOIN detalle_ingreso d
                ON i.id_ingreso = d.id_ingreso
            WHERE d.id_animal = ?
            ORDER BY i.id_ingreso DESC
            """;

    try (Connection conexion = ConexionSQLite.conectar();
         PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

        sentencia.setInt(1, idAnimal);

        try (ResultSet resultado =
                     sentencia.executeQuery()) {

            while (resultado.next()) {

                Ingreso ingreso = new Ingreso();

                ingreso.setIdIngreso(
                        resultado.getInt("id_ingreso")
                );

                ingreso.setNumeroActa(
                        resultado.getString("numero_acta")
                );

                ingreso.setFechaIngreso(
                        resultado.getString("fecha_ingreso")
                );

                ingreso.setOrganismoProcedencia(
                        resultado.getString(
                                "organismo_procedencia"
                        )
                );

                ingreso.setResponsableEntrega(
                        resultado.getString(
                                "responsable_entrega"
                        )
                );

                ingreso.setProcedencia(
                        resultado.getString("procedencia")
                );

                ingreso.setMotivoIngreso(
                        resultado.getString(
                                "motivo_ingreso"
                        )
                );

                ingreso.setDocumentacion(
                        resultado.getString(
                                "documentacion"
                        )
                );

                ingreso.setObservaciones(
                        resultado.getString(
                                "observaciones"
                        )
                );

                ingresos.add(ingreso);
            }

        }

    } catch (Exception e) {

        System.out.println(
                "Error al listar ingresos por animal."
        );

        System.out.println(e.getMessage());
    }

    return ingresos;
}
}