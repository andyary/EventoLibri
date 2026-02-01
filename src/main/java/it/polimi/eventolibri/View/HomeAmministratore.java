package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
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
import java.util.ArrayList;
/**
 * Classe View per la schermata principale dell'amministratore.
 */
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

    /**
     * Costruttore della classe HomeAmministratore.
     *
     * @param client                    l'istanza del client per la comunicazione con il server
     * @param profiloAmministratore     la view per il profilo dell'amministratore
     * @param registraNewLettore       la view per la registrazione di un nuovo lettore
     * @param registraNewAmministratore la view per la registrazione di un nuovo amministratore
     * @param libroDetailedView        la view per il dettaglio del libro
     */
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

    /**
     * Imposta l'elenco dei libri disponibili.
     *
     * @param elencoLibri l'elenco dei libri
     */
    public void setElencoLibri(ArrayList<Libro> elencoLibri) {
        this.elencoLibri = elencoLibri == null ? new ArrayList<>() : elencoLibri;
    }

    /**
     * Imposta se il libro è recensibile.
     *
     * @param recensibile true se il libro è recensibile, false altrimenti
     */
    public void setRecensibile(boolean recensibile) {
        this.recensibile = recensibile;
    }

    /**
     * Imposta l'elenco delle recensioni.
     *
     * @param recensioni l'elenco delle recensioni
     */
    public void setRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    /**
     * Rimuove una recensione dall'elenco delle recensioni.
     *
     * @param recensione la recensione da rimuovere
     */
    public void delRecensione(Recensione recensione) {
        if (this.recensioni != null) this.recensioni.remove(recensione);
    }

    /**
     * Imposta lo stato di attesa.
     *
     * @param attendi true se in attesa, false altrimenti
     */
    public void setAttendi(boolean attendi) {
        this.attendi = attendi;
    }

    /**
     * Mostra la schermata principale dell'amministratore.
     *
     * @param stage          lo stage principale dell'applicazione
     * @param amministratore l'amministratore che ha effettuato l'accesso
     * @param onBack         l'azione da eseguire quando si preme il pulsante "Logout"
     */
    public void show(Stage stage, Amministratore amministratore, Runnable onBack) {
        this.stage = stage;
        this.amministratore = amministratore;
        this.onBack = onBack;

        // richiedi lettori, luoghi e libri
        RichiestaLettoriELuoghiELibri richiestaLettoriELuoghiELibri = new RichiestaLettoriELuoghiELibri();
        try {
            client.sendMessage(richiestaLettoriELuoghiELibri);
        } catch (IOException e) {
            System.out.println("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
            messaggioerrore.setText("Errore nel richiestaLettoriELuoghiELibri" + e.getMessage());
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

        HBox topBar1 = new HBox(new Label("  Benvenuto, (admin) " + amministratore.getNome() + "!          "), profiloButton, scegliLibroBtn, backButton);
        topBar1.setAlignment(Pos.CENTER_RIGHT);
        HBox topBar2 = new HBox(newLettoreButton);
        topBar2.setAlignment(Pos.CENTER);
        HBox topBar3 = new HBox(newAmministratoreButton);
        topBar3.setAlignment(Pos.CENTER);
        
        VBox topBar = new VBox(20,topBar1, topBar2, topBar3, messaggioerrore);

        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.TOP_RIGHT);

        // ---------- LAYOUT FINALE ----------

        BorderPane root = new BorderPane();
        root.setTop(topBar);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);  // adatta la larghezza del contenuto alla finestra
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // scroll verticale solo se serve
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane,  750, 780);
        Platform.runLater(() -> {;
            stage.setScene(scene);
            stage.setTitle("Home Amministratore");
            stage.show();
        });
    }

    /**
     * Aggiorna l'elenco dei libri disponibili.
     *
     * @param elencolibri l'elenco dei libri
     */
    public void aggiornaLibri(ArrayList<Libro> elencolibri) {
        Platform.runLater(() -> this.elencoLibri = elencolibri);
    }

    /**
     * Mostra un messaggio di errore nella schermata principale.
     *
     * @param msgerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String msgerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setText(msgerrore);
        });
    }

}
