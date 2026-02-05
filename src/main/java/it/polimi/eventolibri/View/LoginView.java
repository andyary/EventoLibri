package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import it.polimi.eventolibri.Message.RichiestaLogin;

import java.io.IOException;

/**
 * Classe View per la schermata di login.
 */
public class LoginView {
    // ----- ATTRIBUTI -----
    private final Client client;
    private VBox layout;
    private Label messaggioerrore;
    private Stage stage;
    private final RegistraNewGenitore registraNewGenitore;
    private final HomeGenitore homeGenitore;
    private final HomeLettore homeLettore;
    private final HomeAmministratore homeAmministratore;

    // ----- COSTRUTTORE -----

    /**
     * Costruttore della classe LoginView.
     *
     * @param client              l'istanza del client per la comunicazione con il server
     * @param registraNewGenitore la view per la registrazione di un nuovo genitore
     * @param homeGenitore        la view della home del genitore
     * @param homeLettore         la view della home del lettore
     * @param homeAmministratore  la view della home dell'amministratore
     */
    public LoginView(Client client, RegistraNewGenitore registraNewGenitore, HomeGenitore homeGenitore, HomeLettore homeLettore, HomeAmministratore homeAmministratore) {
        this.registraNewGenitore = registraNewGenitore;
        this.homeGenitore = homeGenitore;
        this.homeLettore = homeLettore;
        this.homeAmministratore = homeAmministratore;
        this.client = client;
    }

    /**
     * Mostra la schermata di login.
     *
     * @param stage lo stage principale dell'applicazione
     */
    public void show(Stage stage) {
        // Salva lo stage
        this.stage = stage;
        // ---------- USERNAME E PASSWORD FIELDS ----------
        TextField usernameField = new TextField();
        usernameField.setPromptText("username");
        usernameField.setId("usernameField"); // per i test
        usernameField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setId("passwordField"); // per i test
        passwordField.setMaxWidth(320);
        // ---------- LOGIN BUTTON ----------
        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 22px;");
        title.setMaxWidth(320);
        title.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setId("loginButton");
        loginButton.setMaxWidth(160);
        loginButton.setOnAction(e -> {
            // Ottieni username e password dai campi di testo
            String username = usernameField.getText().trim(); // Rimuovi spazi bianchi iniziali e finali
            String password = passwordField.getText().trim(); // Rimuovi spazi bianchi iniziali e finali
            this.messaggioerrore.setText(""); // Pulisci messaggio di errore precedente
            System.out.println("Username: " + username + ", Password: " + password); // Debug
            if (username.isEmpty() || password.isEmpty()) { // Controlla campi vuoti
                System.out.println("Campi mancanti."); // Debug
                this.messaggioerrore.setText("Campi mancanti."); // Mostra messaggio di errore
                return; // Esci
            }
            // Crea e invia la richiesta di login al server
            RichiestaLogin req = new RichiestaLogin(username, password);
            try {
                client.sendMessage(req);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("Login sent: " + username); // Debug
        });
        // ---------- MESSAGGIO ERRORE ----------
        messaggioerrore = new Label("");
        messaggioerrore.setStyle("-fx-text-fill: red;");
        messaggioerrore.setMaxWidth(320);
        messaggioerrore.setId("messaggioerrore");

        // ---------- REGISTRA NUOVO GENITORE BUTTON ----------
        Button newGenitoreButton = new Button("Registra nuovo genitore");
        newGenitoreButton.setMaxWidth(220);
        newGenitoreButton.setOnAction(e -> {
            System.out.println("Apertura schermata registra nuovo genitore...");
            registraNewGenitore.show(stage, () -> {
                this.show(stage); // Torna alla schermata di login dopo la registrazione
            });

        });
        // ---------- LAYOUT ----------
        layout = new VBox(12, title, usernameField, passwordField, loginButton, messaggioerrore, newGenitoreButton);
        layout.setAlignment(Pos.TOP_CENTER); // centra orizzontalmente ma posiziona in alto
        layout.setPadding(new Insets(40, 40, 20, 40)); // margini: top, right, bottom, left

        Scene scene = new Scene(layout, 750, 780);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show(); // mostra la finestra
    }

    /**
     * Mostra un messaggio di errore nella schermata di login.
     *
     * @param messaggioerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String messaggioerrore) {
        Platform.runLater(() -> {
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

    /**
     * Restituisce lo stage associato alla view.
     *
     * @return lo stage
     */
    public Stage getStage() {
        return stage;
    }

}