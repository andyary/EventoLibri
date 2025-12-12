package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.Messaggio;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeGenitore {

    private final Client client;
    private Stage stage;
    private Genitore genitore;
    private ArrayList<Evento> eventiProssimi;
    private Button nextEventiButton;
    private EventoView eventoView;
    private ProfiloGenitore profiloGenitore;

    public HomeGenitore(Client client, EventoView eventoView, ProfiloGenitore profiloGenitore) {
        this.eventoView = eventoView;
        this.client = client;
        this.profiloGenitore = profiloGenitore;
    }

    public void show(Stage stage, Genitore genitore, ArrayList<Evento> eventiProssimi) {
        this.stage = stage;
        this.genitore = genitore;
        this.eventiProssimi = eventiProssimi;

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo e figli");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            profiloGenitore.show(stage, genitore, () -> {
                this.show(stage, genitore, eventiProssimi);
            });

        });

        HBox topBar = new HBox(new Label("  Benvenuto, " + genitore.getNome() + "!          "), profiloButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);


        // ---------- EVENTI FIGLI ----------
        Map<Figlio, ArrayList<Evento>> eventiPerFiglio = new HashMap<>();

        for (Figlio f : genitore.getFigli()) {
            ArrayList<Evento> eventi = f.getIscrizioni();
            eventiPerFiglio.put(f, eventi);
        }

        VBox figliSection = new VBox(25);
        figliSection.setPadding(new Insets(10));
        Label titoloFigli = new Label("Eventi a cui sono iscritti i tuoi figli:");
        titoloFigli.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        figliSection.getChildren().add(titoloFigli);
        if (eventiPerFiglio.isEmpty()) {
            figliSection.getChildren().add(new Label("Nessun figlio oppure nessuna iscrizione."));
        } else {
            eventiPerFiglio.forEach((figlio, listaEventi) -> {
                VBox boxFiglio = new VBox(10);
                boxFiglio.setPadding(new Insets(5, 0, 5, 10));
                // usa figlio.getNome() per il titolo
                Label titoloFiglio = new Label("Eventi di " + figlio.getNome() + ":");
                titoloFiglio.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                boxFiglio.getChildren().add(titoloFiglio);
                if (listaEventi.isEmpty()) {
                    boxFiglio.getChildren().add(new Label("Nessun evento iscritto."));
                } else {
                    for (Evento evento : listaEventi) {
                        boxFiglio.getChildren().add(creaRigaEvento(evento));
                    }
                }
                figliSection.getChildren().add(boxFiglio);
            });
        }



        // ---------- EVENTI PROSSIMI ----------
        VBox eventiProssimiBox = new VBox(10);
        eventiProssimiBox.getChildren().add(new Label("Prossimi eventi disponibili:"));
        eventiProssimiBox.setPadding(new Insets(10));

        if (eventiProssimi.isEmpty()) {
            eventiProssimiBox.getChildren().add(new Label("Nessun evento disponibile."));
        } else {
            for (Evento evento : eventiProssimi) {
                HBox riga = creaRigaEvento(evento);
                eventiProssimiBox.getChildren().add(riga);
            }
        }

        nextEventiButton = new Button("Carica Eventi Successivi");
        nextEventiButton.setOnAction(e -> {
            RichiestaNextEventi req = new RichiestaNextEventi(eventiProssimi.getLast());
            try {
                client.sendMessage(req);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        });
        eventiProssimiBox.getChildren().add(nextEventiButton);


        // ---------- CONTENUTO CENTRALE ----------
        VBox centro = new VBox(30, figliSection, eventiProssimiBox);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(20));


        // ---------- LAYOUT FINALE ----------

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centro);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // adatta la larghezza del contenuto alla finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // scroll verticale solo se serve
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 500, 800);
        Platform.runLater(() -> {;
            stage.setScene(scene);
            // stage.setMaximized(true);
            stage.setTitle("Home");
            stage.show();
        });

    }


    public void nascondiBottoneNextEventi() {
        nextEventiButton.setVisible(false);
    }


    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        eventiProssimi.addAll(prossimiEventi);
        this.show(stage, genitore, eventiProssimi);
    }


    private HBox creaRigaEvento(Evento evento) {
        Label nome = new Label(evento.getNome());
        Button apri = new Button("Apri");

        apri.setOnAction(e -> {
            System.out.println("Apro dettagli evento: " + evento.getNome());

            eventoView.show(stage, evento, genitore, () -> {
                this.show(stage, genitore, eventiProssimi);
            });
        });

        HBox riga = new HBox(20, nome, apri);
        riga.setAlignment(Pos.CENTER_LEFT);

        return riga;
    }



}
