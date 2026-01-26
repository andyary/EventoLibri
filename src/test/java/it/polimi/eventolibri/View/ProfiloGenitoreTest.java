package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaGenitore;
import it.polimi.eventolibri.Model.CreaGenitore;
import it.polimi.eventolibri.Model.Genitore;
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

@ExtendWith(ApplicationExtension.class)
class ProfiloGenitoreTest {

    @Mock
    private Client mockClient;

    private ProfiloGenitore view;
    private boolean backCalled;
    private final AutoCloseable mocks;

    public ProfiloGenitoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this);
        backCalled = false;
    }

    @AfterEach
    void chiudi() throws Exception {
        backCalled = false;
        mocks.close();
    }

    @Start
    // inizializza la view con un genitore di test
    public void start(Stage stage) {
        view = new ProfiloGenitore(mockClient);
        CreaGenitore factory = new CreaGenitore();
        // crea un genitore di test
        Genitore genitore = factory.nuovoUtente(1, "John", "Doe", "jdoe");
        view.show(stage, genitore, () -> backCalled = true);
        WaitForAsyncUtils.waitForFxEvents();
    }
    // cerca un nodo per un massimo di timeoutMs
    private <T extends Node> Optional<T> findWithRetry(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) && predicate.test(cls.cast(n))).tryQueryAs(cls);
            if (opt.isPresent()) return opt;
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
            WaitForAsyncUtils.waitForFxEvents();
        }
        return Optional.empty();
    }
    // test che verifica che senza modifiche non venga inviato alcun messaggio
    @Test
    // verifica che senza modifiche non venga inviato alcun messaggio
    void senza_modifica_niente_messaggio(FxRobot robot) {
        // trova e clicca il pulsante "Salva modifiche"
        Optional<Button> optSave = findWithRetry(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get());
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che non sia stato inviato alcun messaggio al client
        try {
            verify(mockClient, never()).sendMessage(any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    // test che verifica che venga inviato RichiestaAggiornaGenitore dopo una modifica
    void invia_messaggio_modifica(FxRobot robot) throws Exception {
        // trova il TextField che contiene il nome iniziale "John"
        Optional<TextField> nomeFieldOpt = findWithRetry(robot, TextField.class,
                tf -> "John".equals(((TextField) tf).getText()), 1000);
        assertTrue(nomeFieldOpt.isPresent(), "Deve esserci il campo Nome pre-popolato con 'John'");

        // modifica il nome tramite interact
        TextField nomeField = nomeFieldOpt.get();
        robot.interact(() -> {
            nomeField.clear();
            nomeField.setText("Johnny");
        });
        WaitForAsyncUtils.waitForFxEvents();
        // clicca sul pulsante Salva modifiche
        Optional<Button> optSave = findWithRetry(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get());
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che sia stato inviato il messaggio di aggiornamento
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaAggiornaGenitore));
    }

    @Test
    // test che verifica che il pulsante Indietro chiami la runnable di back
    void bottone_indietro(FxRobot robot) {
        // trova e clicca il pulsante Indietro
        Optional<Button> optBack = findWithRetry(robot, Button.class, b -> "Indietro".equals(b.getText()), 1000);
        assertTrue(optBack.isPresent(), "Deve esserci il pulsante Indietro");
        robot.clickOn(optBack.get());
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che la runnable di back sia stata chiamata
        assertTrue(backCalled, "La runnable di back deve essere stata chiamata");
    }

    @Test
    // test che verifica che mostraErrore visualizzi il messaggio
    void mostra_errore(FxRobot robot) {
        // chiama mostraErrore e verifica che venga visualizzata una label con il testo
        robot.interact(() -> view.mostraErrore("Errore prova"));
        WaitForAsyncUtils.waitForFxEvents();
        // cerca la label con il messaggio di errore
        Optional<Label> optLabel = findWithRetry(robot, Label.class, l -> "Errore prova".equals(((Label) l).getText()), 1000);
        assertTrue(optLabel.isPresent(), "La label di errore deve mostrare 'Errore prova'");
    }
}
