package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaIscrittiEvento;
import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Message.RichiestaSalvaEvento;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Classe View per la visualizzazione e gestione di un evento dal punto di vista di un lettore.
 */
public class EventoViewLettore {
    // Attributi
    private Client client;
    private Stage stage;
    private Scene scene = null;
    private Evento evento;
    private Lettore lettore;
    private ArrayList<Luogo> luoghi = new ArrayList<>();
    private ArrayList<Lettore> lettori = new ArrayList<>();
    private ArrayList<Libro> elencoLibri = new ArrayList<>();
    private Runnable onBack;
    private Label messaggioerrore;
    private Label iscritti;
    private ComboBox<Luogo> luogoCombo = new ComboBox<>(); // combo box per i luoghi
    private ComboBox<Lettore> lettoreCombo = new ComboBox<>(); // combo box per i lettori
    private ComboBox<Lettore> lettoreScalettaCombo = new ComboBox<>(); // combo box per i lettori nella scaletta

    /**
     * Costruttore della classe EventoViewLettore.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public EventoViewLettore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.iscritti = new Label("TBD");
    }

    /**
     * Imposta la lista dei luoghi disponibili.
     *
     * @param luoghi la lista dei luoghi
     */
    public void setLuoghi(ArrayList<Luogo> luoghi) {
        this.luoghi = luoghi;
    }

    /**
     * Imposta la lista dei lettori disponibili.
     *
     * @param lettori la lista dei lettori
     */
    public void setLettori(ArrayList<Lettore> lettori) {
        this.lettori = lettori;
    }

    /**
     * Imposta l'elenco dei libri disponibili.
     *
     * @param elencoLibri l'elenco dei libri
     */
    public void setElencoLibri(ArrayList<Libro> elencoLibri) {
        this.elencoLibri = elencoLibri;
    }

    /**
     * Restituisce il lettore associato alla view.
     *
     * @return il lettore
     */
    public Lettore getLettore() {
        return lettore;
    }

