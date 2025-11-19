module it.polimi.eventolibri {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;

    opens it.polimi.eventolibri to javafx.fxml;
    exports it.polimi.eventolibri;
}