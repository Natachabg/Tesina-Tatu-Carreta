package com.tatucarreta.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/** Campo de contraseña con botón para mostrar/ocultar el texto. */
public final class OjoPassword {
    private OjoPassword() {}

    public static Campo crear(String prompt) {
        PasswordField oculto = Estilos.password(prompt);
        TextField visible = Estilos.campo(prompt);
        visible.setManaged(false);
        visible.setVisible(false);

        Button ojo = new Button("👁");
        ojo.setFocusTraversable(false);
        ojo.setPrefSize(42, 42);
        ojo.setStyle("-fx-background-color:transparent;-fx-text-fill:" + Estilos.GRIS + ";-fx-font-size:17px;-fx-cursor:hand;");

        StackPane caja = new StackPane(oculto, visible);
        HBox.setHgrow(caja, Priority.ALWAYS);
        caja.setAlignment(Pos.CENTER_RIGHT);

        StackPane contenedor = new StackPane(caja, ojo);
        StackPane.setAlignment(ojo, Pos.CENTER_RIGHT);

        ojo.setOnAction(e -> {
            boolean mostrar = !visible.isVisible();
            if (mostrar) {
                visible.setText(oculto.getText());
                visible.setManaged(true);
                visible.setVisible(true);
                oculto.setManaged(false);
                oculto.setVisible(false);
                visible.requestFocus();
            } else {
                oculto.setText(visible.getText());
                oculto.setManaged(true);
                oculto.setVisible(true);
                visible.setManaged(false);
                visible.setVisible(false);
                oculto.requestFocus();
            }
        });

        return new Campo(oculto, visible, contenedor);
    }

    public record Campo(PasswordField password, TextField visible, StackPane control) {
        public String getText() { return password.isVisible() ? password.getText() : visible.getText(); }
        public void clear() { password.clear(); visible.clear(); }
    }
}
