package it.polimi.eventolibri.View;

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

public class EventoViewLettore {
    private Client client;
    private Stage stage;
    private Evento evento;
    private Lettore lettore;
    private ArrayList<Luogo> luoghi = new ArrayList<>();
    private ArrayList<Lettore> lettori = new ArrayList<>();
    private ArrayList<Libro> elencoLibri = new ArrayList<>();
    private Runnable onBack;
    private Label messaggioerrore;
    private Label iscritti;
    private ComboBox<Luogo> luogoCombo = new ComboBox<>();
    private ComboBox<Lettore> lettoreCombo = new ComboBox<>();
    private ComboBox<Lettore> lettoreScalettaCombo = new ComboBox<>();


    public EventoViewLettore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
        this.iscritti = new Label("Iscritti: TBD");
    }

    public void setLuoghi(ArrayList<Luogo> luoghi) {
        this.luoghi = luoghi;
    }

    public void setLettori(ArrayList<Lettore> lettori) {
        this.lettori = lettori;
    }

    public void setElencoLibri(ArrayList<Libro> elencoLibri) {
        this.elencoLibri = elencoLibri;
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
     * Mostra la finestra dei dettagli evento con dettaglio scaletta ed iscizione come lettore
     */
    public void show(Stage stage, Evento evento, Lettore lettore, Runnable onBack) {
        this.stage = stage;
        this.evento = evento;
        this.lettore = lettore;
        this.onBack = onBack;
        this.messaggioerrore.setText("");

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
        /* ---------- LUOGO ---------- */
        if (evento.getLuogo() == null) {
            luogoCombo.setPromptText("Scegli luogo");
        } else {
            luogoCombo.setValue(evento.getLuogo());
            luogoCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Luogo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome());
                }
            });
            luogoCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Luogo luogo) {
                    return luogo == null ? "" : luogo.getNome();
                }
                @Override
                public Luogo fromString(String s) {
                    return null;
                }
            });
        }
        HBox luogoBox = new HBox(5,
                new Label("Luogo:"),
                luogoCombo
        );
        /* ---------- DATA / ORA ---------- */
        DatePicker datePicker = new DatePicker(evento.getData().toLocalDate());
        datePicker.setPromptText("Data evento");
        HBox dataBox = new HBox(5,
                new Label("Data:"),
                datePicker
        );
        dataBox.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, evento.getData().toLocalTime().getHour());
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, evento.getData().toLocalTime().getMinute());
        hourSpinner.setEditable(true);
        // hourSpinner.getEditor().setPrefColumnCount(2);
        minuteSpinner.setEditable(true);
        // minuteSpinner.getEditor().setPrefColumnCount(2);
        HBox oraBox = new HBox(5,
                new Label("Ora:"),
                hourSpinner,
                new Label(":"),
                minuteSpinner
        );
        oraBox.setAlignment(Pos.CENTER_LEFT);
        /* ---------- SCALETTA ---------- */
        Label scalettaLabel = new Label("Scaletta");
        scalettaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox scalettaBox = new VBox(6);
        scalettaBox.setPadding(new Insets(10));
        scalettaBox.setStyle("-fx-border-color: lightgray;");
        ArrayList<LibroLettore> scaletta = evento.getScaletta();
        Label libroSelezionatoLabel = new Label("Nessun libro selezionato");
        Button scegliLibroBtn = new Button("Scegli libro");
        final Libro[] libroSelezionato = new Libro[1];

        scegliLibroBtn.setOnAction(e -> {
            messaggioerrore.setText("");
                LibroView dialog = new LibroView();
                Libro libro = dialog.show(stage, elencoLibri);
                if (libro != null) {
                    libroSelezionatoLabel.setText(
                            libro.getTitolo() + " (" + libro.getTempoLettura() + " min)"
                    );
                    libroSelezionato[0] = libro;
                }
        });

        lettoreCombo.setPromptText("Lettore (facoltativo)");

