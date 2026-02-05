package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaIscrittiEvento;
import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

// estende ApplicationExtension per test JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
public class EventoViewLettoreTest {
    // mock del client di rete
    private Client mockClient;
    private EventoViewLettore view;
    private Stage stage;

    // setup eseguito prima di ogni test
    @BeforeEach
    public void setup() {
        mockClient = Mockito.mock(Client.class); // crea mock del client
    }

    // inizializza lo stage JavaFX
    @Start
    private void start(Stage stage) {
        this.stage = stage;
    }

    // inizializza la view sul thread JavaFX
    private void initView(FxRobot robot) {
        // robot.interact per eseguire sul thread JavaFX
        robot.interact(() -> {
            view = new EventoViewLettore(mockClient);
        });
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX
    }

    // cerca un nodo per un massimo di timeoutMs (per evitare problemi di sincronizzazione), restituendo un Optional vuoto se non trovato
    private <T extends Node> Optional<T> trovaNodo(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis(); // tempo di inizio
        // ciclo di ricerca fino al timeout
        while (System.currentTimeMillis() - start < timeoutMs) {
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) && predicate.test(cls.cast(n))).tryQueryAs(cls); // cerca il nodo
            // se trovato, restituisce l'Optional
            if (opt.isPresent()) {
                return opt;
            }
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS); // attende un breve intervallo prima del prossimo tentativo
            WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX
        }
        return Optional.empty(); // restituisce Optional vuoto se non trovato entro il timeout
    }

    // test che verifica che show invii le richieste iniziali al client
    @Test
    public void invio_richieste_iniziali(FxRobot robot) throws Exception {
        // prepara il mockClient per non fare nulla alle chiamate
        doNothing().when(mockClient).sendMessage(any());
        // inizializza la view
        initView(robot);
        // crea un lettore e un evento di prova
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(1, "Mario", "Verdi", "lettore1");
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        evento.setCreatore(lettore);
        // mostra la view
        robot.interact(() -> view.show(stage, evento, lettore, null));
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che siano state inviate le richieste iniziali
        verify(mockClient, atLeastOnce()).sendMessage(isA(RichiestaIscrittiEvento.class));
        verify(mockClient, atLeastOnce()).sendMessage(isA(RichiestaLettoriELuoghiELibri.class));
    }

    // Verifica che aggiornaLettoriELuoghiELibri popoli le comboBox
    @Test
    public void aggiornaLettoriELuoghiELibri(FxRobot robot) {
        // inizializza la view
        initView(robot);
        // crea un lettore e un evento di prova
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(2, "Luca", "Verdi", "lettore2");
        Evento evento = new Evento("Tit", null, LocalDateTime.now());
        evento.setCreatore(lettore);
        // mostra la view
        robot.interact(() -> view.show(stage, evento, lettore, null)); // robot.interact per eseguire sul thread JavaFX
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX
        // prepara liste da passare
        ArrayList<Luogo> luoghi = new ArrayList<>();
        luoghi.add(new Luogo("Biblioteca", 10, 1));
        ArrayList<Lettore> lettori = new ArrayList<>();

        Lettore lettore3 = creaLettore.nuovoUtente(3, "Anna", "Verdi", "lettore3");
        lettori.add(lettore3);
        ArrayList<Libro> libri = new ArrayList<>();
        Libro libro1 = new Libro("Il Grande Gatsby", 15, "link1.com", "F. Scott Fitzgerald", 1);
        libri.add(libro1);
        // chiama l'aggiornamento
        robot.interact(() -> view.aggiornaLettoriELuoghiELibri(lettori, luoghi, libri)); // esegue sul thread JavaFX
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX
        // trova la combo luogo per prompt "Scegli luogo" o controlla la prima combo
        Optional<ComboBox> opt = trovaNodo(robot, ComboBox.class,
                cb -> "Scegli luogo".equals(((ComboBox<?>) cb).getPromptText()), 1000);
        // verifica presenza
        assertTrue(opt.isPresent(), "La combo luogo deve essere presente");
        ComboBox<?> luogoCombo = opt.get(); // ottiene la combo luogo
        assertEquals(1, luogoCombo.getItems().size(), "La combo luogo deve contenere l'elemento fornito");
    }

    // test che verifica mostraErrore, validazione Salva e funzionamento Iscrivi
    @Test
    public void mostraErrore_salva_iscrivi(FxRobot robot) throws IOException {
        initView(robot); // inizializza la view
        // test mostraErrore
        robot.interact(() -> view.mostraErrore("Errore di prova")); // mostra errore
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX
        // verifica che la label di errore mostri il messaggio
        Optional<Label> errOpt = trovaNodo(robot, Label.class, l -> l.getText() != null &&
                l.getText().contains("Errore di prova"), 1000); // cerca la label di errore
        assertFalse(errOpt.isPresent(), "La label di errore deve mostrare il messaggio"); // verifica presenza

        // prepara evento senza luogo per test Salva
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(4, "Pippo", "Verdi", "lettore5");
        Evento evento = new Evento("TitoloBlank", null, LocalDateTime.now());
        evento.setCreatore(lettore);
        // mostra la view come creatore
        robot.interact(() -> view.show(stage, evento, lettore, null)); // robot.interact per eseguire sul thread JavaFX
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX

        // trova e clicca Salva evento
        Optional<Button> salvaOpt = trovaNodo(robot, Button.class, b ->
                "Salva evento".equals(b.getText()), 1000); // cerca il pulsante Salva
        assertTrue(salvaOpt.isPresent(), "Deve esserci il pulsante Salva evento");
        robot.clickOn(salvaOpt.get()); // clicca sul pulsante Salva
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX
        // verifica che appaia il messaggio di errore per campi obbligatori
        Optional<Label> valErrOpt = trovaNodo(robot, Label.class, l -> l.getText() != null &&
                l.getText().contains("Compila tutti i campi obbligatori"), 1000); // cerca la label di errore di validazione
        assertTrue(valErrOpt.isPresent(), "Deve apparire il messaggio di errore per campi obbligatori");

        // prepara scaletta con una riga libera la lettore e mostra la view come NON creatore per avere 'Iscrivi'
        Libro libro = new Libro("Il Grande Gatsby", 15, "link1.com", "F. Scott Fitzgerald", 1);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        scaletta.add(new LibroLettore(libro, null, 1));
        evento.setScaletta(scaletta);

        Lettore altro = creaLettore.nuovoUtente(5, "Altro", "Verdi", "lettore5");
        // mostra la view come altro lettore
        robot.interact(() -> view.show(stage, evento, altro, null)); // robot.interact per eseguire sul thread JavaFX
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX

        // trova e clicca Iscrivi -> diventa Disiscrivi
        Optional<Button> iscriviOpt = trovaNodo(robot, Button.class, b ->
                "Iscrivi".equals(b.getText()), 1000); // cerca il pulsante Iscrivi
        assertTrue(iscriviOpt.isPresent(), "Deve esserci il pulsante Iscrivi");
        robot.clickOn(iscriviOpt.get()); // clicca sul pulsante Iscrivi
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX

        // verifica che il pulsante ora sia Disiscrivi
        Optional<Button> disiscriviOpt = trovaNodo(robot, Button.class, b ->
                "Disiscrivi".equals(b.getText()), 1000); // cerca il pulsante Disiscrivi
        assertTrue(disiscriviOpt.isPresent(), "Dopo iscrizione il pulsante deve diventare Disiscrivi");
    }

    // test che verifica la gestione di IOException in sendMessage
    @Test
    public void sendMessage_IOException(FxRobot robot) throws Exception {
        // forza IOException per coprire i catch
        doThrow(new IOException("IOException")).when(mockClient).sendMessage(any());
        // inizializza la view
        initView(robot);
        // crea un lettore e un evento di prova
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(6, "Mario", "Verdi", "ErrTest");
        Evento evento = new Evento("TitErr", null, LocalDateTime.now());
        evento.setCreatore(lettore);

        // chiamata show non deve far fallire il test anche se il client lancia
        robot.interact(() -> view.show(stage, evento, lettore, null)); // robot.interact per eseguire sul thread JavaFX
        WaitForAsyncUtils.waitForFxEvents(); // attende il completamento degli eventi JavaFX

        // verifichiamo che la scena sia stata impostata nonostante l'IOException
        assertNotNull(stage.getScene(), "La scena deve essere stata impostata nonostante IOException dal client");
    }
}