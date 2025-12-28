package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Recensione;
import it.polimi.eventolibri.Model.Utente;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Consumer;

public class LibroDetailedView {
    private final Client client;

    public LibroDetailedView(Client client) {
        this.client = client;
    }

    /**
     * Mostra la view dettagliata del libro.
     * Il caller deve calcolare (server-side) la variabile `recensibile` per i genitori/lettori
     * oppure passare null se vuole che la view richiami il server in modo asincrono.
     *
     * @param stage owner
     * @param libro libro visualizzato
     * @param recensioni lista recensioni (può essere vuota)
     * @param utente chiamante (instanceof Genitore/Lettore/Amministratore)
     * @param recensibile se non null indica se il genitore può recensire; per lettore/adm viene ignorato
     * @param onBack callback per tornare alla home
     * @param onSubmit consumer eseguito all'invio recensione (caller imposta id/genitore e invia al server)
     * @param onDelete consumer per cancellare recensione (solo admin)
     */
    public void show(Stage stage,
                     Libro libro,
                     ArrayList<Recensione> recensioni,
                     Utente utente,
                     Boolean recensibile,
                     Runnable onBack,
                     Consumer<Recensione> onSubmit,
                     Consumer<Recensione> onDelete) {

        // decidere permessi basati sulla gerarchia di Utente
        boolean canAdd = false;
        boolean canDelete = false;

        if (utente == null) {
            // nessun utente: vista in sola lettura
            canAdd = false;
            canDelete = false;
        } else {
            String tipo = utente.getClass().getSimpleName(); // utile per log/debug
            // amministratore: pieno controllo
            if ("Amministratore".equals(tipo)) {
                canAdd = true;
                canDelete = true;
            }
            // genitore: può aggiungere solo se recensibile == TRUE (caller/server decide)
            else if (utente instanceof Genitore) {
                canAdd = Boolean.TRUE.equals(recensibile);
                canDelete = false;
            }
            // lettore: non può aggiungere recensioni secondo regole (solo visualizza)
            else {
                canAdd = false;
                canDelete = false;
            }
        }

        Stage s = new Stage();
        s.initOwner(stage);
        s.setTitle("Dettaglio libro");
        s.initModality(Modality.WINDOW_MODAL);

        Button backBtn = new Button("Indietro");
        backBtn.setOnAction(e -> {
            s.close();
            if (onBack != null) onBack.run();
        });
        HBox topBar = new HBox(backBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label titolo = new Label("Titolo: " + libro.getTitolo());
        Label autore = new Label("Autore: " + libro.getAutore());
        Label tempo = new Label("Tempo di lettura: " + libro.getTempoLettura() + " min");
        Label isbn = new Label("ISBN: " + (libro.getIsbn() != null ? libro.getIsbn() : ""));
        Hyperlink link = new Hyperlink(libro.getLink() == null ? "" : libro.getLink());
        link.setOnAction(ev -> {
            if (link.getText() == null || link.getText().isBlank()) return;
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(link.getText()));
            } catch (IOException | URISyntaxException ex) {
                System.out.println("Impossibile aprire il link: " + ex.getMessage());
            }
        });

        String recLabel = "Recensioni: ";
        if (utente instanceof Genitore) recLabel += (canAdd ? "puoi aggiungere" : "non puoi aggiungere");
        else if ("Amministratore".equals(utente.getClass().getSimpleName())) recLabel += "amministratore: pieno controllo";
        else recLabel += "solo lettura";

        VBox dettagliBox = new VBox(8, titolo, autore, tempo, isbn, new Label("Link:"), link, new Label(recLabel));
        dettagliBox.setPadding(new Insets(10));

        Button vediRecensioniBtn = new Button("Vedi recensioni");
        boolean finalCanDelete = canDelete;
        vediRecensioniBtn.setOnAction(ev -> showRecensioniWindow(s, recensioni, finalCanDelete, onDelete));

