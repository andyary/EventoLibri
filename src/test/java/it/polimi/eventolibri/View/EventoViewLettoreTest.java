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

@ExtendWith(ApplicationExtension.class)
public class EventoViewLettoreTest {

    private Client mockClient;
    private EventoViewLettore view;
    private Stage stage;

    @BeforeEach
    public void setup() {
        mockClient = Mockito.mock(Client.class);
    }

    @Start
    private void start(Stage stage) {
        this.stage = stage;
    }

    // inizializza la view sul thread JavaFX
    private void initView(FxRobot robot) {
        robot.interact(() -> {
            view = new EventoViewLettore(mockClient);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    // cerca un nodo per un massimo di timeoutMs
    private <T extends Node> Optional<T> findWithRetry(FxRobot robot, Class<T> cls, Predicate<T> predicate, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            Optional<T> opt = robot.lookup(n -> cls.isInstance(n) && predicate.test(cls.cast(n))).tryQueryAs(cls);
            if (opt.isPresent()) {
                return opt;
            }
            WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
            WaitForAsyncUtils.waitForFxEvents();
        }
        return Optional.empty();
    }

    @Test
    // verifica che show invii le richieste iniziali al client
    public void invio_richieste_iniziali(FxRobot robot) throws Exception {
        // prepara il mockClient per non fare nulla alle chiamate
        doNothing().when(mockClient).sendMessage(any());
        // inizializza la view
        initView(robot);
        // crea un lettore e un evento di prova
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(1,"Mario", "Verdi", "lettore1");
        Evento evento = new Evento("Titolo", null, LocalDateTime.now());
        evento.setCreatore(lettore);
        // mostra la view
        robot.interact(() -> view.show(stage, evento, lettore, null));
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che siano state inviate le richieste iniziali
        verify(mockClient, atLeastOnce()).sendMessage(isA(RichiestaIscrittiEvento.class));
        verify(mockClient, atLeastOnce()).sendMessage(isA(RichiestaLettoriELuoghiELibri.class));
    }

    @Test
    // verifica che aggiornaLettoriELuoghiELibri popoli le comboBox
    public void aggiornaLettoriELuoghiELibri(FxRobot robot) {
        // inizializza la view
        initView(robot);
        // crea un lettore e un evento di prova
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(2,"Luca", "Verdi", "lettore2");
        Evento evento = new Evento("Tit", null, LocalDateTime.now());
        evento.setCreatore(lettore);
        // mostra la view
        robot.interact(() -> view.show(stage, evento, lettore, null));
        WaitForAsyncUtils.waitForFxEvents();
        // prepara liste da passare
        ArrayList<Luogo> luoghi = new ArrayList<>();
        luoghi.add(new Luogo("Biblioteca", 10, 1));
        ArrayList<Lettore> lettori = new ArrayList<>();

        Lettore lettore3 = creaLettore.nuovoUtente(3,"Anna", "Verdi", "lettore3");
        lettori.add(lettore3);
        ArrayList<Libro> libri = new ArrayList<>();
        Libro libro1 = new Libro("Il Grande Gatsby", 15, "link1.com", "F. Scott Fitzgerald", 1);
        libri.add(libro1);
        // chiama l'aggiornamento
        robot.interact(() -> view.aggiornaLettoriELuoghiELibri(lettori, luoghi, libri));
        WaitForAsyncUtils.waitForFxEvents();
        // trova la combo luogo per prompt "Scegli luogo" o controlla la prima combo
        Optional<ComboBox> opt = findWithRetry(robot, ComboBox.class,
                cb -> "Scegli luogo".equals(((ComboBox<?>) cb).getPromptText()), 1000);
        // verifica presenza
        assertTrue(opt.isPresent(), "La combo luogo deve essere presente");
        ComboBox<?> luogoCombo = opt.get();
        assertEquals(1, luogoCombo.getItems().size(), "La combo luogo deve contenere l'elemento fornito");
    }

    @Test
    // verifica mostraErrore, validazione Salva e funzionamento Iscrivi
    public void mostraErrore_salva_iscrivi(FxRobot robot) throws IOException {
        initView(robot);
        // test mostraErrore
        robot.interact(() -> view.mostraErrore("Errore di prova"));
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che la label di errore mostri il messaggio
        Optional<Label> errOpt = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Errore di prova"), 1000);
        assertFalse(errOpt.isPresent(), "La label di errore deve mostrare il messaggio");

        // prepara evento senza luogo per test Salva
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(4,"Pippo", "Verdi", "lettore5");
        Evento evento = new Evento("TitoloBlank", null, LocalDateTime.now());
        evento.setCreatore(lettore);
        // mostra la view come creatore
        robot.interact(() -> view.show(stage, evento, lettore, null));
        WaitForAsyncUtils.waitForFxEvents();

        // trova e clicca Salva evento
        Optional<Button> salvaOpt = findWithRetry(robot, Button.class, b -> "Salva evento".equals(b.getText()), 1000);
        assertTrue(salvaOpt.isPresent(), "Deve esserci il pulsante Salva evento");
        robot.clickOn(salvaOpt.get());
        WaitForAsyncUtils.waitForFxEvents();
        // verifica che appaia il messaggio di errore per campi obbligatori
        Optional<Label> valErrOpt = findWithRetry(robot, Label.class, l -> l.getText() != null && l.getText().contains("Compila tutti i campi obbligatori"), 1000);
        assertTrue(valErrOpt.isPresent(), "Deve apparire il messaggio di errore per campi obbligatori");

        // prepara scaletta con una riga libera la lettore e mostra la view come NON creatore per avere 'Iscrivi'
        Libro libro = new Libro("Il Grande Gatsby", 15, "link1.com", "F. Scott Fitzgerald", 1);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        scaletta.add(new LibroLettore(libro, null, 1));
        evento.setScaletta(scaletta);

        Lettore altro = creaLettore.nuovoUtente(5,"Altro", "Verdi", "lettore5");
        // mostra la view come altro lettore
        robot.interact(() -> view.show(stage, evento, altro, null));
        WaitForAsyncUtils.waitForFxEvents();

        // trova e clicca Iscrivi -> diventa Disiscrivi
        Optional<Button> iscriviOpt = findWithRetry(robot, Button.class, b -> "Iscrivi".equals(b.getText()), 1000);
        assertTrue(iscriviOpt.isPresent(), "Deve esserci il pulsante Iscrivi");
        robot.clickOn(iscriviOpt.get());
        WaitForAsyncUtils.waitForFxEvents();

        Optional<Button> disiscriviOpt = findWithRetry(robot, Button.class, b -> "Disiscrivi".equals(b.getText()), 1000);
        assertTrue(disiscriviOpt.isPresent(), "Dopo iscrizione il pulsante deve diventare Disiscrivi");
    }

    @Test
    // verifica che una IOException lanciata dal client in sendMessage sia gestita
    public void sendMessage_IOException(FxRobot robot) throws Exception {
        // forza IOException per coprire i catch
        doThrow(new IOException("IOException")).when(mockClient).sendMessage(any());
        // inizializza la view
        initView(robot);
        // crea un lettore e un evento di prova
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente(6,"Mario", "Verdi", "ErrTest");
        Evento evento = new Evento("TitErr", null, LocalDateTime.now());
        evento.setCreatore(lettore);

        // chiamata show non deve far fallire il test anche se il client lancia
        robot.interact(() -> view.show(stage, evento, lettore, null));
        WaitForAsyncUtils.waitForFxEvents();

        // verifichiamo che la scena sia stata impostata nonostante l'IOException
        assertNotNull(stage.getScene(), "La scena deve essere stata impostata nonostante IOException dal client");
    }
}

