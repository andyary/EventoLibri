package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNuovoGenitore;
import it.polimi.eventolibri.Message.RichiestaNuovoLettore;
import it.polimi.eventolibri.Model.Amministratore;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// estende ApplicationExtension per testare componenti JavaFX
@ExtendWith(ApplicationExtension.class)
class RegistraNewGenitoreTest {
    // crea i mock con Mockito
    @Mock
    // mock usati nei test
    private Client mockClient;
    private RegistraNewGenitore view;
    private boolean backCalled;
    private final AutoCloseable mocks; // per chiudere openMocks

    // inizializza i mock prima di ogni test
    public RegistraNewGenitoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this);
        backCalled = false; // inizializza la variabile di controllo, usata per verificare se il callback "back" è stato chiamato
    }

    // chiude i mock dopo ogni test
    @AfterEach
    void chiudi() throws Exception {
        backCalled = false; // resetta la variabile di controllo
        mocks.close(); // chiude i mock
    }

    // start viene eseguito sul thread JavaFX: istanzia la view qui
    @Start
    public void start(Stage stage) {
        // crea la view solo sul FX thread (toolkit già inizializzato da TestFX)
        view = new RegistraNewGenitore(mockClient);
        // mostra la view
        view.show(stage, () -> backCalled = true); // passa un callback che setta backCalled a true
        // attende che tutti gli eventi JavaFX siano processati
        WaitForAsyncUtils.waitForFxEvents();
    }


    @Test
        // verifica che venga mostrato l'errore quando i campi sono vuoti
    void testMostraErrore(FxRobot robot) {
        // clicca sul pulsante salva senza compilare i campi
        robot.clickOn("#saveButton");
        // verifica che l'etichetta di errore mostri il messaggio di campo obbligatorio
        Label error = robot.lookup("#errorLabel").queryAs(Label.class);
        assertEquals("Tutti i campi sono obbligatori.", error.getText());
    }

    @Test
        // verifica che venga inviato il messaggio corretto quando i campi sono compilati
    void testInvioMessaggio(FxRobot robot) throws Exception {
        // compila i campi
        robot.clickOn("#nomeField").write("TestNome");
        robot.clickOn("#cognomeField").write("TestCognome");
        robot.clickOn("#usernameField").write("testuser");
        robot.clickOn("#pswField").write("password");
        robot.clickOn("#saveButton");
        // verifica che il messaggio corretto sia stato inviato
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaNuovoGenitore));
    }
}