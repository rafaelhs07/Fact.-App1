module org.example.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    exports org.example.fact_app;
    exports org.example.fact_app.application;
    exports org.example.fact_app.model;

    opens org.example.fact_app.controller to javafx.fxml;
    opens org.example.fact_app.model to javafx.base;
}