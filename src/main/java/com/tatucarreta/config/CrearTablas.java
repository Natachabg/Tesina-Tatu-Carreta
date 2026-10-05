package com.tatucarreta.config;

import java.sql.*;

/** Inicialización y migración segura de la base SQLite. Nunca elimina información del usuario. */
public final class CrearTablas {
    private CrearTablas() {}

    public static void crearTablas() {
        try (Connection c=ConexionSQLite.conectar(); Statement s=c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("CREATE TABLE IF NOT EXISTS usuarios (id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,nombre_usuario TEXT NOT NULL,usuario TEXT NOT NULL UNIQUE,password TEXT NOT NULL,rol TEXT NOT NULL DEFAULT 'Usuario',estado TEXT NOT NULL DEFAULT 'Activo')");
            s.execute("CREATE TABLE IF NOT EXISTS especies (id_especie INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT NOT NULL UNIQUE,clasificacion TEXT NOT NULL DEFAULT 'Autóctonos',clase TEXT NOT NULL DEFAULT 'Sin clasificar',estado_conservacion TEXT NOT NULL DEFAULT 'Sin evaluar',estado TEXT NOT NULL DEFAULT 'Activo')");
            s.execute("CREATE TABLE IF NOT EXISTS animales (id_animal INTEGER PRIMARY KEY AUTOINCREMENT,id_especie INTEGER NOT NULL,nombre_vulgar TEXT NOT NULL,nombre_cientifico TEXT,cantidad_actual INTEGER NOT NULL DEFAULT 0,origen TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_especie) REFERENCES especies(id_especie))");
            s.execute("CREATE TABLE IF NOT EXISTS habitaculos (id_habitaculo INTEGER PRIMARY KEY AUTOINCREMENT,nombre TEXT NOT NULL,sector TEXT NOT NULL,capacidad INTEGER NOT NULL,estado TEXT NOT NULL DEFAULT 'Activo',observaciones TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS ingresos (id_ingreso INTEGER PRIMARY KEY AUTOINCREMENT,numero_acta TEXT NOT NULL,fecha_ingreso TEXT NOT NULL,organismo_procedencia TEXT, responsable_entrega TEXT,recibe_reserva TEXT,procedencia TEXT,motivo_ingreso TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo')");
            s.execute("CREATE TABLE IF NOT EXISTS detalle_ingreso (id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,id_ingreso INTEGER NOT NULL,id_animal INTEGER NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,edad TEXT,peso REAL,estado_ingreso TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_ingreso) REFERENCES ingresos(id_ingreso),FOREIGN KEY(id_animal) REFERENCES animales(id_animal))");
            s.execute("CREATE TABLE IF NOT EXISTS plantel_permanente (id_plantel INTEGER PRIMARY KEY AUTOINCREMENT,id_animal INTEGER NOT NULL,cantidad INTEGER NOT NULL DEFAULT 1,tipo_ubicacion TEXT,id_habitaculo INTEGER,fecha_ingreso_plantel TEXT,estado TEXT NOT NULL DEFAULT 'Activo',observaciones TEXT,FOREIGN KEY(id_animal) REFERENCES animales(id_animal),FOREIGN KEY(id_habitaculo) REFERENCES habitaculos(id_habitaculo))");
            s.execute("CREATE TABLE IF NOT EXISTS identificaciones (id_identificacion INTEGER PRIMARY KEY AUTOINCREMENT,id_animal INTEGER NOT NULL,tipo_identificacion TEXT NOT NULL,numero_identificacion TEXT NOT NULL,fecha_identificacion TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_animal) REFERENCES animales(id_animal))");
            s.execute("CREATE TABLE IF NOT EXISTS movimientos (id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,id_detalle INTEGER,id_plantel INTEGER,fecha_movimiento TEXT NOT NULL,tipo_movimiento TEXT NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,destino TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_detalle) REFERENCES detalle_ingreso(id_detalle),FOREIGN KEY(id_plantel) REFERENCES plantel_permanente(id_plantel))");
            s.execute("CREATE TABLE IF NOT EXISTS nacimientos (id_nacimiento INTEGER PRIMARY KEY AUTOINCREMENT,id_animal INTEGER NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,fecha_nacimiento TEXT NOT NULL,id_habitaculo INTEGER,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_animal) REFERENCES animales(id_animal),FOREIGN KEY(id_habitaculo) REFERENCES habitaculos(id_habitaculo))");
            s.execute("CREATE TABLE IF NOT EXISTS papelera (id_papelera INTEGER PRIMARY KEY AUTOINCREMENT,tipo_registro TEXT NOT NULL,id_registro INTEGER NOT NULL,datos TEXT NOT NULL,fecha_eliminacion TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS reportes_plantel (id_reporte INTEGER PRIMARY KEY AUTOINCREMENT,periodo TEXT NOT NULL,fecha_generacion TEXT NOT NULL,UNIQUE(periodo))");
            s.execute("CREATE TABLE IF NOT EXISTS reportes_plantel_detalle (id_detalle_reporte INTEGER PRIMARY KEY AUTOINCREMENT,id_reporte INTEGER NOT NULL,id_animal INTEGER,nombre_vulgar TEXT,nombre_cientifico TEXT,especie TEXT,clase TEXT,clasificacion TEXT,cantidad INTEGER,FOREIGN KEY(id_reporte) REFERENCES reportes_plantel(id_reporte))");

            ensureColumn(c,"usuarios","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"especies","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"especies","clasificacion","TEXT NOT NULL DEFAULT 'Autóctonos'");
            ensureColumn(c,"especies","clase","TEXT NOT NULL DEFAULT 'Sin clasificar'");
            ensureColumn(c,"especies","estado_conservacion","TEXT NOT NULL DEFAULT 'Sin evaluar'");
            ensureColumn(c,"animales","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"habitaculos","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"ingresos","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"ingresos","recibe_reserva","TEXT");
            ensureColumn(c,"detalle_ingreso","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"plantel_permanente","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"identificaciones","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"identificaciones","fecha_identificacion","TEXT");
            if (tieneColumna(c,"identificaciones","fecha_asignacion")) s.execute("UPDATE identificaciones SET fecha_identificacion=COALESCE(fecha_identificacion,fecha_asignacion)");
            ensureColumn(c,"movimientos","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"movimientos","sexo","TEXT");
            s.execute("UPDATE movimientos SET tipo_movimiento='Traslado' WHERE LOWER(TRIM(tipo_movimiento))='egreso'");
            ensureColumn(c,"nacimientos","estado","TEXT NOT NULL DEFAULT 'Activo'");
            ensureColumn(c,"nacimientos","sexo","TEXT");

            if (columnaEsNotNull(c,"movimientos","id_detalle")) migrarMovimientos(c);

            s.execute("CREATE INDEX IF NOT EXISTS idx_animales_estado ON animales(estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_ingresos_estado ON ingresos(estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_movimientos_estado ON movimientos(estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_plantel_estado ON plantel_permanente(estado)");
            normalizarAdministrador(c);
            migrarTablasDuplicadas(c);
            normalizarReferenciasPlantel(c);
            deduplicarRegistrosActivos(c);
            crearRestriccionesUnicasActivas(c);
            normalizarCantidades(c);
        } catch(SQLException e){throw new RuntimeException("No se pudo inicializar la base de datos: "+e.getMessage(),e);}
    }

    /** Unifica tablas heredadas que quedaron de versiones anteriores. No se mantienen copias operativas. */
    private static void migrarTablasDuplicadas(Connection c) throws SQLException {
        try (Statement s=c.createStatement()) {
            // identificaciones_plantel es una tabla antigua. Sus datos pasan a la tabla canónica.
            if (tablaExiste(c,"identificaciones_plantel")) {
                try (ResultSet r=s.executeQuery("SELECT id_animal,tipo_identificacion,numero_identificacion,fecha_identificacion,observaciones FROM identificaciones_plantel")) {
                    while(r.next()) {
                        int animal=r.getInt(1); String tipo=r.getString(2); String numero=r.getString(3);
                        if(!existe(c,"SELECT 1 FROM identificaciones WHERE id_animal=? AND LOWER(TRIM(tipo_identificacion))=LOWER(TRIM(?)) AND LOWER(TRIM(numero_identificacion))=LOWER(TRIM(?))",animal,tipo,numero)) {
                            try(PreparedStatement p=c.prepareStatement("INSERT INTO identificaciones(id_animal,tipo_identificacion,numero_identificacion,fecha_identificacion,observaciones,estado) VALUES(?,?,?,?,?,'Activo')")) {
                                p.setInt(1,animal); p.setString(2,tipo); p.setString(3,numero); p.setString(4,r.getString(4)); p.setString(5,r.getString(5)); p.executeUpdate();
                            }
                        }
                    }
                }
                s.execute("DROP TABLE identificaciones_plantel");
            }
            if (tablaExiste(c,"identificaciones_nueva")) s.execute("DROP TABLE identificaciones_nueva");
            if (tablaExiste(c,"plantel_permanente_nueva")) s.execute("DROP TABLE plantel_permanente_nueva");
        }
    }
    private static boolean tablaExiste(Connection c,String tabla) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")){p.setString(1,tabla);try(ResultSet r=p.executeQuery()){return r.next();}}
    }
    private static boolean existe(Connection c,String sql,Object...ps) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<ps.length;i++)p.setObject(i+1,ps[i]);try(ResultSet r=p.executeQuery()){return r.next();}}
    }

    private static boolean columnaEsNotNull(Connection c,String table,String column)throws SQLException{try(PreparedStatement p=c.prepareStatement("PRAGMA table_info("+table+")");ResultSet r=p.executeQuery()){while(r.next())if(column.equalsIgnoreCase(r.getString("name")))return r.getInt("notnull")==1;}return false;}
    private static void migrarMovimientos(Connection c)throws SQLException{
        boolean estado=tieneColumna(c,"movimientos","estado"), sexo=tieneColumna(c,"movimientos","sexo");
        String es=estado?"estado":"'Activo'", sx=sexo?"sexo":"NULL";
        try(Statement s=c.createStatement()){
            s.execute("PRAGMA foreign_keys=OFF");
            s.execute("CREATE TABLE movimientos_nuevo (id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,id_detalle INTEGER,id_plantel INTEGER,fecha_movimiento TEXT NOT NULL,tipo_movimiento TEXT NOT NULL,cantidad INTEGER NOT NULL,sexo TEXT,destino TEXT,observaciones TEXT,estado TEXT NOT NULL DEFAULT 'Activo',FOREIGN KEY(id_detalle) REFERENCES detalle_ingreso(id_detalle),FOREIGN KEY(id_plantel) REFERENCES plantel_permanente(id_plantel))");
            s.execute("INSERT INTO movimientos_nuevo(id_movimiento,id_detalle,id_plantel,fecha_movimiento,tipo_movimiento,cantidad,sexo,destino,observaciones,estado) SELECT id_movimiento,id_detalle,id_plantel,fecha_movimiento,tipo_movimiento,cantidad,"+sx+",destino,observaciones,"+es+" FROM movimientos");
            s.execute("DROP TABLE movimientos"); s.execute("ALTER TABLE movimientos_nuevo RENAME TO movimientos"); s.execute("CREATE INDEX IF NOT EXISTS idx_movimientos_estado ON movimientos(estado)");
        }finally{try(Statement s=c.createStatement()){s.execute("PRAGMA foreign_keys=ON");}}
    }
    private static boolean tieneColumna(Connection c,String table,String column)throws SQLException{try(PreparedStatement p=c.prepareStatement("PRAGMA table_info("+table+")");ResultSet r=p.executeQuery()){while(r.next())if(column.equalsIgnoreCase(r.getString("name")))return true;}return false;}
    private static void ensureColumn(Connection c,String table,String column,String definition)throws SQLException{if(tieneColumna(c,table,column))return;try(Statement s=c.createStatement()){s.execute("ALTER TABLE "+table+" ADD COLUMN "+column+" "+definition);}}
    /** Conserva como inactivos los registros históricos de Plantel que ya no tienen animal maestro asociado. */
    private static void normalizarReferenciasPlantel(Connection c) throws SQLException {
        try(Statement s=c.createStatement()){
            s.executeUpdate("UPDATE plantel_permanente SET estado='Inactivo',observaciones=CASE WHEN trim(COALESCE(observaciones,''))='' THEN 'Registro histórico sin animal asociado; conservado como inactivo.' ELSE observaciones END WHERE estado='Activo' AND id_animal NOT IN (SELECT id_animal FROM animales)");
        }
    }

    /**
     * Evita que una misma carga operativa quede registrada dos veces.
     * No se borran filas: los duplicados activos pasan a Inactivo y se conserva su historial.
     */
    private static void deduplicarRegistrosActivos(Connection c)throws SQLException{
        try(Statement s=c.createStatement()){
            // Un acta es única por número. Se conserva la primera registrada.
            s.executeUpdate("UPDATE ingresos SET estado='Inactivo' WHERE estado='Activo' AND id_ingreso NOT IN (SELECT MIN(id_ingreso) FROM ingresos WHERE estado='Activo' GROUP BY TRIM(numero_acta))");

            // Un animal de catálogo se identifica por especie + nombre vulgar + nombre científico.
            s.executeUpdate("UPDATE animales SET estado='Inactivo' WHERE estado='Activo' AND id_animal NOT IN (SELECT MIN(id_animal) FROM animales WHERE estado='Activo' GROUP BY id_especie,LOWER(TRIM(nombre_vulgar)),LOWER(TRIM(COALESCE(nombre_cientifico,''))))");

            // Habitáculos: mismo nombre dentro del mismo sector no se duplica.
            s.executeUpdate("UPDATE habitaculos SET estado='Inactivo' WHERE estado='Activo' AND id_habitaculo NOT IN (SELECT MIN(id_habitaculo) FROM habitaculos WHERE estado='Activo' GROUP BY LOWER(TRIM(nombre)),LOWER(TRIM(sector)))");

            // Identificaciones: mismo animal + tipo + número no se registra dos veces.
            s.executeUpdate("UPDATE identificaciones SET estado='Inactivo' WHERE estado='Activo' AND id_identificacion NOT IN (SELECT MIN(id_identificacion) FROM identificaciones WHERE estado='Activo' GROUP BY id_animal,LOWER(TRIM(tipo_identificacion)),LOWER(TRIM(numero_identificacion)))");

            // Detalle repetido dentro de una misma acta: se consolidan las cantidades.
            try(ResultSet r=s.executeQuery("SELECT id_ingreso,id_animal,MIN(id_detalle) AS conservar,SUM(cantidad) AS total,COUNT(*) AS n FROM detalle_ingreso WHERE estado='Activo' GROUP BY id_ingreso,id_animal HAVING COUNT(*)>1")){
                while(r.next()){
                    int ingreso=r.getInt(1), animal=r.getInt(2), conservar=r.getInt(3), total=r.getInt(4);
                    try(PreparedStatement u=c.prepareStatement("UPDATE detalle_ingreso SET cantidad=? WHERE id_detalle=?")){u.setInt(1,total);u.setInt(2,conservar);u.executeUpdate();}
                    try(PreparedStatement b=c.prepareStatement("UPDATE detalle_ingreso SET estado='Inactivo' WHERE id_ingreso=? AND id_animal=? AND estado='Activo' AND id_detalle<>?")){b.setInt(1,ingreso);b.setInt(2,animal);b.setInt(3,conservar);b.executeUpdate();}
                }
            }

            // Plantel repetido para el mismo animal y ubicación: se consolidan cantidades.
            try(ResultSet r=s.executeQuery("SELECT id_animal,COALESCE(id_habitaculo,0),LOWER(TRIM(COALESCE(tipo_ubicacion,''))),MIN(id_plantel) AS conservar,SUM(cantidad) AS total,COUNT(*) AS n FROM plantel_permanente WHERE estado='Activo' GROUP BY id_animal,COALESCE(id_habitaculo,0),LOWER(TRIM(COALESCE(tipo_ubicacion,''))) HAVING COUNT(*)>1")){
                while(r.next()){
                    int animal=r.getInt(1), hab=r.getInt(2), conservar=r.getInt(4), total=r.getInt(5);
                    try(PreparedStatement u=c.prepareStatement("UPDATE plantel_permanente SET cantidad=? WHERE id_plantel=?")){u.setInt(1,total);u.setInt(2,conservar);u.executeUpdate();}
                    try(PreparedStatement b=c.prepareStatement("UPDATE plantel_permanente SET estado='Inactivo' WHERE id_animal=? AND COALESCE(id_habitaculo,0)=? AND LOWER(TRIM(COALESCE(tipo_ubicacion,'')))=(SELECT LOWER(TRIM(COALESCE(tipo_ubicacion,''))) FROM plantel_permanente WHERE id_plantel=?) AND estado='Activo' AND id_plantel<>?")){b.setInt(1,animal);b.setInt(2,hab);b.setInt(3,conservar);b.setInt(4,conservar);b.executeUpdate();}
                }
            }
        }
    }

    private static void crearRestriccionesUnicasActivas(Connection c)throws SQLException{
        try(Statement s=c.createStatement()){
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_ingreso_acta_activo ON ingresos(TRIM(numero_acta)) WHERE estado='Activo'");
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_animal_catalogo_activo ON animales(id_especie,LOWER(TRIM(nombre_vulgar)),LOWER(TRIM(COALESCE(nombre_cientifico,'')))) WHERE estado='Activo'");
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_habitaculo_activo ON habitaculos(LOWER(TRIM(nombre)),LOWER(TRIM(sector))) WHERE estado='Activo'");
            s.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_identificacion_activa ON identificaciones(id_animal,LOWER(TRIM(tipo_identificacion)),LOWER(TRIM(numero_identificacion))) WHERE estado='Activo'");
            s.execute("CREATE INDEX IF NOT EXISTS idx_detalle_ingreso_animal ON detalle_ingreso(id_ingreso,id_animal,estado)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_plantel_animal ON plantel_permanente(id_animal,estado)");
        }
    }

    private static void normalizarCantidades(Connection c)throws SQLException{
        try(Statement s=c.createStatement()){
            s.executeUpdate("UPDATE animales SET cantidad_actual=CASE WHEN COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p WHERE p.id_animal=animales.id_animal AND p.estado='Activo'),0)>0 THEN COALESCE((SELECT SUM(p.cantidad) FROM plantel_permanente p WHERE p.id_animal=animales.id_animal AND p.estado='Activo'),0) ELSE COALESCE((SELECT SUM(d.cantidad) FROM detalle_ingreso d WHERE d.id_animal=animales.id_animal AND d.estado='Activo'),0) END");
        }
    }

    private static void normalizarAdministrador(Connection c)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE usuario='admin'");ResultSet r=p.executeQuery()){if(r.next()&&r.getInt(1)==0)try(PreparedStatement i=c.prepareStatement("INSERT INTO usuarios(nombre_usuario,usuario,password,rol,estado) VALUES(?,?,?,?,?)")){i.setString(1,"Administrador");i.setString(2,"admin");i.setString(3,"admin");i.setString(4,"Administrador");i.setString(5,"Activo");i.executeUpdate();}}
        try(PreparedStatement u=c.prepareStatement("UPDATE usuarios SET password='admin',estado='Activo' WHERE usuario='admin'")){u.executeUpdate();}
    }
}
