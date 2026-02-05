package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

// estensione di ApplicationExtension per testare JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
class HomeLettoreTest {
    // dichiarazione mock
    @Mock
    private Client mockClient;

    @Mock
    private EventoViewLettore mockEventoView;

    @Mock
    private ProfiloLettore mockProfiloLettore;

    @Mock
    private LibroDetailedView mockLibroDetailed;

    private HomeLettore view;
    private final AutoCloseable mocks; // classe per chiudere i mock dopo il test (@AfterEach) evitando consumo di memoria
    private boolean backCalled; // flag per verificare chiamata Runnable onBack

    public HomeLettoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this); // inizializza i mock tramito MockitoAnnotations che supporta AutoCloseable
        backCalled = false; // inizializza flag a false
    }

    // chiude i mock dopo ogni test
    @AfterEach
    void chiudi() throws Exception {
        mocks.close(); // chiude i mock
        backCalled = false; // resetta flag
    }

    // setup iniziale prima di ogni test
    @Start
    public void start(Stage stage) {
        // evita che il mock lanci eccezioni durante sendMessage
        try {
            doNothing().when(mockClient).sendMessage(any()); // ignora chiamate a sendMessage
        } catch (Exception ignorata) {
        }

        // crea lettore1 con eventi per popolare le tabelle
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(11, "Mario", "Verdi", "lettore1");

        lettore.setNome("Tester");

        ArrayList<Evento> creati = new ArrayList<>();
        Evento e1 = new Evento("TitoloEvento1", null, java.time.LocalDateTime.now());
        e1.setId(1);
        creati.add(e1);

        ArrayList<Evento> iscrizioni = new ArrayList<>();
        Evento e2 = new Evento("TitoloEvento2", null, java.time.LocalDateTime.now().plusDays(1));
        e2.setId(2);
        iscrizioni.add(e2);

        // imposta eventi nel lettore1 (metodi del model devono esistere)
        lettore.setEventiCreati(creati);
        lettore.setIscrizioniLettura(iscrizioni);

        // prossimi eventi (meno di 10 per non creare il bottone "Carica Eventi Successivi")
        ArrayList<Evento> prossimi = new ArrayList<>();
        Evento p1 = new Evento("Prossimo1", null, java.time.LocalDateTime.now().plusDays(2));
        p1.setId(3);
        prossimi.add(p1);
        // crea la view con i mock
        view = new HomeLettore(mockClient, mockEventoView, mockProfiloLettore, mockLibroDetailed);

        // mostra la view
        view.show(stage, lettore, prossimi, () -> backCalled = true); // imposta onBack per settare il flag a true
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che JavaFX sia pronto
    }

    // Metodo di utilità per cercare nodi con timeout per evitare problemi di sincronizzazione, restituisce un Optional contenente il nodo trovato o vuoto se non trovato
    private <T extends Node> Optional<T> trovaNodo(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis(); // tempo di inizio
        while (System.currentTimeMillis() - start < timeoutMs) { // ciclo fino al timeout
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) &&
                    predicate.test(cls.cast(n))).tryQueryAs(cls); // cerca il nodo
            if (opt.isPresent()) return opt; // se trovato, restituisci
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS); // aspetta 50ms prima di riprovare
            WaitForAsyncUtils.waitForFxEvents(); // aspetta eventi JavaFX
        }
        return Optional.empty(); // restituisci vuoto se non trovato
    }

    @Test
        // Verifica presenza pulsanti e loro azioni
    void bottoni(FxRobot robot) throws Exception {
        // Profilo lettore
        Optional<Button> profOpt = trovaNodo(robot, Button.class,
                b -> "Profilo lettore".equals(b.getText()), 1000); // cerca il pulsante Profilo lettore
        assertTrue(profOpt.isPresent(), "Pulsante 'Profilo lettore' deve essere presente");
        robot.clickOn(profOpt.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta eventi JavaFX
        verify(mockProfiloLettore, timeout(1000)).show(any(), any(), any());
        // verifica che il metodo show sia stato chiamato con qualsiasi argomento

        // Crea Nuovo Evento
        Optional<Button> newEvtOpt = trovaNodo(robot, Button.class,
                b -> "Crea Nuovo Evento".equals(b.getText()), 1000); // cerca il pulsante Crea Nuovo Evento
        assertTrue(newEvtOpt.isPresent(), "Pulsante 'Crea Nuovo Evento' deve essere presente");
        robot.clickOn(newEvtOpt.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta eventi JavaFX
        verify(mockEventoView, timeout(1000)).show(any(), any(), any(), any());
        // verifica che il metodo show sia stato chiamato con qualsiasi argomento

        // Scegli libro (presenza pulsante)
        Optional<Button> scegliLibroOpt = trovaNodo(robot, Button.class,
                b -> "Scegli libro".equals(b.getText()), 1000); // cerca il pulsante Scegli libro
        assertTrue(scegliLibroOpt.isPresent(), "Pulsante 'Scegli libro' deve essere presente");

        // Logout
        Optional<Button> logoutOpt = trovaNodo(robot, Button.class,
                b -> "Logout".equals(b.getText()), 1000); // cerca il pulsante Logout
        assertTrue(logoutOpt.isPresent(), "Pulsante 'Logout' deve essere presente");
        robot.clickOn(logoutOpt.get()); // clicca sul pulsante
        WaitForAsyncUtils.waitForFxEvents(); // aspetta eventi JavaFX
        assertTrue(backCalled, "Runnable onBack deve essere chiamato");
    }

    @Test
        // Verifica invio richiesta iniziale al client
    void richieste_iniziali(FxRobot robot) throws Exception {
        // verifica che all'avvio sia stata inviata RichiestaLettoriELuoghiELibri
        verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaLettoriELuoghiELibri.class));
    }

    @Test
        // Verifica che il doppio click su un evento apra la vista evento
    void doppio_click_rigaevento(FxRobot robot) {
        // attendi che la UI sia stabile
        WaitForAsyncUtils.waitForFxEvents();

        // cerca il nodo che contiene il testo "TitoloEvento1"
        boolean present = robot.lookup("TitoloEvento1").tryQuery().isPresent();
        assertTrue(present, "Dovrebbe esserci una cella con 'TitoloEvento1'");

        // doppio click direttamente sul testo trovato
        robot.doubleClickOn("TitoloEvento1");
        WaitForAsyncUtils.waitForFxEvents();

        // verifica che eventoView.show sia stato chiamato con qualsiasi argomento
        verify(mockEventoView, timeout(1000)).show(any(), any(), any(), any());
    }
}