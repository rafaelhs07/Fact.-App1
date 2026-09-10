package org.example.fact_app.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.fact_app.model.Cargo;
import org.example.fact_app.model.Empleado;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CargoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;

    @FXML private TableView<Cargo> tblDatos;
    @FXML private TableColumn<Cargo, Integer> colId;
    @FXML private TableColumn<Cargo, String> colNombre;

    @FXML private TableView<Empleado> tblPersonas;
    @FXML private TableColumn<Empleado, String> colPersonaNombre;
    @FXML private TableColumn<Empleado, String> colPersonaCargo;
    @FXML private TableColumn<Empleado, String> colPersonaFecha;

    private static final ObservableList<Cargo> cargos =
            FXCollections.observableArrayList();

    private static final ObservableList<Empleado> empleados =
            FXCollections.observableArrayList();

    private static int siguienteIdCargo = 1;
    private static int siguienteIdEmpleado = 1;

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

        colPersonaNombre.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getNombres()
                                + " "
                                + datos.getValue().getApellidos()
                )
        );

        colPersonaCargo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().getCargo().getNombre()
                )
        );

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colPersonaFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .getFechaContratacion()
                                .format(formato)
                )
        );

        tblPersonas.setItems(empleados);
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
                siguienteIdCargo++,
                txtNombre.getText().trim(),
                ""
        );

        cargos.add(cargo);
        onLimpiar();
    }

    @FXML
    private void onActualizar() {
        Cargo seleccionado =
                tblDatos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona un cargo en la tabla.");
            return;
        }

        if (!validar(seleccionado)) {
            return;
        }

        seleccionado.setNombre(txtNombre.getText().trim());

        tblDatos.refresh();
        tblPersonas.refresh();

        onLimpiar();
    }

    @FXML
    private void onEliminar() {
        Cargo seleccionado =
                tblDatos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona un cargo en la tabla.");
            return;
        }

        boolean asignado = empleados.stream()
                .anyMatch(empleado ->
                        empleado.getCargo().getId()
                                .equals(seleccionado.getId())
                );

        if (asignado) {
            mensaje("No puedes eliminar este cargo "
                    + "porque está asignado a una persona.");
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
    private void onAsignarPersona() {
        Cargo seleccionado =
                tblDatos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje("Selecciona primero el cargo que deseas asignar.");
            return;
        }

        Dialog<Empleado> dialogo = new Dialog<>();
        dialogo.setTitle("Asignar cargo a persona");
        dialogo.setHeaderText("Cargo: " + seleccionado.getNombre());
        dialogo.initOwner(txtNombre.getScene().getWindow());

        ButtonType botonAsignar = new ButtonType(
                "Asignar",
                ButtonBar.ButtonData.OK_DONE
        );

        dialogo.getDialogPane().getButtonTypes().addAll(
                botonAsignar,
                ButtonType.CANCEL
        );

        TextField txtNombresPersona = new TextField();
        txtNombresPersona.setPromptText("Nombres");

        TextField txtApellidosPersona = new TextField();
        txtApellidosPersona.setPromptText("Apellidos");

        DatePicker fechaContratacion = new DatePicker(LocalDate.now());
        fechaContratacion.setEditable(false);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #b91c1c;");
        lblError.setWrapText(true);

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(12);
        formulario.setPadding(new Insets(15));

        formulario.add(new Label("Nombres:"), 0, 0);
        formulario.add(txtNombresPersona, 1, 0);

        formulario.add(new Label("Apellidos:"), 0, 1);
        formulario.add(txtApellidosPersona, 1, 1);

        formulario.add(new Label("Fecha de contratación:"), 0, 2);
        formulario.add(fechaContratacion, 1, 2);

        formulario.add(lblError, 0, 3, 2, 1);

        dialogo.getDialogPane().setContent(formulario);

        Button btnAsignar = (Button) dialogo.getDialogPane()
                .lookupButton(botonAsignar);

        // Evita cerrar el diálogo si faltan datos.
        btnAsignar.addEventFilter(ActionEvent.ACTION, evento -> {
            if (txtNombresPersona.getText().isBlank()
                    || txtApellidosPersona.getText().isBlank()
                    || fechaContratacion.getValue() == null) {

                lblError.setText(
                        "Completa los nombres, apellidos y la fecha."
                );

                evento.consume();
            }
        });

        dialogo.setResultConverter(boton -> {
            if (boton == botonAsignar) {
                return new Empleado(
                        siguienteIdEmpleado++,
                        txtNombresPersona.getText().trim(),
                        txtApellidosPersona.getText().trim(),
                        seleccionado,
                        fechaContratacion.getValue(),
                        true
                );
            }

            return null;
        });

        dialogo.showAndWait().ifPresent(empleado -> {
            empleados.add(empleado);
            tblPersonas.getSelectionModel().select(empleado);
            tblPersonas.scrollTo(empleado);
        });
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