package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaGenitore;
import it.polimi.eventolibri.Message.RichiestaAggiungiFiglio;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfiloLettore {

    private final Client client;
    private Stage stage;
    private Lettore lettore;
    private Label messaggioerrore;
    private Label messaggioerrore2;
    private Label noFigliLabel;
    private VBox figliBox;
    private TextField nomeFiglioField;
    private DatePicker dataNascitaPicker;


    public ProfiloLettore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.messaggioerrore2 = new Label("");
        this.messaggioerrore2.setStyle("-fx-text-fill: red;");
        this.figliBox = new VBox(10);
        this.nomeFiglioField = new TextField();
        this.dataNascitaPicker = new DatePicker();
        this.noFigliLabel = new Label("Nessun figlio registrato.");
    }

    public Lettore getLettore() {
        return lettore;
    }

    public void show(Stage stage, Lettore lettore, Runnable onBack) {
        this.stage = stage;
        this.lettore = lettore;
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Profilo Lettore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI GENITORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField(lettore.getNome());
        TextField cognomeField = new TextField(lettore.getCognome());
        TextField usernameField = new TextField(lettore.getUserName());
        usernameField.setDisable(true); // non si cambia normalmente
        datiBox.getChildren().addAll(
                new Label("Nome:"),
                nomeField,
                new Label("Cognome:"),
                cognomeField,
                new Label("Username:"),
                usernameField
        );
        Button salvaDati = new Button("Salva modifiche");
        salvaDati.setOnAction(e -> {
            if (!lettore.getNome().equals(nomeField.getText()) || !lettore.getCognome().equals(cognomeField.getText())) {
                this.messaggioerrore.setText("");
                CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                Genitore genitoreTemp = creaGenitore.nuovoUtente(lettore.getId(), nomeField.getText(), cognomeField.getText(), lettore.getUserName());
                try {
                    client.sendMessage(new RichiestaAggiornaGenitore(genitoreTemp));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        datiBox.getChildren().add(salvaDati);
        datiBox.getChildren().add(messaggioerrore);


        VBox aggiungiBox = new VBox(10, nomeFiglioField, dataNascitaPicker, messaggioerrore2);
        aggiungiBox.setPadding(new Insets(10));
        // ===========================
        // INDIETRO
        // ===========================
        Button back = new Button("Indietro");
        back.setOnAction(e -> onBack.run());
        HBox backBox = new HBox(back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox, figliBox, aggiungiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 500, 800);
        Platform.runLater(() -> {
            stage.setTitle("Profilo Genitore");
            stage.setScene(scene);
            stage.show();
        });
    }

    // ======================================================
    //      RIGA FIGLIO (nome + data nascita + "Rimuovi")
    // ======================================================
    private HBox creaRigaFiglio(Genitore genitore, Figlio figlio, VBox container) {
        Label label = new Label(figlio.getNome() + " - nato il " + figlio.getDataNascita());
//        Button eliminaButton = new Button("Rimuovi");
//        eliminaButton.setOnAction(e -> {
//            genitore.getFigli().remove(figlio);
//            container.getChildren().remove(eliminaButton.getParent());
//            // TODO: client.sendMessage(new RichiestaRimuoviFiglio(genitore, figlio));
//        });
        HBox riga = new HBox(20, label /*, eliminaButton*/);
        riga.setAlignment(Pos.CENTER_LEFT);
        return riga;

    }

    public void mostraErrore(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore.setText(msgerrore);
        });
    }

    public void mostraErrore2(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore2.setText(msgerrore);
        });
    }

}



