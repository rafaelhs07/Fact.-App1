package org.example.fact_app.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class FacturacionApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL recurso = FacturacionApplication.class.getResource(
                "/org/example/fact_app/fxml/menu-principal.fxml"
        );

        if (recurso == null) {
            throw new IOException("No se encontró menu-principal.fxml");
        }

        FXMLLoader loader = new FXMLLoader(recurso);

        stage.setTitle("Sistema de facturación");
        stage.setScene(new Scene(loader.load(), 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}