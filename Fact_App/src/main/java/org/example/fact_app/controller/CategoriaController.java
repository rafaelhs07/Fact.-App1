package org.example.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.fact_app.dao.CategoriaDAO;
import org.example.fact_app.model.Categoria;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private CheckBox chkActiva;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Categoria> categoriasLista = FXCollections.observableArrayList();

    public static ObservableList<Categoria> getCategorias() {
        try {
            return FXCollections.observableArrayList(new CategoriaDAO().listar());
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            return FXCollections.observableArrayList();
        }
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        colActiva.setCellFactory(columna -> new TableCell<Categoria, Boolean>() {
            @Override
            protected void updateItem(Boolean activa, boolean empty) {
                super.updateItem(activa, empty);
                if (empty || activa == null) {
                    setText(null);
                } else {
                    setText(activa ? "Sí" : "No");
                }
            }
        });

        tblCategorias.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionada) -> {
                    if (seleccionada != null) {
                        txtId.setText(String.valueOf(seleccionada.getId()));
                        txtNombre.setText(seleccionada.getNombre());
                        if (seleccionada.getDescripcion() != null) {
                            txtDescripcion.setText(seleccionada.getDescripcion());
                        } else {
                            txtDescripcion.clear();
                        }
                        if (chkActiva != null) {
                            chkActiva.setSelected(seleccionada.isActiva());
                        }
                    }
                });

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<Categoria> lista = categoriaDAO.listar();
            categoriasLista.setAll(lista);
            tblCategorias.setItems(categoriasLista);
        } catch (SQLException e) {
            System.err.println("Error al cargar categorías de la base de datos: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos",
                    "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    private Categoria obtenerCategoriaFormulario(Integer id) {
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio y no puede consistir únicamente en espacios en blanco.");
        }

        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
        boolean activa = chkActiva != null && chkActiva.isSelected();

        return new Categoria(id, nombre, activa, descripcion);
    }

    @FXML
    private void onGuardar() {
        if (tblCategorias.getSelectionModel().getSelectedItem() != null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Operación no permitida",
                    "Pulsa Limpiar para crear otra categoría o Modificar para actualizar la seleccionada.");
            return;
        }

        try {
            Categoria categoria = obtenerCategoriaFormulario(null);

            if (categoriaDAO.existeNombre(categoria.getNombre(), null)) {
                txtNombre.requestFocus();
                mostrarAlerta(Alert.AlertType.WARNING, "Nombre duplicado",
                        "Ya existe una categoría con ese nombre.");
                return;
            }

            boolean exito = categoriaDAO.guardar(categoria);
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                        "Categoría guardada correctamente en la base de datos.");
                try {
                    cargarDatos();
                    onLimpiar();
                } catch (Exception eReload) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia de sincronización",
                            "La categoría fue guardada con éxito, pero ocurrió un problema al refrescar la lista.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo guardar la categoría en la base de datos (0 filas afectadas).");
            }

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error SQL al guardar categoría: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", traducirErrorSql(e));
        }
    }

    @FXML
    private void onActualizar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null || seleccionada.getId() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Selecciona una categoría en la tabla.");
            return;
        }

        try {
            Categoria categoria = obtenerCategoriaFormulario(seleccionada.getId());

            if (categoriaDAO.existeNombre(categoria.getNombre(), seleccionada.getId())) {
                txtNombre.requestFocus();
                mostrarAlerta(Alert.AlertType.WARNING, "Nombre duplicado",
                        "Ya existe una categoría con ese nombre.");
                return;
            }

            boolean exito = categoriaDAO.actualizar(categoria);
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                        "Categoría actualizada correctamente en la base de datos.");
                try {
                    cargarDatos();
                    onLimpiar();
                } catch (Exception eReload) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia de sincronización",
                            "La categoría fue actualizada con éxito, pero ocurrió un problema al refrescar la lista.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo actualizar la categoría en la base de datos (0 filas afectadas).");
            }

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error SQL al actualizar categoría: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", traducirErrorSql(e));
        }
    }

    @FXML
    private void onEliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null || seleccionada.getId() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Selecciona una categoría en la tabla.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría " + seleccionada.getNombre() + "?",
                ButtonType.OK,
                ButtonType.CANCEL
        );
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            if (categoriaDAO.tieneProductos(seleccionada.getId())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Operación no permitida",
                        "No puede eliminar la categoría porque tiene productos asociados.");
                return;
            }

            boolean exito = categoriaDAO.eliminar(seleccionada.getId());
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                        "Categoría eliminada correctamente de la base de datos.");
                try {
                    cargarDatos();
                    onLimpiar();
                } catch (Exception eReload) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia de sincronización",
                            "La categoría fue eliminada con éxito, pero ocurrió un problema al refrescar la lista.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo eliminar la categoría en la base de datos (0 filas afectadas).");
            }

        } catch (SQLException e) {
            System.err.println("Error SQL al eliminar categoría: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", traducirErrorSql(e));
        }
    }

    @FXML
    private void onLimpiar() {
        tblCategorias.getSelectionModel().clearSelection();
        txtId.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        if (chkActiva != null) {
            chkActiva.setSelected(true);
        }
        txtNombre.requestFocus();
    }

    @FXML
    private void onVolver() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    private String traducirErrorSql(SQLException e) {
        String estado = e.getSQLState();
        int codigo = e.getErrorCode();
        if (estado != null) {
            if (estado.startsWith("23505") || codigo == 2601 || codigo == 2627) {
                return "Infracción de unicidad: ya existe una categoría con ese nombre en la base de datos.";
            } else if (estado.startsWith("23503") || codigo == 547) {
                return "No puede eliminar la categoría porque tiene productos asociados.";
            } else if (estado.startsWith("23514")) {
                return "Los datos no cumplen las restricciones de integridad de la base de datos.";
            }
        }
        return "Ocurrió un error en la base de datos: " + e.getMessage();
    }
}