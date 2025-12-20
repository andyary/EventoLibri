package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Lettore;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        this.eventiProssimi = eventiProssimi;
        this.onBack = onBack;

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo lettore");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            profiloLettore.show(stage, lettore, () -> {
                this.show(stage, lettore, eventiProssimi, onBack);
            });

        });

        Button newEventoButton = new Button("Crea Nuovo Evento");
        newEventoButton.setOnAction(e -> {
            System.out.println("Apertura schermata crea nuovo evento...");
            eventoView.show(stage, new Evento("", null, LocalDateTime.now()), lettore, () -> {
                this.show(stage, lettore, eventiProssimi, onBack);
            });
        });

        Button backButton = new Button("Logout");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox topBar = new HBox(new Label("  Benvenuto, " + lettore.getNome() + "!          "), profiloButton, newEventoButton, backButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);


        // ---------- EVENTI CREATI ----------
        VBox eventiCreatiBox = new VBox(10);
        eventiCreatiBox.getChildren().add(new Label("Eventi creati:"));
        eventiCreatiBox.setPadding(new Insets(10));

        if (lettore.getEventiCreati().isEmpty()) {
            eventiCreatiBox.getChildren().add(new Label("Nessun evento creato."));
        } else {
            for (Evento evento : lettore.getEventiCreati()) {
                HBox riga = creaRigaEvento(evento);
                eventiCreatiBox.getChildren().add(riga);
            }
        }


        // ---------- EVENTI A CUI IL LETTORE è ISCRITTO ----------
        VBox eventiIscrittoBox = new VBox(10);
        eventiIscrittoBox.getChildren().add(new Label("Eventi a cui leggerai:"));
        eventiIscrittoBox.setPadding(new Insets(10));

        if (lettore.getIscrizioniLettura().isEmpty()) {
            eventiIscrittoBox.getChildren().add(new Label("Nessun evento."));
        } else {
            for (Evento evento : lettore.getIscrizioniLettura()) {
                HBox riga = creaRigaEvento(evento);
                eventiIscrittoBox.getChildren().add(riga);
            }
        }

        // ---------- EVENTI PROSSIMI ----------
        VBox eventiProssimiBox = new VBox(10);
        eventiProssimiBox.getChildren().add(new Label("Prossimi eventi disponibili:"));
        eventiProssimiBox.setPadding(new Insets(10));

        if (eventiProssimi.isEmpty()) {
            eventiProssimiBox.getChildren().add(new Label("Nessun evento disponibile."));
        } else {
            for (Evento evento : eventiProssimi) {
                if (lettore.getEventiCreati().stream().anyMatch(e -> e.getId() == evento.getId()) ||
                        lettore.getIscrizioniLettura().stream().anyMatch(e -> e.getId() == evento.getId())) {
                    continue; // salta gli eventi già creati o a cui è iscritto
                }
                HBox riga = creaRigaEvento(evento);
                eventiProssimiBox.getChildren().add(riga);
            }
        }

        if (eventiProssimi.size() == 10) {
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
        }


        // ---------- CONTENUTO CENTRALE ----------
        VBox centro = new VBox(30, eventiCreatiBox, eventiIscrittoBox, eventiProssimiBox);
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

        Scene scene = new Scene(scrollPane, 700, 700);
        Platform.runLater(() -> {;
            stage.setScene(scene);
            // stage.setMaximized(true);
            stage.setTitle("Home Lettore");
            stage.show();
        });

    }


    public void nascondiBottoneNextEventi() {
        nextEventiButton.setVisible(false);
    }


    public void aggiornaEventi(ArrayList<Evento> prossimiEventi) {
        eventiProssimi.addAll(prossimiEventi);
        this.show(stage, lettore, eventiProssimi, onBack);
    }


    private HBox creaRigaEvento(Evento evento) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm");
        Label nome = new Label(evento.getNome() + " del " + evento.getData().format(fmt));
        Button apri = new Button("Apri");

        apri.setOnAction(e -> {
            System.out.println("Apro dettagli evento: " + evento.getNome());

            eventoView.show(stage, evento, lettore, () -> {
                this.show(stage, lettore, eventiProssimi, onBack);
            });
        });

        HBox riga = new HBox(20, nome, apri);
        riga.setAlignment(Pos.CENTER_LEFT);

        return riga;
    }

}

