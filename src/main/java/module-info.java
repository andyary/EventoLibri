module it.polimi.eventolibri {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;

    opens it.polimi.eventolibri to javafx.fxml;

    exports it.polimi.eventolibri.View;
    exports it.polimi.eventolibri.Controller;
    exports it.polimi.eventolibri.Model;
    exports it.polimi.eventolibri.Message;
    exports it.polimi.eventolibri.Network;

}