package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Model.Libro;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class LibroView {

    private Libro libroSelezionato = null;

    public Libro show(Stage owner, List<Libro> libri) {

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Seleziona libro");

        Label titolo = new Label("Scegli un libro");
        titolo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<Libro> listView = new ListView<>();
        listView.getItems().addAll(libri);

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

        VBox layout = new VBox(15, titolo, listView, buttons);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 400, 400));
        stage.showAndWait();   // ⬅️ BLOCCA la view chiamante

        return libroSelezionato;
    }
}