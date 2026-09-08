module org.example.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens org.example.fact_app to javafx.fxml;
    exports org.example.fact_app;
}