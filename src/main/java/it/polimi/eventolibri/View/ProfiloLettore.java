package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaLettore;
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
 * Classe View per la visualizzazione e modifica del profilo di un lettore.
 */
public class ProfiloLettore {

    private final Client client;
    private Stage stage;
    private Lettore lettore;
    private Label messaggioerrore;

    /**
     * Costruttore della classe ProfiloLettore.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public ProfiloLettore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
    }

    /**
     * Restituisce il lettore associato al profilo.
     *
     * @return il lettore
     */
    public Lettore getLettore() {
        return lettore;
    }

    /**
     * Mostra la schermata del profilo del lettore.
     *
     * @param stage   lo stage principale dell'applicazione
     * @param lettore il lettore di cui visualizzare il profilo
     * @param onBack  l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Lettore lettore, Runnable onBack) {
        // Salva i riferimenti a stage e lettore
        this.stage = stage;
        this.lettore = lettore;
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Profilo Lettore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI  LETTORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField(lettore.getNome());
        TextField cognomeField = new TextField(lettore.getCognome());
        TextField usernameField = new TextField(lettore.getUserName());
        usernameField.setDisable(true); // lo username non è modificabile
        datiBox.getChildren().addAll(
                new Label("Nome:"),
                nomeField,
                new Label("Cognome:"),
                cognomeField,
                new Label("Username:"),
                usernameField
        );
        // Pulsante Salva Modifiche
        Button salvaDati = new Button("Salva modifiche");
        salvaDati.setOnAction(e -> { // Aggiorna i dati del lettore se sono stati modificati
            if (!lettore.getNome().equals(nomeField.getText()) || !lettore.getCognome().equals(cognomeField.getText())) {
                this.messaggioerrore.setText(""); // pulisce il messaggio di errore
                // Crea un nuovo lettore con i dati aggiornati
                CreaUtente<Lettore> creaLettore = new CreaLettore();
                Lettore lettoreTemp = creaLettore.nuovoUtente(lettore.getId(), nomeField.getText(), cognomeField.getText(), lettore.getUserName());
                try { // Invia la richiesta di aggiornamento al server
                    client.sendMessage(new RichiestaAggiornaLettore(lettoreTemp));
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
        back.setOnAction(e -> onBack.run()); // Esegue l'azione di ritorno
        HBox backBox = new HBox(new Label("  Benvenuto, (lettore) " + lettore.getNome() + "!          "), back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto); // Aggiunge lo scroll pane per gestire contenuti lunghi
        scrollPane.setFitToWidth(true); // Adatta il contenuto alla larghezza della finestra
        Scene scene = new Scene(scrollPane, 750, 780); // Dimensioni della scena
        Platform.runLater(() -> { // Mostra la scena nel thread dell'interfaccia utente
            stage.setTitle("Profilo Lettore");
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
        Platform.runLater(() -> { // Aggiorna il messaggio di errore nel thread dell'interfaccia utente
            this.messaggioerrore.setText(msgerrore);
        });
    }
}