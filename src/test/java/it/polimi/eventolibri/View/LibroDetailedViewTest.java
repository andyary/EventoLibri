package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaAggiungiRecensione;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/*
  Test per la view LibroDetailedView.
  - metodo di supporto waitForNode(...) per attendere la comparsa dei nodi UI
  - avvio unblocker prima del click su Elimina
*/

// estensione di TestFX
@ExtendWith(ApplicationExtension.class)
class LibroDetailedViewTest {

    // mock client per intercettare sendMessage
    private Client mockClient;

    // view sotto test
    private LibroDetailedView view;

    // stage owner fornito da TestFX
    private Stage ownerStage;

    // setup iniziale prima di ogni test
    @Start
    public void start(Stage stage) {
        this.ownerStage = stage;
        this.mockClient = mock(Client.class);
        this.view = new LibroDetailedView(mockClient);
    }

    // metodo di supporto per eseguire codice sulla FX Application Thread e attendere il completamento
    // con timeout per evitare deadlock nei test
    // necessario per interagire con la UI in modo sincrono nei test
    private void lanciaEAttendi(Runnable azione) {
        // se siamo già sulla FX thread, esegui direttamente
        if (Platform.isFxApplicationThread()) {
            azione.run(); // esegui l'azione
            return;
        }
        // altrimenti usa un latch per attendere il completamento
        CountDownLatch latch = new CountDownLatch(1); // latch per attendere il completamento
        Platform.runLater(() -> {
            try {
                azione.run(); // esegui l'azione
            } finally {
                latch.countDown(); // segnala il completamento
            }
        });
        // attendi il completamento con timeout
        try {
            // attendi fino a 5 secondi, lancia eccezione se scade il timeout
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout in attesa del completamento dell'azione sulla FX Application Thread");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // ripristina lo stato di interruzione
            throw new RuntimeException(e); // rilancia come RuntimeException
        }
    }

    // helper che attende la presenza di un nodo fino al timeout, timeout necessario per evitare deadlock
    private boolean attendiNodo(FxRobot robot, String query, long timeoutMillis) {
        long end = System.currentTimeMillis() + timeoutMillis; // calcola il tempo di fine attesa
        while (System.currentTimeMillis() < end) { // finché non scade il timeout
            try { // prova a cercare il nodo
                if (robot.lookup(query).tryQuery().isPresent()) return true; // nodo trovato
            } catch (Exception exception) {
            }
            try {
                Thread.sleep(50); // breve pausa prima del prossimo tentativo
            } catch (InterruptedException e) { // gestisci l'interruzione
                Thread.currentThread().interrupt(); // ripristina lo stato di interruzione
                return false; // esci se interrotto senza trovare il nodo
            }
        }
        return false; // timeout scaduto, nodo non trovato
    }

    // pulizia dopo ogni test
    @AfterEach
    void pulizia() {
        // chiudi tutte le finestre aperte dopo ogni test
        lanciaEAttendi(() -> {
            ArrayList<Window> windows = new ArrayList<>(Window.getWindows()); // copia delle finestre aperte
            for (Window w : windows) { // per ogni finestra
                if (w instanceof Stage) ((Stage) w).close(); // chiudi lo stage
            }
        });
        clearInvocations(mockClient); // pulisci le invocazioni del mock client a fine test
        WaitForAsyncUtils.waitForFxEvents(); // attendi che tutti gli eventi FX siano processati
    }

    // test che verifica la visualizzazione dei dettagli del libro e del pulsante "Vedi recensioni"
    @Test
    void mostra_dettagli_e_pulsante_vedi_recensioni(FxRobot robot) {
        Libro mockLibro = mock(Libro.class); // crea un mock di Libro
        when(mockLibro.getTitolo()).thenReturn("TitoloTest");
        when(mockLibro.getAutore()).thenReturn("AutoreTest");
        when(mockLibro.getTempoLettura()).thenReturn(42);
        when(mockLibro.getIsbn()).thenReturn("ISBN123");
        when(mockLibro.getLink()).thenReturn("http://example.com");
        // mostra la view con il libro mock
        lanciaEAttendi(() -> view.show(ownerStage, mockLibro, new ArrayList<>(),
                mock(Genitore.class), false, null)); // mostra la view
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati
        // verifica che i dettagli del libro siano presenti
        boolean titoloPresente = robot.lookup((Node n) -> (n instanceof Labeled) && "Titolo: TitoloTest".equals(((Labeled) n).getText()))
                .tryQuery().isPresent(); // cerca il label del titolo
        // verifica la presenza del pulsante "Vedi recensioni"
        assertTrue(titoloPresente, "La label con il titolo del libro deve essere presente");
        assertTrue(robot.lookup("Vedi recensioni").tryQuery().isPresent(), "Il pulsante 'Vedi recensioni' deve essere presente");
    }

