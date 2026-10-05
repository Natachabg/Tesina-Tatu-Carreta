package com.tatucarreta.data;

import com.tatucarreta.config.ConexionSQLite;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Acceso a datos centralizado. Todas las bajas son lógicas. */
public class SistemaDAO {
    public record Opcion(int id,String texto){ public String toString(){return texto;} }
    public record Fila(int id,List<String> c){}
    private static Connection c(){return ConexionSQLite.conectar();}

    public static List<Fila> filas(String sql,String...ps){
        List<Fila> out=new ArrayList<>();
        try(Connection cn=c();PreparedStatement p=cn.prepareStatement(sql)){
            for(int i=0;i<ps.length;i++)p.setString(i+1,ps[i]);
            try(ResultSet r=p.executeQuery()){
                ResultSetMetaData m=r.getMetaData();
                while(r.next()){
                    List<String> v=new ArrayList<>(); int id=0;
                    for(int i=1;i<=m.getColumnCount();i++){Object x=r.getObject(i);if(i==1&&x instanceof Number)id=((Number)x).intValue();v.add(x==null?"":String.valueOf(x));}
                    out.add(new Fila(id,v));
                }
            }
        }catch(SQLException e){throw new RuntimeException(e);}
        return out;
    }
    public static int update(String sql,Object...ps){
        try(Connection cn=c();PreparedStatement p=cn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            for(int i=0;i<ps.length;i++)p.setObject(i+1,ps[i]); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){return r.next()?r.getInt(1):0;}
        }catch(SQLException e){throw new RuntimeException(e);}
    }
    public static String scalar(String sql){try(Connection cn=c();PreparedStatement p=cn.prepareStatement(sql);ResultSet r=p.executeQuery()){return r.next()?r.getString(1):"0";}catch(SQLException e){throw new RuntimeException(e);}}
    private static boolean existe(String sql,Object...ps){try(Connection cn=c();PreparedStatement p=cn.prepareStatement(sql)){for(int i=0;i<ps.length;i++)p.setObject(i+1,ps[i]);try(ResultSet r=p.executeQuery()){return r.next()&&r.getInt(1)>0;}}catch(SQLException e){throw new RuntimeException(e);}}
    public static List<Opcion> opciones(String sql){List<Opcion>l=new ArrayList<>();try(Connection cn=c();PreparedStatement p=cn.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())l.add(new Opcion(r.getInt(1),r.getString(2)));}catch(SQLException e){throw new RuntimeException(e);}return l;}
    public static String hoy(){return LocalDate.now().toString();}
    public static void baja(String tabla,String pk,int id){update("UPDATE "+tabla+" SET estado='Inactivo' WHERE "+pk+"=?",id);}
    public static void activar(String tabla,String pk,int id){update("UPDATE "+tabla+" SET estado='Activo' WHERE "+pk+"=?",id);}

    // ESPECIES
    public static List<Fila> especies(String q){return filas("SELECT id_especie,nombre,clasificacion,clase,estado_conservacion,estado FROM especies WHERE estado='Activo' AND (nombre LIKE ? OR clasificacion LIKE ? OR clase LIKE ?) ORDER BY nombre","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static List<Fila> especiesInactivas(String q){return filas("SELECT id_especie,nombre,clasificacion,clase,estado_conservacion,estado FROM especies WHERE estado='Inactivo' AND (nombre LIKE ? OR clasificacion LIKE ? OR clase LIKE ?) ORDER BY nombre","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static int guardarEspecie(int id,String n){return guardarEspecie(id,n,"Autóctonos","Sin clasificar","Sin evaluar");}
    public static int guardarEspecie(int id,String n,String clas,String clase){return guardarEspecie(id,n,clas,clase,"Sin evaluar");}
    public static int guardarEspecie(int id,String n,String clas,String clase,String conservacion){
        if(existe("SELECT 1 FROM especies WHERE LOWER(TRIM(nombre))=LOWER(TRIM(?)) AND id_especie<>?",n,id))throw new IllegalArgumentException("Ya existe una especie con ese nombre.");
        if(id==0)return update("INSERT INTO especies(nombre,clasificacion,clase,estado_conservacion,estado) VALUES(?,?,?,?,'Activo')",n,clas,clase,conservacion);
        update("UPDATE especies SET nombre=?,clasificacion=?,clase=?,estado_conservacion=? WHERE id_especie=?",n,clas,clase,conservacion,id);return id;}
    public static boolean especiePuedeEliminar(int id){return Integer.parseInt(scalar("SELECT COUNT(*) FROM animales WHERE id_especie="+id))==0;}

    // ANIMALES
    public static List<Fila> animales(String q){return filas("SELECT a.id_animal,a.nombre_vulgar,COALESCE(e.nombre,''),COALESCE(a.nombre_cientifico,''),a.cantidad_actual,COALESCE(a.origen,''),a.estado FROM animales a LEFT JOIN especies e ON e.id_especie=a.id_especie WHERE a.estado='Activo' AND (a.nombre_vulgar LIKE ? OR COALESCE(e.nombre,'') LIKE ? OR COALESCE(a.nombre_cientifico,'') LIKE ?) ORDER BY a.nombre_vulgar","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static List<Fila> animalesInactivos(String q){return filas("SELECT a.id_animal,a.nombre_vulgar,COALESCE(e.nombre,''),COALESCE(a.nombre_cientifico,''),a.cantidad_actual,COALESCE(a.origen,''),a.estado FROM animales a LEFT JOIN especies e ON e.id_especie=a.id_especie WHERE a.estado='Inactivo' AND (a.nombre_vulgar LIKE ? OR COALESCE(e.nombre,'') LIKE ?) ORDER BY a.nombre_vulgar","%"+q+"%","%"+q+"%");}
    public static int guardarAnimal(int id,int esp,String vul,String cient,String origen){
        if(existe("SELECT 1 FROM animales WHERE estado='Activo' AND id_especie=? AND LOWER(TRIM(nombre_vulgar))=LOWER(TRIM(?)) AND LOWER(TRIM(COALESCE(nombre_cientifico,'')))=LOWER(TRIM(COALESCE(?,''))) AND id_animal<>?",esp,vul,cient,id))throw new IllegalArgumentException("Ya existe ese animal en el catálogo para la especie seleccionada.");
        if(id==0)return update("INSERT INTO animales(id_especie,nombre_vulgar,nombre_cientifico,cantidad_actual,origen,estado) VALUES(?,?,?,0,?,'Activo')",esp,vul,cient,origen);update("UPDATE animales SET id_especie=?,nombre_vulgar=?,nombre_cientifico=?,origen=? WHERE id_animal=?",esp,vul,cient,origen,id);return id;}
    public static boolean animalPuedeEliminar(int id){return Integer.parseInt(scalar("SELECT COUNT(*) FROM detalle_ingreso WHERE id_animal="+id))+Integer.parseInt(scalar("SELECT COUNT(*) FROM plantel_permanente WHERE id_animal="+id))==0;}
    public static List<Opcion> animalesOpciones(){return opciones("SELECT id_animal,nombre_vulgar FROM animales WHERE estado='Activo' ORDER BY nombre_vulgar");}
    public static List<Opcion> especiesOpciones(){return opciones("SELECT id_especie,nombre FROM especies WHERE estado='Activo' ORDER BY nombre");}

    // HABITACULOS
    public static List<Fila> habitaculos(String q){return filas("SELECT id_habitaculo,nombre,COALESCE(sector,''),COALESCE(capacidad,''),estado,COALESCE(observaciones,'') FROM habitaculos WHERE estado='Activo' AND (nombre LIKE ? OR sector LIKE ?) ORDER BY nombre","%"+q+"%","%"+q+"%");}
    public static List<Fila> habitaculosInactivos(String q){return filas("SELECT id_habitaculo,nombre,COALESCE(sector,''),COALESCE(capacidad,''),estado,COALESCE(observaciones,'') FROM habitaculos WHERE estado='Inactivo' AND (nombre LIKE ? OR sector LIKE ?) ORDER BY nombre","%"+q+"%","%"+q+"%");}
    public static int guardarHabitaculo(int id,String n,String s,int cap,String obs){
        if(existe("SELECT 1 FROM habitaculos WHERE estado='Activo' AND LOWER(TRIM(nombre))=LOWER(TRIM(?)) AND LOWER(TRIM(sector))=LOWER(TRIM(?)) AND id_habitaculo<>?",n,s,id))throw new IllegalArgumentException("Ya existe un habitáculo con ese nombre en ese sector.");
        if(id==0)return update("INSERT INTO habitaculos(nombre,sector,capacidad,estado,observaciones) VALUES(?,?,?,'Activo',?)",n,s,cap,obs);update("UPDATE habitaculos SET nombre=?,sector=?,capacidad=?,observaciones=? WHERE id_habitaculo=?",n,s,cap,obs,id);return id;}
    public static boolean habitaculoPuedeEliminar(int id){return Integer.parseInt(scalar("SELECT COUNT(*) FROM plantel_permanente WHERE id_habitaculo="+id))==0;}
    public static List<Opcion> habitaculoOpciones(){return opciones("SELECT id_habitaculo,nombre FROM habitaculos WHERE estado='Activo' ORDER BY nombre");}
    public static int ocupacionHabitaculo(int id,int excluirPlantel){return Integer.parseInt(scalar("SELECT COALESCE(SUM(p.cantidad),0) FROM plantel_permanente p JOIN animales a ON a.id_animal=p.id_animal WHERE p.id_habitaculo="+id+" AND p.estado='Activo' AND p.id_plantel<>"+excluirPlantel));}
    public static int capacidadHabitaculo(int id){return Integer.parseInt(scalar("SELECT COALESCE(capacidad,0) FROM habitaculos WHERE id_habitaculo="+id));}

    // INGRESOS / ACTAS
    public static List<Fila> ingresos(String q){return filas("SELECT id_ingreso,numero_acta,fecha_ingreso,COALESCE(organismo_procedencia,''),COALESCE(responsable_entrega,''),COALESCE(recibe_reserva,''),COALESCE(procedencia,''),COALESCE(motivo_ingreso,''),estado FROM ingresos WHERE estado='Activo' AND (numero_acta LIKE ? OR organismo_procedencia LIKE ? OR procedencia LIKE ?) ORDER BY fecha_ingreso DESC","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static List<Fila> ingresosInactivos(String q){return filas("SELECT id_ingreso,numero_acta,fecha_ingreso,COALESCE(organismo_procedencia,''),COALESCE(responsable_entrega,''),COALESCE(recibe_reserva,''),COALESCE(procedencia,''),COALESCE(motivo_ingreso,''),estado FROM ingresos WHERE estado='Inactivo' AND (numero_acta LIKE ? OR organismo_procedencia LIKE ? OR procedencia LIKE ?) ORDER BY fecha_ingreso DESC","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static int guardarIngreso(int id,String acta,String fecha,String org,String resp,String recibe,String proc,String mot,String obs){
        if(resp==null||resp.isBlank())throw new IllegalArgumentException("El responsable de entrega es obligatorio.");
        if(recibe==null||recibe.isBlank())throw new IllegalArgumentException("La persona que recibe en la Reserva es obligatoria.");
        if(existe("SELECT 1 FROM ingresos WHERE estado='Activo' AND TRIM(numero_acta)=TRIM(?) AND id_ingreso<>?",acta,id))throw new IllegalArgumentException("Ya existe un acta activa con ese número.");
        if(id==0)return update("INSERT INTO ingresos(numero_acta,fecha_ingreso,organismo_procedencia,responsable_entrega,recibe_reserva,procedencia,motivo_ingreso,observaciones,estado) VALUES(?,?,?,?,?,?,?, ?,'Activo')",acta,fecha,org,resp,recibe,proc,mot,obs);update("UPDATE ingresos SET numero_acta=?,fecha_ingreso=?,organismo_procedencia=?,responsable_entrega=?,recibe_reserva=?,procedencia=?,motivo_ingreso=?,observaciones=? WHERE id_ingreso=?",acta,fecha,org,resp,recibe,proc,mot,obs,id);return id;}
    public static boolean ingresoPuedeBaja(int id){return Integer.parseInt(scalar("SELECT COUNT(*) FROM detalle_ingreso WHERE id_ingreso="+id))==0;}
    public static List<Fila> detallesIngreso(int ingreso){return filas("SELECT d.id_detalle,a.nombre_vulgar,d.cantidad,COALESCE(d.sexo,''),COALESCE(d.edad,''),CASE WHEN d.peso IS NULL OR d.peso<=0 THEN '' ELSE printf('%.2f',d.peso) END,COALESCE(d.estado_ingreso,''),d.estado FROM detalle_ingreso d JOIN animales a ON a.id_animal=d.id_animal WHERE d.id_ingreso=? ORDER BY d.id_detalle",String.valueOf(ingreso));}
    public static int guardarDetalle(int id,int ingreso,int animal,int cant,String sexo,String edad,String peso,String estado,String obs){
        if(existe("SELECT 1 FROM detalle_ingreso WHERE estado='Activo' AND id_ingreso=? AND id_animal=? AND id_detalle<>?",ingreso,animal,id))throw new IllegalArgumentException("Ese animal ya está cargado en esta acta. Modificá el registro existente si necesitás cambiar la cantidad.");
        Double pv=peso==null||peso.isBlank()?null:Double.parseDouble(peso);
        if(id==0){int nuevo=update("INSERT INTO detalle_ingreso(id_ingreso,id_animal,cantidad,sexo,edad,peso,estado_ingreso,observaciones,estado) VALUES(?,?,?,?,?,?,?,?, 'Activo')",ingreso,animal,cant,sexo,edad,pv,estado,obs);update("INSERT INTO movimientos(id_detalle,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) SELECT ?,fecha_ingreso,'Ingreso',?,?,?,?,'Activo' FROM ingresos WHERE id_ingreso=?",nuevo,cant,sexo,"Reserva Natural",obs,ingreso);recalcularCantidadAnimal(animal);return nuevo;}
        int oldAnimal=0,oldCant=0;try(Connection cn=c();PreparedStatement p=cn.prepareStatement("SELECT id_animal,cantidad FROM detalle_ingreso WHERE id_detalle=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(r.next()){oldAnimal=r.getInt(1);oldCant=r.getInt(2);}}}catch(SQLException e){throw new RuntimeException(e);}
        update("UPDATE detalle_ingreso SET id_animal=?,cantidad=?,sexo=?,edad=?,peso=?,estado_ingreso=?,observaciones=? WHERE id_detalle=?",animal,cant,sexo,edad,pv,estado,obs,id);
        if(oldAnimal==animal){} else {recalcularCantidadAnimal(oldAnimal);recalcularCantidadAnimal(animal);} recalcularCantidadAnimal(animal); if(oldAnimal!=animal)recalcularCantidadAnimal(oldAnimal); update("UPDATE movimientos SET cantidad=?,sexo=?,observaciones=? WHERE id_detalle=? AND tipo_movimiento='Ingreso' AND estado='Activo'",cant,sexo,obs,id); return id;
    }
    public static boolean puedeDesactivarDetalle(int id){return true;}
    public static void bajaDetalle(int id){int animal=0;try(Connection cn=c();PreparedStatement p=cn.prepareStatement("SELECT id_animal FROM detalle_ingreso WHERE id_detalle=? AND estado='Activo'")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(r.next())animal=r.getInt(1);}}catch(SQLException e){throw new RuntimeException(e);}update("UPDATE detalle_ingreso SET estado='Inactivo' WHERE id_detalle=?",id);update("UPDATE movimientos SET estado='Inactivo' WHERE id_detalle=? AND tipo_movimiento='Ingreso'",id);if(animal>0)recalcularCantidadAnimal(animal);}
    public static List<Opcion> detallesOpciones(){return opciones("SELECT d.id_detalle,a.nombre_vulgar || ' · Acta ' || i.numero_acta FROM detalle_ingreso d JOIN animales a ON a.id_animal=d.id_animal JOIN ingresos i ON i.id_ingreso=d.id_ingreso WHERE d.estado='Activo' AND i.estado='Activo' ORDER BY i.fecha_ingreso DESC");}

    // PLANTEL
    public static List<Fila> plantel(String q){return filas("SELECT p.id_plantel,COALESCE(a.nombre_vulgar,'⚠ Sin animal asociado'),COALESCE((SELECT GROUP_CONCAT(i.tipo_identificacion || ': ' || i.numero_identificacion, ' | ') FROM identificaciones i WHERE i.id_animal=p.id_animal AND i.estado='Activo'),'Sin identificación'),COALESCE(e.nombre,'Sin especie'),p.cantidad,COALESCE(p.tipo_ubicacion,''),COALESCE(h.nombre,''),COALESCE(p.fecha_ingreso_plantel,''),p.estado FROM plantel_permanente p LEFT JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo WHERE p.estado='Activo' AND (COALESCE(a.nombre_vulgar,'') LIKE ? OR COALESCE(e.nombre,'') LIKE ? OR COALESCE(h.nombre,'') LIKE ? OR a.id_animal IS NULL) ORDER BY a.nombre_vulgar","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static List<Fila> plantelInactivos(String q){return filas("SELECT p.id_plantel,COALESCE(a.nombre_vulgar,'⚠ Sin animal asociado'),COALESCE((SELECT GROUP_CONCAT(i.tipo_identificacion || ': ' || i.numero_identificacion, ' | ') FROM identificaciones i WHERE i.id_animal=p.id_animal AND i.estado='Activo'),'Sin identificación'),COALESCE(e.nombre,'Sin especie'),p.cantidad,COALESCE(p.tipo_ubicacion,''),COALESCE(h.nombre,''),COALESCE(p.fecha_ingreso_plantel,''),p.estado FROM plantel_permanente p LEFT JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo WHERE p.estado='Inactivo' AND (COALESCE(a.nombre_vulgar,'') LIKE ? OR COALESCE(e.nombre,'') LIKE ? OR COALESCE(h.nombre,'') LIKE ? OR a.id_animal IS NULL) ORDER BY a.nombre_vulgar","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static int guardarPlantel(int id,int animal,int cantidad,String tipo,Integer hab,String fecha,String obs){
        if(hab!=null){int ocup=ocupacionHabitaculo(hab,id);int cap=capacidadHabitaculo(hab);if(cap>0&&ocup+cantidad>cap)throw new IllegalArgumentException("El habitáculo seleccionado tiene capacidad para "+cap+" ejemplares y ya hay "+ocup+" asignados. No se puede superar la capacidad.");}
        if(id==0){int nuevo=update("INSERT INTO plantel_permanente(id_animal,cantidad,tipo_ubicacion,id_habitaculo,fecha_ingreso_plantel,estado,observaciones) VALUES(?,?,?,?,?,'Activo',?)",animal,cantidad,tipo,hab,fecha,obs);recalcularCantidadAnimal(animal);return nuevo;}
        int viejoAnimal=Integer.parseInt(scalar("SELECT id_animal FROM plantel_permanente WHERE id_plantel="+id));
        update("UPDATE plantel_permanente SET id_animal=?,cantidad=?,tipo_ubicacion=?,id_habitaculo=?,fecha_ingreso_plantel=?,observaciones=? WHERE id_plantel=?",animal,cantidad,tipo,hab,fecha,obs,id);recalcularCantidadAnimal(viejoAnimal);recalcularCantidadAnimal(animal);return id;
    }
    private static void recalcularCantidadAnimal(int animal){
        int plantel=Integer.parseInt(scalar("SELECT COALESCE(SUM(cantidad),0) FROM plantel_permanente WHERE id_animal="+animal+" AND estado='Activo'"));
        int ingresos=Integer.parseInt(scalar("SELECT COALESCE(SUM(cantidad),0) FROM detalle_ingreso WHERE id_animal="+animal+" AND estado='Activo'"));
        int total=plantel>0?plantel:ingresos;
        update("UPDATE animales SET cantidad_actual=?,estado=CASE WHEN ?<=0 THEN 'Activo' ELSE 'Activo' END WHERE id_animal=?",total,total,animal);
    }
    public static int animalPorPlantel(int id){return Integer.parseInt(scalar("SELECT id_animal FROM plantel_permanente WHERE id_plantel="+id));}
    public static int cantidadPlantel(int id){return Integer.parseInt(scalar("SELECT COALESCE(cantidad,0) FROM plantel_permanente WHERE id_plantel="+id));}
    public static void trasladarPlantel(int plantel,int cantidad,String fecha,String sexo,String destino,String observaciones){
        if(cantidad<=0)throw new IllegalArgumentException("La cantidad a trasladar debe ser mayor que cero.");
        int actual=cantidadPlantel(plantel);
        if(actual<=0)throw new IllegalArgumentException("El ejemplar seleccionado ya no tiene existencia activa en el Plantel.");
        if(cantidad>actual)throw new IllegalArgumentException("La cantidad no puede superar la existencia actual ("+actual+").");
        if(destino==null||destino.isBlank())throw new IllegalArgumentException("Indicá el centro o institución de destino.");
        int animal=animalPorPlantel(plantel);
        update("INSERT INTO movimientos(id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) VALUES(?,?,?,?,?,?,?, 'Activo')",plantel,fecha,"Traslado",cantidad,sexo,destino.trim(),observaciones==null?"":observaciones.trim());
        update("UPDATE plantel_permanente SET cantidad=MAX(cantidad-?,0),estado=CASE WHEN cantidad-?<=0 THEN 'Inactivo' ELSE estado END WHERE id_plantel=?",cantidad,cantidad,plantel);
        recalcularCantidadAnimal(animal);
    }
    public static List<Fila> resumenActa(int ingreso){
        return filas("SELECT d.id_detalle,a.nombre_vulgar,d.cantidad,COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p WHERE p.id_animal=d.id_animal AND p.estado='Activo'),0),COALESCE((SELECT SUM(m.cantidad) FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE p.id_animal=d.id_animal AND m.tipo_movimiento='Fallecimiento' AND m.estado='Activo'),0),COALESCE((SELECT SUM(m.cantidad) FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE p.id_animal=d.id_animal AND m.tipo_movimiento='Traslado' AND m.estado='Activo'),0),COALESCE((SELECT GROUP_CONCAT(DISTINCT m.destino) FROM movimientos m LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE p.id_animal=d.id_animal AND m.tipo_movimiento='Traslado' AND m.estado='Activo' AND COALESCE(m.destino,'')<>''),'') FROM detalle_ingreso d JOIN animales a ON a.id_animal=d.id_animal WHERE d.id_ingreso=? AND d.estado='Activo' ORDER BY a.nombre_vulgar",String.valueOf(ingreso));
    }
    public static void bajaPlantel(int id){int animal=animalPorPlantel(id);baja("plantel_permanente","id_plantel",id);recalcularCantidadAnimal(animal);}
    public static List<Opcion> plantelOpciones(){return opciones("SELECT p.id_plantel,a.nombre_vulgar || ' · ' || COALESCE(e.nombre,'') || ' · ' || p.cantidad FROM plantel_permanente p JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie WHERE p.estado='Activo' AND p.cantidad>0 ORDER BY a.nombre_vulgar");}

    // MOVIMIENTOS
    public static List<Fila> movimientos(String q){return filas("SELECT m.id_movimiento,COALESCE(a.nombre_vulgar,'Plantel'),COALESCE(i.numero_acta,''),m.fecha_movimiento,m.tipo_movimiento,m.cantidad,COALESCE(m.sexo,''),COALESCE((SELECT GROUP_CONCAT(ix.tipo_identificacion || ': ' || ix.numero_identificacion, ' | ') FROM identificaciones ix WHERE ix.id_animal=a.id_animal AND ix.estado='Activo'),'Sin identificación'),COALESCE(m.destino,''),m.estado FROM movimientos m LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle LEFT JOIN ingresos i ON i.id_ingreso=d.id_ingreso LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel LEFT JOIN animales a ON a.id_animal=COALESCE(d.id_animal,p.id_animal) WHERE m.estado='Activo' AND (COALESCE(a.nombre_vulgar,'') LIKE ? OR COALESCE(i.numero_acta,'') LIKE ? OR m.tipo_movimiento LIKE ? OR COALESCE(m.destino,'') LIKE ?) ORDER BY m.fecha_movimiento DESC","%"+q+"%","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static List<Fila> movimientosInactivos(String q){return filas("SELECT m.id_movimiento,COALESCE(a.nombre_vulgar,'Plantel'),COALESCE(i.numero_acta,''),m.fecha_movimiento,m.tipo_movimiento,m.cantidad,COALESCE(m.sexo,''),COALESCE((SELECT GROUP_CONCAT(ix.tipo_identificacion || ': ' || ix.numero_identificacion, ' | ') FROM identificaciones ix WHERE ix.id_animal=a.id_animal AND ix.estado='Activo'),'Sin identificación'),COALESCE(m.destino,''),m.estado FROM movimientos m LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle LEFT JOIN ingresos i ON i.id_ingreso=d.id_ingreso LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel LEFT JOIN animales a ON a.id_animal=COALESCE(d.id_animal,p.id_animal) WHERE m.estado='Inactivo' AND (COALESCE(a.nombre_vulgar,'') LIKE ? OR COALESCE(i.numero_acta,'') LIKE ? OR m.tipo_movimiento LIKE ?) ORDER BY m.fecha_movimiento DESC","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static int ingresoDeMovimiento(int movimiento){return Integer.parseInt(scalar("SELECT COALESCE(i.id_ingreso,0) FROM movimientos m LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle LEFT JOIN ingresos i ON i.id_ingreso=d.id_ingreso WHERE m.id_movimiento="+movimiento));}
    public static int guardarMovimiento(int id,Integer detalle,Integer plantel,String fecha,String tipo,int cant,String sexo,String destino,String obs){if(id==0)return update("INSERT INTO movimientos(id_detalle,id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) VALUES(?,?,?,?,?,?,?,?, 'Activo')",detalle,plantel,fecha,tipo,cant,sexo,destino,obs);update("UPDATE movimientos SET id_detalle=?,id_plantel=?,fecha_movimiento=?,tipo_movimiento=?,cantidad=?,sexo=?,destino=?,observaciones=? WHERE id_movimiento=?",detalle,plantel,fecha,tipo,cant,sexo,destino,obs,id);return id;}

    // USUARIOS
    public static List<Fila> usuarios(String q){return filas("SELECT id_usuario,nombre_usuario,usuario,rol,estado FROM usuarios WHERE estado='Activo' AND (nombre_usuario LIKE ? OR usuario LIKE ? OR rol LIKE ?) ORDER BY nombre_usuario","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static List<Fila> usuariosInactivos(String q){return filas("SELECT id_usuario,nombre_usuario,usuario,rol,estado FROM usuarios WHERE estado='Inactivo' AND (nombre_usuario LIKE ? OR usuario LIKE ? OR rol LIKE ?) ORDER BY nombre_usuario","%"+q+"%","%"+q+"%","%"+q+"%");}
    public static int guardarUsuario(int id,String n,String u,String p,String rol){if(id==0)return update("INSERT INTO usuarios(nombre_usuario,usuario,password,rol,estado) VALUES(?,?,?,?, 'Activo')",n,u,p,rol);if(p==null||p.isBlank())update("UPDATE usuarios SET nombre_usuario=?,usuario=?,rol=? WHERE id_usuario=?",n,u,rol,id);else update("UPDATE usuarios SET nombre_usuario=?,usuario=?,password=?,rol=? WHERE id_usuario=?",n,u,p,rol,id);return id;}
    public static boolean puedeDesactivarUsuario(int id){String u=scalar("SELECT usuario FROM usuarios WHERE id_usuario="+id);if("admin".equalsIgnoreCase(u))return false;return true;}

    // IDENTIFICACIONES
    public static List<Fila> identificaciones(int animal){return filas("SELECT id_identificacion,tipo_identificacion,numero_identificacion,COALESCE(fecha_identificacion,''),COALESCE(observaciones,''),estado FROM identificaciones WHERE id_animal=? AND estado='Activo' ORDER BY tipo_identificacion",String.valueOf(animal));}
    public static void guardarIdentificacion(int id,int animal,String tipo,String numero,String fecha,String obs){
        if(existe("SELECT 1 FROM identificaciones WHERE estado='Activo' AND id_animal=? AND LOWER(TRIM(tipo_identificacion))=LOWER(TRIM(?)) AND LOWER(TRIM(numero_identificacion))=LOWER(TRIM(?)) AND id_identificacion<>?",animal,tipo,numero,id))throw new IllegalArgumentException("Esa identificación ya está registrada para este animal.");
        if(id==0)update("INSERT INTO identificaciones(id_animal,tipo_identificacion,numero_identificacion,fecha_identificacion,observaciones,estado) VALUES(?,?,?,?,?,'Activo')",animal,tipo,numero,fecha,obs);else update("UPDATE identificaciones SET tipo_identificacion=?,numero_identificacion=?,fecha_identificacion=?,observaciones=? WHERE id_identificacion=?",tipo,numero,fecha,obs,id);}
    public static void bajaIdentificacion(int id){baja("identificaciones","id_identificacion",id);}

    // NACIMIENTOS / FALLECIMIENTOS
    public static void registrarNacimiento(int plantel,int animal,int cant,String sexo,String fecha,String obs){
        int hab=0; try{hab=Integer.parseInt(scalar("SELECT COALESCE(id_habitaculo,0) FROM plantel_permanente WHERE id_plantel="+plantel));}catch(Exception ignored){}
        if(hab>0){int ocup=ocupacionHabitaculo(hab,plantel),cap=capacidadHabitaculo(hab);if(cap>0&&ocup+cant>cap)throw new IllegalArgumentException("El nacimiento supera la capacidad del habitáculo. Capacidad: "+cap+" · ocupación actual: "+ocup+".");}
        update("INSERT INTO nacimientos(id_animal,cantidad,sexo,fecha_nacimiento,id_habitaculo,observaciones,estado) VALUES(?,?,?,?,?,?, 'Activo')",animal,cant,sexo,fecha,hab==0?null:hab,obs);
        update("UPDATE plantel_permanente SET cantidad=cantidad+? WHERE id_plantel=? AND estado='Activo'",cant,plantel);
        update("INSERT INTO movimientos(id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) VALUES(?,?,?,?,?,?,?, 'Activo')",plantel,fecha,"Nacimiento",cant,sexo,"Plantel Permanente",obs);
        recalcularCantidadAnimal(animal);
    }
    public static void registrarFallecimiento(int plantel,int animal,int cant,String sexo,String fecha,String obs){
        update("INSERT INTO movimientos(id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) VALUES(?,?,?,?,?,?,?, 'Activo')",plantel,fecha,"Fallecimiento",cant,sexo,"Baja por fallecimiento",obs);
        update("UPDATE plantel_permanente SET cantidad=MAX(cantidad-?,0),estado=CASE WHEN cantidad-?<=0 THEN 'Inactivo' ELSE estado END WHERE id_plantel=?",cant,cant,plantel);
        recalcularCantidadAnimal(animal);
    }

    // DASHBOARD
    public static int ejemplaresPlantel(){return Integer.parseInt(scalar("SELECT COALESCE(SUM(p.cantidad),0) FROM plantel_permanente p WHERE p.estado='Activo' AND p.cantidad>0"));}
    public static int animalesCatalogados(){return Integer.parseInt(scalar("SELECT COUNT(*) FROM animales WHERE estado='Activo'"));}
    public static int especiesActivas(){return Integer.parseInt(scalar("SELECT COUNT(*) FROM especies WHERE estado='Activo'"));}
    public static int actasActivas(){return Integer.parseInt(scalar("SELECT COUNT(*) FROM ingresos WHERE estado='Activo'"));}
    public static int habitaculosActivos(){return Integer.parseInt(scalar("SELECT COUNT(*) FROM habitaculos WHERE estado='Activo'"));}
    public static int capacidadTotal(){return Integer.parseInt(scalar("SELECT COALESCE(SUM(capacidad),0) FROM habitaculos WHERE estado='Activo'"));}
    public static int ocupacionTotal(){return Integer.parseInt(scalar("SELECT COALESCE(SUM(p.cantidad),0) FROM plantel_permanente p WHERE p.estado='Activo' AND p.id_habitaculo IS NOT NULL"));}


    public static int movimientosPeriodo(int animal,String inicio,String fin,String tipo){
        String t=tipo.replace("'","''");
        String sql="SELECT COALESCE(SUM(m.cantidad),0) FROM movimientos m LEFT JOIN detalle_ingreso d ON d.id_detalle=m.id_detalle LEFT JOIN plantel_permanente p ON p.id_plantel=m.id_plantel WHERE (d.id_animal="+animal+" OR p.id_animal="+animal+") AND m.tipo_movimiento='"+t+"' AND m.estado='Activo' AND m.fecha_movimiento>='"+inicio+"' AND m.fecha_movimiento<='"+fin+"'";
        return Integer.parseInt(scalar(sql));
    }
    public static void guardarSnapshot(String periodo,String fecha,List<Fila> filas){
        int rid=update("INSERT INTO reportes_plantel(periodo,fecha_generacion) VALUES(?,?) ON CONFLICT(periodo) DO UPDATE SET fecha_generacion=excluded.fecha_generacion",periodo,fecha);
        rid=Integer.parseInt(scalar("SELECT id_reporte FROM reportes_plantel WHERE periodo='"+periodo.replace("'","''")+"'"));
        update("DELETE FROM reportes_plantel_detalle WHERE id_reporte=?",rid);
        for(Fila f:filas){List<String> c=f.c();update("INSERT INTO reportes_plantel_detalle(id_reporte,id_animal,nombre_vulgar,nombre_cientifico,especie,clase,clasificacion,cantidad) VALUES(?,?,?,?,?,?,?,?)",rid,f.id(),get(c,1),get(c,2),get(c,3),get(c,4),get(c,5),toInt(get(c,6)));}
    }
    public static int snapshotAnterior(int animal){return Integer.parseInt(scalar("SELECT COALESCE((SELECT d.cantidad FROM reportes_plantel_detalle d JOIN reportes_plantel r ON r.id_reporte=d.id_reporte WHERE d.id_animal="+animal+" ORDER BY r.fecha_generacion DESC LIMIT 1),-1)"));}
    private static String get(List<String> c,int i){return c.size()>i&&c.get(i)!=null?c.get(i):"";}
    private static int toInt(String s){try{return Integer.parseInt(s);}catch(Exception e){return 0;}}

    // REPORTE
    public static List<Fila> reportePlantelMensual(){return filas("SELECT p.id_plantel,a.nombre_vulgar,COALESCE(a.nombre_cientifico,''),COALESCE(e.nombre,''),COALESCE(e.clase,'Sin clasificar'),COALESCE(e.clasificacion,'Autóctonos'),p.cantidad,COALESCE(h.nombre,''),COALESCE(p.tipo_ubicacion,''),COALESCE(p.fecha_ingreso_plantel,''),COALESCE(p.observaciones,''),COALESCE(a.origen,'') FROM plantel_permanente p JOIN animales a ON a.id_animal=p.id_animal LEFT JOIN especies e ON e.id_especie=a.id_especie LEFT JOIN habitaculos h ON h.id_habitaculo=p.id_habitaculo WHERE p.estado='Activo' AND p.cantidad>0 ORDER BY e.clasificacion,e.clase,e.nombre,a.nombre_vulgar");}
}
