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

    private final Client client;
    private VBox layout;
    private Label messaggioerrore;
    private Stage stage;
    private final RegistraNewGenitore registraNewGenitore;
    private final HomeGenitore homeGenitore;
    private final HomeLettore homeLettore;
    private final HomeAmministratore homeAmministratore;

    /**
     * Costruttore della classe LoginView.
     *
     * @param client                l'istanza del client per la comunicazione con il server
     * @param registraNewGenitore   la view per la registrazione di un nuovo genitore
     * @param homeGenitore          la view della home del genitore
     * @param homeLettore           la view della home del lettore
     * @param homeAmministratore    la view della home dell'amministratore
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
        this.stage = stage;

        TextField usernameField = new TextField();
        usernameField.setPromptText("username");
        usernameField.setMaxWidth(320);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(320);

        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 22px;");
        title.setMaxWidth(320);
        title.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(160);
        loginButton.setOnAction(e -> {

            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            this.messaggioerrore.setText("");

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Campi mancanti.");

                this.messaggioerrore.setText("Campi mancanti.");

                return;
            }

            RichiestaLogin req = new RichiestaLogin(username, password);
            try {
                client.sendMessage(req);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("Login sent: " + username);
        });

        messaggioerrore = new Label("");
        messaggioerrore.setStyle("-fx-text-fill: red;");
        messaggioerrore.setMaxWidth(320);

        // ---------- REGISTRA NUOVO GENITORE BUTTON ----------
        Button newGenitoreButton = new Button("Registra nuovo genitore");
        newGenitoreButton.setMaxWidth(220);
        newGenitoreButton.setOnAction(e -> {
            System.out.println("Apertura schermata registra nuovo genitore...");
            registraNewGenitore.show(stage, () -> {
                this.show(stage);
            });

        });

        layout = new VBox(12, title, usernameField, passwordField, loginButton, messaggioerrore, newGenitoreButton);
        layout.setAlignment(Pos.TOP_CENTER); // centra orizzontalmente ma posiziona in alto
        layout.setPadding(new Insets(40, 40, 20, 40)); // margini: top, right, bottom, left

        Scene scene = new Scene(layout, 800, 750);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
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