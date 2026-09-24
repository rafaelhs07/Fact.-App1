module org.example.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    requires java.sql;

    opens org.example.fact_app to javafx.fxml;
    opens org.example.fact_app.controller to javafx.fxml;
    exports org.example.fact_app;
}