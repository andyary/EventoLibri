package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiornaGenitore;
import it.polimi.eventolibri.Message.RichiestaAggiungiFiglio;
import it.polimi.eventolibri.Model.CreaGenitore;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

// estensione di ApplicationExtension per testare JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
class ProfiloGenitoreTest {

    @Mock
    // mock del client di rete
    private Client mockClient;
    // istanza della view da testare
    private ProfiloGenitore view;
    private boolean backCalled;
    private final AutoCloseable mocks;

    public ProfiloGenitoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this); // inizializza i mock, con MockitoAnnotations per gestire le chiusure automatiche con AutoCloseable
        backCalled = false; // inizializza la variabile di controllo per il callback di back a false
    }

    // pulisce i mock dopo ogni test
    @AfterEach
    void chiudi() throws Exception {
        backCalled = false; // resetta la variabile di controllo per il callback di back
        mocks.close(); // chiude i mock automaticamente tramite AutoCloseable
    }

    // metodo di setup eseguito prima di ogni test
    @Start
    // inizializza la view con un genitore di test
    public void start(Stage stage) {
        view = new ProfiloGenitore(mockClient);
        CreaGenitore factory = new CreaGenitore();
        // crea un genitore di test
        Genitore genitore = factory.nuovoUtente(1, "John", "Doe", "jdoe");
        view.show(stage, genitore, () -> backCalled = true); // apre la view con il genitore e una runnable di back che setta backCalled a true
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
    }

    // cerca un nodo per un massimo di timeoutMs per evitare problemi di timing nei test, restituendo un Optional del nodo trovato o vuoto se non trovato
    private <T extends Node> Optional<T> trovaNodo(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis(); // tempo di inizio
        while (System.currentTimeMillis() - start < timeoutMs) { // ciclo fino al timeout
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) &&
                    predicate.test(cls.cast(n))).tryQueryAs(cls); // cerca il nodo che soddisfa il predicato
            if (opt.isPresent()) return opt; // se trovato, restituisce l'Optional
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS); // attende 50ms prima di riprovare
            WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
        }
        return Optional.empty(); // restituisce vuoto se non trovato entro il timeout
    }

    // test che verifica che senza modifiche non venga inviato alcun messaggio
    @Test
    // verifica che senza modifiche non venga inviato alcun messaggio
    void senza_modifica(FxRobot robot) {
        // trova e clicca il pulsante "Salva modifiche"
        Optional<Button> optSave = trovaNodo(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
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
        Optional<TextField> nomeFieldOpt = trovaNodo(robot, TextField.class,
                tf -> "John".equals(((TextField) tf).getText()), 1000); // cerca il campo con il nome pre-popolato
        assertTrue(nomeFieldOpt.isPresent(), "Deve esserci il campo Nome pre-popolato con 'John'");

        // modifica il nome tramite interact
        TextField nomeField = nomeFieldOpt.get();
        robot.interact(() -> { // robot interact per eseguire sul thread FX
            nomeField.clear(); // pulisce il campo
            nomeField.setText("Johnny"); // imposta il nuovo nome
        });
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
        // clicca sul pulsante Salva modifiche
        Optional<Button> optSave = trovaNodo(robot, Button.class, b -> "Salva modifiche".equals(b.getText()), 1000);
        assertTrue(optSave.isPresent(), "Deve esserci il pulsante Salva modifiche");
        robot.clickOn(optSave.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
        // verifica che sia stato inviato il messaggio di aggiornamento
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg ->
                arg instanceof RichiestaAggiornaGenitore)); // verifica che sia stato inviato il messaggio corretto
    }

    @Test
        // test che verifica che il pulsante Indietro chiami la runnable di back
    void bottone_indietro(FxRobot robot) {
        // trova e clicca il pulsante Indietro
        Optional<Button> optBack = trovaNodo(robot, Button.class, b -> "Indietro".equals(b.getText()), 1000);
        assertTrue(optBack.isPresent(), "Deve esserci il pulsante Indietro");
        robot.clickOn(optBack.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
        // verifica che la runnable di back sia stata chiamata
        assertTrue(backCalled, "La runnable di back deve essere stata chiamata");
    }

    @Test
        // test che verifica che mostraErrore visualizzi il messaggio
    void mostra_errore(FxRobot robot) {
        // chiama mostraErrore e verifica che venga visualizzata una label con il testo
        robot.interact(() -> view.mostraErrore("Errore prova")); //robot interact per eseguire sul thread FX
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati
        // cerca la label con il messaggio di errore
        Optional<Label> optLabel = trovaNodo(robot, Label.class,
                l -> "Errore prova".equals(((Label) l).getText()), 1000); // cerca la label con il testo di errore
        assertTrue(optLabel.isPresent(), "La label di errore deve mostrare 'Errore prova'");
    }

    @Test
        // verifica che la sezione Aggiungi Figlio invii RichiestaAggiungiFiglio
    void aggiungi_figlio(FxRobot robot) throws Exception {
        // trova il TextField con prompt "Nome figlio"
        Optional<TextField> nomeFiglioOpt = trovaNodo(robot, TextField.class,
                tf -> "Nome figlio".equals(tf.getPromptText()), 1000); // cerca il campo con il prompt "Nome figlio"
        assertTrue(nomeFiglioOpt.isPresent(), "Deve esserci il campo Nome figlio");

        // trova il DatePicker con prompt "Data di nascita"
        Optional<DatePicker> datePickerOpt = trovaNodo(robot, DatePicker.class,
                dp -> "Data di nascita".equals(dp.getPromptText()), 1000); // cerca il DatePicker con il prompt "Data di nascita"
        assertTrue(datePickerOpt.isPresent(), "Deve esserci il DatePicker Data di nascita");

        // inserisce valori
        TextField nomeField = nomeFiglioOpt.get(); // ottiene il TextField
        DatePicker dp = datePickerOpt.get(); // ottiene il DatePicker
        robot.interact(() -> { // robot interact per eseguire sul thread FX
            nomeField.clear(); // pulisce il campo
            nomeField.setText("Mario"); // imposta i campi
            dp.setValue(LocalDate.of(2016, 6, 15));
        });
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati

        // clicca Aggiungi figlio
        Optional<Button> aggiungiBtn = trovaNodo(robot, Button.class,
                b -> "Aggiungi figlio".equals(b.getText()), 1000); // cerca il pulsante Aggiungi figlio
        assertTrue(aggiungiBtn.isPresent(), "Deve esserci il pulsante Aggiungi figlio"); // verifica che il pulsante esista
        clearInvocations(mockClient); // pulisce le invocazioni precedenti sul mock per evitare falsi positivi
        robot.clickOn(aggiungiBtn.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati

        // verifica invio richiesta
        verify(mockClient, timeout(1000)).sendMessage(argThat(arg -> arg instanceof RichiestaAggiungiFiglio));
    }

    @Test
        // verifica che aggiornaFigli aggiorni l'interfaccia (mostra il nuovo figlio)
    void aggiorna_figli(FxRobot robot) {
        Figlio nuovo = new Figlio("BambinoTest", LocalDate.of(2010, 5, 20));
        // chiamiamo il metodo che aggiorna l'UI sul thread FX
        robot.interact(() -> view.aggiornaFigli(nuovo)); // robot interact per eseguire sul thread FX
        WaitForAsyncUtils.waitForFxEvents(); // attende che tutti gli eventi FX siano processati

        // cerchiamo una Label che contenga il nome del nuovo figlio
        Optional<Label> labelFiglio = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("BambinoTest"), 1000); // cerca la label con il nome del nuovo figlio
        assertTrue(labelFiglio.isPresent(), "La UI deve mostrare il nuovo figlio 'BambinoTest'");
    }
}