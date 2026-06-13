module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
}