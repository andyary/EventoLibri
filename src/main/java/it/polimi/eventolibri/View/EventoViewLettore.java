package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaDisiscrizioneEvento;
import it.polimi.eventolibri.Message.RichiestaIscrittiEvento;
import it.polimi.eventolibri.Message.RichiestaIscrizioneEvento;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EventoViewLettore {
    private Client client;
    private Stage stage;
    private Evento evento;
    private Lettore lettore;
    private Runnable onBack;
    private Label messaggioerrore;
    private Label iscritti;

    public EventoViewLettore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.iscritti = new Label("Iscritti: TBD");
    }

    public Lettore getLettore() {
        return lettore;
    }

    public void setLettore(Lettore lettore) {
        this.lettore = lettore;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    /**
     * Mostra la finestra dei dettagli evento con iscrizione/disiscrizione figli
     */
    public void show(Stage stage, Evento evento, Lettore lettore, Runnable onBack) {
        this.stage = stage;
        this.evento = evento;
        this.lettore = lettore;
        this.onBack = onBack;

        RichiestaIscrittiEvento richiestaIscritti = new RichiestaIscrittiEvento(evento);
        try {
            client.sendMessage(richiestaIscritti);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaIscrittiEvento" + e.getMessage());
        }

        Label titolo = new Label(evento.getNome());
        titolo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm");
        Label data = new Label("Data: " + evento.getData().format(fmt));
        Label luogo = new Label("Luogo: " + evento.getLuogo().getNome());
        Label capienza = new Label("Capienza: " + evento.getLuogo().getCapienza());
        iscritti.setText("Iscritti: " + evento.getIscritti());



        Button backButton = new Button("Indietro");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        VBox scalettaBox = new VBox(10);
        scalettaBox.setPadding(new Insets(10));
        Label titoloScaletta = new Label("Scaletta dell'evento:");
        titoloScaletta.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        scalettaBox.getChildren().add(titoloScaletta);
        for (LibroLettore ll : evento.getScaletta()) {
            String nomeLettore = "---------";
            if (ll.getLettore().getNome()!=null) {nomeLettore = ll.getLettore().getNome();}
            String titoloLibro = ll.getLibro().getTitolo();
            HBox riga = new HBox(10);
            riga.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(ll.getProgressivo() + ") <" + titoloLibro + "> letto da <" + nomeLettore + "> durata " + ll.getLibro().getTempoLettura() +" minuti");
            lbl.setStyle("-fx-font-size: 14px;");
            riga.getChildren().add(lbl);
            scalettaBox.getChildren().add(riga);
        }


        VBox layout = new VBox(15, titolo, data, luogo, capienza, iscritti, messaggioerrore, backButton, scalettaBox);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(20));

        System.out.println("dalla show contenuto di messaggio errore:" + messaggioerrore);

        Scene scene = new Scene(layout, 500, 800);

        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle("Dettagli Evento");
            stage.show();
        });
    }

    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            System.out.println("dalla mostraerrore contenuto di this.messaggio errore:" + this.messaggioerrore);
            System.out.println("dalla mostraerrore contenuto di messaggio errore:" + msgerrore);
            this.messaggioerrore.setText(msgerrore);
        });
    }

    public void aggiornaIscritti(int numIscritti) {
        evento.setIscritti(numIscritti);
        Platform.runLater(() -> {
            iscritti.setText("Iscritti: " + evento.getIscritti());
        });
    }



}
