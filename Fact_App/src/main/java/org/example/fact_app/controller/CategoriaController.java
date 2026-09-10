package org.example.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.fact_app.model.Categoria;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;

    private static final ObservableList<Categoria> categorias =
            FXCollections.observableArrayList(
                    new Categoria(1, "Alimentos", true),
                    new Categoria(2, "Bebidas", true),
                    new Categoria(3, "Limpieza", true)
            );

    private static int siguienteId = 4;

    public static ObservableList<Categoria> getCategorias() {
        return categorias;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );

        tblCategorias.setItems(categorias);

        tblCategorias.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {
                    if (seleccionada != null) {
                        txtId.setText(String.valueOf(seleccionada.getId()));
                        txtNombre.setText(seleccionada.getNombre());
                        txtDescripcion.setText(seleccionada.getDescripcion());
                    }
                });
    }

    @FXML
    private void onGuardar() {
        if (tblCategorias.getSelectionModel().getSelectedItem() != null) {
            mensaje("Pulsa Limpiar para crear otra categoría "
                    + "o Modificar para actualizar la seleccionada.");
            return;
        }

        if (!validar(null)) {
            return;
        }

        Categoria categoria = new Categoria(
                siguienteId++,
                txtNombre.getText().trim(),
                true,
                txtDescripcion.getText().trim()
        );

        categorias.add(categoria);
        onLimpiar();
    }

    @FXML
    private void onActualizar() {
        Categoria seleccionada =
                tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje("Selecciona una categoría en la tabla.");
            return;
        }

        if (!validar(seleccionada)) {
            return;
        }

        seleccionada.setNombre(txtNombre.getText().trim());
        seleccionada.setDescripcion(txtDescripcion.getText().trim());

        tblCategorias.refresh();
        onLimpiar();
    }

    @FXML
    private void onEliminar() {
        Categoria seleccionada =
                tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje("Selecciona una categoría en la tabla.");
            return;
        }

        if (ProductoController.categoriaEnUso(seleccionada)) {
            mensaje("No puedes eliminar una categoría "
                    + "que está asignada a productos.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría " + seleccionada.getNombre() + "?",
                ButtonType.OK,
                ButtonType.CANCEL
        );
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.OK) {
            categorias.remove(seleccionada);
            onLimpiar();
        }
    }

    @FXML
    private void onLimpiar() {
        tblCategorias.getSelectionModel().clearSelection();
        txtId.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtNombre.requestFocus();
    }

    @FXML
    private void onVolver() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private boolean validar(Categoria actual) {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mensaje("Escribe el nombre de la categoría.");
            return false;
        }

        boolean repetida = categorias.stream()
                .anyMatch(categoria ->
                        categoria != actual
                                && categoria.getNombre().equalsIgnoreCase(nombre)
                );

        if (repetida) {
            mensaje("Ya existe una categoría con ese nombre.");
            return false;
        }

        return true;
    }

    private void mensaje(String texto) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Categorías");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}