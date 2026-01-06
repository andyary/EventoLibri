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
    private final Client client;
    private Label messaggioerrore;
    private Label messaggioerrore2;
    private ArrayList<Recensione> recensioniAggiornate = new ArrayList<>();
    private boolean attendi;

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
     * @param stage owner
     * @param libro libro visualizzato
     * @param recensioni lista recensioni (può essere vuota)
     * @param utente chiamante (instanceof Genitore/Lettore/Amministratore)
     * @param recensibile se non null indica se il genitore può recensire; per lettore/adm viene ignorato
     * @param onBack callback per tornare alla home
     */
    public void show(Stage stage,
                     Libro libro,
                     ArrayList<Recensione> recensioni,
                     Utente utente,
                     Boolean recensibile,
                     Runnable onBack) {

        messaggioerrore = new Label(" ");
        messaggioerrore.setStyle("-fx-text-fill: red;");

        recensioniAggiornate.clear();
        for (Recensione recensione : recensioni) {
            recensioniAggiornate.add(recensione);
        }

        // decidere permessi basati sulla gerarchia di Utente
        boolean canAdd = false;
        boolean canDelete = false;

        if (utente instanceof Amministratore) {
            canAdd = false;
            canDelete = true;
        }
        if (utente instanceof Lettore) {
            canAdd = false;
            canDelete = false;
        }
        if (utente instanceof Genitore) {
            canAdd = recensibile;
            canDelete = false;
        }

        Stage s = new Stage();
        s.initOwner(stage);
        s.setTitle("Dettaglio libro");
        s.initModality(Modality.WINDOW_MODAL);

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

        Label titolo = new Label("Titolo: " + libro.getTitolo());
        Label autore = new Label("Autore: " + libro.getAutore());
        Label tempo = new Label("Tempo di lettura: " + libro.getTempoLettura() + " min");
        Label isbn = new Label("ISBN: " + libro.getIsbn());
        Hyperlink hlink = new Hyperlink(libro.getLink() == null ? "" : libro.getLink());
        hlink.setOnAction(ev -> {
            if (hlink.getText() == null || hlink.getText().isBlank()) return;
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(hlink.getText()));
            } catch (IOException | URISyntaxException ex) {
                messaggioerrore.setText("Impossibile aprire il link");
                System.out.println("Impossibile aprire il link: " + ex.getMessage());
            }
        });

        String recLabel = "Recensioni: ";
        recLabel += (canAdd ? "puoi aggiungere" : "non puoi aggiungere");
        recLabel += (" , ");
        recLabel += (canDelete ? "puoi cancellare" : "non puoi cancellare");

        Label linkLabel = new Label("Link: ");
        HBox link = new HBox(linkLabel, hlink);
        link.setAlignment(Pos.CENTER_LEFT);

        VBox dettagliBox = new VBox(8, titolo, autore, tempo, isbn, link, new Label(recLabel));
        dettagliBox.setPadding(new Insets(10));

        Button vediRecensioniBtn = new Button("Vedi recensioni");
        boolean finalCanDelete = canDelete;
        vediRecensioniBtn.setOnAction(ev -> showRecensioniWindow(s, recensioniAggiornate, finalCanDelete));


        VBox centerBox = new VBox(10, dettagliBox);
        centerBox.setPadding(new Insets(10));

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().add(vediRecensioniBtn);

        if (canAdd) {
            Button aggiungiRecBtn = new Button("Aggiungi recensione");
            aggiungiRecBtn.setOnAction(ev -> showAggiungiRecensioneDialog(s, libro, utente));
            buttonsBox.getChildren().add(aggiungiRecBtn);
        }

        centerBox.getChildren().add(buttonsBox);
        centerBox.getChildren().add(messaggioerrore);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 650, 380);
        Platform.runLater(() -> {
            s.setScene(scene);
            s.show();
        });
    }

    /**
     * Mostra una finestra con le recensioni del libro.
     *
     * @param owner finestra proprietaria
     * @param recensioni lista delle recensioni da visualizzare
     * @param canDelete indica se l'utente può cancellare le recensioni
     */
    private void showRecensioniWindow(Stage owner, ArrayList<Recensione> recensioni, boolean canDelete) {
        Stage rStage = new Stage();
        rStage.initOwner(owner);
        rStage.initModality(Modality.WINDOW_MODAL);
        rStage.setTitle("Recensioni");

        messaggioerrore2 = new Label(" ");
        messaggioerrore2.setStyle("-fx-text-fill: red;");

        Button backBtn = new Button("Indietro");
        backBtn.setOnAction(ev -> rStage.close());

        Region spacer = new Region();
        HBox topBar = new HBox(8, messaggioerrore2, spacer, backBtn);
        topBar.setPadding(new Insets(10));
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        VBox listBox = new VBox(8);
        listBox.setPadding(new Insets(10));

        if (recensioni == null || recensioni.isEmpty()) {
            listBox.getChildren().add(new Label("Nessuna recensione disponibile"));
        } else {
            for (Recensione r : recensioni) {
                Text labelTesto = new Text("Testo: ");
                Text contenuto = new Text(r.getTesto() != null ? r.getTesto() : "");
                contenuto.setStyle("-fx-font-weight: bold;");

                TextFlow tf = new TextFlow(labelTesto, contenuto);
                tf.setLineSpacing(1.2);
                tf.setMaxWidth(Double.MAX_VALUE);

                String autore = (r.getGenitore() != null)
                        ? (r.getGenitore().getNome() + " " + r.getGenitore().getCognome())
                        : "autore sconosciuto";
                Label autoreLbl = new Label("Autore: " + autore);

                VBox v = new VBox(4, tf, autoreLbl);
                v.setMaxWidth(Double.MAX_VALUE);

                if (canDelete) {
                    Button del = new Button("Elimina");
                    del.setOnAction(ev -> {
                        RichiestaCancellaRecensione richiesta = new RichiestaCancellaRecensione(r);
                        try {
                            client.sendMessage(richiesta);
                        } catch (IOException e) {
                            System.out.println("Errore invio cancellazione recensione: " + e.getMessage());
                            messaggioerrore2.setText("Errore invio cancellazione recensione: " + e.getMessage());
                        }

                        this.attendi = true;
                        while (attendi) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                System.out.println("Errore attesa recensioni: " + ex.getMessage());
                                messaggioerrore2.setText("Errore attesa recensioni: " + ex.getMessage());
                            }
                        }
                        rStage.close();
                    });

                    HBox h = new HBox(8, del, v);
                    h.setAlignment(Pos.CENTER_LEFT);
                    listBox.getChildren().addAll(h, new Separator());
                } else {
                    listBox.getChildren().addAll(v, new Separator());
                }
            }
        }

        ScrollPane sc = new ScrollPane(listBox);
        sc.setFitToWidth(true);
        sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(sc);

        Scene scene = new Scene(root, 540, 420);
        rStage.setScene(scene);
        rStage.show();
    }

    /**
     * Mostra un dialog per aggiungere una nuova recensione.
     *
     * @param owner finestra proprietaria
     * @param libro libro da recensire
     * @param utente utente che aggiunge la recensione
     */
    private void showAggiungiRecensioneDialog(Stage owner, Libro libro, Utente utente) {
        Stage d = new Stage();
        d.initOwner(owner);
        d.initModality(Modality.WINDOW_MODAL);
        d.setTitle("Aggiungi recensione");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        Label info = new Label("Scrivi la recensione (max 150 caratteri):");
        TextArea area = new TextArea();
        area.setWrapText(true);
        area.setPromptText("La tua recensione...");
        Label counter = new Label("0/150");

        area.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 150) area.setText(newV.substring(0, 150));
            else counter.setText(area.getText().length() + "/150");
        });

        CheckBox conferma = new CheckBox("Confermo la recensione");
        Button invia = new Button("Invia");
        invia.setDisable(true);

        conferma.selectedProperty().addListener((obs, oldV, newV) -> invia.setDisable(!newV || area.getText().isBlank()));
        area.textProperty().addListener((obs, oldV, newV) -> invia.setDisable(!conferma.isSelected() || newV.isBlank()));

        invia.setOnAction(ev -> {
            String testo = area.getText().trim();
            if (testo.isEmpty()) return;
            Recensione r = new Recensione(null, null, null);
            r.setTesto(testo);
            r.setLibro(libro);
            r.setGenitore((Genitore) utente);
            // logica messaggio da aggiorngere
            RichiestaAggiungiRecensione richiesta = new RichiestaAggiungiRecensione(r);
            try {
                client.sendMessage(richiesta);
            } catch (IOException e) {
                System.out.println("Errore invio recensione: " + e.getMessage());
                messaggioerrore.setText("Errore invio recensione: " + e.getMessage());
            }
            d.close();
        });

        Button annulla = new Button("Annulla");
        annulla.setOnAction(ev -> d.close());

        HBox btns = new HBox(8, invia, annulla);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(info, area, counter, conferma, btns);

        Scene sc = new Scene(root, 520, 320);
        d.setScene(sc);
        d.show();
    }

    /**
     * Mostra un messaggio di errore nella sezione del dettaglio libro.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }

    /**
     * Mostra un messaggio di errore nella sezione delle recensioni.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore2(String msgerrore) {
        Platform.runLater(()->{
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


