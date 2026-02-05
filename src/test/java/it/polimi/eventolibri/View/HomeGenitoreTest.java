package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Message.RichiestaNextEventi;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
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

// Estensione di ApplicationExtension per test JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
class HomeGenitoreTest {
    // mock vari usati nei test
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
        // conserva stage per chiusura dopo test
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
        for (int i = 0; i < 10; i++) { // 10 eventi
            prossimi.add(new Evento("ProssimoEvento" + i, null, LocalDateTime.now().plusDays(i + 1)));
        }

        // istanzia la view e mostra
        view = new HomeGenitore(mockClient, mockEventoView, mockProfiloGenitore, mockLibroDetailedView);
        view.show(stage, genitore, prossimi, () -> { /* onBack vuoto per test */ });

        // attendi che JavaFX abbia applicato le modifiche alla UI
        WaitForAsyncUtils.waitForFxEvents();
    }

    // Dopo ogni test, chiudi la stage se aperta e resetta i mock
    @AfterEach
    void tearDown() {
        try {
            // chiudi la stage se aperta
            if (stage != null) {
                Platform.runLater(() -> { // chiudi sul FX thread
                    try {
                        if (stage.isShowing()) { // controlla se è aperta
                            stage.close(); // chiudi la stage
                        }
                    } catch (Exception ex) {
                    }
                });
                WaitForAsyncUtils.waitForFxEvents(); // attendi che la chiusura sia processata
            }
        } catch (Exception ex) {
        }
        // reset mock invocations
        clearInvocations(mockClient, mockEventoView, mockProfiloGenitore, mockLibroDetailedView);
    }


    @Test
        // Verifica che all'apertura venga inviata la richiesta per lettori/luoghi/libri
    void invia_richiesta_iniziale() {
        // la sendMessage viene chiamata nella show(); verifica che sia stata invocata
        try {
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaLettoriELuoghiELibri.class));
        } catch (Exception e) {
            fail("Non è stata inviata RichiestaLettoriELuoghiELibri: " + e.getMessage());
        }
    }

    @Test
        // Verifica che il bottone "Carica Eventi Successivi" sia presente e al click invii RichiestaNextEventi
    void bottone_next_eventi(FxRobot robot) {
        // trova il pulsante con testo
        Button nextBtn = robot.lookup("Carica Eventi Successivi").queryButton(); // lookup per trovare il bottone
        assertNotNull(nextBtn, "Bottone 'Carica Eventi Successivi' dovrebbe essere presente");

        // pulisci chiamate precedenti per isolare questa verifica
        clearInvocations(mockClient); // pulisci invocazioni precedenti

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
    void reload_eventi() {
        // pulisci invocazioni precedenti
        clearInvocations(mockClient);
        // chiama reloadEventiFromServer
        view.reloadEventiFromServer();

        try { // verifica invio RichiestaNextEventi
            verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaNextEventi.class));
        } catch (Exception e) {
            fail("reloadEventiFromServer non ha inviato RichiestaNextEventi: " + e.getMessage());
        }
    }

    @Test
        // Verifica che il doppio click su una riga evento del figlio apra la view evento
    void doppio_click_rigaevento(FxRobot robot) {
        // prova a trovare nella UI il testo del titolo evento del figlio e doppio click
        // potrebbe essere necessario che TestFX trovi il nodo; esegui una ricerca generica per etichetta testuale
        Node target = null;
        try {
            // cerco un nodo Labeled con il testo del titolo dell'evento del figlio
            target = robot.lookup(node -> {
                if (node instanceof Labeled) {
                    String txt = ((Labeled) node).getText();
                    return "TitoloFiglioEvento".equals(txt) || txt != null && txt.contains("TitoloFiglioEvento"); // match esatto o parziale
                }
                return false;  // altrimenti non è il nodo cercato
            }).query(); // esegui la query
        } catch (Exception ex) {
        }

        if (target != null) { // se trova il nodo
            // doppio click sulla cella trovata
            robot.doubleClickOn(target);
            // attendi che JavaFX processi gli eventi
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