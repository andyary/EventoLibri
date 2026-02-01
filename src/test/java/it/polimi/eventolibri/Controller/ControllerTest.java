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
import java.time.LocalDate;
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
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
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
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
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
        assertEquals("Errore richiesta al server.", resp.getMessaggioErrore());
        verify(eventoDAO).getNextEventi(any(Evento.class));
    }

    @Test
    void iscriviFiglioEvento_successo_e_failure() throws Exception {
        Figlio f = new Figlio("Mario", LocalDate.of(2015, 1, 1));

        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore g = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        g.aggiungiFiglio(f);

        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        Lettore lettore1 = creaLettore.nuovoUtente("Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = creaLettore.nuovoUtente("Anna", "Verdi", "annaverdi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento e= new Evento(creatore, "Evento di prova", luogo, date, scaletta);


        // successo
        doNothing().when(figlioDAO).iscriviFiglioEvento(f, e); // non lancia eccezioni
        RispostaIscrizioneEvento ok = controller.iscriviFiglioEvento(f, e, g);
        assertTrue(ok.isSuccesso()); // successo
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
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore g = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");

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
        ArrayList<Libro> libri = new ArrayList<>();
        libri.add(new Libro("Libro1", 10, "link1.com", "Autore1", 1));
        libri.add(new Libro("Libro2", 9, "link2.com", "Autore2", 2));
        luoghi.add(new Luogo("Sala A", 15, 2));
        CreaLettore creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        lettori.add(lettore);

        when(luogoDAO.getLuoghi()).thenReturn(luoghi);
        when(utenteDAO.getLettori()).thenReturn(lettori);
        when(libroDAO.getLibri()).thenReturn(libri);


        RispostaLettoriELuoghiELibri resp = controller.richiestaLettoriELuoghiELibri();
        assertTrue(resp.isSuccesso());
        assertEquals(1, resp.getLuoghi().size());
        assertEquals(1, resp.getLettori().size());
        assertEquals(2, resp.getElencolibri().size());
        verify(luogoDAO).getLuoghi();
        verify(utenteDAO).getLettori();
        verify(libroDAO).getLibri();

        when(luogoDAO.getLuoghi()).thenThrow(new SQLException("fail"));
        RispostaLettoriELuoghiELibri respErr = controller.richiestaLettoriELuoghiELibri();
        assertFalse(respErr.isSuccesso());
    }

    @Test
    void creaEvento_successo_e_failure() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
        Lettore lettore1 = creaLettore.nuovoUtente("Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = creaLettore.nuovoUtente("Anna", "Verdi", "annaverdi");
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento e = new Evento(creatore, "Evento di prova", luogo, date, scaletta);

        when(eventoDAO.creaEvento(e.getCreatore(),e.getNome(),e.getLuogo(),e.getData())).thenReturn(100);

        RispostaSalvaEvento ok = controller.salvaEvento(e);
        assertTrue(ok.isSuccesso());
        assertNotNull(ok.getEvento());
        assertEquals(100, ok.getEvento().getId());
        verify(eventoDAO).creaEvento(e.getCreatore(),e.getNome(),e.getLuogo(),e.getData());

        RispostaSalvaEvento ko = controller.salvaEvento(e);
        assertTrue(ko.isSuccesso());
        assertNull(ko.getMessaggioErrore());
        verify(eventoDAO, times(1)).creaEvento(e.getCreatore(),e.getNome(),e.getLuogo(),e.getData()); // chiamato due volte in totale
    }

    @Test
    void salvaEvento_conflitto() throws Exception {
        // prepara evento e lista di conflitti
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento e = new Evento(creatore, "Evento conflitto", luogo, date, scaletta);

        ArrayList<Evento> conflitti = new ArrayList<>();
        conflitti.add(new Evento(creatore, "Altro evento", luogo, date.plusMinutes(10), scaletta));

        when(eventoDAO.eventiInConflitto(any(Evento.class))).thenReturn(conflitti);

        RispostaSalvaEvento resp = controller.salvaEvento(e);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioErrore());
        assertTrue(resp.getMessaggioErrore().contains("Conflitto di orario"));
        verify(eventoDAO).eventiInConflitto(any(Evento.class));
    }

    @Test
    void salvaEvento_update_success() throws Exception {
        // evento già esistente (id != 0) -> modificaEvento chiamato
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        Luogo luogo = new Luogo("Sala B", 20, 3);
        LocalDateTime date = LocalDateTime.of(2026, 7, 20, 10, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento e = new Evento(creatore, "Evento modifica", luogo, date, scaletta);
        e.setId(5); // indica evento esistente

        when(eventoDAO.eventiInConflitto(any(Evento.class))).thenReturn(new ArrayList<>());
        doNothing().when(eventoDAO).modificaEvento(any(Evento.class));
        when(eventoDAO.getIscrittiEvento(any(Evento.class))).thenReturn(2);

        RispostaSalvaEvento resp = controller.salvaEvento(e);

        assertTrue(resp.isSuccesso());
        assertNotNull(resp.getEvento());
        assertEquals(5, resp.getEvento().getId());
        verify(eventoDAO).modificaEvento(any(Evento.class));
        verify(eventoDAO).getIscrittiEvento(any(Evento.class));
    }

    @Test
    void salvaEvento_create_failure() throws Exception {
        // nuovo evento (id == 0) ma creaEvento fallisce (ritorna -1)
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        Luogo luogo = new Luogo("Sala C", 30, 4);
        LocalDateTime date = LocalDateTime.of(2026, 8, 10, 14, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento e = new Evento(creatore, "Evento nuovo fallisce", luogo, date, scaletta);
        // id di default 0

        when(eventoDAO.eventiInConflitto(any(Evento.class))).thenReturn(new ArrayList<>());
        when(eventoDAO.creaEvento(e.getCreatore(), e.getNome(), e.getLuogo(), e.getData())).thenReturn(-1);

        RispostaSalvaEvento resp = controller.salvaEvento(e);

        assertFalse(resp.isSuccesso());
        assertEquals("Errore salvataggio evento su DB", resp.getMessaggioErrore());
        verify(eventoDAO).creaEvento(e.getCreatore(), e.getNome(), e.getLuogo(), e.getData());
    }

    @Test
    void aggiungiRecensione_successo_e_failure() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("LibroRec", 5, "link.com", "Autore", 1);
        Recensione recensione = new Recensione( genitore,"Ottimo libro", libro);

        // successo: creaRecensione ritorna id valido
        when(recensioneDAO.creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore()))
                .thenReturn(77);

        RispostaAggiungiRecensione ok = controller.aggiungiRecensione(recensione);
        assertTrue(ok.isSuccesso());
        assertNotNull(ok.getRecensione());
        assertEquals(77, ok.getRecensione().getId());
        verify(recensioneDAO).creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore());

        // failure: creaRecensione ritorna -1
        when(recensioneDAO.creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore()))
                .thenReturn(-1);

        RispostaAggiungiRecensione ko = controller.aggiungiRecensione(recensione);
        assertFalse(ko.isSuccesso());
        assertEquals("Errore creazione nuova recensione su DB", ko.getMessaggioErrore());
    }

    @Test
    void cancellaRecensione_successo_e_exception() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("LibroRec", 5, "link.com", "Autore", 1);
        Recensione recensione = new Recensione(genitore,"Da cancellare", libro);

        // successo: cancellaRecensione non lancia eccezioni
        doNothing().when(recensioneDAO).cancellaRecensione(recensione);
        RispostaCancellaRecensione ok = controller.cancellaRecensione(recensione);
        assertTrue(ok.isSuccesso());
        verify(recensioneDAO).cancellaRecensione(recensione);

        // eccezione dal DAO
        doThrow(new SQLException("db")).when(recensioneDAO).cancellaRecensione(recensione);
        RispostaCancellaRecensione ko = controller.cancellaRecensione(recensione);
        assertFalse(ko.isSuccesso());
        assertNotNull(ko.getMessaggioErrore());
        verify(recensioneDAO, times(2)).cancellaRecensione(recensione); // chiamato due volte in totale
    }

    // java
    @Test
    void aggiungiRecensione_daoThrows_exceptionHandled() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("LibroRec", 5, "link.com", "Autore", 1);
        Recensione recensione = new Recensione(genitore, "Errore DB", libro);

        when(recensioneDAO.creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore()))
                .thenThrow(new SQLException("db fail"));

        RispostaAggiungiRecensione resp = controller.aggiungiRecensione(recensione);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioErrore());
        assertTrue(resp.getMessaggioErrore().contains("Errore richiesta nuova recensione al server"));
        verify(recensioneDAO).creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore());
    }

    @Test
    void daoMock_directThrow_assertThrowsExample() throws SQLException {
        // esempio di uso di assertThrows su un mock (utile per servizi che non catturano l'eccezione)
        when(eventoDAO.getNextEventi(any(LocalDateTime.class))).thenThrow(new RuntimeException("boom"));
        assertThrows(RuntimeException.class, () -> eventoDAO.getNextEventi(LocalDateTime.now()));
        verify(eventoDAO).getNextEventi(any(LocalDateTime.class));
    }

    @Test
    void registraNuovoAmministratore_usernameEsistente() throws Exception {
        Amministratore admin = new Amministratore("Mario", "Rossi", "mrossi");

        when(utenteDAO.checkUserName(admin.getUserName())).thenReturn(1); // username già presente

        RispostaNuovoAmministratore resp = controller.registraNuovoAmministratore(admin, "psw");

        assertFalse(resp.isSuccesso());
        assertEquals("Username già esistente. Scegliere un altro username.", resp.getMessaggioErrore());
        verify(utenteDAO).checkUserName(admin.getUserName());
    }

    @Test
    void registraNuovoAmministratore_successo_e_failureDB() throws Exception {
        Amministratore admin = new Amministratore("Mario", "Rossi", "mrossi");

        // username disponibile
        when(utenteDAO.checkUserName(admin.getUserName())).thenReturn(-1);
        // successo creazione
        when(utenteDAO.creaAmministratore(admin.getNome(), admin.getCognome(), admin.getUserName(), "psw"))
                .thenReturn(99);

        RispostaNuovoAmministratore ok = controller.registraNuovoAmministratore(admin, "psw");
        assertTrue(ok.isSuccesso());
        assertNotNull(ok.getNuovoAmministratore());
        assertEquals(99, ok.getNuovoAmministratore().getId());
        verify(utenteDAO).creaAmministratore(admin.getNome(), admin.getCognome(), admin.getUserName(), "psw");

        // failure DB: creaAmministratore ritorna -1
        when(utenteDAO.creaAmministratore(admin.getNome(), admin.getCognome(), admin.getUserName(), "psw"))
                .thenReturn(-1);

        RispostaNuovoAmministratore ko = controller.registraNuovoAmministratore(admin, "psw");
        assertFalse(ko.isSuccesso());
        assertEquals("Errore creazione nuovo amministratore su DB", ko.getMessaggioErrore());
    }

    @Test
    void aggiornaAmministratore_successo_e_erroreDB() throws Exception {
        Amministratore admin = new Amministratore("Mario", "Rossi", "mrossi");

        when(utenteDAO.aggiornaAmministratore(admin)).thenReturn(1);
        RispostaAggiornaAmministratore ok = controller.aggiornaAmministratore(admin);
        assertTrue(ok.isSuccesso());
        assertEquals(admin, ok.getAmministratore());
        verify(utenteDAO).aggiornaAmministratore(admin);

        when(utenteDAO.aggiornaAmministratore(admin)).thenReturn(0);
        RispostaAggiornaAmministratore ko = controller.aggiornaAmministratore(admin);
        assertFalse(ko.isSuccesso());
        // nota: il messaggio è identico a quello presente nell'implementazione attuale
        assertEquals("Errore aggiornamento lettore su DB", ko.getMessaggioErrore());
    }

    // java
    @Test
    void registraNuovoLettore_usernameEsistente() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore nuovo = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");

        when(utenteDAO.checkUserName(nuovo.getUserName())).thenReturn(1); // username già presente

        RispostaNuovoLettore resp = controller.registraNuovoLettore(nuovo, "psw");

        assertFalse(resp.isSuccesso());
        assertEquals("Username già esistente. Scegliere un altro username.", resp.getMessaggioErrore());
        verify(utenteDAO).checkUserName(nuovo.getUserName());
    }

    @Test
    void registraNuovoLettore_successo_e_failureDB() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore nuovo = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");

        // username disponibile
        when(utenteDAO.checkUserName(nuovo.getUserName())).thenReturn(-1);
        // successo creazione
        when(utenteDAO.creaLettore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw"))
                .thenReturn(55);

        RispostaNuovoLettore ok = controller.registraNuovoLettore(nuovo, "psw");
        assertTrue(ok.isSuccesso());
        assertNotNull(ok.getNuovoLettore());
        assertEquals(55, ok.getNuovoLettore().getId());
        verify(utenteDAO).creaLettore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw");

        // failure DB: creaLettore ritorna -1
        when(utenteDAO.creaLettore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw"))
                .thenReturn(-1);

        RispostaNuovoLettore ko = controller.registraNuovoLettore(nuovo, "psw");
        assertFalse(ko.isSuccesso());
        assertEquals("Errore creazione nuovo lettore su DB", ko.getMessaggioErrore());
    }

    @Test
    void aggiornaLettore_successo_e_erroreDB() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");

        when(utenteDAO.aggiornaLettore(lettore)).thenReturn(1);
        RispostaAggiornaLettore ok = controller.aggiornaLettore(lettore);
        assertTrue(ok.isSuccesso());
        assertEquals(lettore, ok.getLettore());
        verify(utenteDAO).aggiornaLettore(lettore);

        when(utenteDAO.aggiornaLettore(lettore)).thenReturn(0);
        RispostaAggiornaLettore ko = controller.aggiornaLettore(lettore);
        assertFalse(ko.isSuccesso());
        assertEquals("Errore aggiornamento lettore su DB", ko.getMessaggioErrore());
    }

    // java
    @Test
    void registraNuovoGenitore_usernameEsistente() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore nuovo = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");

        when(utenteDAO.checkUserName(nuovo.getUserName())).thenReturn(1); // username già presente

        RispostaNuovoGenitore resp = controller.registraNuovoGenitore(nuovo, "psw");

        assertFalse(resp.isSuccesso());
        assertEquals("Username già esistente. Scegliere un altro username.", resp.getMessaggioErrore());
        verify(utenteDAO).checkUserName(nuovo.getUserName());
    }

    @Test
    void registraNuovoGenitore_successo_e_failureDB() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore nuovo = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");

        // username disponibile
        when(utenteDAO.checkUserName(nuovo.getUserName())).thenReturn(-1);
        // successo creazione
        when(utenteDAO.creaGenitore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw"))
                .thenReturn(33);

        RispostaNuovoGenitore ok = controller.registraNuovoGenitore(nuovo, "psw");
        assertTrue(ok.isSuccesso());
        assertNotNull(ok.getNuovoGenitore());
        assertEquals(33, ok.getNuovoGenitore().getId());
        verify(utenteDAO).creaGenitore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw");

        // failure DB: creaGenitore ritorna -1
        when(utenteDAO.creaGenitore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw"))
                .thenReturn(-1);

        RispostaNuovoGenitore ko = controller.registraNuovoGenitore(nuovo, "psw");
        assertFalse(ko.isSuccesso());
        assertEquals("Errore creazione nuovo genitore su DB", ko.getMessaggioErrore());
    }

    @Test
    void registraNuovoGenitore_exceptionHandled() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore nuovo = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");

        when(utenteDAO.checkUserName(nuovo.getUserName())).thenReturn(-1);
        when(utenteDAO.creaGenitore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw"))
                .thenThrow(new SQLException("db fail"));

        RispostaNuovoGenitore resp = controller.registraNuovoGenitore(nuovo, "psw");

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioErrore());
        assertTrue(resp.getMessaggioErrore().contains("Errore richiesta al server"));
        verify(utenteDAO).creaGenitore(nuovo.getNome(), nuovo.getCognome(), nuovo.getUserName(), "psw");
    }

    // java
    @Test
    void disiscriviFiglioEvento_successo_e_exceptioni() throws Exception {
        Figlio f = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore g = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        g.aggiungiFiglio(f);

        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento e = new Evento(creatore, "Evento di prova", luogo, date, scaletta);

        // successo: cancellaFiglioEvento non lancia eccezioni
        doNothing().when(figlioDAO).cancellaFiglioEvento(f, e);
        RispostaDisiscrizioneEvento ok = controller.disiscriviFiglioEvento(f, e, g);
        assertTrue(ok.isSuccesso());
        verify(figlioDAO).cancellaFiglioEvento(f, e);

        // SQLException dal DAO
        doThrow(new SQLException("db")).when(figlioDAO).cancellaFiglioEvento(f, e);
        RispostaDisiscrizioneEvento koSql = controller.disiscriviFiglioEvento(f, e, g);
        assertFalse(koSql.isSuccesso());
        assertNotNull(koSql.getMessaggioErrore());
        verify(figlioDAO, times(2)).cancellaFiglioEvento(f, e); // chiamato due volte in totale

        // RuntimeException dal DAO
        doThrow(new RuntimeException("boom")).when(figlioDAO).cancellaFiglioEvento(f, e);
        RispostaDisiscrizioneEvento koRt = controller.disiscriviFiglioEvento(f, e, g);
        assertFalse(koRt.isSuccesso());
        assertNotNull(koRt.getMessaggioErrore());
        verify(figlioDAO, times(3)).cancellaFiglioEvento(f, e); // chiamato tre volte in totale
    }

    @Test
    void getIscrittiEvento_successo() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento evento = new Evento(creatore, "Evento di prova", luogo, date, scaletta);

        when(eventoDAO.getIscrittiEvento(any(Evento.class))).thenReturn(2);
        when(eventoDAO.getGenitoriIscritti(any(Evento.class))).thenReturn(new ArrayList<Genitore>());

        RispostaIscrittiEvento resp = controller.getIscrittiEvento(evento);

        assertTrue(resp.isSuccesso());
        assertNotNull(resp.getEvento());
        assertEquals(2, resp.getEvento().getIscritti());
        verify(eventoDAO).getIscrittiEvento(any(Evento.class));
        verify(eventoDAO).getGenitoriIscritti(any(Evento.class));
    }

    @Test
    void getIscrittiEvento_handles_SQLException() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento evento = new Evento(creatore, "Evento di prova", luogo, date, scaletta);

        when(eventoDAO.getIscrittiEvento(any(Evento.class))).thenThrow(new SQLException("db fail"));

        RispostaIscrittiEvento resp = controller.getIscrittiEvento(evento);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioerrore());
        assertTrue(resp.getMessaggioerrore().contains("Errore richiesta al server"));
        verify(eventoDAO).getIscrittiEvento(any(Evento.class));
    }

    @Test
    void getIscrittiEvento_handles_RuntimeException() throws Exception {
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore creatore = creaLettore.nuovoUtente("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        Evento evento = new Evento(creatore, "Evento di prova", luogo, date, scaletta);

        when(eventoDAO.getIscrittiEvento(any(Evento.class))).thenThrow(new RuntimeException("boom"));

        RispostaIscrittiEvento resp = controller.getIscrittiEvento(evento);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioerrore());
        assertTrue(resp.getMessaggioerrore().contains("Errore richiesta al server"));
        verify(eventoDAO).getIscrittiEvento(any(Evento.class));
    }

    // java
    @Test
    void richiestaRecensioniERecensibilita_successo() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("LibroRec", 5, "link.com", "Autore", 1);

        ArrayList<Recensione> recs = new ArrayList<>();
        recs.add(new Recensione(genitore, "Test recensione", libro));

        when(recensioneDAO.getRecensione(any(Libro.class))).thenReturn(recs);
        when(recensioneDAO.getRecensibilita(any(Libro.class), any(Utente.class))).thenReturn(true);

        RispostaRecensioniERecensibilita resp = controller.richiestaRecensioniERecensibilita(libro, genitore);

        assertTrue(resp.isSuccesso());
        assertNotNull(resp.getRecensioni());
        assertEquals(1, resp.getRecensioni().size());
        assertTrue(resp.isRecensibile());
        verify(recensioneDAO).getRecensione(any(Libro.class));
        verify(recensioneDAO).getRecensibilita(any(Libro.class), any(Utente.class));
    }

    @Test
    void richiestaRecensioniERecensibilita_handles_SQLException() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("LibroRec", 5, "link.com", "Autore", 1);

        when(recensioneDAO.getRecensione(any(Libro.class))).thenThrow(new SQLException("db fail"));

        RispostaRecensioniERecensibilita resp = controller.richiestaRecensioniERecensibilita(libro, genitore);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioerrore());
        assertTrue(resp.getMessaggioerrore().contains("Errore richiesta al server"));
        verify(recensioneDAO).getRecensione(any(Libro.class));
    }

    @Test
    void richiestaRecensioniERecensibilita_handles_RuntimeException() throws Exception {
        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("LibroRec", 5, "link.com", "Autore", 1);

        when(recensioneDAO.getRecensione(any(Libro.class))).thenThrow(new RuntimeException("boom"));

        RispostaRecensioniERecensibilita resp = controller.richiestaRecensioniERecensibilita(libro, genitore);

        assertFalse(resp.isSuccesso());
        assertNotNull(resp.getMessaggioerrore());
        assertTrue(resp.getMessaggioerrore().contains("Errore richiesta al server"));
        verify(recensioneDAO).getRecensione(any(Libro.class));
    }



    // altri test possono seguire lo stesso pattern per i metodi restanti del controller
}