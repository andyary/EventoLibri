package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaGenitore;
import it.polimi.eventolibri.Message.RichiestaAggiungiFiglio;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfiloGenitore {

    private final Client client;
    private Stage stage;
    private Genitore genitore;
    private Label messaggioerrore;
    private Label messaggioerrore2;
    private VBox figliBox;
    private TextField nomeFiglioField;
    private DatePicker dataNascitaPicker;


    public ProfiloGenitore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.messaggioerrore2 = new Label("");
        this.messaggioerrore2.setStyle("-fx-text-fill: red;");
        this.figliBox = new VBox(10);
        this.nomeFiglioField = new TextField();
        this.dataNascitaPicker = new DatePicker();
    }

    public Genitore getGenitore() {
        return genitore;
    }

    public void show(Stage stage, Genitore genitore, Runnable onBack) {
        this.stage = stage;
        this.genitore = genitore;
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Profilo Genitore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI GENITORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField(genitore.getNome());
        TextField cognomeField = new TextField(genitore.getCognome());
        TextField usernameField = new TextField(genitore.getUserName());
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
            if (!genitore.getNome().equals(nomeField.getText()) || !genitore.getCognome().equals(cognomeField.getText())) {
                this.messaggioerrore.setText("");
                CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                Genitore genitoreTemp= creaGenitore.nuovoUtente(genitore.getId(),nomeField.getText(), cognomeField.getText(), genitore.getUserName());
                try {
                    client.sendMessage(new RichiestaAggiornaGenitore(genitoreTemp));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        datiBox.getChildren().add(salvaDati);
        datiBox.getChildren().add(messaggioerrore);

        // ===========================
        // FIGLI
        // ===========================
        Label figliTitle = new Label("Figli:");
        figliTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        figliBox.setPadding(new Insets(10));
        if (genitore.getFigli().isEmpty()) {
            figliBox.getChildren().add(new Label("Nessun figlio registrato."));
        } else {
            for (Figlio f : genitore.getFigli()) {
                figliBox.getChildren().add(creaRigaFiglio(genitore, f, figliBox));
            }
        }
        // ===========================
        // AGGIUNTA FIGLIO
        // ===========================
        Label addTitle = new Label("Aggiungi Figlio:");
        addTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        nomeFiglioField.setPromptText("Nome figlio");
        dataNascitaPicker.setPromptText("Data di nascita");
        Button aggiungiFiglioButton = new Button("Aggiungi figlio");
        aggiungiFiglioButton.setOnAction(e -> {
            this.messaggioerrore2.setText("");
            if (nomeFiglioField.getText().isBlank() || dataNascitaPicker.getValue() == null) {
                Alert alert2 = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
                alert2.setTitle("Aggiungi Figlio");
                alert2.setHeaderText("Inserisci nome e data di nascita.");
                alert2.setContentText(null);
                alert2.show();
                return;
            }
            CreaUtente<Genitore> creaGenitore = new CreaGenitore();
            Genitore genitoreTemp2= creaGenitore.nuovoUtente(genitore.getId(),genitore.getNome(), genitore.getCognome(), genitore.getUserName());
            Figlio nuovoFiglio = new Figlio(nomeFiglioField.getText(), dataNascitaPicker.getValue());
            // genitoreTemp2.aggiungiFiglio(nuovo);
            try {
                client.sendMessage(new RichiestaAggiungiFiglio(genitoreTemp2, nuovoFiglio));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
//            figliBox.getChildren().add(creaRigaFiglio(genitore, nuovo, figliBox));
//            nomeFiglioField.clear();
//            dataNascitaPicker.setValue(null);
        });
        VBox aggiungiBox = new VBox(10, addTitle, nomeFiglioField, dataNascitaPicker, aggiungiFiglioButton, messaggioerrore2);
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
        VBox contenuto = new VBox(25, backBox, title, datiBox, figliTitle, figliBox, aggiungiBox);
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
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }

    public void mostraErrore2(String msgerrore) {
        Platform.runLater(()->{
            this.messaggioerrore2.setText(msgerrore);
        });
    }

    public void aggiornaFigli(Figlio nuovoFiglio) {
        Platform.runLater(()->{
            this.figliBox.getChildren().add(creaRigaFiglio(genitore, nuovoFiglio, figliBox));
            this.nomeFiglioField.clear();
            this.dataNascitaPicker.setValue(null);
        });
    }

}


