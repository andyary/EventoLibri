package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Network.Client;
import it.polimi.eventolibri.View.HomeAmministratore;
import it.polimi.eventolibri.View.HomeGenitore;
import it.polimi.eventolibri.View.HomeLettore;
import it.polimi.eventolibri.View.RegistraNewGenitore;
import it.polimi.eventolibri.View.LoginView;
import it.polimi.eventolibri.Message.RichiestaLogin;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

import java.io.IOException;

import static org.mockito.Mockito.*;

// Test della classe LoginView utilizzando TestFX e Mockito
// Estende ApplicationTest per testare le componenti JavaFX
class LoginViewTest extends ApplicationTest {
    // Mock delle dipendenze
    private Client mockClient;
    private RegistraNewGenitore mockRegistraNewGenitore;
    private HomeGenitore mockHomeGenitore;
    private HomeLettore mockHomeLettore;
    private HomeAmministratore mockHomeAmministratore;

    private LoginView loginView;

    // Instanza di Stage per i test
    private Stage stage;

    // Configurazione iniziale prima di ogni test
    @Override
    public void start(Stage stage) {
        // Mock delle dipendenze
        mockClient = Mockito.mock(Client.class); // Mock del client di rete
        mockRegistraNewGenitore = Mockito.mock(RegistraNewGenitore.class); // Mock della view di registrazione
        mockHomeGenitore = Mockito.mock(HomeGenitore.class); // Mock della home del genitore
        mockHomeLettore = Mockito.mock(HomeLettore.class); // Mock della home del lettore
        mockHomeAmministratore = Mockito.mock(HomeAmministratore.class); // Mock della home dell'amministratore

        // Inizializzazione della classe da testare
        loginView = new LoginView(mockClient, mockRegistraNewGenitore, mockHomeGenitore, mockHomeLettore, mockHomeAmministratore);
        loginView.show(stage); // Mostra la view di login
    }

    // Test del login con campi vuoti
    @Test
    void testLoginConCampiVuoti() {
        // Simula il click sul pulsante "Login" identificato per il suo testo
        clickOn("#loginButton");

        // Aspetta che JavaFX termini l'aggiornamento dell'interfaccia
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();

        // Verifica che venga visualizzato il messaggio di errore
        Label errorLabel = lookup("#messaggioerrore").queryAs(Label.class); // lookup per trovare il Label del messaggio di errore
        FxAssert.verifyThat("#messaggioerrore", LabeledMatchers.hasText("Campi mancanti.")); // Verifica il testo del messaggio di errore
    }

    // Test del login con credenziali corrette
    @Test
    void testLoginConCredenzialiCorrette() throws IOException {
        // Inserisci credenziali valide
        clickOn("#usernameField").write("testuser"); // sfrutta l'ID per trovare il campo username
        clickOn("#passwordField").write("password");

        // Simula il click sul pulsante "Login"
        clickOn("#loginButton");

        // Verifica che il client invii il messaggio `RichiestaLogin` e quante volte viene inviato
        verify(mockClient, times(1)).sendMessage(any(RichiestaLogin.class));

        // Controlla che il messaggio di errore resti vuoto
        FxAssert.verifyThat("#messaggioerrore", LabeledMatchers.hasText(""));
    }

    // Test della registrazione di un nuovo genitore
    @Test
    void testRegistraNewGenitore() {
        // Simula il click sul pulsante "Registra nuovo genitore"
        clickOn("Registra nuovo genitore");

        // Verifica che la view `RegistraNewGenitore` abbia invocato il metodo `show`
        verify(mockRegistraNewGenitore, times(1)).show(any(Stage.class), any());
    }
}