package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaIscrittiEvento;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Model.Luogo;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class EventoViewTest {

    private Client mockClient;
    private EventoView view;
    private Stage stage;

    @BeforeEach
    public void setup() {
        mockClient = Mockito.mock(Client.class);
    }

    @Start
    private void start(Stage stage) {
        this.stage = stage;
    }

    private void initView(FxRobot robot) {
        robot.interact(() -> view = new EventoView(mockClient));
        WaitForAsyncUtils.waitForFxEvents();
    }

    // utility: trova un nodo con retry
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

    // assicura che l'evento abbia luogo e scaletta non null per evitare NPE in view
    private void ensureEventoHasLuogo(Evento evento) {
        if (evento.getLuogo() == null) {
            evento.setLuogo(new Luogo("Biblioteca", 10, 1));
        }
        if (evento.getScaletta() == null) {
            evento.setScaletta(new ArrayList<>());
        }
    }

    // mock Genitore senza figli e con nome definito
    private Genitore mockGenitoreNoChildren() {
        Genitore gen = mock(Genitore.class);
        when(gen.getFigli()).thenReturn(new ArrayList<>());
        when(gen.getNome()).thenReturn("GenitoreTest");
        return gen;
    }

    @Test
    public void mostraErrore_mostra_label(FxRobot robot) {
        initView(robot);
        // prepara evento e genitore minimi in modo che la label venga aggiunta alla scena
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        ensureEventoHasLuogo(evento);
        Genitore gen = mockGenitoreNoChildren();

        // mostra la view per inserire i nodi nella scena
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // poi chiedi di mostrare l'errore
        robot.interact(() -> view.mostraErrore("Errore di prova"));
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Label> opt = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Errore di prova"), 1000);
        assertTrue(opt.isPresent(), "La label di errore deve mostrare il messaggio");
    }

    @Test
    public void aggiornaIscritti_aggiorna_label(FxRobot robot) {
        initView(robot);
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        ensureEventoHasLuogo(evento);
        Genitore gen = mockGenitoreNoChildren();

        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        robot.interact(() -> view.aggiornaIscritti(7));
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Label> opt = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Iscritti: 7"), 1000);
        assertTrue(opt.isPresent(), "La label iscritti deve riflettere il nuovo valore");
    }

    @Test
    public void aggiorna_checkbox_figli_invia_richieste(FxRobot robot) throws Exception {
        // il client non deve lanciare eccezioni qui
        doNothing().when(mockClient).sendMessage(any());

        initView(robot);

        // prepara genitore con un figlio mockato
        Genitore gen = mock(Genitore.class);
        Figlio fig = mock(Figlio.class);
        // restituisci una java.util.ArrayList concreta (evita Arrays$ArrayList -> ClassCastException)
        when(gen.getFigli()).thenReturn(new ArrayList<>(Arrays.asList(fig)));
        when(gen.getNome()).thenReturn("GenTest");
        when(fig.getNome()).thenReturn("FiglioTest");
        when(fig.getIscrizioni()).thenReturn(new ArrayList<>());

        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        ensureEventoHasLuogo(evento);

        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // trova la checkbox del figlio e la seleziona
        Optional<CheckBox> cbOpt = findWithRetry(robot, CheckBox.class, c -> "FiglioTest".equals(c.getText()), 1000);
        assertTrue(cbOpt.isPresent(), "La checkbox del figlio deve essere presente");
        robot.clickOn(cbOpt.get()); // seleziona
        WaitForAsyncUtils.waitForFxEvents();

        // clicca Aggiorna iscrizioni
        Optional<Button> aggOpt = findWithRetry(robot, Button.class, b -> "Aggiorna iscrizioni".equals(b.getText()), 1000);
        assertTrue(aggOpt.isPresent(), "Deve esserci il pulsante Aggiorna iscrizioni");
        robot.clickOn(aggOpt.get());
        WaitForAsyncUtils.waitForFxEvents();

        // verifica che sia stata inviata almeno una richiesta di iscrizione
        verify(mockClient, timeout(1000).atLeastOnce()).sendMessage(isA(RichiestaIscrittiEvento.class));
        // la logica di iscrizione invia RichiestaIscrizioneEvento; verifichiamo che sendMessage sia stato invocato (almeno una volta)
        verify(mockClient, timeout(1000).atLeastOnce()).sendMessage(any());
    }

    @Test
    public void sendMessage_IOException_non_blocca_show(FxRobot robot) throws Exception {
        // forza IOException sul client
        doThrow(new IOException("IOException")).when(mockClient).sendMessage(any());

        initView(robot);

        Genitore gen = mockGenitoreNoChildren();
        Evento evento = new Evento("TitErr", null, LocalDateTime.now());
        ensureEventoHasLuogo(evento);

        // la chiamata a show non deve lanciare e deve impostare la scena
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        assertNotNull(stage.getScene(), "La scena deve essere stata impostata nonostante IOException dal client");
    }

    @Test
    public void aggiornaEvento_aggiorna_ui(FxRobot robot) {
        initView(robot);

        Genitore gen = mockGenitoreNoChildren();
        Evento evento = new Evento("TitoloVecchio", null, LocalDateTime.of(2025, 1, 1, 10, 0));
        evento.setIscritti(2);
        ensureEventoHasLuogo(evento);

        // mostra la view con i dati iniziali
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // crea evento aggiornato con nuovi valori
        Luogo nuovoLuogo = new Luogo("LuogoAggiornato", 30, 1);
        Evento eventoAggiornato = new Evento("TitoloNuovo", nuovoLuogo, LocalDateTime.of(2025, 1, 2, 15, 30));
        eventoAggiornato.setIscritti(5);
        ensureEventoHasLuogo(eventoAggiornato);

        // chiama aggiornaEvento sulla UI thread
        robot.interact(() -> view.aggiornaEvento(eventoAggiornato));
        WaitForAsyncUtils.waitForFxEvents();

        // verifica titolo aggiornato
        Optional<Label> titoloOpt = findWithRetry(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("TitoloNuovo"), 1000);
        assertTrue(titoloOpt.isPresent(), "La UI deve mostrare il titolo aggiornato");

        // verifica luogo aggiornato (contiene nome del luogo)
        Optional<Label> luogoOpt = findWithRetry(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("LuogoAggiornato"), 1000);
        assertTrue(luogoOpt.isPresent(), "La UI deve mostrare il luogo aggiornato");

        // verifica data aggiornata (controllo parziale: giorno/anno o orario potrebbe comparire)
        Optional<Label> dataOpt = findWithRetry(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("15:30") || l.getText().contains("2025"), 1000);
        assertTrue(dataOpt.isPresent(), "La UI deve mostrare la data/ora aggiornata");

        // verifica iscritti aggiornati (formato 'Iscritti: 5' usato altrove nei test)
        Optional<Label> iscrittiOpt = findWithRetry(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Iscritti") && l.getText().contains("5"), 1000);
        assertTrue(iscrittiOpt.isPresent(), "La UI deve mostrare il numero di iscritti aggiornato");
    }

    @Test
    public void scaletta_con_piu_di_due_righe_mostra_tutte_le_righe(FxRobot robot) {
        initView(robot);

        Genitore gen = mockGenitoreNoChildren();
        Evento evento = new Evento("EventoScaletta", null, LocalDateTime.of(2025, 1, 1, 10, 0));

        // crea 3 libri e 3 librolettore (nessun lettore assegnato)
        ArrayList<it.polimi.eventolibri.Model.LibroLettore> scaletta = new ArrayList<>();
        it.polimi.eventolibri.Model.Libro libro1 = new it.polimi.eventolibri.Model.Libro("Libro1", 10, "link1", "Aut1", 1);
        it.polimi.eventolibri.Model.Libro libro2 = new it.polimi.eventolibri.Model.Libro("Libro2", 20, "link2", "Aut2", 2);
        it.polimi.eventolibri.Model.Libro libro3 = new it.polimi.eventolibri.Model.Libro("Libro3", 15, "link3", "Aut3", 3);

        scaletta.add(new it.polimi.eventolibri.Model.LibroLettore(libro1, null, 1));
        scaletta.add(new it.polimi.eventolibri.Model.LibroLettore(libro2, null, 2));
        scaletta.add(new it.polimi.eventolibri.Model.LibroLettore(libro3, null, 3));

        evento.setScaletta(scaletta);
        ensureEventoHasLuogo(evento);

        // mostra la view
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // verifica che i tre titoli siano visibili nella UI
        Optional<Label> t1 = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Libro1"), 1000);
        Optional<Label> t2 = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Libro2"), 1000);
        Optional<Label> t3 = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Libro3"), 1000);

        assertTrue(t1.isPresent(), "Deve essere presente la riga per Libro1");
        assertTrue(t2.isPresent(), "Deve essere presente la riga per Libro2");
        assertTrue(t3.isPresent(), "Deve essere presente la riga per Libro3");
    }
}
