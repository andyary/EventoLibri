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

@ExtendWith(ApplicationExtension.class)
class ProfiloAmministratoreTest {

    @Mock
    private Client mockClient;

    private ProfiloAmministratore view;
    private boolean backCalled;
    private final AutoCloseable mocks;

    public ProfiloAmministratoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this);
        backCalled = false;
    }

    @AfterEach
    void chiudi() throws Exception {
        backCalled = false;
        mocks.close();
    }

    @Start
    public void start(Stage stage) {
        view = new ProfiloAmministratore(mockClient);
        CreaAmministratore factory = new CreaAmministratore();
        // crea un amministratore di test
        Amministratore amministratore = factory.nuovoUtente(1, "John", "Doe", "jdoe");
        view.show(stage, amministratore, () -> backCalled = true);
        WaitForAsyncUtils.waitForFxEvents();
    }

    // metodo di utilità per cercare un nodo con timeout
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

    @Test
    // verifica che senza modifiche non venga inviato alcun messaggio
    void senza_modifiche_niente_messaggio(FxRobot robot) {
        Optional<Button> optSave = findWithRetry(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get());
        WaitForAsyncUtils.waitForFxEvents();

        try {
            verify(mockClient, never()).sendMessage(any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    // verifica che una modifica invii il messaggio di aggiornamento
    void invio_messaggio_modifica(FxRobot robot) throws Exception {
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
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaAggiornaAmministratore));
    }

    @Test
    // verifica che il pulsante Indietro chiami la runnable di back
    void bottone_indietro(FxRobot robot) {
        Optional<Button> optBack = findWithRetry(robot, Button.class, b -> "Indietro".equals(b.getText()), 1000);
        assertTrue(optBack.isPresent(), "Deve esserci il pulsante Indietro");
        robot.clickOn(optBack.get());
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(backCalled, "La runnable di back deve essere stata chiamata");
    }

    @Test
    // verifica che mostraErrore visualizzi il messaggio
    void mostra_errore(FxRobot robot) {
        // chiama mostraErrore e verifica che venga visualizzata una label con il testo
        robot.interact(() -> view.mostraErrore("Errore prova"));
        WaitForAsyncUtils.waitForFxEvents();
        // cerca la label con il messaggio di errore
        Optional<Label> optLabel = findWithRetry(robot, Label.class, l -> "Errore prova".equals(((Label) l).getText()), 1000);
        assertTrue(optLabel.isPresent(), "La label di errore deve mostrare 'Errore prova'");
    }
}