    /**
     * Imposta il lettore associato alla view.
     *
     * @param lettore il lettore
     */
    public void setLettore(Lettore lettore) {
        this.lettore = lettore;
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
     * @param evento l'evento
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    /**
     * Restituisce la scena corrente della view.
     *
     * @return la scena
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Mostra la schermata di gestione dell'evento, con dettaglio scaletta ed iscizione come lettore
     *
     * @param stage   lo stage principale dell'applicazione
     * @param evento  l'evento da gestire
     * @param lettore il lettore che sta gestendo l'evento
     * @param onBack  l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Evento evento, Lettore lettore, Runnable onBack) {
        // Imposta lo stage, l'evento e il lettore
        this.stage = stage;
        this.evento = evento;
        this.lettore = lettore;
        this.onBack = onBack; // azione da eseguire al ritorno
        this.messaggioerrore.setText(""); // reset messaggio errore

        // Determina se l'utente corrente è il creatore dell'evento
        boolean isCreator = false;
        if (evento != null && evento.getCreatore() != null && lettore != null) {
            isCreator = evento.getCreatore().getId() == lettore.getId();
        }

        // Richiesta iscritti evento
        RichiestaIscrittiEvento richiestaIscritti = new RichiestaIscrittiEvento(evento);
        try {
            client.sendMessage(richiestaIscritti);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaIscrittiEvento" + e.getMessage());
        }

        // richiedi lettori, luoghi e libri
        RichiestaLettoriELuoghiELibri richiestaLettoriELuoghiELibri = new RichiestaLettoriELuoghiELibri();
        try {
            client.sendMessage(richiestaLettoriELuoghiELibri);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
        }

        /* ---------- TITOLO ---------- */
        Label titoloLabel = new Label("Gestione evento");
        titoloLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        TextField titoloField = new TextField(evento.getNome());
        titoloField.setPromptText("Titolo evento");
        titoloField.setAlignment(Pos.CENTER_LEFT);
        // Se non è creatore, non può modificare il titolo
        titoloField.setEditable(isCreator); // rende il campo non editabile
        titoloField.setDisable(!isCreator); // disabilita il campo

        HBox titoloBox = new HBox(5,
                new Label("Titolo:"),
                titoloField
        );

        /* ---------- LUOGO ---------- */
        if (evento.getLuogo() == null) {
            luogoCombo.setPromptText("Scegli luogo");
        } else {
            luogoCombo.setValue(evento.getLuogo());
            // Imposta la visualizzazione dei nomi dei luoghi nella ComboBox
            luogoCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Luogo item, boolean empty) {
                    super.updateItem(item, empty); // chiamata al metodo della superclasse
                    setText(empty || item == null ? "" : item.getNome()); // imposta il testo
                }
            });
            // Imposta la visualizzazione del nome del luogo selezionato
            luogoCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Luogo luogo) {
                    return luogo == null ? "" : luogo.getNome();
                } // converte il luogo in stringa per la visualizzazione

                @Override
                public Luogo fromString(String s) {
                    return null;
                } // non usato in questo contesto
            });
        }
        // Se non è creatore, non può cambiare il luogo
        luogoCombo.setDisable(!isCreator);

        HBox luogoBox = new HBox(5,
                new Label("Luogo:"),
                luogoCombo
        );

        HBox iscrittiBox = new HBox(5,
                new Label("Iscritti:"),
                iscritti
        );

        HBox titoloLuogoBox = new HBox(20,
                titoloBox,
                luogoBox,
                iscrittiBox
        );
        titoloLuogoBox.setAlignment(Pos.CENTER_LEFT);

        /* ---------- DATA / ORA ---------- */
        DatePicker datePicker = new DatePicker(evento.getData().toLocalDate());
        datePicker.setPromptText("Data evento");
        // Se non è creatore, non può cambiare data/ora
        datePicker.setDisable(!isCreator);

        HBox dataBox = new HBox(5,
                new Label("Data:"),
                datePicker
        );
        dataBox.setAlignment(Pos.CENTER_LEFT);
        // ---------- ORA ---------- //
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, evento.getData().toLocalTime().getHour()); // spinner per le ore
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, evento.getData().toLocalTime().getMinute()); // spinner per i minuti
        hourSpinner.setEditable(true); // rende lo spinner editabile
        minuteSpinner.setEditable(true);
        hourSpinner.setDisable(!isCreator); // disabilita lo spinner se non è creatore
        minuteSpinner.setDisable(!isCreator);

        HBox oraBox = new HBox(5,
                new Label("Ora:"),
                hourSpinner,
                new Label(":"),
                minuteSpinner
        );
        oraBox.setAlignment(Pos.CENTER_LEFT);

        HBox dataOraBox = new HBox(20,
                dataBox,
                oraBox
        );
        dataOraBox.setAlignment(Pos.CENTER_LEFT);

        /* ---------- SCALETTA ---------- */
        Label scalettaLabel = new Label("Scaletta");
        scalettaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox scalettaBox = new VBox(6);
        scalettaBox.setPadding(new Insets(10));
        scalettaBox.setStyle("-fx-border-color: lightgray;");
        ArrayList<LibroLettore> scalettaTemp = new ArrayList<>(); // copia temporanea della scaletta
        for (LibroLettore l : evento.getScaletta()) {
            scalettaTemp.add(new LibroLettore(l.getLibro(), l.getLettore(), l.getProgressivo())); // copia profonda
        }
        Label libroSelezionatoLabel = new Label("Nessun libro selezionato");
        // Combo box per selezionare il lettore della nuova riga
        Button scegliLibroBtn = new Button("Scegli libro");
        final Libro[] libroSelezionato = new Libro[1]; // array per tenere traccia del libro selezionato

        scegliLibroBtn.setOnAction(e -> {
            messaggioerrore.setText("");
            LibroView dialog = new LibroView();
            Libro libro = dialog.show(stage, elencoLibri); // mostra la finestra di selezione libro
            if (libro != null) {
                // aggiorna il libro selezionato
                libroSelezionatoLabel.setText(
                        libro.getTitolo() + " (" + libro.getTempoLettura() + " min)"
                );
                libroSelezionato[0] = libro; // aggiorna il libro selezionato
            }
        });
        // solo il creatore può scegliere libri per la scaletta
        scegliLibroBtn.setDisable(!isCreator);

        lettoreCombo.setPromptText("Lettore (facoltativo)");
        // solo il creatore può assegnare un lettore alla nuova riga
        lettoreCombo.setDisable(!isCreator);

        // Combo box per selezionare il lettore della nuova riga
        Button aggiungiRigaBtn = new Button("Aggiungi riga");
        aggiungiRigaBtn.setOnAction(e -> {
            messaggioerrore.setText("");
            if (libroSelezionato[0] == null) {
                messaggioerrore.setText("Seleziona un libro"); // errore se nessun libro selezionato
                return;
            }
            LibroLettore ll = new LibroLettore(
                    libroSelezionato[0],
                    lettoreCombo.getValue(),
                    scalettaTemp.size() + 1
            ); // crea nuova riga
            scalettaTemp.add(ll); // aggiunge la riga alla scaletta temporanea
            refreshScalettaUI(scalettaBox, scalettaTemp); // aggiorna l'interfaccia della scaletta
            libroSelezionato[0] = null; // reset libro selezionato
            libroSelezionatoLabel.setText("Nessun libro selezionato");
            lettoreCombo.setValue(null); // reset lettore selezionato
        });
        // solo il creatore può aggiungere righe
        aggiungiRigaBtn.setDisable(!isCreator);

        HBox libroLettoreBox = new HBox(5,
                new Label("Libro:"),
                scegliLibroBtn,
                new Label("Lettore:"),
                lettoreCombo,
                aggiungiRigaBtn
        );
        libroLettoreBox.setAlignment(Pos.CENTER_LEFT);

        /* ---------- BOTTONI ---------- */
        Button salvaBtn = new Button("Salva evento");
        Button annullaBtn = new Button("Annulla");
        salvaBtn.setOnAction(e -> {
            messaggioerrore.setText("");
            // controlla campi obbligatori
            if (titoloField.getText().isBlank()
                    || luogoCombo.getValue() == null
                    || datePicker.getValue() == null
            ) {
                messaggioerrore.setText("Compila tutti i campi obbligatori");
                return;
            }
            // crea evento temporaneo con i nuovi dati
            LocalDateTime dataOra = LocalDateTime.of(
                    datePicker.getValue(),
                    LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
            );
            Evento eventoTemp = new Evento(
                    titoloField.getText(),
                    luogoCombo.getValue(),
                    dataOra
            );
            // rimuovo tutti i listener precedenti per evitare duplicati
            // i lettori cancellati (da questa modifica) non saranno più aggiunti come listener
            eventoTemp.removeAllListeners();
            // imposto la scaletta (con aggiunta listener)
            eventoTemp.setScaletta(scalettaTemp);
            // imposto il creatore e lo aggiungo come listener
            eventoTemp.setCreatore(evento.getCreatore());
            eventoTemp.addListenersGenitori(evento.getListenersGenitori());
            // recupera id esistente dal evento originale (in caso di modifica)
            if (evento.getId() != 0) {
                eventoTemp.setId(evento.getId());
            }
            try {
                client.sendMessage(new RichiestaSalvaEvento(eventoTemp));
            } catch (IOException ex) {
                messaggioerrore.setText("Errore durante il salvataggio evento");
            }
        });
        // solo il creatore può salvare modifiche all'evento
        // salvaBtn.setDisable(!isCreator);

        // azione annulla: torna indietro senza salvare
        annullaBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox mainBtnBox = new HBox(5,
                new Label("  Benvenuto, (lettore) " + lettore.getNome() + "!          "),
                salvaBtn,
                annullaBtn
        );
        mainBtnBox.setAlignment(Pos.CENTER_RIGHT);

        /* ---------- LAYOUT ---------- */
        VBox layout = new VBox(15,
                mainBtnBox,
                messaggioerrore,
                titoloLabel,
                titoloLuogoBox,
                // titoloBox,
                // luogoBox,
                dataOraBox,
                // dataBox,
                // oraBox,
                scalettaLabel,
                libroSelezionatoLabel,
                libroLettoreBox,
                scalettaBox
        );
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);

        BorderPane root = new BorderPane();
        root.setTop(layout);

        ScrollPane scrollPane = new ScrollPane(root); // aggiunto ScrollPane
        scrollPane.setFitToWidth(true);  // adatta la larghezza del contenuto alla finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // scroll verticale solo se serve
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // mai scroll orizzontale

        refreshScalettaUI(scalettaBox, scalettaTemp); // inizializza l'interfaccia della scaletta

        this.scene = new Scene(scrollPane, 750, 780); // dimensioni della scena

        Platform.runLater(() -> {
            ;
            stage.setScene(scene);
            stage.setTitle("Crea Evento");
            stage.show();
        });

    }

    /**
     * Mostra un messaggio di errore nella schermata dell'evento.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore.setText(msgerrore);
        });
    }

    /**
     * Aggiorna le liste di lettori, luoghi e libri disponibili.
     *
     * @param lettori     la lista dei lettori
     * @param luoghi      la lista dei luoghi
     * @param elencolibri l'elenco dei libri
     */
    public void aggiornaLettoriELuoghiELibri(ArrayList<Lettore> lettori, ArrayList<Luogo> luoghi, ArrayList<Libro> elencolibri) {
        this.lettori.clear(); // pulisce la lista esistente
        this.lettori.addAll(lettori); // aggiunge i nuovi lettori
        this.luoghi.clear(); // pulisce la lista esistente
        this.luoghi.addAll(luoghi); // aggiunge i nuovi luoghi
        Platform.runLater(() -> {
            luogoCombo.getItems().clear();
            luogoCombo.getItems().addAll(luoghi);
            luogoCombo.setCellFactory(cb -> new ListCell<>() { // modifica le celle della ComboBox
                @Override
                protected void updateItem(Luogo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome()); // imposta il testo della cella
                }
            });
            luogoCombo.setButtonCell(luogoCombo.getCellFactory().call(null)); // imposta la cella del pulsante della ComboBox

            lettoreCombo.getItems().clear(); // pulisce la lista esistente
            lettoreCombo.getItems().addAll(lettori); // aggiunge i nuovi lettori
            lettoreCombo.setCellFactory(cb -> new ListCell<>() { // modifica le celle della ComboBox
                @Override
                protected void updateItem(Lettore item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome()); // imposta il testo della cella
                }
            });
            lettoreCombo.setButtonCell(lettoreCombo.getCellFactory().call(null)); // imposta la cella del pulsante della ComboBox

            lettoreScalettaCombo.getItems().clear(); // pulisce la lista esistente
            Lettore lettorevuoto = null; // opzione per nessun lettore
            lettoreScalettaCombo.getItems().addAll(lettorevuoto); // aggiunge l'opzione vuota
            lettoreScalettaCombo.getItems().addAll(lettori); // aggiunge i nuovi lettori
            lettoreScalettaCombo.setCellFactory(cb -> new ListCell<>() { // modifica le celle della ComboBox
                @Override
                protected void updateItem(Lettore item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome()); // imposta il testo della cella
                }
            });
            lettoreScalettaCombo.setButtonCell(lettoreCombo.getCellFactory().call(null)); // imposta la cella del pulsante della ComboBox

            this.elencoLibri = elencolibri; // aggiorna l'elenco dei libri
        });
    }

    /**
     * Aggiorna l'interfaccia utente della scaletta dell'evento.
     *
     * @param scalettaBox il contenitore VBox della scaletta
     * @param scaletta    la lista di LibroLettore che compone la scaletta
     */
    private void refreshScalettaUI(VBox scalettaBox, ArrayList<LibroLettore> scaletta) {
        scalettaBox.getChildren().clear();

        // controllo se il lettore corrente è il creatore dell'evento
        boolean isCreator = false;
        if (evento != null && evento.getCreatore() != null && lettore != null) {
            isCreator = evento.getCreatore().getId() == lettore.getId();
        }

        // menù a tendina per cambiare il lettore: mostrato solo al creatore
        if (isCreator) {
            lettoreScalettaCombo.setPromptText("");
            HBox rigaScaletta = new HBox(10,
                    new Label("Selezione lettore per modifica:"),
                    lettoreScalettaCombo
            );
            rigaScaletta.setAlignment(Pos.CENTER_RIGHT);
            scalettaBox.getChildren().add(rigaScaletta);
        }

        LocalTime tempoinizio = evento.getData().toLocalTime(); // inizio evento
        LocalTime tempofine; // fine lettura libro

        for (int i = 0; i < scaletta.size(); i++) { // per ogni libro nella scaletta
            LibroLettore ll = scaletta.get(i); // libro lettore corrente
            ll.setProgressivo(i + 1); // aggiorna il progressivo
            tempofine = tempoinizio.plusMinutes(ll.getLibro().getTempoLettura()); // calcola il tempo di fine lettura
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm"); // formato orario
            Label orario = new Label(
                    "[" + tempoinizio.format(formato) + " - " + tempofine.format(formato) + "]"
            ); // orario lettura
            tempoinizio = tempofine; // aggiorna il tempo di inizio per il prossimo libro
            Label titolo = new Label(ll.getLibro().getTitolo()); // titolo del libro
            Label durata = new Label("(" + ll.getLibro().getTempoLettura() + " min)"); // durata lettura
            Label lettoreLabel = new Label(
                    ll.getLettore() != null ? ll.getLettore().getNome() : ""
            ); // nome del lettore

            // Bottone cancella riga: solo se creatore e la riga non ha lettore assegnato
            Button eliminaBtn = null;
            if (isCreator && ll.getLettore() == null) {
                eliminaBtn = new Button("Cancella Riga");
                eliminaBtn.setOnAction(e -> {
                    scaletta.remove(ll); // rimuove la riga dalla scaletta
                    refreshScalettaUI(scalettaBox, scaletta);
                }); // azione di cancellazione riga
            }

            // Bottone modifica lettore: solo se creatore
            Button cambiaLettoreBtn = null;
            if (isCreator) {
                cambiaLettoreBtn = new Button("Modifica lettore");
                final int progressivoTemp = i; // variabile finale per l'uso nel lambda
                cambiaLettoreBtn.setOnAction(e -> { // azione di modifica lettore
                    Lettore selezionato = lettoreScalettaCombo.getSelectionModel().getSelectedItem(); // lettore selezionato
                    ll.modificaLettore(selezionato); // modifica il lettore della riga
                    scaletta.set(progressivoTemp, ll); // aggiorna la riga nella scaletta
                    refreshScalettaUI(scalettaBox, scaletta); // aggiorna l'interfaccia della scaletta
                });
            }

            // Bottone iscrivi/disiscrivi: solo se NON creatore e la riga non è già associata ad un altro lettore non null
            Button iscriviBtn = null;
            if (!isCreator) {
                boolean rigaLiberaOPropria = (ll.getLettore() == null) || (lettore != null && (ll.getLettore() != null) && ll.getLettore().getId() == lettore.getId()); // controlla se la riga è libera o associata al lettore corrente
                if (rigaLiberaOPropria) {
                    iscriviBtn = new Button((ll.getLettore() == null) ? "Iscrivi" : "Disiscrivi");
                    iscriviBtn.setOnAction(e -> {
                        if (ll.getLettore() == null) { // se la riga è libera, iscrive il lettore corrente
                            ll.modificaLettore(lettore); // iscrive il lettore corrente
                        } else {
                            // se e' il lettore corrente, si disiscrive; se e' qualcun altro (non previsto dalla condizione) non tocca
                            if (lettore != null && ll.getLettore().getId() == lettore.getId()) {
                                ll.modificaLettore(null); // disiscrive il lettore
                            }
                        }
                        refreshScalettaUI(scalettaBox, scaletta);  // aggiorna l'interfaccia della scaletta
                    });
                }
            }

            HBox riga = new HBox(10);
            // Aggiungi i pulsanti nell'ordine richiesto
            if (eliminaBtn != null) riga.getChildren().add(eliminaBtn); // bottone elimina
            if (cambiaLettoreBtn != null) riga.getChildren().add(cambiaLettoreBtn); // bottone cambia lettore
            if (iscriviBtn != null) riga.getChildren().add(iscriviBtn); // bottone iscrivi/disiscrivi

            riga.getChildren().addAll(
                    new Label((i + 1) + ")"),
                    orario,
                    titolo,
                    durata,
                    lettoreLabel
            );
            riga.setAlignment(Pos.CENTER_LEFT);
            scalettaBox.getChildren().add(riga);
        }
    }

    /**
     * Aggiorna il numero di iscritti all'evento.
     *
     * @param numIscritti il nuovo numero di iscritti
     */
    public void aggiornaIscritti(int numIscritti) {
        evento.setIscritti(numIscritti);
        Platform.runLater(() -> {
            iscritti.setText(evento.getIscritti() + "");
        });
    }
}