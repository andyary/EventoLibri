package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNuovoAmministratore;
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

/**
 * Classe View per la registrazione di un nuovo amministratore da parte di un amministratore esistente.
 */
public class RegistraNewAmministratore {
    private final Client client;
    private Stage stage;
    private Label messaggioerrore;
    private Amministratore amministratore;

    // Costruttore della classe RegistraNewAmministratore.
    public RegistraNewAmministratore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setId("errorLabel"); // id per test
    }

    /**
     * Mostra la schermata di registrazione di un nuovo amministratore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param amministratore l'amministratore che sta registrando il nuovo amministratore
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Amministratore amministratore, Runnable onBack) {
        this.stage = stage;
        this.amministratore = amministratore;
        this.messaggioerrore.setText("");
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Registra Nuovo Amministratore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI NUOVO AMMINISTRATORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField("");
        nomeField.setId("nomeField"); // id per test
        TextField cognomeField = new TextField("");
        cognomeField.setId("cognomeField"); // id per test
        TextField usernameField = new TextField("");
        usernameField.setId("usernameField"); // id per test
        PasswordField pswField = new PasswordField();
        pswField.setId("pswField");
        datiBox.getChildren().addAll(
                new Label("Nome:"),
                nomeField,
                new Label("Cognome:"),
                cognomeField,
                new Label("Username:"),
                usernameField,
                new Label("Password:"),
                pswField
        );
        // ===========================
        // PULSANTE SALVA DATI
        // ===========================
        Button salvaDati = new Button("Registra nuovo amministratore");
        salvaDati.setId("saveButton"); // id per test
        salvaDati.setOnAction(e -> { // Azione al click del pulsante
            if ((!nomeField.getText().isBlank()) && (!cognomeField.getText().isBlank()) && (!usernameField.getText().isBlank()) && (!pswField.getText().isBlank())) {
                this.messaggioerrore.setText("");
                // Creazione del nuovo amministratore e invio della richiesta al server
                CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                Amministratore amministratoreTemp = creaAmministratore.nuovoUtente(nomeField.getText(), cognomeField.getText(),
                        usernameField.getText());
                try {
                    client.sendMessage(new RichiestaNuovoAmministratore(amministratoreTemp, pswField.getText()));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                messaggioerrore.setText("Tutti i campi sono obbligatori.");
                messaggioerrore.setStyle("-fx-text-fill: red;");
            }
        });
        datiBox.getChildren().add(salvaDati);
        datiBox.getChildren().add(messaggioerrore);

        // ===========================
        // INDIETRO
        // ===========================
        Button back = new Button("Indietro");
        back.setId("backButton");  // id per test
        back.setOnAction(e -> onBack.run());
        HBox backBox = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "), back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto); // Aggiunta dello scroll pane
        scrollPane.setFitToWidth(true); // Adatta il contenuto alla larghezza della finestra
        Scene scene = new Scene(scrollPane, 750, 780); // Dimensioni della scena
        // Mostra la scena nello stage
        Platform.runLater(() -> {
            stage.setTitle("Registra Nuovo Amministratore");
            stage.setScene(scene);
            stage.show();
        });
        ;
    }

    /**
     * Mostra un messaggio di successo nella schermata.
     *
     * @param messaggioerrore il messaggio di successo da mostrare
     */
    public void mostraSuccesso(String messaggioerrore) {
        Platform.runLater(() -> { // Aggiornamento dell'interfaccia grafica
            this.messaggioerrore.setStyle("-fx-text-fill: green;");
            this.messaggioerrore.setText(messaggioerrore); // Impostazione del messaggio di successo
        });
    }

    /**
     * Mostra un messaggio di errore nella schermata.
     *
     * @param messaggioerrore il messaggio di errore da mostrare
     */
    public void mostraErrore(String messaggioerrore) {
        Platform.runLater(() -> { // Aggiornamento dell'interfaccia grafica
            this.messaggioerrore.setStyle("-fx-text-fill: red;");
            this.messaggioerrore.setText(messaggioerrore); // Impostazione del messaggio di errore
        });
    }
}
