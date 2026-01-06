package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe View per la visualizzazione e modifica del profilo di un amministratore.
 */
public class ProfiloAmministratore {

    private final Client client;
    private Stage stage;
    private Amministratore amministratore;
    private Label messaggioerrore;

    /**
     * Costruttore della classe ProfiloAmministratore.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public ProfiloAmministratore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
    }

    /**
     * Restituisce l'amministratore associato al profilo.
     *
     * @return l'amministratore
     */
    public Amministratore getAmministratore() {
        return amministratore;
    }

    /**
     * Mostra la schermata del profilo dell'amministratore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param amministratore l'amministratore di cui visualizzare il profilo
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Amministratore amministratore, Runnable onBack) {
        this.stage = stage;
        this.amministratore = amministratore;
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Profilo Amministratore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI AMMINISTRATORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField(amministratore.getNome());
        TextField cognomeField = new TextField(amministratore.getCognome());
        TextField usernameField = new TextField(amministratore.getUserName());
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
            if (!amministratore.getNome().equals(nomeField.getText()) || !amministratore.getCognome().equals(cognomeField.getText())) {
                this.messaggioerrore.setText("");
                CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                Amministratore amministratoreTemp = creaAmministratore.nuovoUtente(amministratore.getId(), nomeField.getText(), cognomeField.getText(), amministratore.getUserName());
                try {
                    client.sendMessage(new RichiestaAggiornaAmministratore(amministratoreTemp));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        datiBox.getChildren().add(salvaDati);
        datiBox.getChildren().add(messaggioerrore);

        // ===========================
        // INDIETRO
        // ===========================
        Button back = new Button("Indietro");
        back.setOnAction(e -> onBack.run());
        HBox backBox = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "),back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 800, 750);
        Platform.runLater(() -> {
            stage.setTitle("Profilo Amministratore");
            stage.setScene(scene);
            stage.show();
        });
    }

    /**
     * Mostra un messaggio di errore nella schermata del profilo.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore.setText(msgerrore);
        });
    }

}



