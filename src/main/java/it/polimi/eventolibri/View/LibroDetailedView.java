package it.polimi.eventolibri.View;

import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import it.polimi.eventolibri.Message.RichiestaAggiungiRecensione;
import it.polimi.eventolibri.Message.RichiestaCancellaRecensione;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Classe View per la visualizzazione dettagliata di un libro selezionato.
 */
public class LibroDetailedView {
    // Attributi
    private final Client client;
    private Label messaggioerrore;
    private Label messaggioerrore2;
    private ArrayList<Recensione> recensioniAggiornate = new ArrayList<>();
    private boolean attendi; // flag per operazioni asincrone

    /**
     * Costruttore della classe LibroDetailedView.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public LibroDetailedView(Client client) {
        this.client = client;
    }

    /**
     * Imposta lo stato di attesa per le operazioni asincrone.
     *
     * @param attendi true se si deve attendere, false altrimenti
     */
    public void setAttendi(boolean attendi) {
        this.attendi = attendi;
    }

    /**
     * Mostra la view dettagliata del libro.
     * Il caller deve calcolare (server-side) la variabile `recensibile` per i genitori/lettori
     * oppure passare null se vuole che la view richiami il server in modo asincrono.
     *
     * @param stage       owner
     * @param libro       libro visualizzato
     * @param recensioni  lista recensioni (può essere vuota)
     * @param utente      chiamante (instanceof Genitore/Lettore/Amministratore)
     * @param recensibile se non null indica se il genitore può recensire; per lettore/adm viene ignorato
     * @param onBack      callback per tornare alla home
     */
    public void show(Stage stage,
                     Libro libro,
                     ArrayList<Recensione> recensioni,
                     Utente utente,
                     Boolean recensibile,
                     Runnable onBack) {

        messaggioerrore = new Label(" ");
        messaggioerrore.setStyle("-fx-text-fill: red;");

        recensioniAggiornate.clear(); // reset recensioni aggiornate
        for (Recensione recensione : recensioni) {
            recensioniAggiornate.add(recensione); // copia recensioni iniziali
        }

        // decidere permessi basati sulla gerarchia di Utente
        boolean canAdd = false;
        boolean canDelete = false;
        // Amministratore può solo cancellare
        if (utente instanceof Amministratore) {
            canAdd = false;
            canDelete = true;
        }
        // Lettore non può fare nulla
        if (utente instanceof Lettore) {
            canAdd = false;
            canDelete = false;
        }
        // Genitore può aggiungere se recensibile, non può cancellare
        if (utente instanceof Genitore) {
            canAdd = recensibile;
            canDelete = false;
        }

        Stage s = new Stage();
        s.initOwner(stage); // imposta la finestra come proprietaria
        s.setTitle("Dettaglio libro");
        s.initModality(Modality.WINDOW_MODAL); // blocca l'interazione con la finestra proprietaria
        // Barra superiore con messaggio errore e bottone indietro
        Button backBtn = new Button("Indietro");
        backBtn.setOnAction(e -> {
            s.close();
            if (onBack != null) onBack.run();
        });
        HBox topBar1 = new HBox(messaggioerrore);
        topBar1.setPadding(new Insets(10));
        topBar1.setAlignment(Pos.CENTER);

        HBox topBar2 = new HBox(backBtn);
        topBar2.setPadding(new Insets(10));
        topBar2.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(topBar1, topBar2);

        // Dettagli del libro
        Label titolo = new Label("Titolo: " + libro.getTitolo());
        Label autore = new Label("Autore: " + libro.getAutore());
        Label tempo = new Label("Tempo di lettura: " + libro.getTempoLettura() + " min");
        Label isbn = new Label("ISBN: " + libro.getIsbn());
        // link ipertestuale
        Hyperlink hlink = new Hyperlink(libro.getLink() == null ? "" : libro.getLink());
        hlink.setOnAction(ev -> { // apri il link nel browser predefinito
            if (hlink.getText() == null || hlink.getText().isBlank()) return; // se vuoto non fare nulla
            try { // prova ad aprire il link
                if (Desktop.isDesktopSupported())
                    Desktop.getDesktop().browse(new URI(hlink.getText())); // apri nel browser
                else throw new UnsupportedOperationException("Desktop non supportato");
            } catch (IOException | URISyntaxException ex) {
                messaggioerrore.setText("Impossibile aprire il link");
                System.out.println("Impossibile aprire il link: " + ex.getMessage());
            }
        });
        Label linkLabel = new Label("Link: ");
        HBox link = new HBox(linkLabel, hlink);
        link.setAlignment(Pos.CENTER_LEFT);
        // permessi recensioni
        String recLabel = "Recensioni: ";
        recLabel += (canAdd ? "puoi aggiungere" : "non puoi aggiungere");
        recLabel += (" , ");
        recLabel += (canDelete ? "puoi cancellare" : "non puoi cancellare");

        // box dettagli
        VBox dettagliBox = new VBox(8, titolo, autore, tempo, isbn, link, new Label(recLabel));
        dettagliBox.setPadding(new Insets(10));
        // bottone vedi recensioni
        Button vediRecensioniBtn = new Button("Vedi recensioni");
        boolean finalCanDelete = canDelete;
        vediRecensioniBtn.setOnAction(ev -> showRecensioniWindow(s, recensioniAggiornate, finalCanDelete));

        // box centrale
        VBox centerBox = new VBox(10, dettagliBox);
        centerBox.setPadding(new Insets(10));

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().add(vediRecensioniBtn);

        // bottone aggiungi recensione se permesso
        if (canAdd) {
            Button aggiungiRecBtn = new Button("Aggiungi recensione");
            aggiungiRecBtn.setOnAction(ev -> showAggiungiRecensioneDialog(s, libro, utente));
            buttonsBox.getChildren().add(aggiungiRecBtn);
        }

        centerBox.getChildren().add(buttonsBox);
        centerBox.getChildren().add(messaggioerrore);

        BorderPane root = new BorderPane(); // layout principale
        root.setTop(topBar);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 650, 380); // crea la scena
        Platform.runLater(() -> { // mostra la finestra
            s.setScene(scene);
            s.show();
        });
    }

    /**
     * Mostra una finestra con le recensioni del libro.
     *
     * @param owner      finestra proprietaria
     * @param recensioni lista delle recensioni da visualizzare
     * @param canDelete  indica se l'utente può cancellare le recensioni
     */
    private void showRecensioniWindow(Stage owner, ArrayList<Recensione> recensioni, boolean canDelete) {
        Stage rStage = new Stage();
        rStage.initOwner(owner); // imposta la finestra come proprietaria
        rStage.initModality(Modality.WINDOW_MODAL); // blocca l'interazione con la finestra proprietaria
        rStage.setTitle("Recensioni"); // titolo finestra
        // Barra superiore con messaggio errore e bottone indietro
        messaggioerrore2 = new Label(" ");
        messaggioerrore2.setStyle("-fx-text-fill: red;");

        Button backBtn = new Button("Indietro");
        backBtn.setOnAction(ev -> rStage.close());

        // top bar con messaggio errore a sinistra e bottone indietro a destra
        Region spacer = new Region(); // spazio vuoto per separare
        HBox topBar = new HBox(8, messaggioerrore2, spacer, backBtn);
        topBar.setPadding(new Insets(10));
        HBox.setHgrow(spacer, Priority.ALWAYS); // spazio vuoto prende tutto lo spazio possibile
        topBar.setAlignment(Pos.CENTER_RIGHT);
        // Lista recensioni
        VBox listBox = new VBox(8);
        listBox.setPadding(new Insets(10));
        // se non ci sono recensioni
        if (recensioni == null || recensioni.isEmpty()) {
            listBox.getChildren().add(new Label("Nessuna recensione disponibile"));
        } else { // altrimenti mostra le recensioni
            for (Recensione r : recensioni) {
                Text labelTesto = new Text("Testo: ");
                Text contenuto = new Text(r.getTesto() != null ? r.getTesto() : ""); // mostra testo recensione se non null, altrimenti vuoto
                contenuto.setStyle("-fx-font-weight: bold;");
                // usa TextFlow per il testo con possibile wrapping
                TextFlow tf = new TextFlow(labelTesto, contenuto);
                tf.setLineSpacing(1.2); // spazio tra le righe
                tf.setMaxWidth(Double.MAX_VALUE); // permette al TextFlow di espandersi
                // autore della recensione
                String autore = (r.getGenitore() != null)
                        ? (r.getGenitore().getNome() + " " + r.getGenitore().getCognome()) // dettagli autore
                        : "autore sconosciuto"; // se genitore null
                Label autoreLbl = new Label("Autore: " + autore);

                VBox v = new VBox(4, tf, autoreLbl);
                v.setMaxWidth(Double.MAX_VALUE); // permette al VBox di espandersi
                // bottone elimina se permesso
                if (canDelete) {
                    Button del = new Button("Elimina");
                    del.setOnAction(ev -> { // azione di cancellazione
                        RichiestaCancellaRecensione richiesta = new RichiestaCancellaRecensione(r); // crea richiesta cancellazione
                        try {
                            client.sendMessage(richiesta); // invia richiesta al server
                        } catch (IOException e) {
                            System.out.println("Errore invio cancellazione recensione: " + e.getMessage());
                            messaggioerrore2.setText("Errore invio cancellazione recensione: " + e.getMessage()); // mostra errore
                        }
                        // attendi la risposta del server
                        this.attendi = true;
                        while (attendi) {
                            try {
                                Thread.sleep(100); // attesa di 100ms
                            } catch (InterruptedException ex) {
                                System.out.println("Errore attesa recensioni: " + ex.getMessage());
                                messaggioerrore2.setText("Errore attesa recensioni: " + ex.getMessage()); // mostra errore
                            }
                        }
                        rStage.close(); // chiudi la finestra
                    });
                    // layout orizzontale con bottone elimina
                    HBox h = new HBox(8, del, v);
                    h.setAlignment(Pos.CENTER_LEFT);
                    listBox.getChildren().addAll(h, new Separator()); // aggiungi alla lista con separatore
                } else {
                    listBox.getChildren().addAll(v, new Separator()); // aggiungi alla lista con separatore
                }
            }
        }

        ScrollPane sc = new ScrollPane(listBox); // scroll pane per la lista
        sc.setFitToWidth(true); // adatta la larghezza al contenuto
        sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // niente scrollbar orizzontale

        BorderPane root = new BorderPane(); // layout principale
        root.setTop(topBar);
        root.setCenter(sc);

        Scene scene = new Scene(root, 540, 420); // crea la scena
        rStage.setScene(scene); // imposta la scena
        rStage.show(); // mostra la finestra
    }

    /**
     * Mostra un dialog per aggiungere una nuova recensione.
     *
     * @param owner  finestra proprietaria
     * @param libro  libro da recensire
     * @param utente utente che aggiunge la recensione
     */
    private void showAggiungiRecensioneDialog(Stage owner, Libro libro, Utente utente) {
        Stage d = new Stage(); // nuova finestra di dialogo per la nuova recensione
        d.initOwner(owner); // imposta la finestra come proprietaria
        d.initModality(Modality.WINDOW_MODAL);  // blocca l'interazione con la finestra proprietaria
        d.setTitle("Aggiungi recensione"); // titolo finestra
        // Layout principale
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        // Componenti della finestra
        Label info = new Label("Scrivi la recensione (max 150 caratteri):");
        TextArea area = new TextArea(); // campo di testo per la recensione
        area.setWrapText(true); // abilita il wrapping del testo
        area.setPromptText("La tua recensione..."); // testo di suggerimento
        Label counter = new Label("0/150"); // contatore caratteri
        // aggiorna contatore caratteri in tempo reale
        area.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 150) area.setText(newV.substring(0, 150)); // limita a 150 caratteri
            else counter.setText(area.getText().length() + "/150"); // aggiorna contatore
        });
        // checkbox di conferma
        CheckBox conferma = new CheckBox("Confermo la recensione");
        Button invia = new Button("Invia");
        invia.setDisable(true); // disabilita il bottone inizialmente
        // abilita il bottone solo se checkbox selezionata e testo non vuoto
        // listener per checkbox e area testo
        conferma.selectedProperty().addListener((obs, oldV, newV) -> invia.setDisable(!newV || area.getText().isBlank())); // listener per area testo
        area.textProperty().addListener((obs, oldV, newV) -> invia.setDisable(!conferma.isSelected() || newV.isBlank())); // listener per checkbox
        // azione bottone invia
        invia.setOnAction(ev -> {
            String testo = area.getText().trim(); // ottieni il testo della recensione
            if (testo.isEmpty()) return; // se vuoto non fare nulla
            // crea la recensione
            Recensione r = new Recensione(null, null, null);
            r.setTesto(testo); // imposta il testo
            r.setLibro(libro); // imposta il libro
            r.setGenitore((Genitore) utente); // imposta il genitore
            // logica messaggio da aggiungere alla recensione
            RichiestaAggiungiRecensione richiesta = new RichiestaAggiungiRecensione(r);
            try {
                client.sendMessage(richiesta);
            } catch (IOException e) {
                System.out.println("Errore invio recensione: " + e.getMessage());
                messaggioerrore.setText("Errore invio recensione: " + e.getMessage());
            }
            d.close(); // chiudi la finestra
        });
        // bottone annulla
        Button annulla = new Button("Annulla");
        annulla.setOnAction(ev -> d.close());
        // layout orizzontale per i bottoni
        HBox btns = new HBox(8, invia, annulla);
        btns.setAlignment(Pos.CENTER_RIGHT);
        // aggiungi componenti al layout principale
        root.getChildren().addAll(info, area, counter, conferma, btns);

        Scene sc = new Scene(root, 520, 320); // crea la scena
        d.setScene(sc); // imposta la scena
        d.show(); // mostra la finestra
    }

    /**
     * Mostra un messaggio di errore nella sezione del dettaglio libro.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore.setText(msgerrore);
        });
    }

    /**
     * Mostra un messaggio di errore nella sezione delle recensioni.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore2(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore2.setText(msgerrore);
        });
    }

    /**
     * Aggiorna la lista delle recensioni con una nuova recensione aggiunta.
     *
     * @param nuovarecensione la nuova recensione da aggiungere
     */
    public void aggiornaRecensioni(Recensione nuovarecensione) {
        recensioniAggiornate.add(nuovarecensione);
    }

    /**
     * Rimuove una recensione dalla lista delle recensioni.
     *
     * @param idRecensione l'ID della recensione da rimuovere
     */
    public void cancellaRecensione(int idRecensione) {
        recensioniAggiornate.removeIf(r -> r.getId() == idRecensione);
    }

}


