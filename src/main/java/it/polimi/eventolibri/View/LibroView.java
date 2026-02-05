package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Model.Libro;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Classe View per la selezione di un libro da una lista con filtro.
 */
public class LibroView {
    // Libro selezionato dall'utente inizialmente null
    private Libro libroSelezionato = null;


    /**
     * Mostra la finestra di selezione del libro (bloccante).
     *
     * @param owner lo stage proprietario della finestra modale
     * @param libri la lista di libri da cui selezionare
     * @return il libro selezionato, o null se l'operazione è stata annullata
     */
    public Libro show(Stage owner, List<Libro> libri) {
        Stage stage = buildStage(owner, libri);
        stage.showAndWait(); // bloccante
        return libroSelezionato;
    }

    /**
     * Mostra la finestra di selezione del libro in modo non bloccante (utile per i test).
     *
     * @param owner lo stage proprietario della finestra
     * @param libri la lista di libri da cui selezionare
     * @return lo Stage creato (ancora aperto)
     */
    public Stage showNonBloccante(Stage owner, List<Libro> libri) {
        Stage stage = buildStage(owner, libri);
        stage.show(); // non bloccante
        return stage;
    }

    /**
     * Costruisce lo stage per la selezione del libro.
     *
     * @param owner lo stage proprietario della finestra modale
     * @param libri la lista di libri da cui selezionare
     * @return lo Stage creato
     */
    private Stage buildStage(Stage owner, List<Libro> libri) {
        // Crea la finestra per la selezione del libro
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Seleziona libro");
        // Titolo
        Label titolo = new Label("Scegli un libro");
        titolo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        // Campo filtro in alto
        TextField txtFiltro = new TextField();
        txtFiltro.setId("filterTextField");
        txtFiltro.setPromptText("Filtra per titolo, usa * come jolly");
        // Lista filtrata
        FilteredList<Libro> filtered = new FilteredList<>(FXCollections.observableArrayList(libri), l -> true);
        // Lista dei libri
        ListView<Libro> listView = new ListView<>();
        listView.setId("bookListView");
        listView.setItems(filtered);
        // Come mostrare i libri
        listView.setCellFactory(lv -> new ListCell<>() {
            // Aggiorna il testo della cella con il titolo e il tempo di lettura
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);
                if (empty || libro == null) {
                    setText(null);
                } else {
                    setText(libro.getTitolo() + " (" + libro.getTempoLettura() + " min)");
                }
            }
        });
        // Listener per aggiornare il filtro
        txtFiltro.textProperty().addListener((obs, oldText, newText) -> {
            Pattern p = costrusciPatternDalFiltro(newText);
            if (p == null) {
                filtered.setPredicate(l -> true);
            } else {
                filtered.setPredicate(libro -> {
                    String titoloLower = libro.getTitolo() != null ? libro.getTitolo().toLowerCase() : "";
                    return p.matcher(titoloLower).matches();
                });
            }
        });
        // Bottoni Conferma/Annulla
        Button btnConferma = new Button("Conferma");
        Button btnAnnulla = new Button("Annulla");
        // Azioni bottoni
        btnConferma.setOnAction(e -> {
            libroSelezionato = listView.getSelectionModel().getSelectedItem();
            stage.close();
        });
        btnAnnulla.setOnAction(e -> {
            libroSelezionato = null;
            stage.close();
        });
        // Layout bottoni
        HBox buttons = new HBox(10, btnConferma, btnAnnulla);
        buttons.setAlignment(Pos.CENTER);
        // Layout principale
        VBox layout = new VBox(15, titolo, txtFiltro, listView, buttons);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 400, 400));
        return stage; // ritorna lo stage per lo stage non bloccante
    }

    // Trasforma una stringa con '*' in una regex sicura, pulendo i metacaratteri
    String daStringaARegex(String asterisco) {
        StringBuilder sb = new StringBuilder(); // StringBuilder per costruire la regex
        for (char c : asterisco.toCharArray()) { // cicla su ogni carattere della stringa
            // gestisce il jolly
            if (c == '*') {
                // aggiunge il jolly regex
                sb.append(".*");
            } else {
                // gestisce i metacaratteri regex scaricandoli
                if ("\\.[]{}()+-^$|?".indexOf(c) >= 0) {
                    // aggiunge il backslash di escape
                    sb.append('\\');
                }
                // aggiunge il carattere minuscolo
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString(); // ritorna la regex costruita
    }

    // Metodo (package\-private) per creare la Pattern dal filtro (usato nei test)
    Pattern costrusciPatternDalFiltro(String filter) {
        // Controlla se il filtro è vuoto, e ritorna null
        if (filter == null || filter.trim().isEmpty()) {
            return null;
        }
        // Pulisce il filtro e crea la regex
        String cleaned = filter.trim().toLowerCase();
        // Crea la regex
        String pattern = daStringaARegex(cleaned);
        // Assicura jolly iniziale/finale
        if (!pattern.startsWith(".*")) pattern = ".*" + pattern;
        if (!pattern.endsWith(".*")) pattern = pattern + ".*";
        return Pattern.compile(pattern);
    }
}