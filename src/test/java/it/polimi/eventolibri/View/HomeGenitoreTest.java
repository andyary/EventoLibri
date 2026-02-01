package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/**
 * Test per la view HomeGenitore.
 * Struttura analoga a HomeLettoreTest: setup in @Start, mocks, e verifiche principali.
 */
@ExtendWith(ApplicationExtension.class)
class HomeGenitoreTest {

    private Client mockClient;
    private EventoView mockEventoView;
    private ProfiloGenitore mockProfiloGenitore;
    private LibroDetailedView mockLibroDetailedView;
    private HomeGenitore view;
    private Stage stage;

    @Start
    public void start(Stage stage) throws Exception {
        // crea mocks
        this.mockClient = mock(Client.class);
        this.mockEventoView = mock(EventoView.class);
        this.mockProfiloGenitore = mock(ProfiloGenitore.class);
        this.mockLibroDetailedView = mock(LibroDetailedView.class);

        this.stage = stage;

        // crea un evento di esempio per il figlio
        Evento eventoFiglio = new Evento("TitoloFiglioEvento", null, LocalDateTime.now().plusDays(1));
        // lista iscrizioni figlio: usa new ArrayList<>(Arrays.asList(...)) per evitare ClassCastException
        ArrayList<Evento> iscrizioniFiglio = new ArrayList<>(Arrays.asList(eventoFiglio));

        // figlio mock con iscrizioni
        Figlio figlio = mock(Figlio.class);
        when(figlio.getNome()).thenReturn("Figlio1");
        when(figlio.getIscrizioni()).thenReturn(iscrizioniFiglio);

        // genitore mock con lista figli
        Genitore genitore = mock(Genitore.class);
        when(genitore.getNome()).thenReturn("GenitoreTest");
        when(genitore.getFigli()).thenReturn(new ArrayList<>(Arrays.asList(figlio)));

        // crea lista prossimi eventi (>= 10 per mostrare il bottone next)
        ArrayList<Evento> prossimi = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            prossimi.add(new Evento("ProssimoEvento" + i, null, LocalDateTime.now().plusDays(i + 1)));
        }

        // istanzia la view e mostra
        view = new HomeGenitore(mockClient, mockEventoView, mockProfiloGenitore, mockLibroDetailedView);
        view.show(stage, genitore, prossimi, () -> { /* onBack vuoto per test */ });

        // attendi che JavaFX abbia applicato le modifiche alla UI
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void tearDown() {
        try {
            // chiudi stage dopo ogni test per evitare interferenze
            if (stage != null) {
                WaitForAsyncUtils.async(() -> {
                    stage.close();
                });
            }
        } catch (Exception ignored) {}
        // reset mock invocations
        clearInvocations(mockClient, mockEventoView, mockProfiloGenitore, mockLibroDetailedView);
    }

    @Test
        // Verifica che all'apertura venga inviata la richiesta per lettori/luoghi/libri
    void invia_richiesta_lettori_eluoghielibri() {
        // la sendMessage viene chiamata nella show(); verifichiamo che sia stata invocata
        try {
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaLettoriELuoghiELibri.class));
        } catch (Exception e) {
            fail("Non è stata inviata RichiestaLettoriELuoghiELibri: " + e.getMessage());
        }
    }

    @Test
        // Verifica che il bottone "Carica Eventi Successivi" sia presente e al click invii RichiestaNextEventi
    void bottone_next_eventi_click_invia_richiesta(FxRobot robot) {
        // trova il pulsante con testo
        Button nextBtn = robot.lookup("Carica Eventi Successivi").queryButton();
        assertNotNull(nextBtn, "Bottone 'Carica Eventi Successivi' dovrebbe essere presente");

        // pulisci chiamate precedenti per isolare questa verifica
        clearInvocations(mockClient);

        // click sul bottone
        robot.clickOn(nextBtn);

        // verifica che sia stata inviata RichiestaNextEventi
        try {
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaNextEventi.class));
        } catch (Exception e) {
            fail("Non è stata inviata RichiestaNextEventi dopo il click: " + e.getMessage());
        }
    }

    @Test
        // Verifica che reloadEventiFromServer invii RichiestaNextEventi
    void reload_eventi_invia_richiesta_next() {
        // pulisci invocazioni precedenti
        clearInvocations(mockClient);

        view.reloadEventiFromServer();

        try {
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaNextEventi.class));
        } catch (Exception e) {
            fail("reloadEventiFromServer non ha inviato RichiestaNextEventi: " + e.getMessage());
        }
    }

    @Test
        // Verifica che il doppio click su una riga evento del figlio apra la view evento
    void doppio_click_rigaevento_figlio_apre_evento(FxRobot robot) {
        // prova a trovare nella UI il testo del titolo evento del figlio e doppio click
        // potrebbe essere necessario che TestFX trovi il nodo; esegui una ricerca generica per etichetta testuale
        Node target = null;
        try {
            // cerco un nodo Labeled con il testo del titolo dell'evento del figlio
            target = robot.lookup(node -> {
                if (node instanceof Labeled) {
                    String txt = ((Labeled) node).getText();
                    return "TitoloFiglioEvento".equals(txt) || txt != null && txt.contains("TitoloFiglioEvento");
                }
                return false;
            }).query();
        } catch (Exception ignored) {}

        if (target != null) {
            // doppio click sulla cella trovata
            robot.doubleClickOn(target);
            // attendi eventuali chiamate
            WaitForAsyncUtils.waitForFxEvents();

            // verifica che eventoView.show sia stato chiamato almeno una volta
            try {
                verify(mockEventoView, timeout(1000)).show(any(), any(), any(), any());
            } catch (Exception e) {
                fail("Dopo doppio click non è stata chiamata eventoView.show: " + e.getMessage());
            }
        } else {
            // se non trova il nodo, fallisci il test con messaggio esplicativo
            fail("Impossibile trovare la cella contenente il titolo dell'evento del figlio per il doppio click");
        }
    }
}