package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeAmministratore {

    private final Client client;
    private Stage stage;
    private Amministratore amministratore;
    private ProfiloAmministratore profiloAmministratore;
    private RegistraNewLettore registraNewLettore;
    private RegistraNewAmministratore registraNewAmministratore;
    private Runnable onBack;


    public HomeAmministratore(Client client, ProfiloAmministratore profiloAmministratore,
                              RegistraNewLettore registraNewLettore, RegistraNewAmministratore registraNewAmministratore) {
        this.client = client;
        this.profiloAmministratore = profiloAmministratore;
        this.registraNewLettore = registraNewLettore;
        this.registraNewAmministratore = registraNewAmministratore;
    }

    public void show(Stage stage, Amministratore amministratore, Runnable onBack) {
        this.stage = stage;
        this.amministratore = amministratore;
        this.onBack = onBack;

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo amministratore");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            profiloAmministratore.show(stage, amministratore, () -> {
                this.show(stage, amministratore, onBack);
            });

        });

        Button newLettoreButton = new Button("Crea Nuovo Lettore");
        newLettoreButton.setOnAction(e -> {
            System.out.println("Apertura schermata crea nuovo Lettore...");
            registraNewLettore.show(stage, amministratore, () -> {
                this.show(stage, amministratore, onBack);
            });

        });

        Button newAmministratoreButton = new Button("Crea Nuovo Amministratore");
        newAmministratoreButton.setOnAction(e -> {
            System.out.println("Apertura schermata crea nuovo Amministratore...");
            registraNewAmministratore.show(stage, amministratore,() -> {
                this.show(stage, amministratore, onBack);
            });

        });

        Button backButton = new Button("Logout");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox topBar = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "), profiloButton, newLettoreButton, newAmministratoreButton, backButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        // ---------- LAYOUT FINALE ----------

        BorderPane root = new BorderPane();
        root.setTop(topBar);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // adatta la larghezza del contenuto alla finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // scroll verticale solo se serve
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 800, 750);
        Platform.runLater(() -> {;
            stage.setScene(scene);
            stage.setTitle("Home Amministratore");
            stage.show();
        });

    }

}
