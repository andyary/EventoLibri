package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNuovoAmministratore;
import it.polimi.eventolibri.Model.Amministratore;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class RegistraNewAmministratoreTest {

    @Mock
    private Client mockClient;
    private RegistraNewAmministratore view;
    private boolean backCalled;
    private final AutoCloseable mocks; // per chiudere openMocks

    // Inizializza i mock QUI, ma NON creare la view (evita uso di JavaFX nel costruttore)
    public RegistraNewAmministratoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this);
        backCalled = false;
    }

    @AfterEach
    void chiudi() throws Exception {
        backCalled = false;
        mocks.close();
    }

    // start viene eseguito sul thread JavaFX: istanzia la view qui
    @Start
    public void start(Stage stage) {
        // crea la view solo sul FX thread (toolkit già inizializzato da TestFX)
        view = new RegistraNewAmministratore(mockClient);
        // crea un amministratore di test
        Amministratore admin = mock(Amministratore.class);
        // configura il mock per restituire un nome
        when(admin.getNome()).thenReturn("Admin");
        // mostra la view
        view.show(stage, admin, () -> backCalled = true);
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
    void testInvioMessaggio_campiCorretti(FxRobot robot) throws Exception {
        // compila i campi
        robot.clickOn("#nomeField").write("TestNome");
        robot.clickOn("#cognomeField").write("TestCognome");
        robot.clickOn("#usernameField").write("testuser");
        robot.clickOn("#pswField").write("password");
        robot.clickOn("#saveButton");
        // verifica che il messaggio RichiestaNuovoAmministratore sia stato inviato
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaNuovoAmministratore));
    }
}
