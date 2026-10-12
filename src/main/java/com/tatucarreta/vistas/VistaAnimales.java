package com.tatucarreta.vistas;

import com.tatucarreta.data.SistemaDAO;
import com.tatucarreta.ui.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Registro maestro de animales.
 *
 * IMPORTANTE:
 * - Solo muestra animales activos con existencia real dentro de la Reserva.
 * - Permite filtrar por existencia proveniente de Actas o del Plantel Permanente.
 * - El doble click es SOLO informativo.
 * - Los animales existentes no se modifican desde esta vista.
 */
public class VistaAnimales extends VistaCRUD {

    private final ComboBox<String> filtroUbicacion = Estilos.combo();

    public VistaAnimales() {

        super(
                "Animales",
                "Registro maestro de ejemplares. La cantidad se calcula desde los registros operativos.",
                new String[] {
                        "Animal",
                        "Identificación",
                        "Especie",
                        "Nombre científico",
                        "Cantidad",
                        "Origen",
                        "Estado"
                });

        // ------------------------------------------------------------
        // ACCIONES CRUD
        // ------------------------------------------------------------

        // Esta vista es únicamente informativa.
        // Los animales se crean/gestionan desde Ingresos y Plantel Permanente.
        mostrarBotonAgregar(false);
        mostrarBotonModificar(false);
        mostrarBotonInactivos(false);
        mostrarBotonEliminar(false);

        // ------------------------------------------------------------
        // FILTRO DE EXISTENCIA
        // ------------------------------------------------------------

        filtroUbicacion.getItems().setAll(
                "Todos los activos",
                "Animales con actas",
                "Animales en Acta General / Plantel");

        filtroUbicacion.setValue("Todos los activos");
        filtroUbicacion.setPrefWidth(235);
        filtroUbicacion.setMinWidth(220);
        filtroUbicacion.setMaxWidth(250);

        Label etiquetaFiltro = Estilos.etiqueta("MOSTRAR");

        VBox bloqueFiltro = new VBox(
                4,
                etiquetaFiltro,
                filtroUbicacion);

        tools.getChildren().add(1, bloqueFiltro);

        filtroUbicacion.setOnAction(e -> cargar(buscador.getText()));

        mostrarMensajeVista(
                "Esta vista es únicamente informativa y muestra todos los ejemplares activos que permanecen en la Reserva. " +
                        "Los ejemplares con identificación se muestran individualmente y los ejemplares sin identificación se agrupan. " +
                        "El doble click solo permite consultar información.");

        // ------------------------------------------------------------
        // TABLA
        // ------------------------------------------------------------

        configurarTabla();

        // ------------------------------------------------------------
        // DOBLE CLICK = INFORMACIÓN, NUNCA EDICIÓN
        // ------------------------------------------------------------

        mostrarDobleClick(false);

        tabla.setRowFactory(tv -> {

            TableRow<SistemaDAO.Fila> fila = new TableRow<>();

            fila.setOnMouseClicked(e -> {

                if (e.getButton() == MouseButton.PRIMARY &&
                        e.getClickCount() == 2 &&
                        !fila.isEmpty()) {

                    mostrarInformacion(fila.getItem());
                }
            });

            return fila;
        });

        cargar("");
    }

