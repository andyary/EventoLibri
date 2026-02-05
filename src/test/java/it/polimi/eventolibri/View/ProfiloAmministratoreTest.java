package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaAmministratore;
import it.polimi.eventolibri.Model.Amministratore;
import it.polimi.eventolibri.Model.CreaAmministratore;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

//estenzione di ApplicationExtension per i test JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
class ProfiloAmministratoreTest {
    // istanzia i mock
    @Mock
    private Client mockClient;

    private ProfiloAmministratore view;
    private boolean backCalled; // flag per verificare se la runnable di back è stata chiamata
    private final AutoCloseable mocks; // per chiudere i mock dopo i test, in @AfterEach

    public ProfiloAmministratoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this); // inizializza i mock tramite MockitoAnnotations per chiudere i mock dopo i test (AutoCloseable)
        backCalled = false; // inizializza il flag a false
    }

    // chiude i mock dopo ogni test
    @AfterEach
    void chiudi() throws Exception {
        backCalled = false; // resetta il flag
        mocks.close(); // chiude i mock (AutoCloseable)
    }

    // setup iniziale della scena JavaFX prima di ogni test
    @Start
    public void start(Stage stage) {
        view = new ProfiloAmministratore(mockClient); // crea la view con il client mock
        CreaAmministratore factory = new CreaAmministratore();
        // crea un amministratore di test
        Amministratore amministratore = factory.nuovoUtente(1, "John", "Doe", "jdoe");
        view.show(stage, amministratore, () -> backCalled = true); // passa una runnable che setta il flag a true
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
    }

    // metodo di utilità per cercare un nodo con timeout per evitare problemi di sincronizzazione
    private <T extends Node> Optional<T> trovaNodo(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis(); // tempo di inizio
        while (System.currentTimeMillis() - start < timeoutMs) { // ciclo fino al timeout
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) &&
                    predicate.test(cls.cast(n))).tryQueryAs(cls); // cerca il nodo
            if (opt.isPresent()) return opt; // se trovato, ritornalo
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS); // aspetta 50ms prima di riprovare
            WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        }
        return Optional.empty(); // ritorna vuoto se non trovato
    }

    @Test
        // verifica che senza modifiche non venga inviato alcun messaggio
    void senza_modifiche(FxRobot robot) {
        Optional<Button> optSave = trovaNodo(robot, Button.class,
                b -> "Salva modifiche".equals(b.getText()), 1000); // trova il pulsante Salva modifiche
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati

        try {
            verify(mockClient, never()).sendMessage(any()); // verifica che non sia stato inviato alcun messaggio
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
        // verifica che una modifica invii il messaggio di aggiornamento
    void invio_messaggio_modifica(FxRobot robot) throws Exception {
        // trova il TextField che contiene il nome iniziale "John"
        Optional<TextField> nomeFieldOpt = trovaNodo(robot, TextField.class,
                tf -> "John".equals(((TextField) tf).getText()), 1000); // cerca il campo Nome popolato
        assertTrue(nomeFieldOpt.isPresent(), "Deve esserci il campo Nome pre-popolato con 'John'");

        // modifica il nome tramite interact
        TextField nomeField = nomeFieldOpt.get();
        robot.interact(() -> { // usa interact per modificare il campo in modo thread-safe
            nomeField.clear(); // pulisce il campo
            nomeField.setText("Johnny"); // imposta il nuovo testo
        });
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // clicca sul pulsante Salva modifiche
        Optional<Button> optSave = trovaNodo(robot, Button.class,
                b -> "Salva modifiche".equals(b.getText()), 1000); // trova il pulsante Salva modifiche
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // verifica che sia stato inviato il messaggio di aggiornamento
        verify(mockClient, timeout(1000)).sendMessage(argThat(
                arg -> arg instanceof RichiestaAggiornaAmministratore));
    }

    @Test
        // verifica che il pulsante Indietro chiami la runnable di back
    void bottone_indietro(FxRobot robot) {
        Optional<Button> optBack = trovaNodo(robot, Button.class,
                b -> "Indietro".equals(b.getText()), 1000); // trova il pulsante Indietro
        assertTrue(optBack.isPresent(), "Deve esserci il pulsante Indietro");
        robot.clickOn(optBack.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // verifica che la runnable di back sia stata chiamata
        assertTrue(backCalled, "La runnable di back deve essere stata chiamata");
    }

    @Test
        // verifica che mostraErrore visualizzi il messaggio
    void mostra_errore(FxRobot robot) {
        // chiama mostraErrore e verifica che venga visualizzata una label con il testo
        robot.interact(() -> view.mostraErrore("Errore prova")); // robot.interact per chiamare il metodo in modo thread-safe
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // cerca la label con il messaggio di errore
        Optional<Label> optLabel = trovaNodo(robot, Label.class,
                l -> "Errore prova".equals(((Label) l).getText()), 1000); // cerca la label con il testo di errore
        assertTrue(optLabel.isPresent(), "La label di errore deve mostrare 'Errore prova'");
    }
}