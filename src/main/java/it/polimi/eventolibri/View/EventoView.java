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

/**
 * Classe View per la visualizzazione dei dettagli di un evento dal punto di vista dell'utente genitore
 * e la gestione delle iscrizioni dei figli.
 */
public class EventoView {
    private Client client;
    private Stage stage;
    private Scene scene = null;
    private Evento evento;
    private Genitore genitore;
    private Runnable onBack;
    private Label messaggioerrore;
    private Label iscritti;
    private Label titolo;
    private Label luogo;
    private Label capienza;
    private Label data;
    private VBox scalettaBox;

    /**
     * Costruttore della classe EventoView.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public EventoView(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.iscritti = new Label("Iscritti: TBD");
        this.titolo = new Label("Titolo: TBD");
        this.luogo = new Label("Luogo: TBD");
        this.capienza = new Label("Capienza: TBD");
        this.data = new Label("Data: TBD");
        this.scalettaBox = new VBox(10);
    }

    /**
     * Restituisce il genitore associato alla view.
     *
     * @return il genitore
     */
    public Genitore getGenitore() {
        return genitore;
    }

    /**
     * Restituisce la scena associata alla view.
     *
     * @return la scena
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Imposta il genitore associato alla view.
     *
     * @param genitore il genitore da impostare
     */
    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    /**
     * Restituisce l'evento associato alla view.
     *
     * @return l'evento
     */
    public Evento getEvento() {
        return evento;
    }

    /**
     * Imposta l'evento associato alla view.
     *
     * @param evento l'evento da impostare
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    /**
     * Mostra la schermata dei dettagli dell'evento, con iscrizione/disiscrizione figli
     *
     * @param stage    lo stage principale dell'applicazione
     * @param evento   l'evento di cui visualizzare i dettagli
     * @param genitore il genitore che gestisce le iscrizioni dei figli
     * @param onBack   l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Evento evento, Genitore genitore, Runnable onBack) {
        this.stage = stage;
        this.evento = evento;
        this.genitore = genitore;
        this.onBack = onBack;

        // Richiesta numero iscritti aggiornato
        RichiestaIscrittiEvento richiestaIscritti = new RichiestaIscrittiEvento(evento);
        try {
            client.sendMessage(richiestaIscritti);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaIscrittiEvento" + e.getMessage());
        }

        // ---------- DETTAGLI EVENTO ----------
        Label titolopagina = new Label("Partecipazione Evento");
        titolopagina.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        titolo.setText("Titolo: " + evento.getNome());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm");
        data.setText("Data: " + evento.getData().format(fmt));
        luogo.setText("Luogo: " + evento.getLuogo().getNome());
        capienza.setText("Capienza: " + evento.getLuogo().getCapienza());
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
        });

        Button backButton = new Button("Indietro");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        // ---------- SCALETTA EVENTO ----------
        scalettaBox.getChildren().clear();
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

        // Layout principale
        VBox layout = new VBox(15,
                mainBtnBox,
                titolopagina,
                titolo,
                data,
                luogo,
                capienza,
                iscritti,
                figliBox,
                aggiornaButton,
                messaggioerrore,
                scalettaBox
        );
        layout.setAlignment(Pos.TOP_LEFT);
        layout.setPadding(new Insets(20));

        this.scene = new Scene(layout, 800, 750);
        // Mostra la scena
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle("Dettagli Evento");
            stage.show();
        });
    }

    /**
     * Mostra un messaggio di errore nella view.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            System.out.println("dalla mostraerrore contenuto di this.messaggio errore:" + this.messaggioerrore);
            System.out.println("dalla mostraerrore contenuto di messaggio errore:" + msgerrore);
            this.messaggioerrore.setText(msgerrore);
        });
    }

    /**
     * Aggiorna il numero di iscritti visualizzato nella view.
     *
     * @param numIscritti il nuovo numero di iscritti
     */
    public void aggiornaIscritti(int numIscritti) {
        evento.setIscritti(numIscritti);
        Platform.runLater(() -> {
            iscritti.setText("Iscritti: " + evento.getIscritti());
        });
    }

    public void aggiornaEvento(Evento eventoAggiornato) {
        this.evento = eventoAggiornato;
        Platform.runLater(() -> {
            titolo.setText(evento.getNome());
            titolo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm");
            data.setText("Data: " + evento.getData().format(fmt));
            luogo.setText("Luogo: " + evento.getLuogo().getNome());
            capienza.setText("Capienza: " + evento.getLuogo().getCapienza());
            iscritti.setText("Iscritti: " + evento.getIscritti());
            iscritti.setText("Iscritti: " + evento.getIscritti());

            scalettaBox.getChildren().clear();
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


        });
    }



}
