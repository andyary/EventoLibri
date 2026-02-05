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

// estensione di ApplicationExtension per test JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
class RegistraNewAmministratoreTest {
    // mock del client di rete
    @Mock
    private Client mockClient;
    private RegistraNewAmministratore view;
    private boolean backCalled;
    private final AutoCloseable mocks; // per chiudere openMocks, final perché inizializzato nel costruttore, autocloseable per close in @AfterEach

    // Inizializza i mock QUI, ma NON creare la view (evitare uso di JavaFX nel costruttore)
    public RegistraNewAmministratoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this); // MockitoAnnotations openMocks restituisce AutoCloseable, AutoCloseable per chiudere in @AfterEach
        backCalled = false; // inizializza la variabile di controllo
    }

    // Chiude i mock dopo ogni test
    @AfterEach
    void chiudi() throws Exception {
        backCalled = false; // reset variabile di controllo
        mocks.close(); // chiude i mock (AutoCloseable)
    }

    // start viene eseguito sul thread JavaFX: istanzia la view qui
    @Start
    public void start(Stage stage) {
        // crea la view solo sul FX thread
        view = new RegistraNewAmministratore(mockClient); // istanzia la view con il mock del client
        // crea un amministratore di test
        Amministratore admin = mock(Amministratore.class);
        // configura il mock per restituire un nome
        when(admin.getNome()).thenReturn("Admin");
        // mostra la view
        view.show(stage, admin, () -> backCalled = true); // settando backCalled a true quando viene chiamato il back
        // attende che tutti gli eventi JavaFX siano processati
        WaitForAsyncUtils.waitForFxEvents();
    }

    // verifica che la view venga inizializzata correttamente

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
        // verifica che il messaggio RichiestaNuovoAmministratore sia stato inviato
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaNuovoAmministratore));
    }
}