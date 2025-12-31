package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaDisiscrizioneEvento;
import it.polimi.eventolibri.Message.RichiestaIscrittiEvento;
import it.polimi.eventolibri.Message.RichiestaIscrizioneEvento;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Model.LibroLettore;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EventoView {
    private Client client;
    private Stage stage;
    private Evento evento;
    private Genitore genitore;
    private Runnable onBack;
    private Label messaggioerrore;
    private Label iscritti;

    public EventoView(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.iscritti = new Label("Iscritti: TBD");
    }

    public Genitore getGenitore() {
        return genitore;
    }

    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
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
    public void show(Stage stage, Evento evento, Genitore genitore, Runnable onBack) {
        this.stage = stage;
        this.evento = evento;
        this.genitore = genitore;
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


        // ---------- CHECKBOX PER TUTTI I FIGLI ----------
        VBox figliBox = new VBox(5);
        figliBox.setPadding(new Insets(10));
        figliBox.getChildren().add(new Label("Gestisci iscrizioni figli:"));

        ArrayList<CheckBox> checkFigli = new ArrayList<>();
        for (Figlio f : genitore.getFigli()) {
            CheckBox cb = new CheckBox(f.getNome());
            // Controllo basato sull'ID dell'evento
            boolean iscritto = f.getIscrizioni().stream()
                    .anyMatch(e -> e.getId() == (evento.getId()));
            cb.setSelected(iscritto);

            checkFigli.add(cb);
            figliBox.getChildren().add(cb);
        }



        Button aggiornaButton = new Button("Aggiorna iscrizioni");
        aggiornaButton.setOnAction(e -> {
            this.messaggioerrore.setText("");
            for (int i = 0; i < checkFigli.size(); i++) {
                Figlio f = genitore.getFigli().get(i);
                boolean selezionato = checkFigli.get(i).isSelected();
                // boolean eraIscritto = f.getIscrizioni().contains(evento);
                boolean eraIscritto = f.getIscrizioni().stream().anyMatch(ev -> ev.getId() == evento.getId());
                if (selezionato && !eraIscritto) {
                    // iscrizione
                    RichiestaIscrizioneEvento req = new RichiestaIscrizioneEvento(f, evento, genitore);
                    try {
                        client.sendMessage(req);
                    } catch (Exception ex) {
                        System.out.println("Errore iscrizione: " + ex.getMessage());
                    }
                } else if (!selezionato && eraIscritto) {
                    // disiscrizione
                    RichiestaDisiscrizioneEvento req = new RichiestaDisiscrizioneEvento(f, evento, genitore);
                    try {
                        client.sendMessage(req);
                    } catch (Exception ex) {
                        System.out.println("Errore disiscrizione: " + ex.getMessage());
                    }
                }
            }

            // if (onBack != null) onBack.run();
        });

        Button backButton = new Button("Indietro");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        VBox scalettaBox = new VBox(10);
        scalettaBox.setPadding(new Insets(10));
        Label titoloScaletta = new Label("Scaletta dell'evento:");
        titoloScaletta.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        scalettaBox.getChildren().add(titoloScaletta);

        LocalTime tempoinizio= evento.getData().toLocalTime();
        LocalTime tempofine;

        for (LibroLettore ll : evento.getScaletta()) {
            tempofine = tempoinizio.plusMinutes(ll.getLibro().getTempoLettura());
            String durata = new String("(" + ll.getLibro().getTempoLettura() + " minuti)");
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
            String orario = new String("[" + tempoinizio.format(formato) + " - " + tempofine.format(formato) + "]");
            String nomeLettore = new String(ll.getLettore() != null ? ll.getLettore().getNome() : "---------");
            String titoloLibro = ll.getLibro().getTitolo();
            HBox riga = new HBox(10);
            riga.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(ll.getProgressivo() + ")   " + orario + "   " + titoloLibro + "   " + durata + "   " + nomeLettore);
            lbl.setStyle("-fx-font-size: 14px;");
            riga.getChildren().add(lbl);
            scalettaBox.getChildren().add(riga);
        }

        HBox mainBtnBox = new HBox(5, new Label("  Benvenuto, (genitore) " + genitore.getNome() + "!          "), backButton);
        mainBtnBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15, mainBtnBox, titolo, data, luogo, capienza, iscritti, figliBox, aggiornaButton, messaggioerrore, scalettaBox);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 800, 750);

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
