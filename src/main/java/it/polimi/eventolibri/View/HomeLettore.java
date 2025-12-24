package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Lettore;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeLettore {

    private final Client client;
    private Stage stage;
    private Lettore lettore;
    private ArrayList<Evento> eventiProssimi;
    private Button nextEventiButton;
    private EventoViewLettore eventoView;
    private ProfiloLettore profiloLettore;
    private Runnable onBack;

    public HomeLettore(Client client, EventoViewLettore eventoView, ProfiloLettore profiloLettore) {
        this.client = client;
        this.eventoView = eventoView;
        this.profiloLettore = profiloLettore;
    }

    public void show(Stage stage, Lettore lettore, ArrayList<Evento> eventiProssimi, Runnable onBack) {
        this.stage = stage;
        this.lettore = lettore;
        this.eventiProssimi = eventiProssimi != null ? eventiProssimi : new ArrayList<>();
        this.onBack = onBack;

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

        HBox topRow = new HBox(10, new Label("  Benvenuto, " + lettore.getNome() + "!          "), profiloButton, newEventoButton, backButton);
        topRow.setAlignment(Pos.TOP_RIGHT);
        VBox topBar = new VBox(topRow);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        // formatter per colonne
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

        Scene scene = new Scene(scrollPane, 700, 750);
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle("Home Lettore");
            stage.show();
        });
    }

    public void nascondiBottoneNextEventi() {
        if (nextEventiButton != null) nextEventiButton.setVisible(false);
    }

    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        if (this.eventiProssimi == null) this.eventiProssimi = new ArrayList<>();
        this.eventiProssimi.addAll(prossimiEventi);
        this.show(stage, lettore, this.eventiProssimi, onBack);
    }

    public void reloadEventiFromServer() {
        if (eventiProssimi == null) eventiProssimi = new ArrayList<>();
        eventiProssimi.clear();

        try {
            client.sendMessage(new RichiestaNextEventi(null));
        } catch (Exception ex) {
            System.out.println("Errore richiesta eventi: " + ex.getMessage());
        }

        this.show(stage, lettore, eventiProssimi, onBack);
    }

    /**
     * Crea TableView<Evento> con colonne: Data, Ora inizio, Ora fine (calcolaOraFine), Titolo.
     * visibleRows indica il numero di righe visibili. TableView gestisce lo scrolling interno se ci sono più righe.
     */
    // java
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

        TableColumn<Evento, String> titoloCol = new TableColumn<>("Titolo evento");
        titoloCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getNome() : ""));
        titoloCol.setSortable(true);
        // titoloCol rimane flessibile: non impostare maxWidth in modo che occupi lo spazio residuo

        table.getColumns().addAll(dataCol, oraInizioCol, oraFineCol, titoloCol);

        ObservableList<Evento> items = FXCollections.observableArrayList(eventi);
        table.setItems(items);

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

}
