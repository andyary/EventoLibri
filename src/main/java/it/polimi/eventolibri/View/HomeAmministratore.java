package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Message.RichiestaRecensioniERecensibilita;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeAmministratore {

    private final Client client;
    private Stage stage;
    private Amministratore amministratore;
    private ProfiloAmministratore profiloAmministratore;
    private RegistraNewLettore registraNewLettore;
    private RegistraNewAmministratore registraNewAmministratore;
    private Runnable onBack;

    private LibroDetailedView libroDetailedView;
    private ArrayList<Libro> elencoLibri = new ArrayList<>();
    private boolean recensibile;
    private ArrayList<Recensione> recensioni = new ArrayList<>(); // da caricare dal server
    private boolean attendi;

    private Label messaggioerrore;


    public HomeAmministratore(Client client, ProfiloAmministratore profiloAmministratore,
                              RegistraNewLettore registraNewLettore, RegistraNewAmministratore registraNewAmministratore, LibroDetailedView libroDetailedView) {
        this.client = client;
        this.profiloAmministratore = profiloAmministratore;
        this.registraNewLettore = registraNewLettore;
        this.registraNewAmministratore = registraNewAmministratore;
        this.libroDetailedView = libroDetailedView;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setStyle("-fx-text-fill: red;");
    }

    public void setElencoLibri(ArrayList<Libro> elencoLibri) {
        this.elencoLibri = elencoLibri == null ? new ArrayList<>() : elencoLibri;
    }

    public void setRecensibile(boolean recensibile) {
        this.recensibile = recensibile;
    }

    public void setRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    public void delRecensione(Recensione recensione) {
        if (this.recensioni != null) this.recensioni.remove(recensione);
    }

    public void setAttendi(boolean attendi) {
        this.attendi = attendi;
    }

    public void show(Stage stage, Amministratore amministratore, Runnable onBack) {
        this.stage = stage;
        this.amministratore = amministratore;
        this.onBack = onBack;

        RichiestaLettoriELuoghiELibri richiestaLettoriELuoghiELibri = new RichiestaLettoriELuoghiELibri();
        try {
            client.sendMessage(richiestaLettoriELuoghiELibri);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
        }


        // ---------- TOP BAR CON PROFILO ----------
        Button profiloButton = new Button("Profilo amministratore");
        profiloButton.setOnAction(e -> {
            System.out.println("Apertura schermata profilo...");
            profiloAmministratore.show(stage, amministratore, () -> {
                this.show(stage, amministratore, onBack);
            });

        });

        Button newLettoreButton = new Button("Crea Nuovo Lettore");
        newLettoreButton.setOnAction(e -> {
            System.out.println("Apertura schermata crea nuovo Lettore...");
            registraNewLettore.show(stage, amministratore, () -> {
                this.show(stage, amministratore, onBack);
            });

        });

        Button newAmministratoreButton = new Button("Crea Nuovo Amministratore");
        newAmministratoreButton.setOnAction(e -> {
            System.out.println("Apertura schermata crea nuovo Amministratore...");
            registraNewAmministratore.show(stage, amministratore,() -> {
                this.show(stage, amministratore, onBack);
            });

        });

        Button backButton = new Button("Logout");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });


        Label libroSelezionatoLabel = new Label("Seleziona libro (recensioni)");
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
                // recupera recensioni libro
                recensioni.clear();
                recensibile = false;

                // recupera recensibilità
                RichiestaRecensioniERecensibilita richiesta = new RichiestaRecensioniERecensibilita(libro, amministratore);
                try {
                    client.sendMessage(richiesta);
                } catch (IOException ex) {
                    messaggioerrore.setText("Errore nell'invio della richiesta recensioni e recensibilità: " + ex.getMessage());
                }

                this.attendi = true;
                while (attendi) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.out.println("Errore attesa recensioni: " + ex.getMessage());
                    }
                };

                // apri dettaglio libro
                libroDetailedView.show(
                        stage,
                        libroSelezionato[0],
                        recensioni,
                        amministratore,
                        recensibile,
                        () -> {
                            this.show(stage, amministratore, onBack);
                        }
                );
            }
        });

        HBox topBar1 = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "), profiloButton);
        topBar1.setAlignment(Pos.CENTER_RIGHT);
        HBox topBar2 = new HBox(newLettoreButton, newAmministratoreButton);
        topBar2.setAlignment(Pos.CENTER_RIGHT);
        HBox topBar3 = new HBox(scegliLibroBtn);
        topBar3.setAlignment(Pos.CENTER_RIGHT);
        HBox topBar4 = new HBox(backButton);
        topBar4.setAlignment(Pos.CENTER_RIGHT);

        VBox topBar = new VBox(topBar1, topBar2, topBar3, topBar4, messaggioerrore);

//        HBox topBar = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "), profiloButton, newLettoreButton, newAmministratoreButton, scegliLibroBtn, backButton);
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        // ---------- LAYOUT FINALE ----------

        BorderPane root = new BorderPane();
        root.setTop(topBar);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // adatta la larghezza del contenuto alla finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // scroll verticale solo se serve
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 800, 750);
        Platform.runLater(() -> {;
            stage.setScene(scene);
            stage.setTitle("Home Amministratore");
            stage.show();
        });
    }

    public void aggiornaLibri(ArrayList<Libro> elencolibri) {
        this.elencoLibri = elencolibri;
    }

    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }

}
