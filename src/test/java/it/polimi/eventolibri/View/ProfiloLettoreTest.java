package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaLettore;
import it.polimi.eventolibri.Model.CreaLettore;
import it.polimi.eventolibri.Model.Lettore;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

// estensione di ApplicationExtension per testare applicazioni JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
class ProfiloLettoreTest {

    @Mock
    private Client mockClient;

    private ProfiloLettore view;
    private boolean backCalled;
    private final AutoCloseable mocks; // classe per gestire il ciclo di vita dei mock in modo automatico

    public ProfiloLettoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this); // inizializza i mock tramite MockitoAnnotations che gestisce automaticamente il ciclo di vita dei mock (AutoCloseable)
        backCalled = false; // inizializza la variabile di stato
    }

    // pulisce le risorse dopo ogni test
    @AfterEach
    void tearDown() throws Exception {
        backCalled = false; // resetta la variabile di stato
        mocks.close(); // chiude i mock
    }

    // metodo eseguito prima di ogni test per inizializzare la scena
    @Start
    public void start(Stage stage) {
        view = new ProfiloLettore(mockClient);
        CreaLettore factory = new CreaLettore();
        // crea un lettore di test
        Lettore lettore = factory.nuovoUtente(1, "John", "Doe", "jdoe");
        view.show(stage, lettore, () -> backCalled = true); // mostra la vista con la runnable di back che imposta backCalled a true
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che tutti gli eventi FX siano processati
    }

    // metodo di utilità per trovare nodi con timeout necessario per evitare problemi di sincronizzazione
    // restituisce Optional.empty() se non trovato, altrimenti Optional con il nodo
    private <T extends Node> Optional<T> trovaNodo(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis(); // tempo di inizio
        while (System.currentTimeMillis() - start < timeoutMs) { // finché non scade il timeout
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) && predicate.test(cls.cast(n))).tryQueryAs(cls); // cerca il nodo
            if (opt.isPresent()) return opt; // se trovato, restituiscilo
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS); // aspetta 50 ms prima di riprovare
            WaitForAsyncUtils.waitForFxEvents(); // assicurati che gli eventi FX siano processati
        }
        return Optional.empty(); // se non trovato entro il timeout, restituisci vuoto
    }

    @Test
        // verifica che senza modifiche non venga inviato alcun messaggio
    void senza_modifiche(FxRobot robot) {
        // trova il pulsante "Salva modifiche" e cliccalo
        Optional<Button> optSave = trovaNodo(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get());
        WaitForAsyncUtils.waitForFxEvents();
        // Try-Catch per gestire l'eccezione IOException
        try {
            verify(mockClient, never()).sendMessage(any()); // nessun messaggio deve essere inviato
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
        // verifica che venga inviato RichiestaAggiornaLettore dopo una modifica
    void invia_messaggio_modifica(FxRobot robot) throws Exception {
        // trova il TextField che contiene il nome iniziale "John"
        Optional<TextField> nomeFieldOpt = trovaNodo(robot, TextField.class,
                tf -> "John".equals(((TextField) tf).getText()), 1000); // cerca il TextField con testo "John"
        assertTrue(nomeFieldOpt.isPresent(), "Deve esserci il campo Nome riempito con 'John'");

        // modifica il nome tramite interact
        TextField nomeField = nomeFieldOpt.get();
        robot.interact(() -> { // robot.interact per eseguire l'operazione sul thread JavaFX
            nomeField.clear(); // cancella il testo esistente
            nomeField.setText("Johnny"); // imposta il nuovo testo
        });
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // clicca sul pulsante Salva modifiche
        Optional<Button> optSave = trovaNodo(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // verifica che sia stato inviato il messaggio di aggiornamento
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaAggiornaLettore));
    }

    @Test
        // verifica che il pulsante Indietro chiami la runnable di back
    void bottone_indietro(FxRobot robot) {
        // trova il pulsante Indietro e cliccalo
        Optional<Button> optBack = trovaNodo(robot, Button.class, b -> "Indietro".equals(b.getText()), 1000);
        assertTrue(optBack.isPresent(), "Deve esserci il pulsante Indietro");
        robot.clickOn(optBack.get());
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che la runnable di back sia stata chiamata
        assertTrue(backCalled, "La runnable di back deve essere stata chiamata");
    }

    @Test
        // verifica che mostraErrore visualizzi il messaggio
    void mostra_errore(FxRobot robot) {
        // chiama mostraErrore e verifica che venga visualizzata una label con il testo
        robot.interact(() -> view.mostraErrore("Errore prova"));
        WaitForAsyncUtils.waitForFxEvents();
        // cerca la label con il messaggio di errore
        Optional<Label> optLabel = trovaNodo(robot, Label.class, l -> "Errore prova".equals(((Label) l).getText()), 1000);
        assertTrue(optLabel.isPresent(), "La label di errore deve mostrare 'Errore prova'");
    }
}