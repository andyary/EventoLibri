package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Message.RichiestaRecensioniERecensibilita;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe View per la schermata principale del genitore.
 */
public class HomeGenitore {
    // ------ ATTRIBUTI ------
    private final Client client;
    private Stage stage;
    private Scene scene;
    private Genitore genitore;
    private ArrayList<Evento> eventiProssimi;
    private Button nextEventiButton;
    private EventoView eventoView;
    private ProfiloGenitore profiloGenitore;
    private Runnable onBack;

    private LibroDetailedView libroDetailedView; // view per visualizzare i dettagli del libro
    private ArrayList<Libro> elencoLibri = new ArrayList<>();
    private boolean recensibile;
    private ArrayList<Recensione> recensioni = new ArrayList<>(); // da caricare dal server
    private boolean attendi;

    private Label messaggioerrore;

    /**
     * Costruttore della classe HomeGenitore.
     *
     * @param client            l'istanza del client per la comunicazione con il server
     * @param eventoView        la view per la visualizzazione dei dettagli di un evento
     * @param profiloGenitore   la view per la visualizzazione e modifica del profilo del genitore
     * @param libroDetailedView la view per la visualizzazione dei dettagli di un libro
     */
    public HomeGenitore(Client client, EventoView eventoView, ProfiloGenitore profiloGenitore, LibroDetailedView libroDetailedView) {
        this.eventoView = eventoView;
        this.client = client;
        this.profiloGenitore = profiloGenitore;
        this.libroDetailedView = libroDetailedView;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
    }

    /**
     * Imposta l'elenco dei libri disponibili.
     *
     * @param elencoLibri l'elenco dei libri
     */
    public void setElencoLibri(ArrayList<Libro> elencoLibri) {
        this.elencoLibri = elencoLibri == null ? new ArrayList<>() : elencoLibri; // imposta elenco libri con lista vuota se null
    }

    /**
     * Imposta se il genitore può recensire un libro.
     *
     * @param recensibile true se il genitore può recensire, false altrimenti
     */
    public void setRecensibile(boolean recensibile) {
        this.recensibile = recensibile;
    }

    /**
     * Imposta l'elenco delle recensioni.
     *
     * @param recensioni l'elenco delle recensioni
     */
    public void setRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    /**
     * Aggiunge una recensione all'elenco delle recensioni.
     *
     * @param recensione la recensione da aggiungere
     */
    public void addRecensione(Recensione recensione) {
        if (this.recensioni != null) this.recensioni.add(recensione);
    }

    /**
     * Rimuove una recensione dall'elenco delle recensioni.
     *
     * @param recensione la recensione da rimuovere
     */
    public void delRecensione(Recensione recensione) {
        if (this.recensioni != null) this.recensioni.remove(recensione); // rimuovi recensione
    }

    /**
     * Restituisce il genitore associato alla home.
     *
     * @return il genitore
     */
    public Genitore getGenitore() {
        return genitore;
    }

    /**
     * Restituisce l'elenco degli eventi prossimi.
     *
     * @return l'elenco degli eventi prossimi
     */
    public ArrayList<Evento> getEventiProssimi() {
        return eventiProssimi;
    }

    /**
     * Imposta lo stato di attesa per le operazioni asincrone.
     *
     * @param attendi true se in attesa, false altrimenti
     */
    public void setAttendi(boolean attendi) {
        this.attendi = attendi;
    }