    private void configurarTabla() {

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        double[] minimos = {
                150, // Animal
                190, // Identificación
                150, // Especie
                190, // Nombre científico
                85,  // Cantidad
                150, // Origen
                90   // Estado
        };

        for (int i = 0; i < tabla.getColumns().size(); i++) {

            TableColumn<SistemaDAO.Fila, ?> columna = tabla.getColumns().get(i);

            columna.setMinWidth(minimos[i]);

            columna.setPrefWidth(minimos[i]);
        }
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    @Override
    protected List<SistemaDAO.Fila> consultar(String q) {

        return SistemaDAO.animalesPorUbicacion(
                q,
                filtroActual());
    }

    @Override
    protected List<SistemaDAO.Fila> consultarInactivos(String q) {

        // Esta vista trabaja únicamente con activos.
        return List.of();
    }

    private String filtroActual() {

        String valor = filtroUbicacion.getValue();

        if ("Animales con actas".equals(valor)) {
            return "ACTAS";
        }

        if ("Animales en Acta General / Plantel".equals(valor)) {
            return "PLANTEL";
        }

        return "TODOS";
    }

    // ============================================================
    // ALTA DE ANIMAL
    // ============================================================

    @Override
    protected void agregarNuevo() {

        form(0, null);
    }

    /**
     * Se conserva el método requerido por VistaCRUD, pero no abre edición.
     * Si por algún motivo se invoca desde la clase base, solamente muestra
     * información de lectura.
     */
    @Override
    protected void editarSeleccionado() {

        SistemaDAO.Fila f = tabla.getSelectionModel().getSelectedItem();

        if (f != null) {
            mostrarInformacion(f);
        }
    }

    @Override
    protected void activarRegistro(int id) {

        // Los registros inactivos no se gestionan desde esta vista.
        SistemaDAO.activar("animales", "id_animal", id);
    }

    @Override
    protected void desactivar(int id) {

        // El botón está oculto, pero se conserva la lógica original para no
        // romper el contrato de VistaCRUD si otra parte la utiliza.
        if (!SistemaDAO.animalPuedeEliminar(id)) {

            info(
                    "No se puede eliminar",
                    "Este animal está asociado a un ingreso/acta o al Plantel Permanente. El historial debe conservarse.");

            return;
        }

        if (confirmarOK(
                "Eliminar animal",
                "¿Estás segura de que querés enviar este animal a registros inactivos?")) {

            SistemaDAO.baja(
                    "animales",
                    "id_animal",
                    id);

            cargar(buscador.getText());
        }
    }

    // ============================================================
    // INFORMACIÓN DEL ANIMAL
    // ============================================================

    private void mostrarInformacion(SistemaDAO.Fila fila) {

        if (fila == null) {
            return;
        }

        try {

            SistemaDAO.Fila detalle =
                    SistemaDAO.animalInformacion(fila.id());

            if (detalle == null) {

                error(
                        "No se puede consultar",
                        "No se encontró información del animal seleccionado.");

                return;
            }

            List<String> c = detalle.c();

            String animal = valor(c, 1);
            String especie = valor(c, 2);
            String cientifico = valor(c, 3);
            String origen = valor(c, 4);
            String plantel = valor(c, 5);
            String actas = valor(c, 6);
            String identificacion = valor(c, 7);
            String estado = valor(c, 8);

            VBox contenido = new VBox(12);

            contenido.setPadding(new Insets(20));
            contenido.setFillWidth(true);
            contenido.setPrefWidth(680);

            contenido.setStyle(
                    "-fx-background-color:white;" +
                    "-fx-background-radius:16;" +
                    "-fx-border-color:" + Estilos.BORDE + ";" +
                    "-fx-border-radius:16;");

            Label titulo = new Label("INFORMACIÓN DEL ANIMAL");
            titulo.setStyle(
                    "-fx-font-size:20px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");

            Label subtitulo = new Label(
                    "Consulta de solo lectura · Registro maestro #" + fila.id());

            subtitulo.setStyle(
                    "-fx-font-size:12px;" +
                    "-fx-text-fill:" + Estilos.GRIS + ";");

            GridPane datos = new GridPane();
            datos.setHgap(18);
            datos.setVgap(10);
            datos.setPadding(new Insets(10, 0, 4, 0));

            agregarDato(datos, 0, 0, "Animal", animal);
            agregarDato(datos, 1, 0, "Especie", especie);
            agregarDato(datos, 0, 1, "Nombre científico", cientifico);
            agregarDato(datos, 1, 1, "Origen / procedencia", origen);
            agregarDato(datos, 0, 2, "Cantidad por actas", actas);
            agregarDato(datos, 1, 2, "Cantidad en Plantel", plantel);
            agregarDato(datos, 0, 3, "Cantidad total actual", sumar(actas, plantel));
            agregarDato(datos, 1, 3, "Estado", estado);

            VBox identificacionBox = new VBox(5);

            Label identificacionTitulo = new Label("Identificación");
            identificacionTitulo.setStyle(
                    "-fx-font-weight:bold;" +
                    "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";");

            Label identificacionValor = new Label(identificacion);
            identificacionValor.setWrapText(true);
            identificacionValor.setMaxWidth(Double.MAX_VALUE);
            identificacionValor.setStyle(
                    "-fx-background-color:" + Estilos.VERDE_SUAVE + ";" +
                    "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";" +
                    "-fx-padding:10;" +
                    "-fx-background-radius:8;");

            identificacionBox.getChildren().addAll(
                    identificacionTitulo,
                    identificacionValor);

            Label nota = new Label(
                    "La información de cantidad se obtiene de los registros operativos activos. " +
                    "Si el animal fue trasladado, falleció o dejó de tener existencia disponible, " +
                    "esa cantidad deja de computarse como existencia actual.");

            nota.setWrapText(true);
            nota.setStyle(
                    "-fx-background-color:rgba(115,77,44,0.08);" +
                    "-fx-text-fill:" + Estilos.GRIS + ";" +
                    "-fx-padding:10;" +
                    "-fx-background-radius:8;");

            contenido.getChildren().addAll(
                    new VBox(2, titulo, subtitulo),
                    datos,
                    identificacionBox,
                    nota);

            Dialog<ButtonType> dialog = new Dialog<>();

            dialog.setTitle("Tatú Carreta");
            dialog.setHeaderText(null);
            dialog.getDialogPane().setContent(contenido);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.setResizable(true);
            dialog.setWidth(760);
            dialog.setHeight(560);

            dialog.setOnShown(e -> {
                Estilos.estilizarDialog(dialog.getDialogPane());
                Node botonCerrar = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
                if (botonCerrar instanceof Button b) {
                    b.setText("CERRAR");
                }
            });

            dialog.showAndWait();

        } catch (Exception e) {

            error(
                    "No se pudo mostrar la información",
                    e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private void agregarDato(
            GridPane g,
            int columna,
            int fila,
            String titulo,
            String valor) {

        VBox box = new VBox(3);

        Label t = new Label(titulo);
        t.setStyle(
                "-fx-font-size:11px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:" + Estilos.GRIS + ";");

        Label v = new Label(
                valor == null || valor.isBlank() ? "—" : valor);

        v.setWrapText(true);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setStyle(
                "-fx-font-size:14px;" +
                "-fx-text-fill:#263238;");

        box.getChildren().addAll(t, v);

        g.add(box, columna, fila);

        GridPane.setHgrow(box, Priority.ALWAYS);
    }

    private String valor(List<String> valores, int indice) {

        return valores.size() > indice && valores.get(indice) != null
                ? valores.get(indice)
                : "";
    }

    private String sumar(String a, String b) {

        try {
            return String.valueOf(
                    Integer.parseInt(a) + Integer.parseInt(b));
        } catch (Exception e) {
            return "0";
        }
    }

    // ============================================================
    // FORMULARIO DE ALTA
    // ============================================================

    private void form(int id, SistemaDAO.Fila f) {

        ComboBox<SistemaDAO.Opcion> esp = Estilos.combo();
        esp.getItems().setAll(SistemaDAO.especiesOpciones());

        Button nueva = Estilos.botonSec("＋ Especie");

        HBox espBox = new HBox(8, esp, nueva);
        HBox.setHgrow(esp, Priority.ALWAYS);

        nueva.setPrefWidth(115);

        TextField vul = Estilos.campo("Nombre común"),
                cient = Estilos.campo("Nombre científico (opcional)"),
                orig = Estilos.campo("Origen o procedencia");

        if (f != null) {

            vul.setText(f.c().get(1));
            cient.setText(f.c().get(3));
            orig.setText(f.c().get(5));

            esp.getItems()
                    .stream()
                    .filter(o -> o.texto().equals(f.c().get(2)))
                    .findFirst()
                    .ifPresent(esp::setValue);
        }

        nueva.setOnAction(e -> crearEspecie(esp));

        GridPane g = Formulario.grid();

        Formulario.add(g, 0, "Especie", espBox, true);
        Formulario.add(g, 1, "Nombre vulgar", vul, true);
        Formulario.add(g, 2, "Nombre científico", cient, false);
        Formulario.add(g, 3, "Origen", orig, false);

        Label nota = new Label(
                "La cantidad no se carga aquí: se actualiza desde Ingresos, Plantel, nacimientos y fallecimientos.");

        nota.setWrapText(true);
        nota.setStyle(
                "-fx-background-color:" + Estilos.VERDE_SUAVE + ";" +
                "-fx-text-fill:" + Estilos.VERDE_OSCURO + ";" +
                "-fx-padding:10;" +
                "-fx-background-radius:8;");

        g.add(nota, 1, 4);

        Dialog<ButtonType> d = Formulario.dialog(
                id == 0 ? "Nuevo animal" : "Modificar animal",
                g);

        d.showAndWait().ifPresent(x -> {

            if (x == ButtonType.OK) {

                try {

                    if (esp.getValue() == null || !validar(vul.getText())) {
                        throw new IllegalArgumentException(
                                "Elegí una especie y completá el nombre del animal.");
                    }

                    if (confirmarOK(
                            "Agregar animal",
                            "¿Estás segura de que querés agregar este animal?")) {

                        SistemaDAO.guardarAnimal(
                                0,
                                esp.getValue().id(),
                                vul.getText().trim(),
                                cient.getText().trim(),
                                orig.getText().trim());

                        cargar(buscador.getText());
                    }

                } catch (Exception e) {

                    error(
                            "No se pudo guardar",
                            e.getMessage());
                }
            }
        });
    }

    private void crearEspecie(ComboBox<SistemaDAO.Opcion> esp) {

        TextField n = Estilos.campo("Nombre de la especie");

        GridPane g = Formulario.grid();
        Formulario.add(g, 0, "Nombre", n, true);

        Dialog<ButtonType> d = Formulario.dialog(
                "Crear especie sin salir del animal",
                g);

        d.showAndWait().ifPresent(x -> {

            if (x == ButtonType.OK &&
                    validar(n.getText()) &&
                    confirmarOK(
                            "Agregar especie",
                            "¿Querés agregar esta especie?")) {

                // Se conserva el comportamiento original de este proyecto.
                // La creación de especies sigue pendiente de la implementación
                // existente en el DAO.
            }
        });
    }
}
