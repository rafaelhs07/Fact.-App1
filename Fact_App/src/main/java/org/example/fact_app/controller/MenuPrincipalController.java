package org.example.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.example.fact_app.util.SceneManager;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private void abrirProductos() {
        abrirFormulario("producto-view.fxml", "Gestión de Productos");
    }

    @FXML
    private void abrirCategorias() {
        abrirFormulario("categoria-view.fxml", "Gestión de Categorías");
    }

    @FXML
    private void abrirCargos() {
        abrirFormulario("cargo-view.fxml", "Gestión de Cargos");
    }

    private void abrirFormulario(String archivo, String titulo) {
        try {
            SceneManager.abrirVentana(
                    "/org/example/fact_app/fxml/" + archivo,
                    titulo
            );

        } catch (IOException e) {
            e.printStackTrace();

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al abrir formulario");
            alerta.setHeaderText("No se pudo abrir " + titulo);
            alerta.setContentText(
                    "Archivo: " + archivo + "\n\n"
                            + "Detalle: " + e.getMessage()
            );
            alerta.showAndWait();
        }
    }

    @FXML
    private void salir() {
        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Deseas cerrar la aplicación?",
                ButtonType.OK,
                ButtonType.CANCEL
        );

        alerta.setTitle("Salir");
        alerta.setHeaderText(null);

        if (alerta.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.OK) {
            Platform.exit();
        }
    }
}