        VBox centerBox = new VBox(10, dettagliBox, vediRecensioniBtn);
        centerBox.setPadding(new Insets(10));

        if (canAdd) {
            Button aggiungiRecBtn = new Button("Aggiungi recensione");
            aggiungiRecBtn.setOnAction(ev -> showAggiungiRecensioneDialog(s, libro, utente, onSubmit));
            centerBox.getChildren().add(aggiungiRecBtn);
        }

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 650, 380);
        Platform.runLater(() -> {
            s.setScene(scene);
            s.show();
        });
    }

    private void showRecensioniWindow(Stage owner, ArrayList<Recensione> recensioni, boolean canDelete, Consumer<Recensione> onDelete) {
        Stage rStage = new Stage();
        rStage.initOwner(owner);
        rStage.initModality(Modality.WINDOW_MODAL);
        rStage.setTitle("Recensioni");

        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        if (recensioni == null || recensioni.isEmpty()) {
            box.getChildren().add(new Label("Nessuna recensione disponibile"));
        } else {
            for (Recensione r : recensioni) {
                Label testo = new Label((r.getTesto() != null ? r.getTesto() : ""));
                testo.setWrapText(true);
                String autore = (r.getGenitore() != null) ? ("genitore id " + r.getGenitore().getId()) : "autore sconosciuto";
                Label autoreLbl = new Label("Di: " + autore);
                VBox v = new VBox(4, testo, autoreLbl);
                if (canDelete) {
                    Button del = new Button("Elimina");
                    del.setOnAction(ev -> {
                        if (onDelete != null) onDelete.accept(r);
                        rStage.close();
                    });
                    HBox h = new HBox(8, v, del);
                    h.setAlignment(Pos.CENTER_LEFT);
                    box.getChildren().addAll(h, new Separator());
                } else {
                    box.getChildren().addAll(v, new Separator());
                }
            }
        }

        Scene sc = new Scene(new ScrollPane(box), 540, 420);
        rStage.setScene(sc);
        rStage.show();
    }

    private void showAggiungiRecensioneDialog(Stage owner, Libro libro, Utente utente, Consumer<Recensione> onSubmit) {
        Stage d = new Stage();
        d.initOwner(owner);
        d.initModality(Modality.WINDOW_MODAL);
        d.setTitle("Aggiungi recensione");

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));

        Label info = new Label("Scrivi la recensione (max 150 caratteri):");
        TextArea area = new TextArea();
        area.setWrapText(true);
        area.setPromptText("La tua recensione...");
        Label counter = new Label("0/150");

        area.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 150) area.setText(newV.substring(0, 150));
            else counter.setText(area.getText().length() + "/150");
        });

        CheckBox conferma = new CheckBox("Confermo di aver partecipato ad almeno un evento con questo libro");
        Button invia = new Button("Invia");
        invia.setDisable(true);

        conferma.selectedProperty().addListener((obs, oldV, newV) -> invia.setDisable(!newV || area.getText().isBlank()));
        area.textProperty().addListener((obs, oldV, newV) -> invia.setDisable(!conferma.isSelected() || newV.isBlank()));

        invia.setOnAction(ev -> {
            String testo = area.getText().trim();
            if (testo.isEmpty()) return;
            Recensione r = new Recensione(null, null, null);
            r.setTesto(testo);
            r.setLibro(libro);
            if (utente instanceof Genitore) {
                r.setGenitore((Genitore) utente); // il caller dovrebbe impostare il genitore sulla recensione prima di inviare
            }
            if (onSubmit != null) onSubmit.accept(r);
            d.close();
        });

        Button annulla = new Button("Annulla");
        annulla.setOnAction(ev -> d.close());

        HBox btns = new HBox(8, invia, annulla);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(info, area, counter, conferma, btns);

        Scene sc = new Scene(root, 520, 320);
        d.setScene(sc);
        d.show();
    }
}


