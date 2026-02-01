package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiungiRecensione;
import it.polimi.eventolibri.Message.RichiestaCancellaRecensione;
import it.polimi.eventolibri.Model.Amministratore;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Recensione;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Labeled;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/*
  Test per la view LibroDetailedView.
  - helper waitForNode(...) per attendere la comparsa dei nodi UI
  - avvio unblocker prima del click su Elimina
*/
@ExtendWith(ApplicationExtension.class)
class LibroDetailedViewTest {

    // mock client per intercettare sendMessage
    private Client mockClient;

    // view sotto test
    private LibroDetailedView view;

    // stage owner fornito da TestFX
    private Stage ownerStage;

    @Start
    public void start(Stage stage) {
        this.ownerStage = stage;
        this.mockClient = mock(Client.class);
        this.view = new LibroDetailedView(mockClient);
    }

    private void runAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for FX task");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // helper che attende la presenza di un nodo (lookup per testo/selector) fino al timeout
    private boolean waitForNode(FxRobot robot, String query, long timeoutMillis) {
        long end = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < end) {
            try {
                if (robot.lookup(query).tryQuery().isPresent()) return true;
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @AfterEach
    void tearDown() {
        runAndWait(() -> {
            ArrayList<Window> windows = new ArrayList<>(Window.getWindows());
            for (Window w : windows) {
                if (w instanceof Stage) ((Stage) w).close();
            }
        });
        clearInvocations(mockClient);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void mostra_dettagli_e_pulsante_vedi_recensioni(FxRobot robot) {
        Libro mockLibro = mock(Libro.class);
        when(mockLibro.getTitolo()).thenReturn("TitoloTest");
        when(mockLibro.getAutore()).thenReturn("AutoreTest");
        when(mockLibro.getTempoLettura()).thenReturn(42);
        when(mockLibro.getIsbn()).thenReturn("ISBN123");
        when(mockLibro.getLink()).thenReturn("http://example.com");

        runAndWait(() -> view.show(ownerStage, mockLibro, new ArrayList<>(), mock(Genitore.class), false, null));
        WaitForAsyncUtils.waitForFxEvents();

        boolean titoloPresente = robot.lookup((Node n) -> (n instanceof Labeled) && "Titolo: TitoloTest".equals(((Labeled) n).getText()))
                .tryQuery().isPresent();
        assertTrue(titoloPresente, "La label con il titolo del libro deve essere presente");
        assertTrue(robot.lookup("Vedi recensioni").tryQuery().isPresent(), "Il pulsante 'Vedi recensioni' deve essere presente");
    }

    @Test
    void vedi_recensioni_vuote_mostra_messaggio(FxRobot robot) {
        Libro mockLibro = mock(Libro.class);
        when(mockLibro.getTitolo()).thenReturn("T");

        runAndWait(() -> view.show(ownerStage, mockLibro, new ArrayList<>(), mock(Genitore.class), false, null));
        WaitForAsyncUtils.waitForFxEvents();

        // apri la finestra recensioni e aspetta che il label sia presente
        robot.clickOn("Vedi recensioni");
        WaitForAsyncUtils.waitForFxEvents();

        boolean present = waitForNode(robot, "Nessuna recensione disponibile", 2000);
        assertTrue(present, "Deve comparire 'Nessuna recensione disponibile' quando non ci sono recensioni");
    }

    @Test
    void recensioni_senza_delete_non_mostra_elimina(FxRobot robot) {
        Recensione r = mock(Recensione.class);
        when(r.getTesto()).thenReturn("Buon libro");
        when(r.getGenitore()).thenReturn(mock(Genitore.class));

        Libro mockLibro = mock(Libro.class);
        runAndWait(() -> view.show(ownerStage, mockLibro, new ArrayList<>(Arrays.asList(r)), mock(Genitore.class), false, null));
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Vedi recensioni");
        WaitForAsyncUtils.waitForFxEvents();

        boolean present = waitForNode(robot, "Elimina", 800);
        assertFalse(present, "Non deve essere presente il pulsante 'Elimina' per utente non admin");
    }

    @Test
    void aggiungi_recensione_dialog_invia_richiesta(FxRobot robot) {
        Genitore gen = mock(Genitore.class);
        Libro mockLibro = mock(Libro.class);

        runAndWait(() -> view.show(ownerStage, mockLibro, new ArrayList<>(), gen, true, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(robot.lookup("Aggiungi recensione").tryQuery().isPresent(), "Dovrebbe essere presente 'Aggiungi recensione'");

        robot.clickOn("Aggiungi recensione");
        WaitForAsyncUtils.waitForFxEvents();

        TextArea area = robot.lookup(".text-area").queryAs(TextArea.class);
        assertNotNull(area, "Dovrebbe esserci una TextArea per la recensione");

        Button invia = robot.lookup("Invia").queryButton();
        assertTrue(invia.isDisabled(), "Il pulsante 'Invia' deve essere disabilitato prima di confermare e scrivere");

        robot.clickOn(area).write("Recensione di prova");
        robot.clickOn("Confermo la recensione");
        WaitForAsyncUtils.waitForFxEvents();

        invia = robot.lookup("Invia").queryButton();
        assertFalse(invia.isDisabled(), "Il pulsante 'Invia' deve essere abilitato dopo testo e conferma");

        clearInvocations(mockClient);
        robot.clickOn("Invia");
        WaitForAsyncUtils.waitForFxEvents();

        try {
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaAggiungiRecensione.class));
        } catch (Exception e) {
            fail("Non è stata inviata RichiestaAggiungiRecensione: " + e.getMessage());
        }
    }
}