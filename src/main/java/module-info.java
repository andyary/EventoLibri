module it.polimi.eventolibri {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;
    requires it.polimi.eventolibri;
    // requires it.polimi.eventolibri;

    opens it.polimi.eventolibri to javafx.fxml;
    exports it.polimi.eventolibri;
}