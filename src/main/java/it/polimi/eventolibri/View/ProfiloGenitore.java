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

/**
 * Classe View per la visualizzazione e modifica del profilo di un genitore.
 */
public class ProfiloGenitore {
    // ======================================================
    //                  ATTRIBUTI
    // ======================================================
    private final Client client;
    private Stage stage;
    private Genitore genitore;
    private Label messaggioerrore;
    private Label messaggioerrore2;
    private Label noFigliLabel;
    private VBox figliBox;
    private TextField nomeFiglioField;
    private DatePicker dataNascitaPicker;
    // ======================================================
    //                  COSTRUTTORE
    // ======================================================

    /**
     * Costruttore della classe ProfiloGenitore.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public ProfiloGenitore(Client client) {
        // Inizializza gli attributi
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

    /**
     * Restituisce il genitore associato al profilo.
     *
     * @return il genitore
     */
    public Genitore getGenitore() {
        return genitore;
    }

    /**
     * Mostra la schermata del profilo del genitore.
     *
     * @param stage    lo stage principale dell'applicazione
     * @param genitore il genitore di cui visualizzare il profilo
     * @param onBack   l'azione da eseguire quando si preme il pulsante "Indietro"
     */
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
        usernameField.setDisable(true); // Lo username non è modificabile
        datiBox.getChildren().addAll(
                new Label("Nome:"),
                nomeField,
                new Label("Cognome:"),
                cognomeField,
                new Label("Username:"),
                usernameField
        );
        // Pulsante Salva modifiche
        Button salvaDati = new Button("Salva modifiche");
        salvaDati.setOnAction(e -> {
            if (!genitore.getNome().equals(nomeField.getText()) || !genitore.getCognome().equals(cognomeField.getText())) {
                this.messaggioerrore.setText("");
                // Crea un nuovo oggetto Genitore con i dati aggiornati
                CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                Genitore genitoreTemp = creaGenitore.nuovoUtente(genitore.getId(), nomeField.getText(), cognomeField.getText(), genitore.getUserName());
                try { // Invia la richiesta di aggiornamento al server
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
        figliBox.getChildren().clear(); // Pulisce la lista dei figli
        if (genitore.getFigli().isEmpty()) { // Se non ci sono figli, mostra un messaggio
            figliBox.getChildren().add(noFigliLabel); // Messaggio "Nessun figlio registrato."
        } else {
            for (Figlio f : genitore.getFigli()) { // Altrimenti, crea una riga per ogni figlio
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
        aggiungiFiglioButton.setOnAction(e -> { // Azione bottone Aggiungi figlio
            this.messaggioerrore2.setText("");
            if (nomeFiglioField.getText().isBlank() || dataNascitaPicker.getValue() == null) {
                // Mostra un alert se i campi sono vuoti
                Alert alert2 = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
                alert2.setTitle("Aggiungi Figlio");
                alert2.setHeaderText("Inserisci nome e data di nascita.");
                alert2.setContentText(null);
                alert2.show();
                return;
            }
            // Crea un nuovo oggetto Genitore temporaneo per la richiesta
            CreaUtente<Genitore> creaGenitore = new CreaGenitore();
            Genitore genitoreTemp2 = creaGenitore.nuovoUtente(genitore.getId(), genitore.getNome(), genitore.getCognome(), genitore.getUserName());
            Figlio nuovoFiglio = new Figlio(nomeFiglioField.getText(), dataNascitaPicker.getValue());
            try { // Invia la richiesta di aggiunta figlio al server
                client.sendMessage(new RichiestaAggiungiFiglio(genitoreTemp2, nuovoFiglio));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        // Layout aggiunta figlio
        VBox aggiungiBox = new VBox(10, addTitle, nomeFiglioField, dataNascitaPicker, aggiungiFiglioButton, messaggioerrore2);
        aggiungiBox.setPadding(new Insets(10));
        // ===========================
        // INDIETRO
        // ===========================
        Button back = new Button("Indietro");
        back.setOnAction(e -> onBack.run());
        // Layout indietro
        HBox backBox = new HBox(
                new Label("  Benvenuto, (genitore) " + genitore.getNome() + "!          "),
                back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox, figliTitle, figliBox, aggiungiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto); // Aggiunge uno scroll pane per gestire lo spazio
        scrollPane.setFitToWidth(true); // Adatta il contenuto alla larghezza della finestra
        Scene scene = new Scene(scrollPane, 750, 780); // Crea la scena con dimensioni adeguate
        Platform.runLater(() -> { // Mostra la scena nel thread JavaFX
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
//        // Bottone Rimuovi figlio (attualmente non funzionante)
//        Button eliminaButton = new Button("Rimuovi");
//        eliminaButton.setOnAction(e -> {
//            genitore.getFigli().remove(figlio);
//            container.getChildren().remove(eliminaButton.getParent());
//            // TODO: client.sendMessage(new RichiestaRimuoviFiglio(genitore, figlio));
//        });
        HBox riga = new HBox(20, label /*, eliminaButton*/);
        riga.setAlignment(Pos.CENTER_LEFT);
        return riga; // Restituisce la riga creata
    }

    /**
     * Mostra un messaggio di errore nella schermata del profilo.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(() -> { // Esegui nel thread JavaFX
            this.messaggioerrore.setText(msgerrore); // Imposta il testo del messaggio di errore
        });
    }

    /**
     * Mostra un messaggio di errore nella schermata del profilo (sezione aggiunta figlio).
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore2(String msgerrore) {
        Platform.runLater(() -> { // Esegui nel thread JavaFX
            this.messaggioerrore2.setText(msgerrore); // Imposta il testo del messaggio di errore
        });
    }

    /**
     * Aggiorna la lista dei figli visualizzata nella schermata del profilo.
     *
     * @param nuovoFiglio il nuovo figlio da aggiungere alla lista
     */
    public void aggiornaFigli(Figlio nuovoFiglio) {
        Platform.runLater(() -> {
            // Esegui nel thread JavaFX
            if (!this.genitore.getFigli().isEmpty() && this.figliBox.getChildren().contains(noFigliLabel)) {
                this.figliBox.getChildren().remove(noFigliLabel); // Rimuovi il messaggio "Nessun figlio registrato." se presente
            }
            // Aggiungi la riga del nuovo figlio alla lista
            this.figliBox.getChildren().add(creaRigaFiglio(genitore, nuovoFiglio, figliBox));
            this.nomeFiglioField.clear(); // Pulisci i campi di input
            this.dataNascitaPicker.setValue(null); // Pulisci i campi di input
        });
    }
}