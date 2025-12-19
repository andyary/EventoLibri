// language: java
package it.polimi.eventolibri.Controller;

import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Model.DAO.*;
import it.polimi.eventolibri.Message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    private Controller controller;

    @Mock
    private UtenteDAO utenteDAO;
    @Mock
    private EventoDAO eventoDAO;
    @Mock
    private FiglioDAO figlioDAO;
    @Mock
    private LibroDAO libroDAO;
    @Mock
    private LibroLettoreDAO libroLettoreDAO;
    @Mock
    private LuogoDAO luogoDAO;
    @Mock
    private RecensioneDAO recensioneDAO;

    @BeforeEach
    void setUp() throws Exception {
        controller = new Controller(); // costruttore può fallire sulla connessione ma non obbliga i DAO non-null
        // inietta i mock nei campi privati del controller tramite reflection
        setPrivateField(controller, "utenteDAO", utenteDAO);
        setPrivateField(controller, "eventoDAO", eventoDAO);
        setPrivateField(controller, "figlioDAO", figlioDAO);
        setPrivateField(controller, "libroDAO", libroDAO);
        setPrivateField(controller, "libroLettoreDAO", libroLettoreDAO);
        setPrivateField(controller, "luogoDAO", luogoDAO);
        setPrivateField(controller, "recensioneDAO", recensioneDAO);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void controllaLogin_successo() throws Exception {
        // prepara mock

        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore mockUser = creaGenitore.nuovoUtente("xxx", "xxx", "andrea");
        when(utenteDAO.checkCredentials("andrea", "pwd")).thenReturn(mockUser);
        when(eventoDAO.getNextEventi(any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        // esecuzione
        RispostaLogin resp = controller.controllaLogin("andrea", "pwd");

        // verifiche
        assertTrue(resp.isSuccesso(), "Login dovrebbe essere successo");
        assertNotNull(resp.getUtente(), "Utente nella risposta non deve essere null");
        verify(utenteDAO, atLeastOnce()).checkCredentials("andrea", "pwd");
        verify(eventoDAO).getNextEventi(any(LocalDateTime.class));
    }

    @Test
    void controllaLogin_fallito() throws Exception {
        when(utenteDAO.checkCredentials("x", "y")).thenReturn(null);

        RispostaLogin resp = controller.controllaLogin("x", "y");

        assertFalse(resp.isSuccesso(), "Login dovrebbe fallire");
        assertEquals("Credenziali non valide.", resp.getMessaggioerrore());
        verify(utenteDAO).checkCredentials("x", "y");
    }

    @Test
    void aggiungiFiglio_successo() throws Exception {

        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("xxx", "xxx", "xxx.xxx");
        genitore.setId(10);
        Figlio nuovo = new Figlio( "Pippo", LocalDateTime.now().toLocalDate());

        when(figlioDAO.creaFiglio(nuovo.getNome(), nuovo.getDataNascita(), genitore)).thenReturn(42);

        RispostaAggiungiFiglio resp = controller.aggiungiFiglio(genitore, nuovo);

        assertTrue(resp.isSuccesso());
        assertNotNull(resp.getNuovoFiglio());
        assertEquals(42, resp.getNuovoFiglio().getId());
        verify(figlioDAO).creaFiglio(nuovo.getNome(), nuovo.getDataNascita(), genitore);
    }

    @Test
    void getNextEventi_successo() throws Exception {
        ArrayList<Evento> lista = new ArrayList<>();
        Lettore creatore = new Lettore("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1, null);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2, null);
        Lettore lettore1 = new Lettore("Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = new Lettore("Anna", "Verdi", "annaverdi");
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento evento= new Evento(creatore, "Evento di prova", luogo, date, scaletta);
        lista.add(evento);
        when(eventoDAO.getNextEventi(any(Evento.class))).thenReturn(lista);

        RispostaNextEventi resp = controller.getNextEventi(evento);

        assertTrue(resp.isSuccesso());
        assertNotNull(resp.getProssimiEventi());
        assertEquals(1, resp.getProssimiEventi().size());
        verify(eventoDAO).getNextEventi(any(Evento.class));
    }

    @Test
    void getNextEventi_exception() throws Exception {
        when(eventoDAO.getNextEventi(any(Evento.class))).thenThrow(new SQLException("boom"));
        Lettore creatore = new Lettore("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1, null);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2, null);
        Lettore lettore1 = new Lettore("Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = new Lettore("Anna", "Verdi", "annaverdi");
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento evento= new Evento(creatore, "Evento di prova", luogo, date, scaletta);


        RispostaNextEventi resp = controller.getNextEventi(evento);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioErrore());
        verify(eventoDAO).getNextEventi(any(Evento.class));
    }

    @Test
    void iscriviFiglioEvento_successo_e_failure() throws Exception {
        Figlio f = new Figlio();
        Evento e = new Evento();
        Genitore g = new Genitore();

        // successo
        doNothing().when(figlioDAO).iscriviFiglioEvento(f, e);
        RispostaIscrizioneEvento ok = controller.iscriviFiglioEvento(f, e, g);
        assertTrue(ok.isSuccesso());
        verify(figlioDAO).iscriviFiglioEvento(f, e);

        // eccezione
        doThrow(new SQLException("db")).when(figlioDAO).iscriviFiglioEvento(f, e);
        RispostaIscrizioneEvento ko = controller.iscriviFiglioEvento(f, e, g);
        assertFalse(ko.isSuccesso());
        assertNotNull(ko.getMessaggioErrore());
        verify(figlioDAO, times(2)).iscriviFiglioEvento(f, e); // chiamato due volte in totale
    }

    @Test
    void aggiornaGenitore_successo_e_erroreDB() throws Exception {
        Genitore g = new Genitore();

        when(utenteDAO.aggiornaGenitore(g)).thenReturn(1);
        RispostaAggiornaGenitore ok = controller.aggiornaGenitore(g);
        assertTrue(ok.isSuccesso());
        assertEquals(g, ok.getGenitore());
        verify(utenteDAO).aggiornaGenitore(g);

        when(utenteDAO.aggiornaGenitore(g)).thenReturn(0);
        RispostaAggiornaGenitore ko = controller.aggiornaGenitore(g);
        assertFalse(ko.isSuccesso());
        assertEquals("Errore aggiornamento genitore su DB", ko.getMessaggioErrore());
    }

    @Test
    void richiestaLettoriELuoghi_successo_e_exceptionELibri() throws Exception {
        ArrayList<Luogo> luoghi = new ArrayList<>();
        ArrayList<Lettore> lettori = new ArrayList<>();
        luoghi.add(new Luogo("Sala A", 15, 2));
        CreaLettore creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        lettori.add(lettore);

        when(luogoDAO.getLuoghi()).thenReturn(luoghi);
        when(utenteDAO.getLettori()).thenReturn(lettori);

        RispostaLettoriELuoghiELibri resp = controller.richiestaLettoriELuoghiELibri();
        assertTrue(resp.isSuccesso());
        assertEquals(1, resp.getLuoghi().size());
        assertEquals(1, resp.getLettori().size());
        verify(luogoDAO).getLuoghi();
        verify(utenteDAO).getLettori();

        when(luogoDAO.getLuoghi()).thenThrow(new SQLException("fail"));
        RispostaLettoriELuoghiELibri respErr = controller.richiestaLettoriELuoghiELibri();
        assertFalse(respErr.isSuccesso());
    }

    // altri test possono seguire lo stesso pattern per i metodi restanti del controller
}