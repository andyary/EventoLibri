package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class EventoDAOTest {

    Connection conn;

    {
        try {
            conn = DBGestore.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getNextEventi() {
        try {
            EventoDAO eventoDAO = new EventoDAO(conn);
            ArrayList<Evento> result1 = eventoDAO.getNextEventi(LocalDateTime.parse("2025-01-02T00:00:00"));
            assertNotNull(result1);
            if (result1.size() > 2) {
                ArrayList<Evento> result2 = eventoDAO.getNextEventi(result1.get(result1.size() - 2));
                assertTrue(result2.getFirst().getId() == result1.getLast().getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void CRUD_Evento() {
        try {
            conn = DBGestore.getConnection();
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            utenteDAO.creaLettore("Luca", "Castagna", "lettore99", "3333");
            Lettore creatore = (Lettore) utenteDAO.checkCredentials("lettore99", "3333");
            utenteDAO.creaGenitore("Luca", "Castagna", "genitore99", "2222");
            Genitore genitore = (Genitore) utenteDAO.checkCredentials("genitore99", "2222");
            LuogoDAO luogoDAO = new LuogoDAO(conn);
            Luogo luogo = luogoDAO.getLuogo(3);
            EventoDAO eventoDAO = new EventoDAO(conn);
            int id_evento = eventoDAO.creaEvento(creatore, "Evento_Test", luogo, LocalDateTime.of(2026, 6, 15, 18, 0));

            Lettore lettore1 = (Lettore) utenteDAO.checkCredentials("lettore1", "3333");
            Lettore lettore2 = (Lettore) utenteDAO.checkCredentials("lettore2", "3333");
            LibroDAO libroDAO = new LibroDAO(conn);
            Libro libro1 = libroDAO.getLibro(3);
            Libro libro2 = libroDAO.getLibro(4);

            RecensioneDAO recensioneDAO = new RecensioneDAO(conn);
            recensioneDAO.creaRecensione("Ottimo libro!", libro1, genitore);
            recensioneDAO.creaRecensione("Non mi è piaciuto.", libro2, genitore);

            ArrayList<Recensione> recensione2 = recensioneDAO.getRecensione(libro2);

            assertEquals(1, libroDAO.getLibro(3).getRecensioni().size());
            assertEquals(1, libroDAO.getLibro(4).getRecensioni().size());

            ArrayList<LibroLettore> scaletta = new ArrayList<>();
            LibroLettore ll11 = new LibroLettore(libro1, lettore1, 1);
            LibroLettore ll22 = new LibroLettore(libro2, lettore2, 2);
            scaletta.add(ll11);
            scaletta.add(ll22);
            Evento evento = eventoDAO.getEventoDaId(id_evento);
            assertEquals(0, evento.getScaletta().size());

            LibroLettoreDAO librolettoreDAO = new LibroLettoreDAO(conn);
            evento.setScaletta(scaletta);
            librolettoreDAO.creaScaletta(evento);
            Evento evento1 = eventoDAO.getEventoDaId(id_evento);
            assertEquals(2, evento1.getScaletta().size());

            LibroLettore ll2 = new LibroLettore(libro1, lettore2, 3);
            LibroLettore l21 = new LibroLettore(libro2, lettore1, 4);
            scaletta.add(l21);
            scaletta.add(ll2);
            evento.setScaletta(scaletta);
            eventoDAO.modificaEvento(evento);
            Evento evento2 = eventoDAO.getEventoDaId(id_evento);
            assertEquals(4, evento2.getScaletta().size());

            recensioneDAO.cancellaRecensione(libro1, genitore);
            recensioneDAO.cancellaRecensione(recensione2.getFirst());

            assertEquals(0, libroDAO.getLibro(3).getRecensioni().size());
            assertEquals(0, libroDAO.getLibro(4).getRecensioni().size());

            eventoDAO.cancellaEvento(evento);
            utenteDAO.cancellaLettore(creatore);
            utenteDAO.cancellaGenitore(genitore);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void eventiInConflitto() {
        try {
            EventoDAO eventoDAO = new EventoDAO(conn);
            Evento evento = eventoDAO.getEventoDaId(1);
            ArrayList<Evento> conflitti = eventoDAO.eventiInConflitto(evento);
            assertNotNull(conflitti);
            assertEquals(0, conflitti.size());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getLuoghieLibrieLettori() {
        try {
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            LuogoDAO luogoDAO = new LuogoDAO(conn);
            LibroDAO libroDAO = new LibroDAO(conn);
            ArrayList<Luogo> luoghi = new ArrayList<>();
            ArrayList<Libro> libri = new ArrayList<>();
            ArrayList<Lettore> lettori = new ArrayList<>();

            assertEquals(0, luoghi.size());
            assertEquals(0, libri.size());
            assertEquals(0, lettori.size());

            luoghi = luogoDAO.getLuoghi();
            libri = libroDAO.getLibri();
            lettori = utenteDAO.getLettori();

            assertEquals(10, luoghi.size());
            assertEquals(100, libri.size());
            assertNotNull(luoghi);
            assertNull(luogoDAO.getLuogo(11));
            assertNull(libroDAO.getLibro(101));

        } catch (SQLException ec) {
        }
    }

    @Test
    void getRecensibilita() {
        try {
            RecensioneDAO recensioneDAO = new RecensioneDAO(conn);
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            LibroDAO libroDAO = new LibroDAO(conn);
            Libro libro1 = libroDAO.getLibro(5);
            Libro libro2 = libroDAO.getLibro(22);
            Genitore genitore1 = (Genitore) utenteDAO.checkCredentials("genitore1", "2222");
            assertTrue(recensioneDAO.getRecensibilita(libro1, genitore1));
            assertFalse(recensioneDAO.getRecensibilita(libro2, genitore1));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}