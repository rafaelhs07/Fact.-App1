package org.example.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.fact_app.dao.CategoriaDAO;
import org.example.fact_app.dao.ProductoDAO;
import org.example.fact_app.model.Categoria;
import org.example.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private TextField txtBuscar;

    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCerrar;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private static final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;

    private String rutaImagen;
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    private void initialize() {
        cargarCategorias();

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

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

        productosFiltrados = new FilteredList<>(productos, p -> true);

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            productosFiltrados.setPredicate(producto -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();
                if (producto.getCodigo() != null && producto.getCodigo().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (producto.getNombre() != null && producto.getNombre().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Producto> productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tblProductos.comparatorProperty());
        tblProductos.setItems(productosOrdenados);

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, seleccionado) -> {
            if (seleccionado != null) {
                txtCodigo.setText(seleccionado.getCodigo());
                txtNombre.setText(seleccionado.getNombre());
                txtPrecio.setText(seleccionado.getPrecioVenta() != null ? seleccionado.getPrecioVenta().toPlainString() : "");
                txtExistencia.setText(String.valueOf(seleccionado.getExistencia()));
                chkActivo.setSelected(seleccionado.isActivo());

                if (seleccionado.getCategoria() != null) {
                    for (Categoria c : cmbCategoria.getItems()) {
                        if (c.getId().equals(seleccionado.getCategoria().getId())) {
                            cmbCategoria.setValue(c);
                            break;
                        }
                    }
                } else {
                    cmbCategoria.getSelectionModel().clearSelection();
                }

                rutaImagen = seleccionado.getRutaImagen();
                if (rutaImagen != null && !rutaImagen.isBlank()) {
                    try {
                        imgProducto.setImage(new Image(rutaImagen));
                    } catch (Exception e) {
                        imgProducto.setImage(null);
                    }
                } else {
                    imgProducto.setImage(null);
                }
            }
        });

        chkActivo.setSelected(true);
        cargarDatos();
    }

    private void cargarCategorias() {
        try {
            List<Categoria> listaCategorias = categoriaDAO.listar();
            cmbCategoria.setItems(FXCollections.observableArrayList(listaCategorias));
        } catch (SQLException e) {
            System.err.println("Error al cargar categorías de la base de datos: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos",
                    "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            List<Producto> listaBD = productoDAO.listar();
            productos.setAll(listaBD);
        } catch (SQLException e) {
            System.err.println("Error al listar productos de la base de datos: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos",
                    "No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    public static boolean categoriaEnUso(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            return false;
        }
        try {
            return new CategoriaDAO().tieneProductos(categoria.getId());
        } catch (SQLException e) {
            System.err.println("Error al consultar si categoría tiene productos: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del producto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            String nuevaRuta = archivo.toURI().toString();
            Image imagen = new Image(nuevaRuta);

            if (imagen.isError()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo leer la imagen seleccionada.");
                return;
            }

            rutaImagen = nuevaRuta;
            imgProducto.setImage(imagen);

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La imagen seleccionada no es válida.");
        }
    }

    private Producto obtenerProductoFormulario(Integer id) {
        String codigo = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        if (codigo.isEmpty()) {
            txtCodigo.requestFocus();
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }

        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre del producto es obligatorio y no puede consistir únicamente en espacios en blanco.");
        }

        Categoria categoria = cmbCategoria.getValue();
        if (categoria == null) {
            cmbCategoria.requestFocus();
            throw new IllegalArgumentException("Debe seleccionar una categoría para el producto.");
        }
        if (!categoria.isActiva()) {
            cmbCategoria.requestFocus();
            throw new IllegalArgumentException("La categoría seleccionada está inactiva. Seleccione una categoría activa.");
        }

        String precioTexto = txtPrecio.getText() != null ? txtPrecio.getText().trim() : "";
        if (precioTexto.isEmpty()) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio de venta es obligatorio.");
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(precioTexto);
        } catch (NumberFormatException e) {
            txtPrecio.requestFocus();
            throw new NumberFormatException("El precio de venta debe ser un número válido (ej. 150.00).");
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio de venta debe ser estrictamente mayor que cero.");
        }

        String existenciaTexto = txtExistencia.getText() != null ? txtExistencia.getText().trim() : "";
        if (existenciaTexto.isEmpty()) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia es obligatoria.");
        }

        int existencia;
        try {
            existencia = Integer.parseInt(existenciaTexto);
        } catch (NumberFormatException e) {
            txtExistencia.requestFocus();
            throw new NumberFormatException("La existencia debe ser un número entero válido (sin decimales ni letras y dentro del rango permitido).");
        }

        if (existencia < 0) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia debe ser un número entero mayor o igual que cero.");
        }

        boolean activo = chkActivo != null && chkActivo.isSelected();

        Producto producto = new Producto();
        producto.setId(id);
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecioVenta(precio);
        producto.setExistencia(existencia);
        producto.setRutaImagen(rutaImagen);
        producto.setActivo(activo);

        return producto;
    }

    @FXML
    private void guardar() {
        if (tblProductos.getSelectionModel().getSelectedItem() != null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Operación no permitida",
                    "Pulse Limpiar para crear un nuevo producto o Modificar para actualizar el seleccionado.");
            return;
        }

        try {
            Producto producto = obtenerProductoFormulario(null);

            if (productoDAO.existeCodigo(producto.getCodigo(), null)) {
                txtCodigo.requestFocus();
                mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado",
                        "Ya existe un producto con el código '" + producto.getCodigo() + "'.");
                return;
            }

            boolean exito = productoDAO.guardar(producto);
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                        "Producto guardado correctamente en la base de datos.");
                try {
                    cargarDatos();
                    limpiar();
                } catch (Exception eReload) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia de sincronización",
                            "El producto fue guardado con éxito, pero ocurrió un problema al refrescar la lista.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo guardar el producto en la base de datos (0 filas afectadas).");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de formato numérico", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error SQL al guardar producto: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", traducirErrorSql(e));
        }
    }

    @FXML
    private void actualizar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null || seleccionado.getId() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Debe seleccionar un producto en la tabla para modificar.");
            return;
        }

        try {
            Producto producto = obtenerProductoFormulario(seleccionado.getId());

            if (productoDAO.existeCodigo(producto.getCodigo(), seleccionado.getId())) {
                txtCodigo.requestFocus();
                mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado",
                        "Ya existe otro producto con el código '" + producto.getCodigo() + "'.");
                return;
            }

            boolean exito = productoDAO.actualizar(producto);
            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                        "Producto modificado correctamente en la base de datos.");
                try {
                    cargarDatos();
                    limpiar();
                } catch (Exception eReload) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia de sincronización",
                            "El producto fue modificado con éxito, pero ocurrió un problema al refrescar la lista.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo actualizar el producto en la base de datos (0 filas afectadas).");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de formato numérico", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error SQL al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", traducirErrorSql(e));
        }
    }

    @FXML
    private void eliminar() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null || productoSeleccionado.getId() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor, seleccione un producto de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro de eliminar el producto: " + productoSeleccionado.getNombre() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean eliminadoBD = productoDAO.eliminar(productoSeleccionado.getId());
            if (eliminadoBD) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa",
                        "Producto eliminado correctamente de la base de datos.");
                try {
                    cargarDatos();
                    limpiar();
                } catch (Exception eReload) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia de sincronización",
                            "El producto fue eliminado con éxito, pero ocurrió un problema al refrescar la lista.");
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo eliminar el producto de la base de datos (0 filas afectadas).");
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al eliminar producto: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", traducirErrorSql(e));
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    @FXML
    private void limpiar() {
        tblProductos.getSelectionModel().clearSelection();
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
                return "Infracción de unicidad: ya existe un producto con ese código en la base de datos.";
            } else if (estado.startsWith("23503") || codigo == 547) {
                return "Infracción de integridad referencial: la categoría seleccionada no existe o el producto está en uso.";
            } else if (estado.startsWith("23514")) {
                return "Infracción de regla de integridad: verifique que el precio sea mayor que cero y la existencia no sea negativa.";
            }
        }
        return "Ocurrió un error en la base de datos: " + e.getMessage();
    }
}