package it.polimi.eventolibri.View;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
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

public class LoginView {

    private final Client client;
    private VBox layout;
    private Label messaggioerrore;

    public LoginView(Client client) {
        this.client = client;
    }

    public void show(Stage stage) {

        TextField usernameField = new TextField();
        usernameField.setPromptText("username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label title = new Label("Login");
        title.setStyle("-fx-font-size: 22px;");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Missing fields");
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

        layout = new VBox(15, title, usernameField, passwordField, loginButton, messaggioerrore);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 350, 250);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    public void mostraErrore(String messaggioerrore) {
              Platform.runLater(()->{
           this.messaggioerrore.setText(messaggioerrore);
       });
    }
}