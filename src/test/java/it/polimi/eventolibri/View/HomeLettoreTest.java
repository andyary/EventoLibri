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

@ExtendWith(ApplicationExtension.class)
class HomeLettoreTest {

    @Mock
    private Client mockClient;

    @Mock
    private EventoViewLettore mockEventoView;

    @Mock
    private ProfiloLettore mockProfiloLettore;

    @Mock
    private LibroDetailedView mockLibroDetailed;

    private HomeLettore view;
    private final AutoCloseable mocks;
    // per chiudere openMocks
    private boolean backCalled;

    public HomeLettoreTest() {
        this.mocks = MockitoAnnotations.openMocks(this);
        backCalled = false;
    }

    @AfterEach
    void chiudi() throws Exception {
        mocks.close();
        backCalled = false;
    }

    @Start
    public void start(Stage stage) {
        // evita che il mock lanci eccezioni durante sendMessage
        try {
            doNothing().when(mockClient).sendMessage(any());
        } catch (Exception ignored) {}

        // crea lettore1 con eventi per popolare le tabelle
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(11,"Mario", "Verdi", "lettore1");

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

        view = new HomeLettore(mockClient, mockEventoView, mockProfiloLettore, mockLibroDetailed);

        // mostra la view
        view.show(stage, lettore, prossimi, () -> backCalled = true);
        WaitForAsyncUtils.waitForFxEvents();
    }

    // Metodo di utilità per cercare nodi con retry
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
    // Verifica presenza pulsanti e loro azioni
    void bottoni_presenti_e_azioni(FxRobot robot) throws Exception {
        // Profilo lettore
        Optional<Button> profOpt = findWithRetry(robot, Button.class, b -> "Profilo lettore".equals(b.getText()), 1000);
        assertTrue(profOpt.isPresent(), "Pulsante 'Profilo lettore' deve essere presente");
        robot.clickOn(profOpt.get());
        WaitForAsyncUtils.waitForFxEvents();
        verify(mockProfiloLettore, timeout(1000)).show(any(), any(), any());

        // Crea Nuovo Evento
        Optional<Button> newEvtOpt = findWithRetry(robot, Button.class, b -> "Crea Nuovo Evento".equals(b.getText()), 1000);
        assertTrue(newEvtOpt.isPresent(), "Pulsante 'Crea Nuovo Evento' deve essere presente");
        robot.clickOn(newEvtOpt.get());
        WaitForAsyncUtils.waitForFxEvents();
        verify(mockEventoView, timeout(1000)).show(any(), any(), any(), any());

        // Scegli libro (presenza pulsante)
        Optional<Button> scegliLibroOpt = findWithRetry(robot, Button.class, b -> "Scegli libro".equals(b.getText()), 1000);
        assertTrue(scegliLibroOpt.isPresent(), "Pulsante 'Scegli libro' deve essere presente");

        // Logout
        Optional<Button> logoutOpt = findWithRetry(robot, Button.class, b -> "Logout".equals(b.getText()), 1000);
        assertTrue(logoutOpt.isPresent(), "Pulsante 'Logout' deve essere presente");
        robot.clickOn(logoutOpt.get());
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(backCalled, "Runnable onBack deve essere chiamato");
    }

    @Test
    // Verifica invio richiesta iniziale al client
    void richieste_iniziali_al_client(FxRobot robot) throws Exception {
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

        // verifica che eventoView.show sia stato chiamato
        verify(mockEventoView, timeout(1000)).show(any(), any(), any(), any());
    }

}

