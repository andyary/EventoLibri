package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeGenitore {

    private final Client client;
    private Stage stage;
    private Genitore genitore;
    private ArrayList<Evento> eventiProssimi;
    private Button nextEventiButton;
    private EventoView eventoView;
    private ProfiloGenitore profiloGenitore;
    private Runnable onBack;

    public HomeGenitore(Client client, EventoView eventoView, ProfiloGenitore profiloGenitore) {
        this.eventoView = eventoView;
        this.client = client;
        this.profiloGenitore = profiloGenitore;
    }

    public void show(Stage stage, Genitore genitore, ArrayList<Evento> eventiProssimi, Runnable onBack) {
        this.stage = stage;
        this.genitore = genitore;
        this.eventiProssimi = eventiProssimi != null ? eventiProssimi : new ArrayList<>();
        this.onBack = onBack;

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

        HBox topBar = new HBox(new Label("  Benvenuto, (genitore) " + genitore.getNome() + "!          "), profiloButton, backButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        // formatter per colonne
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
        root.setTop(topBar);
        root.setCenter(centro);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 800, 750);
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle("Home Genitore");
            stage.show();
        });
    }

    public void nascondiBottoneNextEventi() {
        if (nextEventiButton != null) nextEventiButton.setVisible(false);
    }

    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        if (this.eventiProssimi == null) this.eventiProssimi = new ArrayList<>();
        this.eventiProssimi.addAll(prossimiEventi);
        this.show(stage, genitore, this.eventiProssimi, onBack);
    }

    /**
     * Crea TableView<Evento> con colonne: Data, Ora inizio, Ora fine (calcolaOraFine), Titolo.
     * visibleRows indica il numero di righe visibili. TableView gestisce lo scrolling interno se ci sono più righe.
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

        TableColumn<Evento, String> titoloCol = new TableColumn<>("Titolo evento");
        titoloCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue() != null ? cell.getValue().getNome() : ""));
        titoloCol.setSortable(true);

        table.getColumns().addAll(dataCol, oraInizioCol, oraFineCol, titoloCol);

        ObservableList<Evento> items = FXCollections.observableArrayList(eventi);
        table.setItems(items);

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

}