    // test che verifica il messaggio mostrato quando non ci sono recensioni
    @Test
    void vedi_recensioni_vuote(FxRobot robot) {
        Libro mockLibro = mock(Libro.class); // crea un mock di Libro
        when(mockLibro.getTitolo()).thenReturn("T");
        // mostra la view con il libro mock e nessuna recensione
        lanciaEAttendi(() -> view.show(ownerStage, mockLibro, new ArrayList<>(),
                mock(Genitore.class), false, null)); // mostra la view
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati

        // apri la finestra recensioni e aspetta che il label sia presente
        robot.clickOn("Vedi recensioni"); // clicca sul pulsante per vedere le recensioni
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati
        // verifica che il messaggio "Nessuna recensione disponibile" sia mostrato
        boolean present = attendiNodo(robot, "Nessuna recensione disponibile", 2000);
        assertTrue(present, "Deve comparire 'Nessuna recensione disponibile' quando non ci sono recensioni");
    }

    // test che verifica che il pulsante "Elimina" non sia mostrato per recensioni senza permessi di delete
    @Test
    void recensioni_senza_delete(FxRobot robot) {
        Recensione r = mock(Recensione.class); // crea un mock di Recensione
        when(r.getTesto()).thenReturn("Buon libro");
        when(r.getGenitore()).thenReturn(mock(Genitore.class)); // mock del genitore senza permessi di cancellazione
        Libro mockLibro = mock(Libro.class);
        // mostra la view con il libro mock e una recensione
        lanciaEAttendi(() -> view.show(ownerStage, mockLibro, new ArrayList<>(Arrays.asList(r)),
                mock(Genitore.class), false, null)); // mostra la view passando una recensione senza permessi di delete
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati

        robot.clickOn("Vedi recensioni"); // clicca sul pulsante per vedere le recensioni
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati

        boolean present = attendiNodo(robot, "Elimina", 800); // cerca il pulsante "Elimina" con timeout
        assertFalse(present, "Non deve essere presente il pulsante 'Elimina' per utente non admin"); // verifica che il pulsante "Elimina" non sia presente
    }

    // test che verifica l'invio della richiesta di aggiunta recensione
    @Test
    void aggiungi_recensione(FxRobot robot) {
        // crea un mock di Genitore e Libro
        Genitore gen = mock(Genitore.class);
        Libro mockLibro = mock(Libro.class);
        // mostra la view con il libro mock e il genitore
        lanciaEAttendi(() -> view.show(ownerStage, mockLibro, new ArrayList<>(), gen, true,
                null)); // mostra la view
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati
        // verifica la presenza del pulsante "Aggiungi recensione"
        assertTrue(robot.lookup("Aggiungi recensione").tryQuery().isPresent(), "Dovrebbe essere presente 'Aggiungi recensione'");
        // clicca su "Aggiungi recensione"
        robot.clickOn("Aggiungi recensione");
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati
        // verifica la presenza della TextArea e del pulsante "Invia"
        TextArea area = robot.lookup(".text-area").queryAs(TextArea.class);
        assertNotNull(area, "Dovrebbe esserci una TextArea per la recensione");
        // verifica che il pulsante "Invia" sia disabilitato inizialmente
        Button invia = robot.lookup("Invia").queryButton();
        assertTrue(invia.isDisabled(), "Il pulsante 'Invia' deve essere disabilitato prima di confermare e scrivere");
        // scrivi la recensione e conferma
        robot.clickOn(area).write("Recensione di prova");
        robot.clickOn("Confermo la recensione"); // clicca sulla checkbox di conferma
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati
        // verifica che il pulsante "Invia" sia ora abilitato
        invia = robot.lookup("Invia").queryButton();
        assertFalse(invia.isDisabled(), "Il pulsante 'Invia' deve essere abilitato dopo testo e conferma");
        // reset delle invocazioni del mock client
        clearInvocations(mockClient); // necessario per evitare interferenze da altre chiamate
        robot.clickOn("Invia"); // clicca su "Invia"
        WaitForAsyncUtils.waitForFxEvents(); // attendi che gli eventi FX siano processati
        // uso del try-catch per verificare l'invio della richiesta, necessario per gestire le eccezioni che potrebbero verificarsi
        try {
            // verifica che sia stata inviata una RichiestaAggiungiRecensione
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaAggiungiRecensione.class));
        } catch (Exception e) {
            fail("Non è stata inviata RichiestaAggiungiRecensione: " + e.getMessage());
        }
    }
}