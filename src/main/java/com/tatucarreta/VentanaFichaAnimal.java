package com.tatucarreta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VentanaFichaAnimal {

    private static final String VERDE = "#23452C";
    private static final String VERDE_CLARO = "#E7F0E8";
    private static final String GRIS = "#68746B";
    private static final String FONDO = "#F4F2EA";
    private static final String BORDE = "#DDE2DC";

    // =========================================================
    // CONTENIDO INTEGRADO
    // =========================================================

    public ScrollPane crearContenido(int idDetalle, Runnable volver) {

        Datos datos = cargarDatos(idDetalle);

        if (datos == null) {
            return new ScrollPane(
                    new Label("No se encontró la información del animal.")
            );
        }

        BorderPane raiz = new BorderPane();

        raiz.setStyle(
                "-fx-background-color: " + FONDO + ";"
        );

        // =====================================================
        // ENCABEZADO
        // =====================================================

        raiz.setTop(
                crearEncabezado(datos, volver)
        );

        // =====================================================
        // CONTENIDO
        // =====================================================

        VBox contenido = new VBox(14);

        contenido.setPadding(
                new Insets(18, 24, 24, 24)
        );

        contenido.getChildren().add(
                crearDatosPrincipales(datos)
        );

        contenido.getChildren().add(
                crearIngreso(datos)
        );

        contenido.getChildren().add(
                crearCantidades(datos)
        );

        Label tituloMovimientos =
                tituloTarjeta(
                        "HISTORIAL DE MOVIMIENTOS"
                );

        contenido.getChildren().add(
                tituloMovimientos
        );

        TableView<MovimientoHistorial> tabla =
                crearTablaHistorial();

        tabla.getItems().addAll(
                datos.historial
        );

        VBox.setVgrow(
                tabla,
                Priority.ALWAYS
        );

        contenido.getChildren().add(
                tabla
        );

        raiz.setCenter(contenido);

        ScrollPane scroll =
                new ScrollPane(raiz);

        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scroll.setStyle(
                "-fx-background-color: " + FONDO + ";"
        );

        return scroll;
    }


    // =========================================================
    // FICHA INTEGRADA DESDE ANIMALES
    // =========================================================

    public ScrollPane crearContenidoAnimal(
            int idAnimal,
            Runnable volver) {

        Datos datos = cargarDatosAnimal(idAnimal);

        if (datos == null) {
            return new ScrollPane(
                    new Label("No se encontró la información del animal.")
            );
        }

        BorderPane raiz = new BorderPane();

        raiz.setStyle(
                "-fx-background-color: " + FONDO + ";"
        );

        raiz.setTop(
                crearEncabezado(datos, volver)
        );

        VBox contenido = new VBox(14);

        contenido.setPadding(
                new Insets(18, 24, 24, 24)
        );

        contenido.getChildren().add(
                crearDatosPrincipales(datos)
        );

        contenido.getChildren().add(
                crearIngreso(datos)
        );

        contenido.getChildren().add(
                crearCantidades(datos)
        );

        Label tituloMovimientos =
                tituloTarjeta(
                        "HISTORIAL DE MOVIMIENTOS"
                );

        contenido.getChildren().add(
                tituloMovimientos
        );

        TableView<MovimientoHistorial> tabla =
                crearTablaHistorial();

        tabla.getItems().addAll(
                datos.historial
        );

        VBox.setVgrow(
                tabla,
                Priority.ALWAYS
        );

        contenido.getChildren().add(
                tabla
        );

        raiz.setCenter(contenido);

        ScrollPane scroll =
                new ScrollPane(raiz);

        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        scroll.setStyle(
                "-fx-background-color: " + FONDO + ";"
        );

        return scroll;
    }


    // =========================================================
    // CARGAR FICHA POR ID DEL ANIMAL
    // =========================================================

    private Datos cargarDatosAnimal(int idAnimal) {

        Datos d = new Datos();

        String sql = """
                SELECT
                    a.nombre_vulgar,
                    a.nombre_cientifico,
                    a.estado AS estado_animal,

                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM detalle_ingreso d0
                            WHERE d0.id_animal = a.id_animal
                        )
                        THEN
                            COALESCE(
                                (
                                    SELECT SUM(d1.cantidad)
                                    FROM detalle_ingreso d1
                                    WHERE d1.id_animal = a.id_animal
                                ),
                                0
                            )
                            -
                            COALESCE(
                                (
                                    SELECT SUM(m.cantidad)
                                    FROM movimientos m
                                    INNER JOIN detalle_ingreso d2
                                        ON d2.id_detalle = m.id_detalle
                                    WHERE d2.id_animal = a.id_animal
                                      AND m.tipo_movimiento IN (
                                          'LIBERACION',
                                          'TRASLADO',
                                          'FALLECIMIENTO'
                                      )
                                ),
                                0
                            )
                        ELSE
                            a.cantidad_actual
                    END AS cantidad_actual,

                    e.nombre AS nombre_especie,
                    di.cantidad,
                    di.sexo,
                    di.edad,
                    di.peso,
                    di.estado_ingreso,
                    di.observaciones AS obs_detalle,
                    i.numero_acta,
                    i.fecha_ingreso,
                    i.organismo_procedencia,
                    i.responsable_entrega,
                    i.procedencia,
                    i.motivo_ingreso,
                    i.documentacion,
                    i.observaciones AS obs_ingreso,
                    i.id_ingreso
                FROM animales a
                LEFT JOIN especies e
                    ON a.id_especie = e.id_especie
                LEFT JOIN detalle_ingreso di
                    ON di.id_detalle = (
                        SELECT d2.id_detalle
                        FROM detalle_ingreso d2
                        WHERE d2.id_animal = a.id_animal
                        ORDER BY d2.id_detalle DESC
                        LIMIT 1
                    )
                LEFT JOIN ingresos i
                    ON di.id_ingreso = i.id_ingreso
                WHERE a.id_animal = ?
                """;

        try (
                Connection conexion = ConexionSQLite.conectar();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            if (conexion == null) return null;

            ps.setInt(1, idAnimal);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) return null;

                d.nombreVulgar = rs.getString("nombre_vulgar");
                d.nombreCientifico = rs.getString("nombre_cientifico");
                d.estadoAnimal = rs.getString("estado_animal");
                d.cantidadActual = rs.getInt("cantidad_actual");
                d.nombreEspecie = rs.getString("nombre_especie");
                d.cantidadDetalle = rs.getInt("cantidad");
                d.sexo = rs.getString("sexo");
                d.edad = rs.getString("edad");
                d.peso = rs.getString("peso");
                d.estadoIngreso = rs.getString("estado_ingreso");
                d.obsDetalle = rs.getString("obs_detalle");
                d.numeroActa = rs.getString("numero_acta");
                d.fechaIngreso = rs.getString("fecha_ingreso");
                d.organismoProcedencia = rs.getString("organismo_procedencia");
                d.responsableEntrega = rs.getString("responsable_entrega");
                d.procedencia = rs.getString("procedencia");
                d.motivoIngreso = rs.getString("motivo_ingreso");
                d.documentacion = rs.getString("documentacion");
                d.obsIngreso = rs.getString("obs_ingreso");

                int idIngreso = rs.getInt("id_ingreso");

                if (idIngreso > 0) {
                    String sqlTotal =
                            "SELECT COALESCE(SUM(cantidad), 0) " +
                            "FROM detalle_ingreso WHERE id_ingreso = ?";

                    try (PreparedStatement psTotal =
                                 conexion.prepareStatement(sqlTotal)) {

                        psTotal.setInt(1, idIngreso);

                        try (ResultSet rt = psTotal.executeQuery()) {
                            if (rt.next()) {
                                d.totalActa = rt.getInt(1);
                            }
                        }
                    }
                }

                String sqlMov = """
                        SELECT
                            m.fecha_movimiento,
                            m.tipo_movimiento,
                            m.cantidad,
                            m.destino,
                            m.observaciones
                        FROM movimientos m
                        INNER JOIN detalle_ingreso di
                            ON m.id_detalle = di.id_detalle
                        WHERE di.id_animal = ?
                        ORDER BY m.id_movimiento DESC
                        """;

                try (PreparedStatement psMov =
                             conexion.prepareStatement(sqlMov)) {

                    psMov.setInt(1, idAnimal);

                    try (ResultSet rm = psMov.executeQuery()) {
                        while (rm.next()) {
                            d.historial.add(
                                    new MovimientoHistorial(
                                            rm.getString("fecha_movimiento"),
                                            rm.getString("tipo_movimiento"),
                                            rm.getInt("cantidad"),
                                            rm.getString("destino"),
                                            rm.getString("observaciones")
                                    )
                            );
                        }
                    }
                }

                d.totalMovimientos = d.historial.size();
            }

        } catch (Exception e) {
            System.out.println(
                    "Error al cargar ficha del animal: "
                            + e.getMessage()
            );
            e.printStackTrace();
            return null;
        }

        return d;
    }


    // =========================================================
    // COMPATIBILIDAD TEMPORAL
    // =========================================================
    // Esto evita que Movimientos se rompa antes de conectarlo
    // definitivamente al área central.

    public void mostrar(int idDetalle) {

        Dialog<Void> dialogo =
                new Dialog<>();

        dialogo.setTitle(
                "Tatú Carreta - Ficha del animal"
        );

        dialogo.getDialogPane()
                .getButtonTypes()
                .add(
                        ButtonType.CLOSE
                );

        ScrollPane contenido =
                crearContenido(
                        idDetalle,
                        () -> dialogo.close()
                );

        dialogo.getDialogPane()
                .setContent(contenido);

        dialogo.setResizable(true);

        dialogo.showAndWait();
    }


    // =========================================================
    // ENCABEZADO
    // =========================================================

    private HBox crearEncabezado(
            Datos d,
            Runnable volver) {

        VBox nombres =
                new VBox(3);

        Label subtitulo =
                new Label(
                        "FICHA DEL ANIMAL"
                );

        subtitulo.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #849087;"
        );

        Label nombre =
                new Label(
                        valor(d.nombreVulgar)
                );

        nombre.setStyle(
                "-fx-font-size: 27px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + VERDE + ";"
        );

        Label especie =
                new Label(
                        valor(d.nombreEspecie)
                );

        especie.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: " + GRIS + ";"
        );

        nombres.getChildren().addAll(
                subtitulo,
                nombre,
                especie
        );

        Region espacio =
                new Region();

        HBox.setHgrow(
                espacio,
                Priority.ALWAYS
        );

        VBox estado =
                crearEstado(
                        d.estadoAnimal
                );

        Button btnVolver =
                new Button(
                        "← VOLVER"
                );

        btnVolver.setStyle(
                "-fx-background-color: #E2E7E3;" +
                "-fx-text-fill: " + VERDE + ";" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 9 15;"
        );

        btnVolver.setOnAction(
                e -> volver.run()
        );

        VBox derecha =
                new VBox(
                        8,
                        estado,
                        btnVolver
                );

        derecha.setAlignment(
                Pos.CENTER_RIGHT
        );

        HBox encabezado =
                new HBox(
                        18,
                        nombres,
                        espacio,
                        derecha
                );

        encabezado.setAlignment(
                Pos.CENTER_LEFT
        );

        encabezado.setPadding(
                new Insets(
                        16,
                        24,
                        16,
                        24
                )
        );

        encabezado.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: " + BORDE + ";" +
                "-fx-border-width: 0 0 1 0;"
        );

        return encabezado;
    }


    // =========================================================
    // ESTADO
    // =========================================================

    private VBox crearEstado(
            String estadoTexto) {

        String estado =
                valor(estadoTexto)
                        .toUpperCase();

        boolean activo =
                estado.equals("ACTIVO")
                || estado.equals("PERMANENTE");

        String fondo =
                activo
                        ? VERDE_CLARO
                        : "#FFF4DE";

        String color =
                activo
                        ? "#3E9B52"
                        : "#C58B32";

        String colorTexto =
                activo
                        ? "#2E713B"
                        : "#8A641F";

        Label titulo =
                new Label("ESTADO");

        titulo.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + GRIS + ";"
        );

        Label punto =
                new Label("●");

        punto.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-text-fill: " + color + ";"
        );

        Label texto =
                new Label(estado);

        texto.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + colorTexto + ";"
        );

        HBox fila =
                new HBox(
                        6,
                        punto,
                        texto
                );

        fila.setAlignment(
                Pos.CENTER
        );

        VBox tarjeta =
                new VBox(
                        2,
                        titulo,
                        fila
                );

        tarjeta.setAlignment(
                Pos.CENTER
        );

        tarjeta.setPadding(
                new Insets(
                        8,
                        14,
                        8,
                        14
                )
        );

        tarjeta.setStyle(
                "-fx-background-color: " + fondo + ";" +
                "-fx-background-radius: 18;"
        );

        return tarjeta;
    }


    // =========================================================
    // DATOS DEL ANIMAL
    // =========================================================

    private VBox crearDatosPrincipales(
            Datos d) {

        VBox tarjeta =
                tarjetaBase();

        Label titulo =
                tituloTarjeta(
                        "DATOS DEL ANIMAL"
                );

        GridPane grid =
                new GridPane();

        grid.setHgap(45);
        grid.setVgap(10);

        ColumnConstraints columna1 =
                new ColumnConstraints();

        columna1.setPercentWidth(50);

        ColumnConstraints columna2 =
                new ColumnConstraints();

        columna2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(
                columna1,
                columna2
        );

        grid.add(
                dato(
                        "Nombre científico",
                        d.nombreCientifico
                ),
                0,
                0
        );

        grid.add(
                dato(
                        "Sexo",
                        d.sexo
                ),
                1,
                0
        );

        grid.add(
                dato(
                        "Edad",
                        d.edad
                ),
                0,
                1
        );

        grid.add(
                dato(
                        "Peso",
                        d.peso
                ),
                1,
                1
        );

        grid.add(
                dato(
                        "Estado de ingreso",
                        d.estadoIngreso
                ),
                0,
                2
        );

        grid.add(
                dato(
                        "Observaciones",
                        d.obsDetalle
                ),
                1,
                2
        );

        tarjeta.getChildren().addAll(
                titulo,
                grid
        );

        return tarjeta;
    }


    // =========================================================
    // INGRESO / ACTA
    // =========================================================

    private VBox crearIngreso(
            Datos d) {

        VBox tarjeta =
                tarjetaBase();

        Label titulo =
                tituloTarjeta(
                        "INGRESO / ACTA"
                );

        GridPane grid =
                new GridPane();

        grid.setHgap(45);
        grid.setVgap(10);

        ColumnConstraints columna1 =
                new ColumnConstraints();

        columna1.setPercentWidth(50);

        ColumnConstraints columna2 =
                new ColumnConstraints();

        columna2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(
                columna1,
                columna2
        );

        grid.add(
                dato(
                        "N° de acta",
                        d.numeroActa
                ),
                0,
                0
        );

        grid.add(
                dato(
                        "Fecha de ingreso",
                        d.fechaIngreso
                ),
                1,
                0
        );

        grid.add(
                dato(
                        "Organismo de procedencia",
                        d.organismoProcedencia
                ),
                0,
                1
        );

        grid.add(
                dato(
                        "Responsable de entrega",
                        d.responsableEntrega
                ),
                1,
                1
        );

        grid.add(
                dato(
                        "Procedencia",
                        d.procedencia
                ),
                0,
                2
        );

        grid.add(
                dato(
                        "Motivo de ingreso",
                        d.motivoIngreso
                ),
                1,
                2
        );

        grid.add(
                dato(
                        "Documentación",
                        d.documentacion
                ),
                0,
                3
        );

        grid.add(
                dato(
                        "Observaciones",
                        d.obsIngreso
                ),
                1,
                3
        );

        tarjeta.getChildren().addAll(
                titulo,
                grid
        );

        return tarjeta;
    }


    // =========================================================
    // CANTIDADES
    // =========================================================

    private HBox crearCantidades(
            Datos d) {

        HBox fila =
                new HBox(12);

        fila.setAlignment(
                Pos.CENTER
        );

        HBox.setHgrow(
                fila,
                Priority.ALWAYS
        );

        fila.getChildren().addAll(

                tarjetaCantidad(
                        "CANTIDAD ACTUAL",
                        d.cantidadActual
                ),

                tarjetaCantidad(
                        "ÚLTIMO INGRESO",
                        d.cantidadDetalle
                ),

                tarjetaCantidad(
                        "MOVIMIENTOS",
                        d.totalMovimientos
                )
        );

        return fila;
    }


    private VBox tarjetaCantidad(
            String titulo,
            int cantidad) {

        Label numero =
                new Label(
                        String.valueOf(cantidad)
                );

        numero.setStyle(
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + VERDE + ";"
        );

        Label texto =
                new Label(
                        titulo
                );

        texto.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + GRIS + ";"
        );

        VBox tarjeta =
                new VBox(
                        2,
                        numero,
                        texto
                );

        tarjeta.setAlignment(
                Pos.CENTER
        );

        tarjeta.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox.setHgrow(
                tarjeta,
                Priority.ALWAYS
        );

        tarjeta.setPadding(
                new Insets(10)
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + BORDE + ";" +
                "-fx-border-radius: 10;"
        );

        return tarjeta;
    }


    // =========================================================
    // TARJETAS
    // =========================================================

    private VBox tarjetaBase() {

        VBox tarjeta =
                new VBox(9);

        tarjeta.setPadding(
                new Insets(
                        13,
                        16,
                        13,
                        16
                )
        );

        tarjeta.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 11;" +
                "-fx-border-color: " + BORDE + ";" +
                "-fx-border-radius: 11;"
        );

        return tarjeta;
    }


    private Label tituloTarjeta(
            String texto) {

        Label label =
                new Label(texto);

        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + VERDE + ";"
        );

        return label;
    }


    private VBox dato(
            String titulo,
            String valor) {

        Label t =
                new Label(
                        titulo.toUpperCase()
                );

        t.setStyle(
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #89928B;"
        );

        Label v =
                new Label(
                        valor(valor)
                );

        v.setWrapText(true);

        v.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #344039;"
        );

        VBox caja =
                new VBox(
                        1,
                        t,
                        v
                );

        return caja;
    }


    // =========================================================
    // TABLA DE MOVIMIENTOS
    // =========================================================

    private TableView<MovimientoHistorial>
    crearTablaHistorial() {

        TableView<MovimientoHistorial> tabla =
                new TableView<>();

        tabla.setPrefHeight(160);
        tabla.setMinHeight(130);

        tabla.setPlaceholder(
                new Label(
                        "No hay movimientos registrados."
                )
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<
                MovimientoHistorial,
                String> fecha =
                new TableColumn<>("Fecha");

        fecha.setCellValueFactory(
                new PropertyValueFactory<>(
                        "fecha"
                )
        );

        TableColumn<
                MovimientoHistorial,
                String> tipo =
                new TableColumn<>("Movimiento");

        tipo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "tipo"
                )
        );

        TableColumn<
                MovimientoHistorial,
                Integer> cantidad =
                new TableColumn<>("Cantidad");

        cantidad.setCellValueFactory(
                new PropertyValueFactory<>(
                        "cantidad"
                )
        );

        TableColumn<
                MovimientoHistorial,
                String> destino =
                new TableColumn<>("Destino");

        destino.setCellValueFactory(
                new PropertyValueFactory<>(
                        "destino"
                )
        );

        TableColumn<
                MovimientoHistorial,
                String> observaciones =
                new TableColumn<>(
                        "Observaciones"
                );

        observaciones.setCellValueFactory(
                new PropertyValueFactory<>(
                        "observaciones"
                )
        );

        tabla.getColumns().addAll(
                fecha,
                tipo,
                cantidad,
                destino,
                observaciones
        );

        return tabla;
    }


    // =========================================================
    // CARGAR DATOS
    // =========================================================

    private Datos cargarDatos(
            int idDetalle) {

        Datos d =
                new Datos();

        String sql = """
                SELECT
                    a.nombre_vulgar,
                    a.nombre_cientifico,
                    a.estado AS estado_animal,
                    e.nombre AS nombre_especie,
                    di.cantidad,
                    di.sexo,
                    di.edad,
                    di.peso,
                    di.estado_ingreso,
                    di.observaciones AS obs_detalle,
                    i.numero_acta,
                    i.fecha_ingreso,
                    i.organismo_procedencia,
                    i.responsable_entrega,
                    i.procedencia,
                    i.motivo_ingreso,
                    i.documentacion,
                    i.observaciones AS obs_ingreso,
                    di.id_ingreso
                FROM detalle_ingreso di
                INNER JOIN animales a
                    ON di.id_animal = a.id_animal
                INNER JOIN especies e
                    ON a.id_especie = e.id_especie
                INNER JOIN ingresos i
                    ON di.id_ingreso = i.id_ingreso
                WHERE di.id_detalle = ?
                """;

        try (
                Connection conexion =
                        ConexionSQLite.conectar();
                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            if (conexion == null) {
                return null;
            }

            ps.setInt(
                    1,
                    idDetalle
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {
                    return null;
                }

                d.nombreVulgar =
                        rs.getString(
                                "nombre_vulgar"
                        );

                d.nombreCientifico =
                        rs.getString(
                                "nombre_cientifico"
                        );

                d.estadoAnimal =
                        rs.getString(
                                "estado_animal"
                        );

                d.nombreEspecie =
                        rs.getString(
                                "nombre_especie"
                        );

                d.cantidadDetalle =
                        rs.getInt(
                                "cantidad"
                        );

                d.sexo =
                        rs.getString(
                                "sexo"
                        );

                d.edad =
                        rs.getString(
                                "edad"
                        );

                d.peso =
                        rs.getString(
                                "peso"
                        );

                d.estadoIngreso =
                        rs.getString(
                                "estado_ingreso"
                        );

                d.obsDetalle =
                        rs.getString(
                                "obs_detalle"
                        );

                d.numeroActa =
                        rs.getString(
                                "numero_acta"
                        );

                d.fechaIngreso =
                        rs.getString(
                                "fecha_ingreso"
                        );

                d.organismoProcedencia =
                        rs.getString(
                                "organismo_procedencia"
                        );

                d.responsableEntrega =
                        rs.getString(
                                "responsable_entrega"
                        );

                d.procedencia =
                        rs.getString(
                                "procedencia"
                        );

                d.motivoIngreso =
                        rs.getString(
                                "motivo_ingreso"
                        );

                d.documentacion =
                        rs.getString(
                                "documentacion"
                        );

                d.obsIngreso =
                        rs.getString(
                                "obs_ingreso"
                        );

                int idIngreso =
                        rs.getInt(
                                "id_ingreso"
                        );

                // =============================================
                // TOTAL DEL ACTA
                // =============================================

                String sqlTotal =
                        """
                        SELECT COALESCE(
                            SUM(cantidad),
                            0
                        )
                        FROM detalle_ingreso
                        WHERE id_ingreso = ?
                        """;

                try (
                        PreparedStatement psTotal =
                                conexion.prepareStatement(
                                        sqlTotal
                                )
                ) {

                    psTotal.setInt(
                            1,
                            idIngreso
                    );

                    try (
                            ResultSet rt =
                                    psTotal.executeQuery()
                    ) {

                        if (rt.next()) {

                            d.totalActa =
                                    rt.getInt(1);
                        }
                    }
                }

                // =============================================
                // MOVIMIENTOS
                // =============================================

                String sqlMov =
                        """
                        SELECT
                            fecha_movimiento,
                            tipo_movimiento,
                            cantidad,
                            destino,
                            observaciones
                        FROM movimientos
                        WHERE id_detalle = ?
                        ORDER BY id_movimiento DESC
                        """;

                try (
                        PreparedStatement psMov =
                                conexion.prepareStatement(
                                        sqlMov
                                )
                ) {

                    psMov.setInt(
                            1,
                            idDetalle
                    );

                    try (
                            ResultSet rm =
                                    psMov.executeQuery()
                    ) {

                        while (rm.next()) {

                            d.historial.add(
                                    new MovimientoHistorial(
                                            rm.getString(
                                                    "fecha_movimiento"
                                            ),
                                            rm.getString(
                                                    "tipo_movimiento"
                                            ),
                                            rm.getInt(
                                                    "cantidad"
                                            ),
                                            rm.getString(
                                                    "destino"
                                            ),
                                            rm.getString(
                                                    "observaciones"
                                            )
                                    )
                            );
                        }
                    }
                }

                d.totalMovimientos =
                        d.historial.size();
            }

        } catch (Exception e) {

            System.out.println(
                    "Error al cargar ficha del animal: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return null;
        }

        return d;
    }


    // =========================================================
    // VALOR
    // =========================================================

    private String valor(
            String texto) {

        return texto == null
                || texto.isBlank()
                ? "Sin registrar"
                : texto;
    }


    // =========================================================
    // HISTORIAL
    // =========================================================

    public static class MovimientoHistorial {

        private final String fecha;
        private final String tipo;
        private final int cantidad;
        private final String destino;
        private final String observaciones;

        public MovimientoHistorial(
                String fecha,
                String tipo,
                int cantidad,
                String destino,
                String observaciones) {

            this.fecha = fecha;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.destino = destino;
            this.observaciones = observaciones;
        }

        public String getFecha() {
            return fecha;
        }

        public String getTipo() {
            return tipo;
        }

        public int getCantidad() {
            return cantidad;
        }

        public String getDestino() {
            return destino;
        }

        public String getObservaciones() {
            return observaciones;
        }
    }


    // =========================================================
    // DATOS
    // =========================================================

    private static class Datos {

        String nombreVulgar;
        String nombreCientifico;
        String estadoAnimal;
        String nombreEspecie;

        int cantidadActual;
        int cantidadDetalle;
        int totalActa;
        int totalMovimientos;

        String sexo;
        String edad;
        String peso;
        String estadoIngreso;
        String obsDetalle;

        String numeroActa;
        String fechaIngreso;
        String organismoProcedencia;
        String responsableEntrega;
        String procedencia;
        String motivoIngreso;
        String documentacion;
        String obsIngreso;

        java.util.List<MovimientoHistorial>
                historial =
                new java.util.ArrayList<>();
    }
}