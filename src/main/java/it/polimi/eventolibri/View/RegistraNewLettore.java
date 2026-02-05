package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNuovoLettore;
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
 * Classe View per la registrazione di un nuovo lettore da parte di un amministratore.
 */
public class RegistraNewLettore {
    private final Client client;
    private Stage stage;
    private Label messaggioerrore;
    private Amministratore amministratore;

    /**
     * Costruttore della classe RegistraNewLettore.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public RegistraNewLettore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setId("errorLabel"); // id per test
    }

    /**
     * Mostra la schermata di registrazione di un nuovo lettore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param amministratore l'amministratore che sta registrando il nuovo lettore
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Amministratore amministratore, Runnable onBack) {
        // Imposta lo stage e l'amministratore
        this.stage = stage;
        this.amministratore = amministratore;
        this.messaggioerrore.setText(""); // reset messaggio errore
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Registra Nuovo Lettore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI NUOVO LETTORE
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
        Button salvaDati = new Button("Registra nuovo lettore");
        salvaDati.setId("saveButton"); // id per test
        salvaDati.setOnAction(e -> { // azione al click del pulsante
            if ((!nomeField.getText().isBlank()) && (!cognomeField.getText().isBlank()) &&
                    (!usernameField.getText().isBlank()) && (!pswField.getText().isBlank())) { // controlla che tutti i campi siano compilati
                this.messaggioerrore.setText("");
                // Crea il nuovo lettore
                CreaUtente<Lettore> creaLettore = new CreaLettore();
                Lettore lettoreTemp = creaLettore.nuovoUtente(nomeField.getText(), cognomeField.getText(),
                        usernameField.getText());
                // Invia la richiesta di registrazione al server
                try {
                    client.sendMessage(new RichiestaNuovoLettore(lettoreTemp, pswField.getText()));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                // Mostra messaggio di errore se ci sono campi vuoti
                messaggioerrore.setText("Tutti i campi sono obbligatori.");
                messaggioerrore.setStyle("-fx-text-fill: red;");
            }
        });
        datiBox.getChildren().add(salvaDati);
        datiBox.getChildren().add(messaggioerrore);

        // ===========================
        // INDIETRO
        // ===========================
        // Pulsante indietro
        Button back = new Button("Indietro");
        back.setId("backButton");  // id per test
        back.setOnAction(e -> onBack.run()); // azione al click del pulsante
        // Box per posizionare il pulsante in alto a destra
        HBox backBox = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "), back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto); // Aggiunto ScrollPane per gestire schermi piccoli
        scrollPane.setFitToWidth(true); // Adatta il contenuto alla larghezza della finestra
        Scene scene = new Scene(scrollPane, 750, 780); // Dimensioni della scena
        // Mostra la scena
        Platform.runLater(() -> {
            stage.setTitle("Registra Nuovo Lettore");
            stage.setScene(scene);
            stage.show();
        });
        ;
    }

    /**
     * Mostra un messaggio di successo.
     *
     * @param messaggioerrore il messaggio di successo da visualizzare
     */
    public void mostraSuccesso(String messaggioerrore) {
        // Aggiorna l'interfaccia grafica nel thread JavaFX
        Platform.runLater(() -> {
            this.messaggioerrore.setStyle("-fx-text-fill: green;");
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

    /**
     * Mostra un messaggio di errore.
     *
     * @param messaggioerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String messaggioerrore) {
        // Aggiorna l'interfaccia grafica nel thread JavaFX
        Platform.runLater(() -> {
            this.messaggioerrore.setStyle("-fx-text-fill: red;");
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

}