//        Button cancellaRigaBtn = new Button("Cancella riga");
//        cancellaRigaBtn.setOnAction(e -> {
//            messaggioerrore.setText("");
//            if (scaletta.isEmpty()) {
//                messaggioerrore.setText("Nessuna riga da cancellare");
//                return;
//            }
//            scaletta.remove(scaletta.size() - 1);
//            refreshScalettaUI(scalettaBox, scaletta);
//        });


        Button aggiungiRigaBtn = new Button("Aggiungi riga");
        aggiungiRigaBtn.setOnAction(e -> {
            messaggioerrore.setText("");
            if (libroSelezionato[0] == null) {
                messaggioerrore.setText("Seleziona un libro");
                return;
            }
            LibroLettore ll = new LibroLettore(
                    libroSelezionato[0],
                    lettoreCombo.getValue(),
                    scaletta.size() + 1
            );
            scaletta.add(ll);
            refreshScalettaUI(scalettaBox, scaletta);
            libroSelezionato[0] = null;
            libroSelezionatoLabel.setText("Nessun libro selezionato");
            lettoreCombo.setValue(null);
        });

        HBox libroLettoreBox = new HBox(5,
                new Label("Libro:"),
                scegliLibroBtn,
                new Label("Lettore:"),
                lettoreCombo,
                /* cancellaRigaBtn, */
                aggiungiRigaBtn
        );
        libroLettoreBox.setAlignment(Pos.CENTER_LEFT);

        /* ---------- BOTTONI ---------- */
        Button salvaBtn = new Button("Salva evento");
        Button annullaBtn = new Button("Annulla");
        salvaBtn.setOnAction(e -> {
            messaggioerrore.setText("");
            if (titoloField.getText().isBlank()
                    || luogoCombo.getValue() == null
                    || datePicker.getValue() == null
            ) {
                messaggioerrore.setText("Compila tutti i campi obbligatori");
                return;
            }
            LocalDateTime dataOra = LocalDateTime.of(
                    datePicker.getValue(),
                    LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
            );
            Evento eventoTemp = new Evento(
                    titoloField.getText(),
                    luogoCombo.getValue(),
                    dataOra
            );
            eventoTemp.setScaletta(scaletta);
            eventoTemp.setCreatore(lettore);
            if (evento.getId() != 0) {
                eventoTemp.setId(evento.getId());
            }
            try {
                client.sendMessage(new RichiestaSalvaEvento(eventoTemp));
            } catch (IOException ex) {
                messaggioerrore.setText("Errore durante il salvataggio evento");
            }
        });
        annullaBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        HBox mainBtnBox = new HBox(5,
                salvaBtn,
                annullaBtn
        );
        mainBtnBox.setAlignment(Pos.CENTER_RIGHT);

        /* ---------- LAYOUT ---------- */
        VBox layout = new VBox(15,
                mainBtnBox,
                messaggioerrore,
                titoloLabel,
                titoloField,
                luogoBox,
                dataBox,
                oraBox,
                scalettaLabel,
                libroSelezionatoLabel,
                libroLettoreBox,
                scalettaBox
        );
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);

        BorderPane root = new BorderPane();
        root.setTop(layout);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // adatta la larghezza del contenuto alla finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // scroll verticale solo se serve
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        refreshScalettaUI(scalettaBox, scaletta);

        Platform.runLater(() -> {;
            stage.setScene(new Scene(scrollPane, 700, 700));
            stage.setTitle("Crea Evento");
            stage.show();
        });

    }

    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }


    public void aggiornaLettoriELuoghiELibri(ArrayList<Lettore> lettori, ArrayList<Luogo> luoghi, ArrayList<Libro> elencolibri) {
        this.lettori.clear();
        this.lettori.addAll(lettori);
        this.luoghi.clear();
        this.luoghi.addAll(luoghi);
        Platform.runLater(() -> {
            luogoCombo.getItems().clear();
            luogoCombo.getItems().addAll(luoghi);
            luogoCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Luogo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome());
                }
            });
            luogoCombo.setButtonCell(luogoCombo.getCellFactory().call(null));

            lettoreCombo.getItems().clear();
            lettoreCombo.getItems().addAll(lettori);
            lettoreCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Lettore item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome());
                }
            });
            lettoreCombo.setButtonCell(lettoreCombo.getCellFactory().call(null));

            lettoreScalettaCombo.getItems().clear();
            Lettore lettorevuoto = null;
            lettoreScalettaCombo.getItems().addAll(lettorevuoto);
            lettoreScalettaCombo.getItems().addAll(lettori);
            lettoreScalettaCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Lettore item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNome());
                }
            });
            lettoreScalettaCombo.setButtonCell(lettoreCombo.getCellFactory().call(null));

            this.elencoLibri = elencolibri;

        });
    }

    private void refreshScalettaUI(VBox scalettaBox, ArrayList<LibroLettore> scaletta) {
        scalettaBox.getChildren().clear();

        // menu a tendina per cambiare il lettore
        lettoreScalettaCombo.setPromptText("");
        HBox rigaScaletta = new HBox(10,
                new Label("Selezione lettore per modifica:"),
                lettoreScalettaCombo
        );
        rigaScaletta.setAlignment(Pos.CENTER_RIGHT);
        scalettaBox.getChildren().add(rigaScaletta);

        LocalTime tempoinizio= evento.getData().toLocalTime();
        LocalTime tempofine;

        for (int i = 0; i < scaletta.size(); i++) {
            LibroLettore ll = scaletta.get(i);
            ll.setProgressivo(i + 1);
            tempofine = tempoinizio.plusMinutes(ll.getLibro().getTempoLettura());
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
            Label orario = new Label(
                    "[" + tempoinizio.format(formato) + " - " + tempofine.format(formato) + "]"
            );
            tempoinizio = tempofine;
            Label titolo = new Label(ll.getLibro().getTitolo());
            Label durata = new Label("(" + ll.getLibro().getTempoLettura() + " min)");
            Label lettore = new Label(
                    ll.getLettore() != null ? ll.getLettore().getNome() : ""
            );
            Button eliminaBtn = new Button("Cancella Riga");
            eliminaBtn.setOnAction(e -> {
                scaletta.remove(ll);
                refreshScalettaUI(scalettaBox, scaletta);
            });

            Button cambiaLettoreBtn = new Button("Modifica lettore");
            final int[] progressivoTemp = new int[1];
            progressivoTemp[0] = i;
            cambiaLettoreBtn.setOnAction(e -> {
                ll.modificaLettore(lettoreScalettaCombo.getSelectionModel().getSelectedItem());
                scaletta.set(progressivoTemp[0],ll);
                refreshScalettaUI(scalettaBox, scaletta);
            });

            HBox riga = new HBox(10,
                    eliminaBtn,
                    cambiaLettoreBtn,
                    new Label((i + 1) + ")"),
                    orario,
                    titolo,
                    durata,
                    lettore
            );
            riga.setAlignment(Pos.CENTER_LEFT);
            scalettaBox.getChildren().add(riga);
        }
    }
}
