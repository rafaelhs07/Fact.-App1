package org.example.fact_app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class SceneManager {

    private SceneManager() {
    }

    public static void abrirVentana(String recurso, String titulo)
            throws IOException {

        URL url = SceneManager.class.getResource(recurso);

        if (url == null) {
            throw new IOException("FXML no encontrado: " + recurso);
        }

        FXMLLoader loader = new FXMLLoader(url);

        Stage ventana = new Stage();
        ventana.setTitle(titulo);
        ventana.setScene(new Scene(loader.load()));
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.showAndWait();
    }
}