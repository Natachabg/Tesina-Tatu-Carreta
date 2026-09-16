package com.tatucarreta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla de Plantel Permanente.
 *
 * La pantalla se organiza en acciones simples:
 * - Ver plantel actual.
 * - Carga inicial: animales que ya estaban en la reserva.
 * - Registrar nacimiento: nacimientos producidos dentro del plantel.
 */
public class VentanaPlantelPermanente {

    private final PlantelPermanenteDAO plantelDAO = new PlantelPermanenteDAO();
    private final HabitaculoDAO habitaculoDAO = new HabitaculoDAO();

    private final TableView<PlantelPermanente> tabla = new TableView<>();
    private final ComboBox<Animal> comboAnimal = new ComboBox<>();
    private final TextField txtCantidad = new TextField();
    private final ComboBox<String> comboUbicacion = new ComboBox<>();
    private final ComboBox<Habitaculo> comboHabitaculo = new ComboBox<>();
    private final DatePicker datePickerFecha = new DatePicker();
    private final ComboBox<String> comboEstado = new ComboBox<>();
    private final TextArea txtObservaciones = new TextArea();

    private final ComboBox<Animal> comboAnimalNacimiento = new ComboBox<>();
    private final TextField txtCantidadNacimiento = new TextField();
    private final ComboBox<Habitaculo> comboHabitaculoNacimiento = new ComboBox<>();
    private final DatePicker datePickerNacimiento = new DatePicker();
    private final ComboBox<String> comboTipoIdentificacionNacimiento = new ComboBox<>();
    private final TextField txtNumeroIdentificacionNacimiento = new TextField();
    private final TextArea txtObservacionesNacimiento = new TextArea();

    private VBox tarjetaCargaInicial;
    private VBox tarjetaNacimiento;

    // Datos auxiliares para mostrar especie e identificación en la pantalla.
    private final Map<Integer, String> mapaEspecies = new HashMap<>();
    private final Map<Integer, Integer> mapaEspeciePorAnimal = new HashMap<>();
    private final Map<Integer, String> mapaIdentificaciones = new HashMap<>();

    private final Label lblEspecieCargaInicial = new Label("—");
    private final Label lblEspecieNacimiento = new Label("—");
    private final ComboBox<String> comboTipoIdentificacion = new ComboBox<>();
    private final TextField txtNumeroIdentificacion = new TextField();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Parent crearContenido() {
        BorderPane principal = new BorderPane();
        principal.setPadding(new Insets(28));
        principal.setStyle("-fx-background-color: #F4F7F2;");

        VBox encabezado = new VBox(5);
        Label titulo = new Label("Plantel Permanente");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #23452C;");
        Label subtitulo = new Label(
                "Administrá los animales que forman parte del plantel y registrá sus nacimientos.");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #5C6F61;");
        encabezado.getChildren().addAll(titulo, subtitulo);

        HBox acciones = new HBox(14);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPadding(new Insets(18, 0, 8, 0));

        Button btnCargaInicial = crearBotonAccion("➕", "CARGA INICIAL", "Animales que ya estaban en la reserva");
        Button btnNacimiento = crearBotonAccion("🐣", "REGISTRAR NACIMIENTO", "Nuevos ejemplares nacidos en el plantel");

        btnCargaInicial.setOnAction(e -> mostrarCargaInicial());
        btnNacimiento.setOnAction(e -> mostrarNacimiento());
        acciones.getChildren().addAll(btnCargaInicial, btnNacimiento);

        tarjetaCargaInicial = crearTarjetaCargaInicial();
        tarjetaNacimiento = crearTarjetaNacimiento();
        tarjetaCargaInicial.setVisible(false);
        tarjetaCargaInicial.setManaged(false);
        tarjetaNacimiento.setVisible(false);
        tarjetaNacimiento.setManaged(false);

        VBox tarjetaTabla = crearTarjetaTabla();

        VBox contenido = new VBox(16, encabezado, acciones,
                tarjetaCargaInicial, tarjetaNacimiento, tarjetaTabla);
        VBox.setVgrow(tarjetaTabla, Priority.ALWAYS);
        principal.setCenter(contenido);

        cargarEspecies();
        cargarAnimales();
        cargarHabitaculos();
        cargarIdentificaciones();
        cargarPlantel();

        comboAnimal.setOnAction(e -> actualizarEspecie(comboAnimal, lblEspecieCargaInicial));
        comboAnimalNacimiento.setOnAction(e -> actualizarEspecie(comboAnimalNacimiento, lblEspecieNacimiento));

        mostrarSoloPlantel();

        return principal;
    }

