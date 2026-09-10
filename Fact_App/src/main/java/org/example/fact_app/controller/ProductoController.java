package org.example.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.fact_app.model.Categoria;
import org.example.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;

    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private static final ObservableList<Producto> productos =
            FXCollections.observableArrayList();

    private static int siguienteId = 1;

    private String rutaImagen;

    @FXML
    private void initialize() {
        cmbCategoria.setItems(CategoriaController.getCategorias());

        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );
        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>("categoria")
        );
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precioVenta")
        );
        colExistencia.setCellValueFactory(
                new PropertyValueFactory<>("existencia")
        );
        colActivo.setCellValueFactory(
                new PropertyValueFactory<>("activo")
        );

        colActivo.setCellFactory(columna ->
                new TableCell<Producto, Boolean>() {
                    @Override
                    protected void updateItem(Boolean activo, boolean empty) {
                        super.updateItem(activo, empty);

                        if (empty || activo == null) {
                            setText(null);
                        } else {
                            setText(activo ? "Sí" : "No");
                        }
                    }
                }
        );

        tblProductos.setItems(productos);
        chkActivo.setSelected(true);
    }

    public static boolean categoriaEnUso(Categoria categoria) {
        return productos.stream().anyMatch(producto ->
                producto.getCategoria() != null
                        && categoria.getId().equals(
                        producto.getCategoria().getId()
                )
        );
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del producto");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Imágenes", "*.png", "*.jpg", "*.jpeg"
                )
        );

        File archivo = chooser.showOpenDialog(
                txtCodigo.getScene().getWindow()
        );

        if (archivo == null) {
            return;
        }

        try {
            String nuevaRuta = archivo.toURI().toString();
            Image imagen = new Image(nuevaRuta);

            if (imagen.isError()) {
                mensaje(Alert.AlertType.ERROR,
                        "No se pudo leer la imagen seleccionada.");
                return;
            }

            rutaImagen = nuevaRuta;
            imgProducto.setImage(imagen);

        } catch (IllegalArgumentException e) {
            mensaje(Alert.AlertType.ERROR,
                    "La imagen seleccionada no es válida.");
        }
    }

    @FXML
    private void guardar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (codigo.isEmpty()
                || nombre.isEmpty()
                || txtPrecio.getText().isBlank()
                || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {

            mensaje(Alert.AlertType.WARNING,
                    "Complete los campos obligatorios.");
            return;
        }

        boolean codigoRepetido = productos.stream()
                .anyMatch(producto ->
                        producto.getCodigo().equalsIgnoreCase(codigo)
                );

        if (codigoRepetido) {
            mensaje(Alert.AlertType.WARNING,
                    "Ya existe un producto con ese código.");
            return;
        }

        if (!cmbCategoria.getValue().isActiva()) {
            mensaje(Alert.AlertType.WARNING,
                    "Seleccione una categoría activa.");
            return;
        }

        try {
            BigDecimal precio =
                    new BigDecimal(txtPrecio.getText().trim());

            int existencia =
                    Integer.parseInt(txtExistencia.getText().trim());

            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING,
                        "El precio debe ser mayor que cero "
                                + "y la existencia no puede ser negativa.");
                return;
            }

            Producto producto = new Producto(
                    siguienteId++,
                    codigo,
                    nombre,
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected()
            );

            productos.add(producto);
            limpiar();

            mensaje(Alert.AlertType.INFORMATION,
                    "Producto agregado correctamente.");

        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR,
                    "Ingrese un precio válido, por ejemplo 150.00, "
                            + "y una existencia entera, por ejemplo 10.");
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();

        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);

        imgProducto.setImage(null);
        rutaImagen = null;

        txtCodigo.requestFocus();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Productos");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}