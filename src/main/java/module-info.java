module org.example {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires org.pcap4j.core;

    opens org.example to javafx.fxml;
    exports org.example;
}