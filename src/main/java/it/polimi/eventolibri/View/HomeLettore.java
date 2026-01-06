package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Message.RichiestaRecensioniERecensibilita;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Lettore;
import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Recensione;
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
import java.util.stream.Collectors;

/**
 * Classe View per la home di un lettore.
 */
public class HomeLettore {

    private final Client client;
    private Stage stage;
    private Scene scene;
    private Lettore lettore;
    private ArrayList<Evento> eventiProssimi;
    private Button nextEventiButton;
    private EventoViewLettore eventoView;
    private ProfiloLettore profiloLettore;
    private Runnable onBack;

    private LibroDetailedView libroDetailedView;
    private ArrayList<Libro> elencoLibri = new ArrayList<>();
    private boolean recensibile;
    private ArrayList<Recensione> recensioni = new ArrayList<>(); // da caricare dal server
    private boolean attendi;

    private Label messaggioerrore;

    /**
     * Costruttore della classe HomeLettore.
     *
     * @param client          l'istanza del client per la comunicazione con il server
     * @param eventoView      la view per la visualizzazione degli eventi
     * @param profiloLettore  la view per la visualizzazione del profilo del lettore
     * @param libroDetailedView la view per la visualizzazione dettagliata del libro
     */
    public HomeLettore(Client client, EventoViewLettore eventoView, ProfiloLettore profiloLettore, LibroDetailedView libroDetailedView) {
        this.client = client;
        this.eventoView = eventoView;
        this.profiloLettore = profiloLettore;
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
        this.elencoLibri = elencoLibri == null ? new ArrayList<>() : elencoLibri;
    }

    /**
     * Imposta se il lettore può recensire il libro selezionato.
     *
     * @param recensibile true se il lettore può recensire, false altrimenti
     */
    public void setRecensibile(boolean recensibile) {
        this.recensibile = recensibile;
    }

    /**
     * Imposta l'elenco delle recensioni del libro selezionato.
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
        if (this.recensioni != null) this.recensioni.remove(recensione);
    }

    /**
     * Restituisce il lettore associato alla home.
     *
     * @return il lettore
     */
    public Lettore getLettore() {
        return lettore;
    }

    /**
     * Restituisce l'elenco dei prossimi eventi disponibili.
     *
     * @return l'elenco dei prossimi eventi
     */
    public ArrayList<Evento> getEventiProssimi() {
        return eventiProssimi;
    }

    /**
     * Imposta lo stato di attesa per le operazioni asincrone.
     *
     * @param attendi true se si è in attesa, false altrimenti
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
     * Mostra la schermata principale della home del lettore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param lettore        il lettore di cui visualizzare la home
     * @param eventiProssimi l'elenco dei prossimi eventi disponibili
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Logout"
     */
    public void show(Stage stage, Lettore lettore, ArrayList<Evento> eventiProssimi, Runnable onBack) {
        this.stage = stage;
        this.lettore = lettore;
        this.eventiProssimi = eventiProssimi != null ? eventiProssimi : new ArrayList<>();
        this.onBack = onBack;

        // richiesta lettori, luoghi, libri al server (per aggiornare le liste nel client)
        RichiestaLettoriELuoghiELibri richiestaLettoriELuoghiELibri = new RichiestaLettoriELuoghiELibri();
        try {
            client.sendMessage(richiestaLettoriELuoghiELibri);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
            messaggioerrore.setText("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
        }

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo lettore");
        profiloButton.setOnAction(e -> {
            profiloLettore.show(stage, lettore, () -> {
                this.show(stage, lettore, this.eventiProssimi, onBack);
            });
        });

        Button newEventoButton = new Button("Crea Nuovo Evento");
        newEventoButton.setOnAction(e -> {
            Evento eventoTemp = new Evento( "", null, java.time.LocalDateTime.now());
            eventoTemp.setCreatore(lettore);
            eventoView.show(stage, eventoTemp , lettore, () -> {
                reloadEventiFromServer();
            });
        });

        Button backButton = new Button("Logout");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        Label libroSelezionatoLabel = new Label("Seleziona libro (recensioni)");
        Button scegliLibroBtn = new Button("Scegli libro");
        final Libro[] libroSelezionato = new Libro[1];

        scegliLibroBtn.setOnAction(e -> {
            messaggioerrore.setText("");
            LibroView dialog = new LibroView();
            Libro libro = dialog.show(stage, elencoLibri);
            if (libro != null) {
                libroSelezionatoLabel.setText(
                        libro.getTitolo() + " (" + libro.getTempoLettura() + " min)"
                );
                libroSelezionato[0] = libro;
                // recupera recensioni libro
                recensioni.clear();
                recensibile = false;

                // recupera recensibilità
                RichiestaRecensioniERecensibilita richiesta = new RichiestaRecensioniERecensibilita(libro, lettore);
                try {
                    client.sendMessage(richiesta);
                } catch (IOException ex) {
                    messaggioerrore.setText("Errore nell'invio della richiesta recensioni e recensibilità: " + ex.getMessage());
                }

                this.attendi = true;
                while (attendi) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.out.println("Errore attesa recensioni: " + ex.getMessage());
                    }
                };

                // apri dettaglio libro
                libroDetailedView.show(
                        stage,
                        libroSelezionato[0],
                        recensioni,
                        lettore,
                        recensibile,
                        () -> {
                            this.show(stage, lettore, this.eventiProssimi, onBack);
                        }
                );
            }
        });

