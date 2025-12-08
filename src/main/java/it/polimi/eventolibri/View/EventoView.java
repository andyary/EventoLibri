package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaDisiscrizioneEvento;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EventoView {
    private Client client;
    private Stage stage;
    private Evento evento;
    private Genitore genitore;
    private Runnable onBack;
    private Label messaggioerrore;

    public EventoView(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
    }

    /**
     * Mostra la finestra dei dettagli evento con iscrizione/disiscrizione figli
     */
    public void show(Stage stage, Evento evento, Genitore genitore, Runnable onBack) {
        this.stage = stage;
        this.evento = evento;
        this.genitore = genitore;
        this.onBack = onBack;

        Label titolo = new Label(evento.getNome());
        titolo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm");
        Label data = new Label("Data: " + evento.getData().format(fmt));
        Label luogo = new Label("Luogo: " + evento.getLuogo().getNome());

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
                boolean eraIscritto = f.getIscrizioni().contains(evento);

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
                    RichiestaDisiscrizioneEvento req = new RichiestaDisiscrizioneEvento(f, evento);
                    try {
                        client.sendMessage(req);
                        f.disiscrivi(evento, genitore);
                        System.out.println("Disiscritto " + f.getNome());
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


        VBox layout = new VBox(15, titolo, data, luogo, figliBox, aggiornaButton, messaggioerrore, backButton, scalettaBox);
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

}
