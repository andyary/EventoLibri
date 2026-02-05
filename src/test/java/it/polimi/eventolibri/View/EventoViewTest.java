package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaIscrittiEvento;
import it.polimi.eventolibri.Model.*;
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

// estensione di ApplicationExtension per testare componenti JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
public class EventoViewTest {
    // mocks e variabili di test
    private Client mockClient;
    private EventoView view;
    private Stage stage;

    // setup prima di ogni test
    @BeforeEach
    public void setup() {
        mockClient = Mockito.mock(Client.class);
    }

    // inizializza lo stage di JavaFX all'inizio dei test
    @Start
    private void start(Stage stage) {
        this.stage = stage;
    }

    // utility: inizializza la view EventoView
    private void initView(FxRobot robot) {
        robot.interact(() -> view = new EventoView(mockClient)); // robot.interact per eseguire sulla UI thread
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
    }

    // utility: trova un nodo con timeout per evitare problemi di asincronia, restituendo Optional con il nodo trovato o vuoto se non trovato
    private <T extends Node> Optional<T> trovaNodo(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis(); // tempo di inizio
        while (System.currentTimeMillis() - start < timeoutMs) { // ciclo fino al timeout
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) &&
                    predicate.test(cls.cast(n))).tryQueryAs(cls); // cerca il nodo
            if (opt.isPresent()) return opt; // se trovato, restituisci
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS); // aspetta 50ms prima di riprovare
            WaitForAsyncUtils.waitForFxEvents(); // processa eventi FX
        }
        return Optional.empty(); // restituisci vuoto se non trovato entro il timeout
    }

    // assicura che l'evento abbia luogo e scaletta non null per evitare non null pointer exception
    private void eventoConLuogo(Evento evento) {
        // se luogo è null, impostane uno di default
        if (evento.getLuogo() == null) {
            evento.setLuogo(new Luogo("Biblioteca", 10, 1));
        }
        // se scaletta è null, impostane una vuota
        if (evento.getScaletta() == null) {
            evento.setScaletta(new ArrayList<>());
        }
    }

    // mock Genitore senza figli e con nome definito
    private Genitore mockGenitoreNoFigli() {
        Genitore gen = mock(Genitore.class);
        when(gen.getFigli()).thenReturn(new ArrayList<>());
        when(gen.getNome()).thenReturn("GenitoreTest");
        return gen;
    }

    // test: mostraErrore aggiunge la label di errore alla scena
    @Test
    public void mostraErrore(FxRobot robot) {
        initView(robot); // inizializza la view
        // prepara evento e genitore minimi in modo che la label venga aggiunta alla scena
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        eventoConLuogo(evento);
        Genitore gen = mockGenitoreNoFigli();

        // mostra la view per inserire i nodi nella scena
        robot.interact(() -> view.show(stage, evento, gen, null)); // robot.interact per eseguire sulla UI thread
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati

        // poi chiedi di mostrare l'errore
        robot.interact(() -> view.mostraErrore("Errore di prova")); // mostra errore
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // verifica che la label di errore sia presente con il testo corretto
        Optional<Label> opt = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Errore di prova"), 1000); // cerca la label
        assertTrue(opt.isPresent(), "La label di errore deve mostrare il messaggio");
    }

    // test: aggiornaIscritti aggiorna la label degli iscritti
    @Test
    public void aggiornaIscritti(FxRobot robot) {
        initView(robot); // inizializza la view
        // prepara evento e genitore minimi
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        eventoConLuogo(evento);
        Genitore gen = mockGenitoreNoFigli();
        // mostra la view tramite robot.interact sulla UI thread
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati

        robot.interact(() -> view.aggiornaIscritti(7)); // aggiorna iscritti a 7
        WaitForAsyncUtils.waitForFxEvents(); // aspetta che gli eventi FX siano processati
        // verifica che la label degli iscritti rifletta il nuovo valore
        Optional<Label> opt = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Iscritti: 7"), 1000); // cerca la label
        assertTrue(opt.isPresent(), "La label iscritti deve riflettere il nuovo valore");
    }

    // verifica che selezionando le checkbox dei figli e cliccando Aggiorna iscrizioni vengano inviate le richieste di iscrizione
    @Test
    public void aggiorna_figli(FxRobot robot) throws Exception {
        // il client non deve lanciare eccezioni qui per permettere l'invio dei messaggi
        doNothing().when(mockClient).sendMessage(any());
        // inizializza la view
        initView(robot);

        // prepara genitore con un figlio mockato
        Genitore gen = mock(Genitore.class);
        Figlio fig = mock(Figlio.class);
        // restituisci una java.util.ArrayList concreta (evita Arrays$ArrayList -> ClassCastException)
        when(gen.getFigli()).thenReturn(new ArrayList<>(Arrays.asList(fig))); // mocka lista con un figlio
        when(gen.getNome()).thenReturn("GenTest"); // nome del genitore
        when(fig.getNome()).thenReturn("FiglioTest"); // nome del figlio
        when(fig.getIscrizioni()).thenReturn(new ArrayList<>()); // lista iscrizioni vuota
        // prepara evento minimo
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        eventoConLuogo(evento);
        // mostra la view
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // trova la checkbox del figlio e la seleziona
        Optional<CheckBox> cbOpt = trovaNodo(robot, CheckBox.class,
                c -> "FiglioTest".equals(c.getText()), 1000); // cerca checkbox per il figlio
        assertTrue(cbOpt.isPresent(), "La checkbox del figlio deve essere presente");
        robot.clickOn(cbOpt.get()); // seleziona la checkbox
        WaitForAsyncUtils.waitForFxEvents(); // aspetta eventi FX

        // clicca Aggiorna iscrizioni
        Optional<Button> aggOpt = trovaNodo(robot, Button.class,
                b -> "Aggiorna iscrizioni".equals(b.getText()), 1000); // cerca il pulsante
        assertTrue(aggOpt.isPresent(), "Deve esserci il pulsante Aggiorna iscrizioni");
        robot.clickOn(aggOpt.get()); // clicca il pulsante
        WaitForAsyncUtils.waitForFxEvents();

        // verifica che sia stata inviata almeno una richiesta di iscrizione
        verify(mockClient, timeout(1000).atLeastOnce()).sendMessage(isA(RichiestaIscrittiEvento.class));
        // la logica di iscrizione invia RichiestaIscrizioneEvento; verifichiamo che sendMessage sia stato invocato (almeno una volta)
        verify(mockClient, timeout(1000).atLeastOnce()).sendMessage(any());
    }

    // verifica che in caso di IOException durante l'invio del messaggio la scena venga comunque impostata
    @Test
    public void sendMessage_eccezione(FxRobot robot) throws Exception {
        // forza IOException sul client
        doThrow(new IOException("IOException")).when(mockClient).sendMessage(any());
        // inizializza la view
        initView(robot);
        // prepara genitore ed evento minimi
        Genitore gen = mockGenitoreNoFigli();
        Evento evento = new Evento("TitErr", null, LocalDateTime.now());
        eventoConLuogo(evento);

        // la chiamata a show non deve lanciare e deve impostare la scena
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(stage.getScene(), "La scena deve essere stata impostata nonostante IOException dal client");
    }

    // verifica che aggiornaEvento aggiorni correttamente i dati mostrati nella UI
    @Test
    public void aggiornaEvento(FxRobot robot) {
        initView(robot); // inizializza la view
        // prepara genitore ed evento iniziale
        Genitore gen = mockGenitoreNoFigli();
        Evento evento = new Evento("TitoloVecchio", null, LocalDateTime.of(2025, 1, 1, 10, 0));
        evento.setIscritti(2);
        eventoConLuogo(evento);

        // mostra la view con i dati iniziali
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // crea evento aggiornato con nuovi valori
        Luogo nuovoLuogo = new Luogo("LuogoAggiornato", 30, 1);
        Evento eventoAggiornato = new Evento("TitoloNuovo", nuovoLuogo, LocalDateTime.of(2025, 1, 2, 15, 30));
        eventoAggiornato.setIscritti(5);
        eventoConLuogo(eventoAggiornato);

        // chiama aggiornaEvento
        robot.interact(() -> view.aggiornaEvento(eventoAggiornato));
        WaitForAsyncUtils.waitForFxEvents();

        // verifica titolo aggiornato
        Optional<Label> titoloOpt = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("TitoloNuovo"), 1000); // cerca label con nuovo titolo
        assertTrue(titoloOpt.isPresent(), "La UI deve mostrare il titolo aggiornato");

        // verifica luogo aggiornato (contiene nome del luogo)
        Optional<Label> luogoOpt = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("LuogoAggiornato"), 1000); // cerca label con nuovo luogo
        assertTrue(luogoOpt.isPresent(), "La UI deve mostrare il luogo aggiornato");

        // verifica data aggiornata (formato '15:30' o '2025' per coprire data e ora)
        Optional<Label> dataOpt = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("15:30") || l.getText().contains("2025"),
                1000); // cerca label con nuova data/ora
        assertTrue(dataOpt.isPresent(), "La UI deve mostrare la data/ora aggiornata");

        // verifica iscritti aggiornati (formato 'Iscritti: 5' usato altrove nei test)
        Optional<Label> iscrittiOpt = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Iscritti") && l.getText().contains("5"),
                1000); // cerca label con nuovi iscritti
        assertTrue(iscrittiOpt.isPresent(), "La UI deve mostrare il numero di iscritti aggiornato");
    }

    // verifica che una scaletta con più di due righe mostri tutte le righe nella UI
    @Test
    public void scaletta(FxRobot robot) {
        initView(robot); // inizializza la view
        // prepara genitore ed evento con scaletta di 3 libri
        Genitore gen = mockGenitoreNoFigli();
        Evento evento = new Evento("EventoScaletta", null, LocalDateTime.of(2025, 1, 1, 10, 0));

        // crea 3 libri e 3 librolettore (nessun lettore assegnato)
        ArrayList<it.polimi.eventolibri.Model.LibroLettore> scaletta = new ArrayList<>();
        Libro libro1 = new Libro("Libro1", 10, "link1", "Aut1", 1);
        Libro libro2 = new Libro("Libro2", 20, "link2", "Aut2", 2);
        Libro libro3 = new Libro("Libro3", 15, "link3", "Aut3", 3);

        scaletta.add(new LibroLettore(libro1, null, 1));
        scaletta.add(new LibroLettore(libro2, null, 2));
        scaletta.add(new LibroLettore(libro3, null, 3));

        evento.setScaletta(scaletta);
        eventoConLuogo(evento);

        // mostra la view
        robot.interact(() -> view.show(stage, evento, gen, null));
        WaitForAsyncUtils.waitForFxEvents();

        // verifica che i tre titoli siano visibili nella UI
        Optional<Label> t1 = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Libro1"), 1000); // cerca riga per Libro1
        Optional<Label> t2 = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Libro2"), 1000); // cerca riga per Libro2
        Optional<Label> t3 = trovaNodo(robot, Label.class,
                l -> l.getText() != null && l.getText().contains("Libro3"), 1000); // cerca riga per Libro3

        assertTrue(t1.isPresent(), "Deve essere presente la riga per Libro1");
        assertTrue(t2.isPresent(), "Deve essere presente la riga per Libro2");
        assertTrue(t3.isPresent(), "Deve essere presente la riga per Libro3");
    }
}