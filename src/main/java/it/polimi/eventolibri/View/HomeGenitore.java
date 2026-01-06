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

    private final Client client;
    private Stage stage;
    private Scene scene;
    private Genitore genitore;
    private ArrayList<Evento> eventiProssimi;
    private Button nextEventiButton;
    private EventoView eventoView;
    private ProfiloGenitore profiloGenitore;
    private Runnable onBack;

    private LibroDetailedView libroDetailedView;
    private ArrayList<Libro> elencoLibri = new ArrayList<>();
    private boolean recensibile;
    private ArrayList<Recensione> recensioni = new ArrayList<>(); // da caricare dal server
    private boolean attendi;

    private Label messaggioerrore;

    /**
     * Costruttore della classe HomeGenitore.
     *
     * @param client             l'istanza del client per la comunicazione con il server
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
        this.elencoLibri = elencoLibri == null ? new ArrayList<>() : elencoLibri;
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
        if (this.recensioni != null) this.recensioni.remove(recensione);
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
     * Mostra la schermata principale del genitore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param genitore       il genitore di cui visualizzare la home
     * @param eventiProssimi l'elenco degli eventi prossimi
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Logout"
     */
    public void show(Stage stage, Genitore genitore, ArrayList<Evento> eventiProssimi, Runnable onBack) {
        this.stage = stage;
        this.genitore = genitore;
        this.eventiProssimi = eventiProssimi != null ? eventiProssimi : new ArrayList<>();
        this.onBack = onBack;

        // richiedi lettori, luoghi, libri per aggiornare dati locali
        RichiestaLettoriELuoghiELibri richiestaLettoriELuoghiELibri = new RichiestaLettoriELuoghiELibri();
        try {
            client.sendMessage(richiestaLettoriELuoghiELibri);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
            messaggioerrore.setText("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
        }

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo e figli");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            profiloGenitore.show(stage, genitore, () -> {
                this.show(stage, genitore, this.eventiProssimi, onBack);
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
                RichiestaRecensioniERecensibilita richiesta = new RichiestaRecensioniERecensibilita(libro, genitore);
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
                        genitore,
                        recensibile,
                        () -> {
                            this.show(stage, genitore, this.eventiProssimi, onBack);
                        }
                );
            }
        });


        HBox topBar = new HBox(new Label("  Benvenuto, (genitore) " + genitore.getNome() + "!          "), profiloButton, scegliLibroBtn, backButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        VBox topBox = new VBox(topBar, messaggioerrore);
        topBox.setAlignment(Pos.CENTER);


        // formatter per colonne
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter formatoOra = DateTimeFormatter.ofPattern("HH:mm");

        // ---------- EVENTI FIGLI: usa TableView per ciascun figlio ----------
        VBox figliSection = new VBox(25);
        figliSection.setPadding(new Insets(10));
        Label titoloFigli = new Label("Eventi a cui sono iscritti i tuoi figli:");
        titoloFigli.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        figliSection.getChildren().add(titoloFigli);

        if (genitore.getFigli().isEmpty()) {
            figliSection.getChildren().add(new Label("Nessun figlio oppure nessuna iscrizione."));
        } else {
            for (Figlio f : genitore.getFigli()) {
                List<Evento> listaEventi = f.getIscrizioni();
                VBox boxFiglio = new VBox(10);
                boxFiglio.setPadding(new Insets(5, 0, 5, 10));
                Label titoloFiglio = new Label("Eventi di " + f.getNome() + ":");
                titoloFiglio.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                boxFiglio.getChildren().add(titoloFiglio);
                if (listaEventi == null || listaEventi.isEmpty()) {
                    boxFiglio.getChildren().add(new Label("Nessun evento iscritto."));
                } else {
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
        } else {
            TableView<Evento> tableProssimi = createEventoTableView(this.eventiProssimi, 5, formatoData, formatoOra);
            eventiProssimiBox.getChildren().add(tableProssimi);
        }

        // pulsante "Carica Eventi Successivi" - come in HomeLettore
        if (this.eventiProssimi != null && this.eventiProssimi.size() >= 10) {
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
        VBox centro = new VBox(20, figliSection, eventiProssimiBox);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(20));

        // ---------- LAYOUT FINALE ----------
        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(centro);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 800, 750);
        Platform.runLater(() -> {
            this.scene = new Scene(root);
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
            if (nextEventiButton != null) nextEventiButton.setVisible(false);
        });
    }

    /**
     * Crea una TableView per visualizzare gli eventi.
     * Crea TableView<Evento> con colonne: Data, Ora inizio, Ora fine (calcolaOraFine), Titolo.
     * visibleRows indica il numero di righe visibili. TableView gestisce lo scrolling interno se ci sono più righe.
     * @param eventi       l'elenco degli eventi da visualizzare
     * @param visibleRows il numero di righe visibili nella tabella
     * @param formatoData il formato per la data
     * @param formatoOra  il formato per l'ora
     * @return la TableView degli eventi
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

        table.getColumns().addAll(dataCol, oraInizioCol, oraFineCol, luogoCol ,titoloCol);

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

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // doppio clic su riga per aprire l'evento
        table.setRowFactory(tv -> {
            TableRow<Evento> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!row.isEmpty() && ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                    Evento selected = row.getItem();
                    eventoView.show(stage, selected, genitore, () -> this.show(stage, genitore, this.eventiProssimi, onBack));
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
     * Aggiorna l'elenco degli eventi prossimi e aggiorna la visualizzazione.
     *
     * @param prossimiEventi l'elenco degli eventi prossimi da aggiungere
     */
    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        if (this.eventiProssimi == null) this.eventiProssimi = new ArrayList<>();
        // aggiungi gli eventi nella nuova lista prossimi eventi se l'id dell'evento
        // non c'è nella vecchia lista
        for (Evento ev : prossimiEventi) {
            boolean found = false;
            for (Evento oldEv : this.eventiProssimi) {
                if (ev.getId() == oldEv.getId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.eventiProssimi.add(ev);
            }
        }
        // this.eventiProssimi.addAll(prossimiEventi);
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
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }

}