        HBox topRow = new HBox(10, new Label("  Benvenuto, (lettore) " + lettore.getNome() + "!          "), profiloButton, newEventoButton, scegliLibroBtn ,backButton);
        topRow.setAlignment(Pos.TOP_RIGHT);
        HBox topRow2 = new HBox(10, messaggioerrore);
        VBox topBar = new VBox(topRow, topRow2);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.CENTER);

        // formatter per colonne
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter formatoOra = DateTimeFormatter.ofPattern("HH:mm");

        // ---------- TABELLE ----------

        // Eventi creati (5 righe visibili)
        TableView<Evento> tableCreati = createEventoTableView(lettore.getEventiCreati(), 5, formatoData, formatoOra);
        Label lblCreati = new Label("Eventi creati:");
        VBox eventiCreatiBox = new VBox(5, lblCreati, tableCreati);
        eventiCreatiBox.setPadding(new Insets(10));

        // Eventi a cui leggerai (5 righe visibili)
        TableView<Evento> tableIscritti = createEventoTableView(lettore.getIscrizioniLettura(), 5, formatoData, formatoOra);
        Label lblIscritti = new Label("Eventi a cui leggerai:");
        VBox eventiIscrittoBox = new VBox(5, lblIscritti, tableIscritti);
        eventiIscrittoBox.setPadding(new Insets(10));

        // Prossimi eventi disponibili (5 righe visibili) - filtrati
        List<Evento> filtrati = this.eventiProssimi.stream()
                .filter(ev -> !lettore.getEventiCreati().stream().anyMatch(e -> e.getId() == ev.getId()))
                .filter(ev -> !lettore.getIscrizioniLettura().stream().anyMatch(e -> e.getId() == ev.getId()))
                .collect(Collectors.toList());
        TableView<Evento> tableProssimi = createEventoTableView(filtrati, 5, formatoData, formatoOra);
        Label lblProssimi = new Label("Prossimi eventi disponibili:");
        VBox eventiProssimiBox = new VBox(5, lblProssimi, tableProssimi);
        eventiProssimiBox.setPadding(new Insets(10));

        // pulsante "Carica Eventi Successivi" - mantiene stessa logica
        if (this.eventiProssimi.size() >= 10) {
            nextEventiButton = new Button("Carica Eventi Successivi");
            nextEventiButton.setOnAction(e -> {
                Evento last = this.eventiProssimi.isEmpty() ? null : this.eventiProssimi.get(this.eventiProssimi.size() - 1);
                RichiestaNextEventi req = new RichiestaNextEventi(last);
                try {
                    client.sendMessage(req);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            });
            eventiProssimiBox.getChildren().add(nextEventiButton);
        }

        // ---------- CONTENUTO CENTRALE ----------
        VBox centro = new VBox(20, eventiCreatiBox, eventiIscrittoBox, eventiProssimiBox);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(20));

        // ---------- LAYOUT FINALE ----------
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centro);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.scene = new Scene(scrollPane, 800, 750);
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle("Home Lettore");
            stage.show();
        });
    }

    /**
     * Restituisce lo stage associato alla view.
     *
     * @return lo stage
     */
    public Stage getStage() {
        return stage;
    }


    /* Nasconde il bottone per caricare più eventi.
     */
    public void nascondiBottoneNextEventi() {
        if (nextEventiButton != null) nextEventiButton.setVisible(false);
    }

    /** Crea una TableView per visualizzare gli eventi.
     * Crea TableView<Evento> con colonne: Data, Ora inizio, Ora fine (calcolaOraFine), Titolo.
     * visibleRows indica il numero di righe visibili. TableView gestisce lo scrolling interno se ci sono più righe.
     * @param eventi       l'elenco degli eventi da visualizzare
     * @param visibleRows il numero di righe visibili nella tabella
     * @param formatoData il formato per la visualizzazione della data
     * @param formatoOra  il formato per la visualizzazione dell'ora
     * @return la TableView contenente gli eventi
     */
    private TableView<Evento> createEventoTableView(List<Evento> eventi, int visibleRows, DateTimeFormatter formatoData, DateTimeFormatter formatoOra) {
        TableView<Evento> table = new TableView<>();

        TableColumn<Evento, String> dataCol = new TableColumn<>("Data");
        dataCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue();
            String val = (ev != null && ev.getData() != null) ? ev.getData().toLocalDate().format(formatoData) : "";
            return new SimpleStringProperty(val);
        });
        dataCol.setSortable(true);
        dataCol.setPrefWidth(90);
        dataCol.setMinWidth(70);
        dataCol.setMaxWidth(120);
        dataCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Evento, String> oraInizioCol = new TableColumn<>("Ora inizio");
        oraInizioCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue();
            String val = (ev != null && ev.getData() != null) ? ev.getData().toLocalTime().format(formatoOra) : "";
            return new SimpleStringProperty(val);
        });
        oraInizioCol.setSortable(true);
        oraInizioCol.setPrefWidth(70);
        oraInizioCol.setMinWidth(50);
        oraInizioCol.setMaxWidth(90);
        oraInizioCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Evento, String> oraFineCol = new TableColumn<>("Ora fine");
        oraFineCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue();
            String val = "";
            try {
                if (ev != null && ev.calcolaOraFine() != null) {
                    val = ev.calcolaOraFine().format(formatoOra);
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty(val);
        });
        oraFineCol.setSortable(true);
        oraFineCol.setPrefWidth(70);
        oraFineCol.setMinWidth(50);
        oraFineCol.setMaxWidth(90);
        oraFineCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Evento, String> luogoCol = new TableColumn<>("Luogo evento");
        luogoCol.setCellValueFactory(cell -> {
            Evento ev = cell.getValue();
            String val = (ev != null && ev.getLuogo() != null) ? ev.getLuogo().getNome() : "";
            return new SimpleStringProperty(val);
        });
        luogoCol.setPrefWidth(70);
        luogoCol.setMinWidth(50);
        luogoCol.setMaxWidth(90);
        luogoCol.setSortable(true);

        TableColumn<Evento, String> titoloCol = new TableColumn<>("Titolo evento");
        titoloCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getNome() : ""));
        titoloCol.setSortable(true);
        // titoloCol rimane flessibile: non impostare maxWidth in modo che occupi lo spazio residuo

        table.getColumns().addAll(dataCol, oraInizioCol, oraFineCol, luogoCol,titoloCol);

        ObservableList<Evento> items = FXCollections.observableArrayList(eventi);
        table.setItems(items);

        // Imposta ordinamento iniziale per data in ordine crescente
        dataCol.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().clear();
        table.getSortOrder().add(dataCol);
        table.sort();

        // Altezza preferita: approssimazione riga 25px + header 30px
        double rowHeight = 25;
        double headerHeight = 30;
        table.setPrefHeight(visibleRows * rowHeight + headerHeight);

        // Mantieni policy che riempie la larghezza disponibile, ma rispetta i max/min impostati
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // doppio clic su riga per aprire l'evento
        table.setRowFactory(tv -> {
            TableRow<Evento> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                    Evento selected = row.getItem();
                    eventoView.show(stage, selected, lettore, () -> reloadEventiFromServer());
                }
            });
            return row;
        });

        if (items.isEmpty()) {
            table.setPlaceholder(new Label("Nessun evento."));
        }

        return table;
    }

    /**
     * Aggiorna l'elenco dei prossimi eventi disponibili.
     *
     * @param prossimiEventi l'elenco dei prossimi eventi
     */
    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        if (this.eventiProssimi == null) this.eventiProssimi = new ArrayList<>();
        this.eventiProssimi.addAll(prossimiEventi);
        Platform.runLater(() -> this.show(stage, lettore, this.eventiProssimi, onBack));
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
     * Ricarica l'elenco dei prossimi eventi dal server.
     */
    public void reloadEventiFromServer() {
        if (eventiProssimi == null) eventiProssimi = new ArrayList<>();
        eventiProssimi.clear();

        try {
            client.sendMessage(new RichiestaNextEventi(null));
        } catch (Exception ex) {
            System.out.println("Errore richiesta eventi: " + ex.getMessage());
        }

        Platform.runLater(() -> this.show(stage, lettore, eventiProssimi, onBack));
    }

    /**
     * Mostra un messaggio di errore nella home del lettore.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }

}
