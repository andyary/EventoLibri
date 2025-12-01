package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Network.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeGenitore {

    private final Client client;

    public HomeGenitore(Client client) {
        this.client = client;
    }

    public void show(Stage stage, Genitore genitore, ArrayList<Evento> eventiProssimi) {

        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo e figli");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            // qui aprirai ProfileView
        });

        HBox topBar = new HBox(profiloButton);
        topBar.setPadding(new Insets(10));
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


        // ---------- CONTENUTO CENTRALE ----------
        VBox centro = new VBox(30, figliSection, eventiProssimiBox);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(20));


        // ---------- LAYOUT FINALE ----------
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centro);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Home");
        stage.show();
    }


    private HBox creaRigaEvento(Evento evento) {
        Label nome = new Label(evento.getNome());
        Button apri = new Button("Apri");

        apri.setOnAction(e -> {
            System.out.println("Apro dettagli evento: " + evento.getNome());
            // Apri DettagliEventoView
        });

        HBox riga = new HBox(20, nome, apri);
        riga.setAlignment(Pos.CENTER_LEFT);

        return riga;
    }


}
