module org.example.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;

    opens org.example.fact_app to javafx.fxml;
    opens org.example.fact_app.controller to javafx.fxml;
    opens org.example.fact_app.application to javafx.graphics, javafx.fxml;


    opens org.example.fact_app.model to javafx.base;

    exports org.example.fact_app;
    exports org.example.fact_app.application;
    exports org.example.fact_app.controller;
    exports org.example.fact_app.model;
}