package org.example.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.fact_app.dao.CategoriaDAO;
import org.example.fact_app.model.Categoria;

import java.util.List;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;

    // Instancia del DAO para operaciones en BD
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private ObservableList<Categoria> categoriasLista = FXCollections.observableArrayList();

    // Ahora este método estático consulta la base de datos para otros controladores
    public static ObservableList<Categoria> getCategorias() {
        return FXCollections.observableArrayList(new CategoriaDAO().listar());
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        // Seguirá funcionando en la interfaz si lo tienes en la clase, aunque no se guarde en BD
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        cargarDatos();

        tblCategorias.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {
                    if (seleccionada != null) {
                        txtId.setText(String.valueOf(seleccionada.getId()));
                        txtNombre.setText(seleccionada.getNombre());
                        // Validar si getDescripcion existe en tu modelo para evitar errores
                        if (seleccionada.getDescripcion() != null) {
                            txtDescripcion.setText(seleccionada.getDescripcion());
                        } else {
                            txtDescripcion.clear();
                        }
                    }
                });
    }

    // Método para refrescar el TableView desde la BD
    private void cargarDatos() {
        List<Categoria> lista = categoriaDAO.listar();
        categoriasLista.setAll(lista);
        tblCategorias.setItems(categoriasLista);
    }

    @FXML
    private void onGuardar() {
        if (tblCategorias.getSelectionModel().getSelectedItem() != null) {
            mensaje("Pulsa Limpiar para crear otra categoría o Modificar para actualizar la seleccionada.");
            return;
        }

        if (!validar(null)) {
            return;
        }

        // Ya no mandamos ID, PostgreSQL lo autogenerará
        Categoria categoria = new Categoria();
        categoria.setNombre(txtNombre.getText().trim());
        categoria.setActiva(true);
        categoria.setDescripcion(txtDescripcion.getText().trim());

        if (categoriaDAO.guardar(categoria)) {
            cargarDatos(); // Refrescamos la tabla
            onLimpiar();
        } else {
            mensaje("Error al guardar en la base de datos.");
        }
    }

    @FXML
    private void onActualizar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje("Selecciona una categoría en la tabla.");
            return;
        }

        if (!validar(seleccionada)) {
            return;
        }

        seleccionada.setNombre(txtNombre.getText().trim());
        seleccionada.setDescripcion(txtDescripcion.getText().trim());

        if (categoriaDAO.actualizar(seleccionada)) {
            cargarDatos();
            onLimpiar();
        } else {
            mensaje("Error al actualizar en la base de datos.");
        }
    }

    @FXML
    private void onEliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje("Selecciona una categoría en la tabla.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría " + seleccionada.getNombre() + "?",
                ButtonType.OK,
                ButtonType.CANCEL
        );
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {

            if (categoriaDAO.eliminar(seleccionada.getId())) {
                cargarDatos();
                onLimpiar();
            } else {
                mensaje("No se pudo eliminar. Es muy probable que esta categoría esté siendo usada por un producto registrado.");
            }
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

        boolean repetida = categoriasLista.stream()
                .anyMatch(categoria ->
                        categoria != actual && categoria.getNombre().equalsIgnoreCase(nombre)
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