    private Button crearBotonAccion(String icono, String titulo, String ayuda) {
        Label lblIcono = new Label(icono);
        lblIcono.setStyle("-fx-font-size: 24px;");
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #28583A;");
        Label lblAyuda = new Label(ayuda);
        lblAyuda.setWrapText(true);
        lblAyuda.setStyle("-fx-font-size: 10px; -fx-text-fill: #718176;");

        VBox contenido = new VBox(4, lblIcono, lblTitulo, lblAyuda);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(13));

        Button boton = new Button();
        boton.setGraphic(contenido);
        boton.setPrefWidth(260);
        boton.setPrefHeight(105);
        boton.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: #CFE0D2; -fx-border-radius: 14; -fx-padding: 0;");
        return boton;
    }

    private VBox crearTarjetaCargaInicial() {
        VBox tarjeta = new VBox(13);
        tarjeta.setPadding(new Insets(18));
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: #D5E2D6; -fx-border-radius: 14;");

        Label titulo = new Label("➕ Carga inicial del plantel");
        titulo.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #315E3A;");
        Label ayuda = new Label("Usá esta opción solamente para animales que ya estaban en la reserva antes del sistema.");
        ayuda.setWrapText(true);
        ayuda.setStyle("-fx-font-size: 12px; -fx-text-fill: #66756A;");

        GridPane g = crearGrid();
        g.add(crearEtiqueta("Animal *"), 0, 0);
        g.add(configurarAnimal(comboAnimal), 1, 0);
        g.add(crearEtiqueta("Especie"), 2, 0);
        configurarEtiquetaDato(lblEspecieCargaInicial);
        g.add(lblEspecieCargaInicial, 3, 0);

        g.add(crearEtiqueta("Cantidad *"), 0, 1);
        txtCantidad.setPromptText("Ej.: 5");
        g.add(txtCantidad, 1, 1);
        g.add(crearEtiqueta("Ubicación *"), 2, 1);
        comboUbicacion.getItems().setAll("Habitáculo", "Recinto", "Campo abierto", "Otro");
        comboUbicacion.setPromptText("Seleccionar...");
        g.add(comboUbicacion, 3, 1);

        g.add(crearEtiqueta("Habitáculo"), 0, 2);
        g.add(configurarHabitaculo(comboHabitaculo), 1, 2);
        g.add(crearEtiqueta("Fecha *"), 2, 2);
        configurarFecha(datePickerFecha);
        g.add(datePickerFecha, 3, 2);

        g.add(crearEtiqueta("Estado *"), 0, 3);
        comboEstado.getItems().setAll("Activo", "Inactivo");
        comboEstado.setValue("Activo");
        g.add(comboEstado, 1, 3);
        g.add(crearEtiqueta("Tipo identificación"), 2, 3);
        configurarIdentificacion();
        g.add(comboTipoIdentificacion, 3, 3);

        g.add(crearEtiqueta("N° identificación"), 0, 4);
        g.add(txtNumeroIdentificacion, 1, 4);
        g.add(crearEtiqueta("Observaciones"), 2, 4);
        txtObservaciones.setPromptText("Información adicional...");
        txtObservaciones.setPrefRowCount(2);
        txtObservaciones.setWrapText(true);
        g.add(txtObservaciones, 3, 4);

        Button btnAgregar = crearBotonPrincipal("✓  AGREGAR AL PLANTEL");
        Button btnLimpiar = crearBotonSecundario("Limpiar");
        Button btnCerrar = crearBotonSecundario("Cerrar");
        btnAgregar.setOnAction(e -> agregarCargaInicial());
        btnLimpiar.setOnAction(e -> limpiarCargaInicial());
        btnCerrar.setOnAction(e -> mostrarSoloPlantel());

        HBox botones = new HBox(10, btnAgregar, btnLimpiar, btnCerrar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        tarjeta.getChildren().addAll(titulo, ayuda, g, botones);
        return tarjeta;
    }

    private VBox crearTarjetaNacimiento() {
        VBox tarjeta = new VBox(13);
        tarjeta.setPadding(new Insets(18));
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-border-color: #BFDAC7; -fx-border-radius: 14;");

        Label titulo = new Label("🐣 Registrar nacimiento");
        titulo.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #315E3A;");
        Label ayuda = new Label("El nacimiento ocurre dentro del plantel permanente y no necesita número de acta.");
        ayuda.setWrapText(true);
        ayuda.setStyle("-fx-font-size: 12px; -fx-text-fill: #66756A;");

        GridPane g = crearGrid();
        g.add(crearEtiqueta("Animal *"), 0, 0);
        g.add(configurarAnimal(comboAnimalNacimiento), 1, 0);
        g.add(crearEtiqueta("Especie"), 2, 0);
        configurarEtiquetaDato(lblEspecieNacimiento);
        g.add(lblEspecieNacimiento, 3, 0);

        g.add(crearEtiqueta("Cantidad nacida *"), 0, 1);
        txtCantidadNacimiento.setPromptText("Ej.: 2");
        g.add(txtCantidadNacimiento, 1, 1);
        g.add(crearEtiqueta("Fecha *"), 2, 1);
        configurarFecha(datePickerNacimiento);
        g.add(datePickerNacimiento, 3, 1);

        g.add(crearEtiqueta("Habitáculo"), 0, 2);
        g.add(configurarHabitaculo(comboHabitaculoNacimiento), 1, 2);

        g.add(crearEtiqueta("Tipo identificación"), 2, 2);
        configurarIdentificacionNacimiento();
        g.add(comboTipoIdentificacionNacimiento, 3, 2);

        g.add(crearEtiqueta("N° identificación"), 0, 3);
        g.add(txtNumeroIdentificacionNacimiento, 1, 3);

        g.add(crearEtiqueta("Observaciones"), 2, 3);
        txtObservacionesNacimiento.setPromptText("Ej.: nacimiento registrado dentro del recinto...");
        txtObservacionesNacimiento.setPrefRowCount(2);
        txtObservacionesNacimiento.setWrapText(true);
        g.add(txtObservacionesNacimiento, 3, 3);

        Button btnRegistrar = crearBotonNacimiento("🐣  REGISTRAR NACIMIENTO");
        Button btnLimpiar = crearBotonSecundario("Limpiar");
        Button btnCerrar = crearBotonSecundario("Cerrar");
        btnRegistrar.setOnAction(e -> registrarNacimiento());
        btnLimpiar.setOnAction(e -> limpiarNacimiento());
        btnCerrar.setOnAction(e -> mostrarSoloPlantel());

        HBox botones = new HBox(10, btnRegistrar, btnLimpiar, btnCerrar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        tarjeta.getChildren().addAll(titulo, ayuda, g, botones);
        return tarjeta;
    }

    private VBox crearTarjetaTabla() {
        VBox tarjeta = new VBox(10);
        tarjeta.setPadding(new Insets(18, 0, 0, 0));

        Label titulo = new Label("🐾 Plantel actual");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #23452C;");
        Label ayuda = new Label("Cantidad y ubicación de los animales que actualmente forman parte del plantel.");
        ayuda.setStyle("-fx-font-size: 12px; -fx-text-fill: #66756A;");

        crearColumnas();
        tabla.setPlaceholder(new Label("No hay registros en el plantel permanente."));
        tabla.setPrefHeight(330);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabla.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) {
                cargarRegistroEnFormulario(tabla.getSelectionModel().getSelectedItem());
            }
        });

        tarjeta.getChildren().addAll(titulo, ayuda, tabla);
        return tarjeta;
    }

    private GridPane crearGrid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        return g;
    }

    private ComboBox<Animal> configurarAnimal(ComboBox<Animal> combo) {
        combo.setPrefWidth(290);
        combo.setPromptText("Seleccionar animal...");
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Animal a) {
                return a == null ? "" : a.getNombreVulgar();
            }
            @Override public Animal fromString(String s) { return null; }
        });
        return combo;
    }

    private ComboBox<Habitaculo> configurarHabitaculo(ComboBox<Habitaculo> combo) {
        combo.setPrefWidth(290);
        combo.setPromptText("Seleccionar habitáculo...");
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Habitaculo h) {
                return h == null ? "" : h.getNombre() + " - " + h.getSector();
            }
            @Override public Habitaculo fromString(String s) { return null; }
        });
        return combo;
    }

    private void configurarFecha(DatePicker picker) {
        picker.setPromptText("dd/MM/yyyy");
        picker.setPrefWidth(180);
        picker.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate f) {
                return f == null ? "" : formatoFecha.format(f);
            }
            @Override public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) return null;
                return LocalDate.parse(s, formatoFecha);
            }
        });
    }

    private void crearColumnas() {
        tabla.getColumns().clear();
        TableColumn<PlantelPermanente, String> animal = new TableColumn<>("Animal");
        animal.setCellValueFactory(new PropertyValueFactory<>("nombreAnimal"));

        TableColumn<PlantelPermanente, String> especie = new TableColumn<>("Especie");
        especie.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        mapaEspecies.getOrDefault(
                                mapaEspeciePorAnimal.getOrDefault(data.getValue().getIdAnimal(), -1),
                                "Sin registrar"
                        )
                ));

        TableColumn<PlantelPermanente, String> identificacion = new TableColumn<>("Identificación");
        identificacion.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        mapaIdentificaciones.getOrDefault(data.getValue().getIdAnimal(), "Sin identificar")
                ));

        TableColumn<PlantelPermanente, Integer> cantidad = new TableColumn<>("Cantidad");
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<PlantelPermanente, String> ubicacion = new TableColumn<>("Ubicación");
        ubicacion.setCellValueFactory(new PropertyValueFactory<>("tipoUbicacion"));
        TableColumn<PlantelPermanente, String> habitaculo = new TableColumn<>("Habitáculo");
        habitaculo.setCellValueFactory(new PropertyValueFactory<>("nombreHabitaculo"));
        TableColumn<PlantelPermanente, String> fecha = new TableColumn<>("Fecha");
        fecha.setCellValueFactory(new PropertyValueFactory<>("fechaIngresoPlantel"));
        TableColumn<PlantelPermanente, String> estado = new TableColumn<>("Estado");
        estado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tabla.getColumns().addAll(animal, especie, cantidad, ubicacion, habitaculo, identificacion, fecha, estado);
    }

    private void cargarEspecies() {
        mapaEspecies.clear();
        String sql = "SELECT id_especie, nombre FROM especies ORDER BY nombre COLLATE NOCASE ASC";
        try (Connection c = ConexionSQLite.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapaEspecies.put(rs.getInt("id_especie"), rs.getString("nombre"));
            }
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Especies", "No se pudieron cargar las especies.\n\n" + e.getMessage());
        }
    }

    private void cargarIdentificaciones() {
        mapaIdentificaciones.clear();
        try (Connection c = ConexionSQLite.conectar();
             PreparedStatement ps = c.prepareStatement("""
                     CREATE TABLE IF NOT EXISTS identificaciones_plantel (
                         id_identificacion INTEGER PRIMARY KEY AUTOINCREMENT,
                         id_animal INTEGER NOT NULL,
                         tipo_identificacion TEXT NOT NULL,
                         numero_identificacion TEXT NOT NULL,
                         fecha_identificacion TEXT,
                         observaciones TEXT,
                         FOREIGN KEY (id_animal) REFERENCES animales(id_animal)
                     )""")) {
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("No se pudo preparar identificaciones del plantel: " + e.getMessage());
        }
        String sql = """
                SELECT id_animal,
                       GROUP_CONCAT(identificacion, ' | ') AS identificacion
                FROM (
                    SELECT a.id_animal,
                           i.tipo_identificacion || ': ' || i.numero_identificacion AS identificacion
                    FROM identificaciones i
                    INNER JOIN detalle_ingreso d ON d.id_detalle = i.id_detalle
                    INNER JOIN animales a ON a.id_animal = d.id_animal

                    UNION ALL

                    SELECT id_animal,
                           tipo_identificacion || ': ' || numero_identificacion AS identificacion
                    FROM identificaciones_plantel
                )
                GROUP BY id_animal
                """;
        try (Connection c = ConexionSQLite.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapaIdentificaciones.put(rs.getInt("id_animal"), rs.getString("identificacion"));
            }
        } catch (Exception e) {
            System.out.println("No se pudieron cargar las identificaciones existentes: " + e.getMessage());
        }
    }

    private void configurarIdentificacion() {
        comboTipoIdentificacion.getItems().setAll("Sin identificación", "Chip", "Caravana", "Anillo", "Otro");
        comboTipoIdentificacion.setValue("Sin identificación");
        comboTipoIdentificacion.setPrefWidth(290);
        txtNumeroIdentificacion.setPromptText("Opcional");
        txtNumeroIdentificacion.setDisable(true);
        comboTipoIdentificacion.setOnAction(e -> {
            boolean habilitar = !"Sin identificación".equals(comboTipoIdentificacion.getValue());
            txtNumeroIdentificacion.setDisable(!habilitar);
            if (!habilitar) txtNumeroIdentificacion.clear();
        });
    }

    private void configurarIdentificacionNacimiento() {
        comboTipoIdentificacionNacimiento.getItems().setAll(
                "Sin identificación", "Chip", "Caravana", "Anillo", "Otro");
        comboTipoIdentificacionNacimiento.setValue("Sin identificación");
        comboTipoIdentificacionNacimiento.setPrefWidth(290);
        txtNumeroIdentificacionNacimiento.setPromptText("Opcional");
        txtNumeroIdentificacionNacimiento.setDisable(true);

        comboTipoIdentificacionNacimiento.setOnAction(e -> {
            boolean habilitar = !"Sin identificación".equals(
                    comboTipoIdentificacionNacimiento.getValue());
            txtNumeroIdentificacionNacimiento.setDisable(!habilitar);
            if (!habilitar) {
                txtNumeroIdentificacionNacimiento.clear();
            }
        });
    }

    private void configurarEtiquetaDato(Label label) {
        label.setStyle("-fx-background-color: #F0F6F1; -fx-border-color: #D5E2D6; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 10; " +
                "-fx-text-fill: #315E3A;");
        label.setMaxWidth(Double.MAX_VALUE);
    }

    private void cargarAnimales() {
        ObservableList<Animal> animales = FXCollections.observableArrayList();
        String sql = "SELECT id_animal, id_especie, nombre_vulgar, nombre_cientifico, estado " +
                "FROM animales ORDER BY nombre_vulgar COLLATE NOCASE ASC";
        try (Connection c = ConexionSQLite.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Animal a = new Animal();
                a.setIdAnimal(rs.getInt("id_animal"));
                a.setIdEspecie(rs.getInt("id_especie"));
                a.setNombreVulgar(rs.getString("nombre_vulgar"));
                a.setNombreCientifico(rs.getString("nombre_cientifico"));
                a.setEstado(rs.getString("estado"));
                mapaEspeciePorAnimal.put(a.getIdAnimal(), a.getIdEspecie());
                animales.add(a);
            }
            comboAnimal.setItems(animales);
            comboAnimalNacimiento.setItems(FXCollections.observableArrayList(animales));
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Animales", "No se pudieron cargar los animales.\n\n" + e.getMessage());
        }
    }

    private void actualizarEspecie(ComboBox<Animal> combo, Label label) {
        Animal animal = combo.getValue();
        if (animal == null) {
            label.setText("—");
            return;
        }
        label.setText(mapaEspecies.getOrDefault(animal.getIdEspecie(),
                animal.getNombreCientifico() == null ? "Sin registrar" : animal.getNombreCientifico()));
    }

    private void cargarHabitaculos() {
        ObservableList<Habitaculo> lista = FXCollections.observableArrayList(habitaculoDAO.listar());
        comboHabitaculo.setItems(lista);
        comboHabitaculoNacimiento.setItems(FXCollections.observableArrayList(lista));
    }

    private void cargarPlantel() {
        tabla.setItems(FXCollections.observableArrayList(plantelDAO.listar()));
    }

    private void agregarCargaInicial() {
        if (!validarCargaInicial()) return;
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Tatú Carreta");
        confirmacion.setHeaderText("Confirmar carga inicial");
        confirmacion.setContentText("¿Está seguro de que desea incorporar este animal al plantel permanente?");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            PlantelPermanente p = crearPlantelDesdeFormulario();
            p.setCantidad(Integer.parseInt(txtCantidad.getText().trim()));
            plantelDAO.agregar(p);
            guardarIdentificacionCargaInicial(p.getIdAnimal());
            cargarIdentificaciones();
            mostrarMensaje(Alert.AlertType.INFORMATION, "Plantel Permanente", "El animal fue incorporado al plantel permanente.");
            limpiarCargaInicial();
            cargarPlantel();
        } catch (Exception ex) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "No se pudo guardar el registro.\n\n" + ex.getMessage());
        }
    }

    private void guardarIdentificacionCargaInicial(int idAnimal) {
        if ("Sin identificación".equals(comboTipoIdentificacion.getValue())) return;
        String numero = txtNumeroIdentificacion.getText().trim();
        if (numero.isBlank()) {
            mostrarMensaje(Alert.AlertType.WARNING, "Identificación", "Ingrese el número de identificación o seleccione 'Sin identificación'.");
            return;
        }
        String sqlCrear = """
                CREATE TABLE IF NOT EXISTS identificaciones_plantel (
                    id_identificacion INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_animal INTEGER NOT NULL,
                    tipo_identificacion TEXT NOT NULL,
                    numero_identificacion TEXT NOT NULL,
                    fecha_identificacion TEXT,
                    observaciones TEXT,
                    FOREIGN KEY (id_animal) REFERENCES animales(id_animal)
                )
                """;
        String sql = """
                INSERT INTO identificaciones_plantel
                (id_animal, tipo_identificacion, numero_identificacion, fecha_identificacion, observaciones)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection c = ConexionSQLite.conectar();
             PreparedStatement crear = c.prepareStatement(sqlCrear)) {
            crear.executeUpdate();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idAnimal);
                ps.setString(2, comboTipoIdentificacion.getValue());
                ps.setString(3, numero);
                ps.setString(4, datePickerFecha.getValue() == null ? null : formatoFecha.format(datePickerFecha.getValue()));
                ps.setString(5, txtObservaciones.getText().trim());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Identificación", "No se pudo guardar la identificación.\n\n" + e.getMessage());
        }
    }

    private void registrarNacimiento() {
        if (comboAnimalNacimiento.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione el animal o especie.");
            return;
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidadNacimiento.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje(Alert.AlertType.WARNING, "Cantidad inválida", "La cantidad nacida debe ser un número entero mayor que cero.");
            return;
        }
        if (datePickerNacimiento.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione la fecha del nacimiento.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Tatú Carreta");
        confirmacion.setHeaderText("Confirmar nacimiento");
        confirmacion.setContentText("¿Está seguro de que desea registrar " + cantidad +
                " nacimiento(s) de " + comboAnimalNacimiento.getValue().getNombreVulgar() + "?");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Animal animal = comboAnimalNacimiento.getValue();
        try (Connection c = ConexionSQLite.conectar()) {
            c.setAutoCommit(false);

            // Tabla propia para conservar el historial de nacimientos.
            try (PreparedStatement ps = c.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS nacimientos (
                        id_nacimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_animal INTEGER NOT NULL,
                        cantidad INTEGER NOT NULL,
                        fecha_nacimiento TEXT NOT NULL,
                        id_habitaculo INTEGER,
                        observaciones TEXT,
                        FOREIGN KEY (id_animal) REFERENCES animales(id_animal),
                        FOREIGN KEY (id_habitaculo) REFERENCES habitaculos(id_habitaculo)
                    )""")) {
                ps.executeUpdate();
            }

            Integer idHabitaculo = comboHabitaculoNacimiento.getValue() == null
                    ? null : comboHabitaculoNacimiento.getValue().getIdHabitaculo();

            try (PreparedStatement ps = c.prepareStatement("""
                    INSERT INTO nacimientos
                    (id_animal, cantidad, fecha_nacimiento, id_habitaculo, observaciones)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                ps.setInt(1, animal.getIdAnimal());
                ps.setInt(2, cantidad);
                ps.setString(3, formatoFecha.format(datePickerNacimiento.getValue()));
                if (idHabitaculo == null) ps.setNull(4, java.sql.Types.INTEGER);
                else ps.setInt(4, idHabitaculo);
                ps.setString(5, txtObservacionesNacimiento.getText().trim());
                ps.executeUpdate();
            }

            // Si el/los ejemplares nacidos ya reciben identificación,
            // se registra también en las identificaciones del plantel.
            if (!"Sin identificación".equals(comboTipoIdentificacionNacimiento.getValue())) {
                String numero = txtNumeroIdentificacionNacimiento.getText().trim();

                if (numero.isBlank()) {
                    c.rollback();
                    mostrarMensaje(Alert.AlertType.WARNING, "Identificación",
                            "Ingrese el número de identificación o seleccione 'Sin identificación'.");
                    return;
                }

                try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO identificaciones_plantel
                        (id_animal, tipo_identificacion, numero_identificacion, fecha_identificacion, observaciones)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
                    ps.setInt(1, animal.getIdAnimal());
                    ps.setString(2, comboTipoIdentificacionNacimiento.getValue());
                    ps.setString(3, numero);
                    ps.setString(4, formatoFecha.format(datePickerNacimiento.getValue()));
                    ps.setString(5, txtObservacionesNacimiento.getText().trim());
                    ps.executeUpdate();
                }
            }

            // El nacimiento se incorpora al registro activo del plantel.
            int actualizado = 0;
            try (PreparedStatement ps = c.prepareStatement("""
                    UPDATE plantel_permanente
                    SET cantidad = cantidad + ?
                    WHERE id_plantel = (
                        SELECT id_plantel
                        FROM plantel_permanente
                        WHERE id_animal = ? AND estado = 'Activo'
                        ORDER BY id_plantel DESC
                        LIMIT 1
                    )
                    """)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, animal.getIdAnimal());
                actualizado = ps.executeUpdate();
            }

            if (actualizado == 0) {
                c.rollback();
                mostrarMensaje(Alert.AlertType.WARNING, "Animal no pertenece al plantel",
                        "Primero debe incorporar este animal al Plantel Permanente.");
                return;
            }

            c.commit();
            mostrarMensaje(Alert.AlertType.INFORMATION, "Nacimiento registrado",
                    "El nacimiento fue registrado correctamente y la cantidad del plantel fue actualizada.");
            limpiarNacimiento();
            cargarPlantel();
        } catch (Exception ex) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "No se pudo registrar el nacimiento.\n\n" + ex.getMessage());
        }
    }

    private PlantelPermanente crearPlantelDesdeFormulario() {
        PlantelPermanente p = new PlantelPermanente();
        p.setIdAnimal(comboAnimal.getValue().getIdAnimal());
        p.setTipoUbicacion(comboUbicacion.getValue());
        if (comboHabitaculo.getValue() != null) p.setIdHabitaculo(comboHabitaculo.getValue().getIdHabitaculo());
        else p.setIdHabitaculo(null);
        p.setFechaIngresoPlantel(formatoFecha.format(datePickerFecha.getValue()));
        p.setEstado(comboEstado.getValue());
        p.setObservaciones(txtObservaciones.getText().trim());
        return p;
    }

    private boolean validarCargaInicial() {
        if (comboAnimal.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione un animal."); return false;
        }
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje(Alert.AlertType.WARNING, "Cantidad inválida", "La cantidad debe ser un número entero mayor que cero."); return false;
        }
        if (comboUbicacion.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione el tipo de ubicación."); return false;
        }
        if (necesitaHabitaculo() && comboHabitaculo.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione un habitáculo."); return false;
        }
        if (datePickerFecha.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione la fecha de ingreso al plantel."); return false;
        }
        if (comboEstado.getValue() == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione el estado."); return false;
        }
        if (!"Sin identificación".equals(comboTipoIdentificacion.getValue())
                && txtNumeroIdentificacion.getText().trim().isBlank()) {
            mostrarMensaje(Alert.AlertType.WARNING, "Identificación", "Ingrese el número de identificación o seleccione 'Sin identificación'.");
            return false;
        }
        return true;
    }

    private boolean necesitaHabitaculo() {
        String u = comboUbicacion.getValue();
        return "Habitáculo".equals(u) || "Recinto".equals(u);
    }

    private void actualizarVisibilidadHabitaculo() {
        boolean mostrar = necesitaHabitaculo();
        comboHabitaculo.setDisable(!mostrar);
        if (!mostrar) comboHabitaculo.setValue(null);
    }

    private void cargarRegistroEnFormulario(PlantelPermanente p) {
        seleccionarAnimal(p.getIdAnimal());
        txtCantidad.setText(String.valueOf(p.getCantidad()));
        comboUbicacion.setValue(p.getTipoUbicacion());
        actualizarVisibilidadHabitaculo();
        if (p.getIdHabitaculo() != null) seleccionarHabitaculo(p.getIdHabitaculo());
        try {
            if (p.getFechaIngresoPlantel() != null && !p.getFechaIngresoPlantel().isBlank())
                datePickerFecha.setValue(LocalDate.parse(p.getFechaIngresoPlantel(), formatoFecha));
        } catch (Exception ex) { datePickerFecha.setValue(null); }
        comboEstado.setValue(p.getEstado());
        txtObservaciones.setText(p.getObservaciones() == null ? "" : p.getObservaciones());
        mostrarCargaInicial();
    }

    private void seleccionarAnimal(int idAnimal) {
        for (Animal a : comboAnimal.getItems()) if (a.getIdAnimal() == idAnimal) { comboAnimal.setValue(a); return; }
    }

    private void seleccionarHabitaculo(int idHabitaculo) {
        for (Habitaculo h : comboHabitaculo.getItems()) if (h.getIdHabitaculo() == idHabitaculo) { comboHabitaculo.setValue(h); return; }
    }

    private void limpiarCargaInicial() {
        comboAnimal.setValue(null); txtCantidad.clear(); comboUbicacion.setValue(null);
        comboHabitaculo.setValue(null); comboHabitaculo.setDisable(true); datePickerFecha.setValue(null);
        comboEstado.setValue("Activo");
        comboTipoIdentificacion.setValue("Sin identificación");
        txtNumeroIdentificacion.clear();
        txtNumeroIdentificacion.setDisable(true);
        lblEspecieCargaInicial.setText("—");
        txtObservaciones.clear();
        tabla.getSelectionModel().clearSelection();
    }

    private void limpiarNacimiento() {
        comboAnimalNacimiento.setValue(null);
        txtCantidadNacimiento.clear();
        comboHabitaculoNacimiento.setValue(null);
        datePickerNacimiento.setValue(null);
        comboTipoIdentificacionNacimiento.setValue("Sin identificación");
        txtNumeroIdentificacionNacimiento.clear();
        txtNumeroIdentificacionNacimiento.setDisable(true);
        txtObservacionesNacimiento.clear();
        lblEspecieNacimiento.setText("—");
    }

    private void mostrarSoloPlantel() {
        tarjetaCargaInicial.setVisible(false); tarjetaCargaInicial.setManaged(false);
        tarjetaNacimiento.setVisible(false); tarjetaNacimiento.setManaged(false);
        cargarPlantel();
    }

    private void mostrarCargaInicial() {
        tarjetaNacimiento.setVisible(false); tarjetaNacimiento.setManaged(false);
        tarjetaCargaInicial.setVisible(true); tarjetaCargaInicial.setManaged(true);
    }

    private void mostrarNacimiento() {
        tarjetaCargaInicial.setVisible(false); tarjetaCargaInicial.setManaged(false);
        tarjetaNacimiento.setVisible(true); tarjetaNacimiento.setManaged(true);
    }

    private Label crearEtiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #3D5143;");
        return l;
    }

    private Button crearBotonPrincipal(String texto) {
        Button b = new Button(texto);
        b.setPrefHeight(36);
        b.setStyle("-fx-background-color: #3D7048; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 8 16;");
        return b;
    }

    private Button crearBotonNacimiento(String texto) {
        Button b = crearBotonPrincipal(texto);
        b.setStyle("-fx-background-color: #4D8560; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 8 16;");
        return b;
    }

    private Button crearBotonSecundario(String texto) {
        Button b = new Button(texto);
        b.setPrefHeight(36);
        b.setStyle("-fx-background-color: #E5EEE6; -fx-text-fill: #315E3A; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 8 16;");
        return b;
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    /** Compatibilidad con código anterior. */
    public void mostrar() {
        Stage ventana = new Stage();
        Scene escena = new Scene(crearContenido(), 1100, 750);
        ventana.setTitle("Tatú Carreta - Plantel Permanente");
        ventana.setScene(escena);
        ventana.show();
    }
}