    /**
     * Restituisce la scena associata alla home.
     *
     * @return la scena
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Mostra la schermata principale del genitore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param genitore       il genitore di cui visualizzare la home
     * @param eventiProssimi l'elenco degli eventi prossimi
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Logout"
     */
    public void show(Stage stage, Genitore genitore, ArrayList<Evento> eventiProssimi, Runnable onBack) {
        // imposta attributi
        this.stage = stage;
        this.genitore = genitore;
        this.eventiProssimi = eventiProssimi != null ? eventiProssimi : new ArrayList<>(); // imposta lista vuota se null
        this.onBack = onBack; // azione logout

        // richiedi lettori, luoghi, libri per aggiornare dati locali
        RichiestaLettoriELuoghiELibri richiestaLettoriELuoghiELibri = new RichiestaLettoriELuoghiELibri();
        try {
            client.sendMessage(richiestaLettoriELuoghiELibri);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
            messaggioerrore.setText("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage()); // mostra errore
        }

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo e figli");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            profiloGenitore.show(stage, genitore, () -> reloadEventiFromServer()); // ricarica eventi al ritorno
        });
        // logout button
        Button backButton = new Button("Logout");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run(); // esegui azione logout
        });
        // selezione libro recensioni
        Label libroSelezionatoLabel = new Label("Seleziona libro (recensioni)");
        Button scegliLibroBtn = new Button("Scegli libro");
        final Libro[] libroSelezionato = new Libro[1]; // array per memorizzare libro selezionato

        scegliLibroBtn.setOnAction(e -> {
            messaggioerrore.setText(""); // reset messaggio errore
            LibroView dialog = new LibroView();
            Libro libro = dialog.show(stage, elencoLibri); // mostra finestra selezione libro
            if (libro != null) {
                libroSelezionatoLabel.setText(
                        libro.getTitolo() + " (" + libro.getTempoLettura() + " min)" // aggiorna label libro selezionato
                );
                libroSelezionato[0] = libro; // memorizza libro selezionato
                // recupera recensioni libro
                recensioni.clear(); // reset recensioni
                recensibile = false; // reset recensibilità

                // recupera recensibilità
                RichiestaRecensioniERecensibilita richiesta = new RichiestaRecensioniERecensibilita(libro, genitore);
                try {
                    client.sendMessage(richiesta);
                } catch (IOException ex) {
                    messaggioerrore.setText("Errore nell'invio della richiesta recensioni e recensibilità: " + ex.getMessage());
                }

                this.attendi = true; // imposta stato attesa
                while (attendi) {
                    try {
                        Thread.sleep(100); // attende 100ms
                    } catch (InterruptedException ex) {
                        System.out.println("Errore attesa recensioni: " + ex.getMessage()); // log errore
                    }
                }
                ;

                // apri dettaglio libro
                libroDetailedView.show(
                        stage,
                        libroSelezionato[0],
                        recensioni,
                        genitore,
                        recensibile,
                        () -> {
                            this.show(stage, genitore, this.eventiProssimi, onBack); // ricarica home al ritorno
                        }
                );
            }
        });

        // top bar layout
        HBox topBar = new HBox(new Label("  Benvenuto, (genitore) " + genitore.getNome() + "!          "), profiloButton, scegliLibroBtn, backButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        VBox topBox = new VBox(topBar, messaggioerrore);
        topBox.setAlignment(Pos.CENTER);


        // formato per colonne
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("yyyy/MM/dd"); // formato data
        DateTimeFormatter formatoOra = DateTimeFormatter.ofPattern("HH:mm"); // formato ora

        // ---------- EVENTI FIGLI: usa TableView per ciascun figlio ----------
        VBox figliSection = new VBox(10);
        figliSection.setPadding(new Insets(10));

        if (genitore.getFigli().isEmpty()) {
            figliSection.getChildren().add(new Label("Nessun figlio.")); // nessun figlio
        } else {
            int countFigli = 1;
            for (Figlio f : genitore.getFigli()) { // per ogni figlio
                List<Evento> listaEventi = f.getIscrizioni(); // eventi iscritti del figlio
                VBox boxFiglio = new VBox(10);
                boxFiglio.setPadding(new Insets(5, 0, 5, 10));
                Label titoloFiglio = new Label("Eventi di " + f.getNome() + " (figlio" + countFigli + "):"); // titolo figlio

                countFigli++; // incrementa contatore figli
                boxFiglio.getChildren().add(titoloFiglio);
                if (listaEventi == null || listaEventi.isEmpty()) {
                    boxFiglio.getChildren().add(new Label("Nessun evento iscritto.")); // iscritto a nessun evento
                } else { // crea tabella eventi iscritto con 3 righe visibili
                    TableView<Evento> tableFiglio = createEventoTableView(listaEventi, 3, formatoData, formatoOra);
                    boxFiglio.getChildren().add(tableFiglio);
                }
                figliSection.getChildren().add(boxFiglio);
            }
        }

        // ---------- EVENTI PROSSIMI ----------
        VBox eventiProssimiBox = new VBox(10);
        eventiProssimiBox.getChildren().add(new Label("Prossimi eventi disponibili:"));
        eventiProssimiBox.setPadding(new Insets(10));

        if (this.eventiProssimi == null || this.eventiProssimi.isEmpty()) {
            eventiProssimiBox.getChildren().add(new Label("Nessun evento disponibile."));
        } else { // crea tabella eventi prossimi con 5 righe visibili
            TableView<Evento> tableProssimi = createEventoTableView(this.eventiProssimi, 5, formatoData, formatoOra);
            eventiProssimiBox.getChildren().add(tableProssimi);
        }

        // pulsante "Carica Eventi Successivi" - come in HomeLettore
        if (this.eventiProssimi != null && this.eventiProssimi.size() >= 10) {
            nextEventiButton = new Button("Carica Eventi Successivi");
            nextEventiButton.setOnAction(e -> {
                Evento last = this.eventiProssimi.isEmpty() ? null : this.eventiProssimi.get(this.eventiProssimi.size() - 1); // prendi ultimo evento
                RichiestaNextEventi req = new RichiestaNextEventi(last); // crea richiesta eventi successivi
                try {
                    client.sendMessage(req); // invia richiesta al server
                } catch (Exception ex) {
                    System.out.println(ex.getMessage()); // log errore
                }
            });
            eventiProssimiBox.getChildren().add(nextEventiButton); // aggiungi pulsante al box
        }

        // ---------- CONTENUTO CENTRALE ----------
        VBox centro = new VBox(20, figliSection, eventiProssimiBox);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(20));

        // ---------- LAYOUT FINALE ----------
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centro);

        ScrollPane scrollPane = new ScrollPane(root); // Aggiunto ScrollPane
        scrollPane.setFitToWidth(true); // Adatta il contenuto alla larghezza della finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // barra verticale se necessaria
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // mai barra orizzontale

        this.scene = new Scene(scrollPane, 750, 780); // crea scena
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle("Home Genitore");
            stage.show();
        });
    }

    /**
     * Verifica se la view è attualmente visualizzata sullo stage specificato.
     *
     * @param stage lo stage da verificare
     * @return true se la view è visualizzata sullo stage, false altrimenti
     */

    /**
     * Restituisce lo stage associato alla view.
     *
     * @return lo stage
     */
    public Stage getStage() {
        return stage;
    }


    /**
     * Nasconde il pulsante "Carica Eventi Successivi".
     */
    public void nascondiBottoneNextEventi() {
        Platform.runLater(() -> {
            if (nextEventiButton != null) nextEventiButton.setVisible(false); // nascondi bottone
        });
    }

    /**
     * Crea una TableView per visualizzare gli eventi.
     * Crea TableView<Evento> con colonne: Data, Ora inizio, Ora fine (calcolaOraFine), Titolo.
     * visibleRows indica il numero di righe visibili. TableView gestisce lo scrolling interno se ci sono più righe.
     *
     * @param eventi      l'elenco degli eventi da visualizzare
     * @param visibleRows il numero di righe visibili nella tabella
     * @param formatoData il formato per la data
     * @param formatoOra  il formato per l'ora
     * @return la TableView degli eventi
     */
    private TableView<Evento> createEventoTableView(List<Evento> eventi, int visibleRows, DateTimeFormatter formatoData, DateTimeFormatter formatoOra) {
        TableView<Evento> table = new TableView<>();
        // crea colonne
        TableColumn<Evento, String> dataCol = new TableColumn<>("Data"); // colonna data
        dataCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue(); // ottieni evento
            String val = (ev != null && ev.getData() != null) ? ev.getData().toLocalDate().format(formatoData) : ""; // formatta data
            return new SimpleStringProperty(val); // restituisci proprietà stringa
        });
        dataCol.setSortable(true); // abilita ordinamento
        dataCol.setPrefWidth(90);
        dataCol.setMinWidth(70);
        dataCol.setMaxWidth(120);
        dataCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Evento, String> oraInizioCol = new TableColumn<>("Ora inizio"); // colonna ora inizio
        oraInizioCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue(); // ottieni evento
            String val = (ev != null && ev.getData() != null) ? ev.getData().toLocalTime().format(formatoOra) : ""; // formatta ora inizio
            return new SimpleStringProperty(val); // restituisci proprietà stringa
        });
        oraInizioCol.setSortable(true); // abilita ordinamento
        oraInizioCol.setPrefWidth(70);
        oraInizioCol.setMinWidth(50);
        oraInizioCol.setMaxWidth(90);
        oraInizioCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Evento, String> oraFineCol = new TableColumn<>("Ora fine"); // colonna ora fine
        oraFineCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue(); // ottieni evento
            String val = ""; // valore di default vuoto
            try {
                if (ev != null && ev.calcolaOraFine() != null) {
                    val = ev.calcolaOraFine().format(formatoOra); // calcola e formatta ora fine
                }
            } catch (Exception ex) {
                System.out.println(ex); // log errore calcolo ora fine
            }
            return new SimpleStringProperty(val); // restituisci proprietà stringa
        });
        oraFineCol.setSortable(true); // abilita ordinamento
        oraFineCol.setPrefWidth(70);
        oraFineCol.setMinWidth(50);
        oraFineCol.setMaxWidth(90);
        oraFineCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Evento, String> luogoCol = new TableColumn<>("Luogo evento"); // colonna luogo evento
        luogoCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue(); // ottieni evento
            String val = (ev != null && ev.getLuogo() != null) ? ev.getLuogo().getNome() : ""; // ottieni nome luogo
            return new SimpleStringProperty(val); // restituisci proprietà stringa
        });
        luogoCol.setPrefWidth(70);
        luogoCol.setMinWidth(50);
        luogoCol.setMaxWidth(90);
        luogoCol.setSortable(true); // abilita ordinamento

        TableColumn<Evento, String> titoloCol = new TableColumn<>("Titolo evento"); // colonna titolo evento
        titoloCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getNome() : "")); // ottieni titolo evento se non null
        titoloCol.setSortable(true); // abilita ordinamento

        table.getColumns().addAll(dataCol, oraInizioCol, oraFineCol, luogoCol, titoloCol); // aggiungi colonne alla tabella

        // popola tabella
        ObservableList<Evento> items = FXCollections.observableArrayList(eventi); // crea ObservableList da lista eventi
        table.setItems(items); // imposta items nella tabella

        // Imposta ordinamento iniziale per data in ordine crescente
        dataCol.setSortType(TableColumn.SortType.ASCENDING); // ordinamento crescente
        table.getSortOrder().clear(); // rimuovi ordinamenti precedenti
        table.getSortOrder().add(dataCol); // aggiungi ordinamento per data
        table.sort(); // applica l'ordinamento

        // Altezza preferita: riga 25px + header 30px
        double rowHeight = 25;
        double headerHeight = 30;
        table.setPrefHeight(visibleRows * rowHeight + headerHeight); // imposta altezza preferita
        // Imposta politica di ridimensionamento delle colonne
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // doppio clic su riga per aprire l'evento
        table.setRowFactory(tv -> {
            TableRow<Evento> row = new TableRow<>(); // crea nuova riga
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) // doppio clic
                {
                    Evento selected = row.getItem(); // ottieni evento selezionato
                    eventoView.show(stage, selected, genitore, () -> reloadEventiFromServer()); // mostra dettagli evento e ricarica eventi al ritorno
                }
            });
            return row; // restituisci riga
        });

        if (items.isEmpty()) {
            table.setPlaceholder(new Label("Nessun evento.")); // messaggio se nessun evento
        }
        return table; // restituisci tabella
    }

    /**
     * Aggiorna l'elenco degli eventi prossimi e aggiorna la visualizzazione.
     *
     * @param prossimiEventi l'elenco degli eventi prossimi da aggiungere
     */
    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        if (this.eventiProssimi == null) this.eventiProssimi = new ArrayList<>(); // inizializza lista se null
        // aggiungi gli eventi nella nuova lista prossimi eventi se l'id dell'evento
        // non c'è nella vecchia lista
        for (Evento ev : prossimiEventi) {
            boolean found = false; // flag per evento trovato
            for (Evento oldEv : this.eventiProssimi) {
                if (ev.getId() == oldEv.getId()) { // confronto id eventi
                    found = true; // cambia flag se trovato
                    break; // esci dal ciclo
                }
            }
            if (!found) {
                this.eventiProssimi.add(ev); // aggiungi evento se non trovato
            }
        }
        Platform.runLater(() -> this.show(stage, genitore, this.eventiProssimi, onBack));
    }

    /**
     * Aggiorna l'elenco dei libri disponibili.
     *
     * @param elencolibri l'elenco dei libri
     */
    public void aggiornaLibri(ArrayList<Libro> elencolibri) {
        Platform.runLater(() -> this.elencoLibri = elencolibri);
    }

    /**
     * Mostra un messaggio di errore nella schermata principale.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore.setText(msgerrore); // mostra messaggio errore
        });
    }

    /**
     * Ricarica l'elenco dei prossimi eventi dal server.
     */
    public void reloadEventiFromServer() {
        if (eventiProssimi == null) eventiProssimi = new ArrayList<>(); // inizializza lista se null
        eventiProssimi.clear(); // pulisci lista eventi prossimi

        // richiedi al server i prossimi eventi
        try {
            client.sendMessage(new RichiestaNextEventi(null));
        } catch (Exception ex) {
            System.out.println("Errore richiesta eventi: " + ex.getMessage());
        }
    }
}