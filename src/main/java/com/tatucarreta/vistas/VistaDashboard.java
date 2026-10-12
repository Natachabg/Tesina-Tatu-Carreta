package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.Estilos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VistaDashboard extends BorderPane {

    public VistaDashboard() {

        Estilos.aplicarVentana(this);
        setPadding(new Insets(20, 24, 24, 24));

        VBox main = new VBox(18);
        main.setFillWidth(true);
        main.setPadding(new Insets(2));
        main.setMinSize(0, 0);
        main.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ImageView title = new ImageView();
        try {
            title.setImage(new Image(
                    getClass().getResourceAsStream("/images/secciones/portada.png")));
        } catch (Exception ignored) {
        }
        title.setPreserveRatio(true);
        title.setFitWidth(1080);
        title.setFitHeight(155);
        title.setSmooth(true);
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(28, 54, 35, 0.12), 14, 0, 0, 4);");

        HBox banner = new HBox(18);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(20, 22, 20, 22));
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setStyle(
                "-fx-background-color: linear-gradient(to right, "
                        + Estilos.VERDE_OSCURO + ", " + Estilos.VERDE + ");"
                        + "-fx-background-radius: 16;"
                        + "-fx-effect: dropshadow(gaussian, rgba(28, 54, 35, 0.16), 18, 0, 0, 5);");

        VBox textoBanner = new VBox(6);
        Label tituloBanner = new Label("SISTEMA DE FAUNA NATIVA");
        tituloBanner.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-letter-spacing: 0.5px;");

        Label descripcionBanner = new Label(
                "Información operativa calculada directamente desde la base de datos.");
        descripcionBanner.setWrapText(true);
        descripcionBanner.setStyle(
                "-fx-text-fill: #E3EEE4;"
                        + "-fx-font-size: 13px;");
        textoBanner.getChildren().addAll(tituloBanner, descripcionBanner);

        Region espacioBanner = new Region();
        HBox.setHgrow(espacioBanner, Priority.ALWAYS);

        Label periodo = new Label(
                "PERÍODO · " + java.time.LocalDate.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy")));
        periodo.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-background-color: rgba(22, 54, 31, 0.62);"
                        + "-fx-background-radius: 18;"
                        + "-fx-padding: 10 14;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;");
        banner.getChildren().addAll(textoBanner, espacioBanner, periodo);

        HBox cards = new HBox(14,
                Estilos.tarjetaIndicador(
                        "🌿", "EJEMPLARES EN PLANTEL",
                        String.valueOf(SistemaDAO.ejemplaresPlantel()),
                        "Cantidad física activa en Plantel Permanente."),
                Estilos.tarjetaIndicador(
                        "🐾", "ANIMALES EN CATÁLOGO",
                        String.valueOf(SistemaDAO.animalesCatalogados()),
                        "Registros maestros de animales, no ejemplares individuales."),
                Estilos.tarjetaIndicador(
                        "🧬", "ESPECIES ACTIVAS",
                        String.valueOf(SistemaDAO.especiesActivas()),
                        "Especies disponibles para registrar."),
                Estilos.tarjetaIndicador(
                        "📋", "ACTAS ACTIVAS",
                        String.valueOf(SistemaDAO.actasActivas()),
                        "Actas / ingresos actualmente activos."),
                Estilos.tarjetaIndicador(
                        "🏠", "HABITÁCULOS",
                        String.valueOf(SistemaDAO.habitaculosActivos()),
                        "Espacios activos configurados en la Reserva."));
        cards.setAlignment(Pos.TOP_LEFT);
        cards.setFillHeight(true);
        cards.setMinHeight(Region.USE_PREF_SIZE);
        cards.getChildren().forEach(card -> {
            HBox.setHgrow(card, Priority.ALWAYS);
            if (card instanceof Region region) {
                region.setMinWidth(0);
                region.setMaxWidth(Double.MAX_VALUE);
            }
        });

        int capacidad = SistemaDAO.capacidadTotal();
        int ocup = SistemaDAO.ocupacionTotal();
        double pct = capacidad <= 0 ? 0 : Math.min(100, (ocup * 100.0) / capacidad);

        VBox capacidadBox = Estilos.tarjeta();
        capacidadBox.setSpacing(12);
        capacidadBox.setPadding(new Insets(20));
        capacidadBox.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: " + Estilos.BORDE + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-effect: dropshadow(gaussian, rgba(28, 54, 35, 0.08), 14, 0, 0, 3);");

        Label tituloCapacidad = new Label("Ocupación de habitáculos");
        tituloCapacidad.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + Estilos.VERDE_OSCURO + ";");

        Label valorCapacidad = new Label(ocup + " / " + capacidad + " ejemplares");
        valorCapacidad.setStyle(
                "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + Estilos.VERDE_OSCURO + ";");

        ProgressBar barra = new ProgressBar(pct / 100.0);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setPrefHeight(15);
        barra.setStyle("-fx-accent: " + Estilos.VERDE + ";");

        Label detalleCapacidad = new Label(
                String.format("%.0f%% de ocupación · La capacidad se valida antes de cada alta o nacimiento.", pct));
        detalleCapacidad.setWrapText(true);
        detalleCapacidad.setStyle(
                "-fx-text-fill: " + Estilos.GRIS + ";"
                        + "-fx-font-size: 12px;");

        capacidadBox.getChildren().addAll(
                tituloCapacidad,
                valorCapacidad,
                barra,
                detalleCapacidad);
        HBox.setHgrow(capacidadBox, Priority.ALWAYS);
        capacidadBox.setMaxWidth(Double.MAX_VALUE);

        VBox circuito = Estilos.tarjeta();
        circuito.setSpacing(12);
        circuito.setPadding(new Insets(20));
        circuito.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: " + Estilos.BORDE + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-effect: dropshadow(gaussian, rgba(28, 54, 35, 0.08), 14, 0, 0, 3);");

        Label tituloTrazabilidad = new Label("Trazabilidad");
        tituloTrazabilidad.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: " + Estilos.VERDE_OSCURO + ";");

        circuito.getChildren().addAll(
                tituloTrazabilidad,
                paso("01", "Catálogo", "Especies y animales forman el registro maestro."),
                paso("02", "Ingreso", "Cada acta conserva entrega, recepción y animales asociados."),
                paso("03", "Plantel", "Se controla ubicación, identificaciones y capacidad."),
                paso("04", "Historial", "Nacimientos, fallecimientos y movimientos no pierden trazabilidad."));
        HBox.setHgrow(circuito, Priority.ALWAYS);
        circuito.setMaxWidth(Double.MAX_VALUE);

        HBox info = new HBox(16, capacidadBox, circuito);
        info.setAlignment(Pos.TOP_LEFT);
        info.setFillHeight(true);
        info.setMinHeight(0);
        VBox.setVgrow(info, Priority.ALWAYS);

        main.getChildren().addAll(title, banner, cards, info);
        setCenter(main);

        Runnable ajustarContenido = () -> {
            double ancho = Math.max(320, getWidth() - 48);
            double alto = getHeight();
            boolean compacto = alto > 0 && alto < 790;

            title.setFitWidth(Math.min(1080, ancho));
            title.setFitHeight(compacto ? 112 : 155);

            main.setSpacing(compacto ? 10 : 18);
            main.setPadding(new Insets(compacto ? 0 : 2));
            banner.setSpacing(compacto ? 10 : 18);
            banner.setPadding(compacto ? new Insets(10, 14, 10, 14) : new Insets(20, 22, 20, 22));
            cards.setSpacing(compacto ? 8 : 14);
            cards.setMinHeight(compacto ? 0 : Region.USE_PREF_SIZE);
            info.setSpacing(compacto ? 10 : 16);

            for (var card : cards.getChildren()) {
                if (card instanceof VBox box) {
                    box.setPadding(compacto ? new Insets(8) : new Insets(14));
                    box.setSpacing(compacto ? 5 : 8);
                    box.setPrefHeight(compacto ? 106 : 122);
                    box.setMinHeight(compacto ? 96 : Region.USE_PREF_SIZE);
                    for (var child : box.getChildren()) {
                        if (child instanceof HBox top) {
                            top.setSpacing(compacto ? 6 : 10);
                            if (!top.getChildren().isEmpty() && top.getChildren().get(0) instanceof Label icon) {
                                double iconSize = compacto ? 34 : 42;
                                icon.setMinSize(iconSize, iconSize);
                                icon.setMaxSize(iconSize, iconSize);
                            }
                            if (top.getChildren().size() > 1 && top.getChildren().get(1) instanceof VBox labels) {
                                for (var labelNode : labels.getChildren()) {
                                    if (labelNode instanceof Label label && label.getText() != null
                                            && label.getText().matches("\\d+")) {
                                        label.setStyle("-fx-font-size:" + (compacto ? 22 : 27)
                                                + "px;-fx-font-weight:bold;-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            capacidadBox.setPadding(compacto ? new Insets(11) : new Insets(20));
            capacidadBox.setSpacing(compacto ? 6 : 12);
            circuito.setPadding(compacto ? new Insets(11) : new Insets(20));
            circuito.setSpacing(compacto ? 6 : 12);
            for (int i = 1; i < circuito.getChildren().size(); i++) {
                if (circuito.getChildren().get(i) instanceof Label paso) {
                    String estilo = paso.getStyle();
                    paso.setStyle(compacto
                            ? estilo.replace("-fx-padding: 11 13;", "-fx-padding: 7 9;")
                                    .replace("-fx-font-size: 12px;", "-fx-font-size: 11px;")
                            : estilo.replace("-fx-padding: 7 9;", "-fx-padding: 11 13;")
                                    .replace("-fx-font-size: 11px;", "-fx-font-size: 12px;"));
                }
            }
        };

        heightProperty().addListener((obs, anterior, nuevo) -> ajustarContenido.run());
        widthProperty().addListener((obs, anterior, nuevo) -> ajustarContenido.run());
        ajustarContenido.run();
    }

    private Label paso(String numero, String titulo, String descripcion) {
        Label paso = new Label(numero + "   " + titulo + "\n          " + descripcion);
        paso.setMaxWidth(Double.MAX_VALUE);
        paso.setWrapText(true);
        paso.setStyle(
                "-fx-background-color: " + Estilos.VERDE_SUAVE + ";"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-color: " + Estilos.BORDE + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-padding: 11 13;"
                        + "-fx-text-fill: " + Estilos.TEXTO + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-line-spacing: 2px;");
        return paso;
    }
}
