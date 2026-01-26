package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNuovoAmministratore;
import it.polimi.eventolibri.Message.RichiestaNuovoLettore;
import it.polimi.eventolibri.Model.Amministratore;
import it.polimi.eventolibri.Model.Lettore;
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

@ExtendWith(ApplicationExtension.class)
class RegistraNewLettoreTest {

    @Mock
    // mock vari usati nei test
    private Client mockClient;
    private RegistraNewLettore view;
    private boolean backCalled;
    private final AutoCloseable mocks; // per chiudere openMocks

    // Inizializzare i mock QUI, ma SENZA creare la view (evitare uso di JavaFX nel costruttore)
    public RegistraNewLettoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this);
        backCalled = false;
    }

    @AfterEach
    // chiudi ogni test
    void chiudi() throws Exception {
        backCalled = false;
        mocks.close();
    }

    // start viene eseguito sul thread JavaFX: istanzia la view qui
    @Start
    public void start(Stage stage) {
        // crea la view solo sul FX thread (toolkit già inizializzato da TestFX)
        view = new RegistraNewLettore(mockClient);
        // crea un amministratore di test
        Amministratore admin = mock(Amministratore.class);
        // configura il mock per restituire un nome
        when(admin.getNome()).thenReturn("admin");
        // mostra la view
        view.show(stage, admin, () -> backCalled = true);
        // attende che tutti gli eventi JavaFX siano processati
        WaitForAsyncUtils.waitForFxEvents();
    }

    // Verifica che venga mostrato l'errore quando i campi sono vuoti
    @Test
    void testMostraErrore(FxRobot robot) {
        // Clicca sul pulsante salva senza compilare i campi
        robot.clickOn("#saveButton");
        // Verifica che l'etichetta di errore mostri il messaggio di campo obbligatorio
        Label error = robot.lookup("#errorLabel").queryAs(Label.class);
        assertEquals("Tutti i campi sono obbligatori.", error.getText());
    }

    // Verifica che venga inviato il messaggio corretto quando i campi sono compilati
    @Test
    void testInvioMessaggio_campiCorretti(FxRobot robot) throws Exception {
        // Compila i campi
        robot.clickOn("#nomeField").write("TestNome");
        robot.clickOn("#cognomeField").write("TestCognome");
        robot.clickOn("#usernameField").write("testuser");
        robot.clickOn("#pswField").write("password");
        robot.clickOn("#saveButton");
        // Verifica che il messaggio corretto sia stato inviato
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaNuovoLettore));
    }
}
