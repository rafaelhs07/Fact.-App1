package org.example.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.fact_app.model.Cargo;

public class CargoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;

    @FXML private TableView<Cargo> tblDatos;
    @FXML private TableColumn<Cargo, Integer> colId;
    @FXML private TableColumn<Cargo, String> colNombre;

    private static final ObservableList<Cargo> cargos =
            FXCollections.observableArrayList();

    private static int siguienteId = 1;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        tblDatos.setItems(cargos);

        tblDatos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        txtId.setText(String.valueOf(seleccionado.getId()));
                        txtNombre.setText(seleccionado.getNombre());
                    }
                });
    }

    @FXML
    private void onGuardar() {
        if (tblDatos.getSelectionModel().getSelectedItem() != null) {
            mensaje("Pulsa Limpiar para crear otro cargo "
                    + "o Modificar para actualizar el seleccionado.");
            return;
        }

        if (!validar(null)) {
            return;
        }

        Cargo cargo = new Cargo(
                siguienteId++,
                txtNombre.getText().trim(),
                ""
        );

        cargos.add(cargo);
        onLimpiar();
    }

    @FXML
    private void onActualizar() {
        Cargo seleccionado = tblDatos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona un cargo en la tabla.");
            return;
        }

        if (!validar(seleccionado)) {
            return;
        }

        seleccionado.setNombre(txtNombre.getText().trim());

        tblDatos.refresh();
        onLimpiar();
    }

    @FXML
    private void onEliminar() {
        Cargo seleccionado = tblDatos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona un cargo en la tabla.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar el cargo " + seleccionado.getNombre() + "?",
                ButtonType.OK,
                ButtonType.CANCEL
        );
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.OK) {
            cargos.remove(seleccionado);
            onLimpiar();
        }
    }

    @FXML
    private void onLimpiar() {
        tblDatos.getSelectionModel().clearSelection();
        txtId.clear();
        txtNombre.clear();
        txtNombre.requestFocus();
    }

    @FXML
    private void onVolver() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private boolean validar(Cargo actual) {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mensaje("Escribe el nombre del cargo.");
            return false;
        }

        boolean repetido = cargos.stream()
                .anyMatch(cargo ->
                        cargo != actual
                                && cargo.getNombre().equalsIgnoreCase(nombre)
                );

        if (repetido) {
            mensaje("Ya existe un cargo con ese nombre.");
            return false;
        }

        return true;
    }

    private void mensaje(String texto) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Cargos");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}