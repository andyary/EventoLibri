
// java
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

public class LibroView {

    private Libro libroSelezionato = null;

    public Libro show(Stage owner, List<Libro> libri) {

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Seleziona libro");

        Label titolo = new Label("Scegli un libro");
        titolo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Campo filtro in alto
        TextField txtFiltro = new TextField();
        txtFiltro.setPromptText("Filtra per titolo, usa * come jolly");

        // Lista filtrata
        FilteredList<Libro> filtered = new FilteredList<>(FXCollections.observableArrayList(libri), l -> true);

        ListView<Libro> listView = new ListView<>();
        listView.setItems(filtered);

        // Come mostrare i libri
        listView.setCellFactory(lv -> new ListCell<>() {
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
            Pattern p = buildPatternFromFilter(newText);
            if (p == null) {
                filtered.setPredicate(l -> true);
            } else {
                filtered.setPredicate(libro -> {
                    String titoloLower = libro.getTitolo() != null ? libro.getTitolo().toLowerCase() : "";
                    return p.matcher(titoloLower).matches();
                });
            }
        });

        Button btnConferma = new Button("Conferma");
        Button btnAnnulla = new Button("Annulla");

        btnConferma.setOnAction(e -> {
            libroSelezionato = listView.getSelectionModel().getSelectedItem();
            stage.close();
        });

        btnAnnulla.setOnAction(e -> {
            libroSelezionato = null;
            stage.close();
        });

        HBox buttons = new HBox(10, btnConferma, btnAnnulla);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, titolo, txtFiltro, listView, buttons);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 400, 400));
        stage.showAndWait();   // blocca la view chiamante

        return libroSelezionato;
    }

    // Trasforma una stringa con '*' in una regex sicura
    String wildcardToRegex(String wildcard) {
        StringBuilder sb = new StringBuilder();
        for (char c : wildcard.toCharArray()) {
            if (c == '*') {
                sb.append(".*");
            } else {
                // escape dei metacaratteri regex
                if ("\\.[]{}()+-^$|?".indexOf(c) >= 0) {
                    sb.append('\\');
                }
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    // Metodo package\-private per creare la Pattern dal filtro (usato nei test)
    Pattern buildPatternFromFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return null;
        }
        String cleaned = filter.trim().toLowerCase();
        String pattern = wildcardToRegex(cleaned);
        if (!pattern.startsWith(".*")) pattern = ".*" + pattern;
        if (!pattern.endsWith(".*")) pattern = pattern + ".*";
        return Pattern.compile(pattern);
    }
}










//package it.polimi.eventolibri.View;
//
//import it.polimi.eventolibri.Model.Libro;
//import javafx.collections.FXCollections;
//import javafx.collections.transformation.FilteredList;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//import java.util.List;
//import java.util.regex.Pattern;
//
//public class LibroView {
//
//    private Libro libroSelezionato = null;
//
//    public Libro show(Stage owner, List<Libro> libri) {
//
//        Stage stage = new Stage();
//        stage.initOwner(owner);
//        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.setTitle("Seleziona libro");
//
//        Label titolo = new Label("Scegli un libro");
//        titolo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        // Campo filtro in alto
//        TextField txtFiltro = new TextField();
//        txtFiltro.setPromptText("Filtra per titolo, usa * come jolly");
//
//        // Lista filtrata
//        FilteredList<Libro> filtered = new FilteredList<>(FXCollections.observableArrayList(libri), l -> true);
//
//        ListView<Libro> listView = new ListView<>();
//        listView.setItems(filtered);
//
//        // Come mostrare i libri
//        listView.setCellFactory(lv -> new ListCell<>() {
//            @Override
//            protected void updateItem(Libro libro, boolean empty) {
//                super.updateItem(libro, empty);
//                if (empty || libro == null) {
//                    setText(null);
//                } else {
//                    setText(libro.getTitolo() + " (" + libro.getTempoLettura() + " min)");
//                }
//            }
//        });
//
//        // Listener per aggiornare il filtro
//        txtFiltro.textProperty().addListener((obs, oldText, newText) -> {
//            if (newText == null || newText.trim().isEmpty()) {
//                filtered.setPredicate(l -> true);
//            } else {
//                String pattern = wildcardToRegex(newText.trim().toLowerCase());
//                // rendere la ricerca per sottostringa se l'utente non ha specificato jolly
//                if (!pattern.startsWith(".*")) pattern = ".*" + pattern;
//                if (!pattern.endsWith(".*")) pattern = pattern + ".*";
//                final Pattern p = Pattern.compile(pattern);
//                filtered.setPredicate(libro -> {
//                    String titoloLower = libro.getTitolo() != null ? libro.getTitolo().toLowerCase() : "";
//                    return p.matcher(titoloLower).matches();
//                });
//            }
//        });
//
//        Button btnConferma = new Button("Conferma");
//        Button btnAnnulla = new Button("Annulla");
//
//        btnConferma.setOnAction(e -> {
//            libroSelezionato = listView.getSelectionModel().getSelectedItem();
//            stage.close();
//        });
//
//        btnAnnulla.setOnAction(e -> {
//            libroSelezionato = null;
//            stage.close();
//        });
//
//        HBox buttons = new HBox(10, btnConferma, btnAnnulla);
//        buttons.setAlignment(Pos.CENTER);
//
//        VBox layout = new VBox(15, titolo, txtFiltro, listView, buttons);
//        layout.setPadding(new Insets(15));
//
//        stage.setScene(new Scene(layout, 400, 400));
//        stage.showAndWait();   // blocca la view chiamante
//
//        return libroSelezionato;
//    }
//
//    // Trasforma una stringa con '*' in una regex sicura
//    private String wildcardToRegex(String wildcard) {
//        StringBuilder sb = new StringBuilder();
//        for (char c : wildcard.toCharArray()) {
//            if (c == '*') {
//                sb.append(".*");
//            } else {
//                // escape dei metacaratteri regex
//                if ("\\.[]{}()+-^$|?".indexOf(c) >= 0) {
//                    sb.append('\\');
//                }
//                sb.append(Character.toLowerCase(c));
//            }
//        }
//        return sb.toString();
//    }
//}
