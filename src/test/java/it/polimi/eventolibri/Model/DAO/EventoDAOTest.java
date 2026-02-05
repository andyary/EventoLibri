package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EventoDAOTest {

    Connection conn; // Connessione al database

    {
        try { // Inizializza la connessione al database
            conn = DBGestore.getConnection(); // Ottieni la connessione al database
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per il metodo getNextEventi
    @Test
    void getNextEventi() {
        try { // Prova a eseguire il test
            EventoDAO eventoDAO = new EventoDAO(conn);
            ArrayList<Evento> result1 = eventoDAO.getNextEventi(LocalDateTime.parse("2025-01-02T00:00:00"));
            assertNotNull(result1); // Verifica che il risultato non sia nullo
            if (result1.size() > 2) { // Se ci sono più di 2 eventi
                ArrayList<Evento> result2 = eventoDAO.getNextEventi(result1.get(result1.size() - 2));
                assertTrue(result2.getFirst().getId() == result1.getLast().getId()); // Verifica che il primo evento del secondo risultato sia uguale all'ultimo del primo
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per le operazioni di lettura, creazione, aggiornamento e cancellazione di un evento
    @Test
    void CRUD_Evento() {
        try { // Prova a eseguire il test
            conn = DBGestore.getConnection();
            // Creazione di un lettore e un genitore per il test
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            utenteDAO.creaLettore("Luca", "Castagna", "lettore99", "3333");
            Lettore creatore = (Lettore) utenteDAO.checkCredentials("lettore99", "3333");
            utenteDAO.creaGenitore("Luca", "Castagna", "genitore99", "2222");
            Genitore genitore = (Genitore) utenteDAO.checkCredentials("genitore99", "2222");
            // Creazione di un luogo per l'evento
            LuogoDAO luogoDAO = new LuogoDAO(conn);
            Luogo luogo = luogoDAO.getLuogo(3);
            // Creazione dell'evento
            EventoDAO eventoDAO = new EventoDAO(conn);
            int id_evento = eventoDAO.creaEvento(creatore, "Evento_Test", luogo, LocalDateTime.of(2026, 6, 15, 18, 0));
            // Creazione di due lettori e due libri
            Lettore lettore1 = (Lettore) utenteDAO.checkCredentials("lettore1", "3333");
            Lettore lettore2 = (Lettore) utenteDAO.checkCredentials("lettore2", "3333");
            LibroDAO libroDAO = new LibroDAO(conn);
            Libro libro1 = libroDAO.getLibro(3);
            Libro libro2 = libroDAO.getLibro(4);
            // Creazione di recensioni per i libri
            RecensioneDAO recensioneDAO = new RecensioneDAO(conn);
            recensioneDAO.creaRecensione("Ottimo libro!", libro1, genitore);
            recensioneDAO.creaRecensione("Non mi è piaciuto.", libro2, genitore);

            ArrayList<Recensione> recensione2 = recensioneDAO.getRecensione(libro2);
            // Verifiche
            assertEquals(1, libroDAO.getLibro(3).getRecensioni().size());
            assertEquals(1, libroDAO.getLibro(4).getRecensioni().size());
            // Creazione della scaletta dell'evento
            ArrayList<LibroLettore> scaletta = new ArrayList<>();
            LibroLettore ll11 = new LibroLettore(libro1, lettore1, 1);
            LibroLettore ll22 = new LibroLettore(libro2, lettore2, 2);
            scaletta.add(ll11);
            scaletta.add(ll22);
            Evento evento = eventoDAO.getEventoDaId(id_evento);
            assertEquals(0, evento.getScaletta().size()); // verifica che la scaletta sia vuota
            // Salvataggio della scaletta nel database
            LibroLettoreDAO librolettoreDAO = new LibroLettoreDAO(conn);
            evento.setScaletta(scaletta);
            librolettoreDAO.creaScaletta(evento);
            Evento evento1 = eventoDAO.getEventoDaId(id_evento);
            assertEquals(2, evento1.getScaletta().size()); // verifica che la scaletta sia stata salvata correttamente
            // Modifica della scaletta dell'evento
            LibroLettore ll2 = new LibroLettore(libro1, lettore2, 3);
            LibroLettore l21 = new LibroLettore(libro2, lettore1, 4);
            scaletta.add(l21);
            scaletta.add(ll2);
            evento.setScaletta(scaletta);
            eventoDAO.modificaEvento(evento);

            Evento evento2 = eventoDAO.getEventoDaId(id_evento); // recupera l'evento aggiornato
            assertEquals(4, evento2.getScaletta().size()); // verifica che la scaletta sia stata aggiornata correttamente
            // Pulizia del database
            recensioneDAO.cancellaRecensione(libro1, genitore);
            recensioneDAO.cancellaRecensione(recensione2.getFirst());
            // Verifica che le recensioni siano state cancellate
            assertEquals(0, libroDAO.getLibro(3).getRecensioni().size());
            assertEquals(0, libroDAO.getLibro(4).getRecensioni().size());
            // Cancellazione dell'evento, del lettore e del genitore creati per il test
            eventoDAO.cancellaEvento(evento);
            utenteDAO.cancellaLettore(creatore);
            utenteDAO.cancellaGenitore(genitore);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per il metodo eventiInConflitto
    @Test
    void eventiInConflitto() {
        try {   // Prova a eseguire il test
            EventoDAO eventoDAO = new EventoDAO(conn);
            Evento evento = eventoDAO.getEventoDaId(1);
            ArrayList<Evento> conflitti = eventoDAO.eventiInConflitto(evento);
            assertNotNull(conflitti); // Verifica che il risultato non sia nullo
            assertEquals(0, conflitti.size()); // Verifica che non ci siano conflitti
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per i metodi getLuoghi, getLibri e getLettori
    @Test
    void getLuoghieLibrieLettori() {
        // Prova a eseguire il test
        try {
            // Inizializza i DAO necessari
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            LuogoDAO luogoDAO = new LuogoDAO(conn);
            LibroDAO libroDAO = new LibroDAO(conn);
            ArrayList<Luogo> luoghi = new ArrayList<>();
            ArrayList<Libro> libri = new ArrayList<>();
            ArrayList<Lettore> lettori = new ArrayList<>();
            // verifica che inizialmente non ci siano luoghi, libri o lettori
            assertEquals(0, luoghi.size());
            assertEquals(0, libri.size());
            assertEquals(0, lettori.size());
            // recupera i luoghi, i libri e i lettori dal database
            luoghi = luogoDAO.getLuoghi();
            libri = libroDAO.getLibri();
            lettori = utenteDAO.getLettori();
            // verifica che siano stati recuperati 10 luoghi, 100 libri
            assertEquals(10, luoghi.size());
            assertEquals(100, libri.size());
            // verifica che i luoghi, i libri e i lettori non siano nulli
            assertNotNull(luoghi);
            assertNull(luogoDAO.getLuogo(11));
            assertNull(libroDAO.getLibro(101));
            assertNotNull(lettori);
        } catch (SQLException ec) {
        }
    }

    // Test per il metodo getRecensibilita
    @Test
    void getRecensibilita() {
        try {
            // Inizializza i DAO necessari
            RecensioneDAO recensioneDAO = new RecensioneDAO(conn);
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            LibroDAO libroDAO = new LibroDAO(conn);
            // recupera due libri e un genitore dal database
            Libro libro1 = libroDAO.getLibro(5);
            Libro libro2 = libroDAO.getLibro(22);
            Genitore genitore1 = (Genitore) utenteDAO.checkCredentials("genitore1", "2222");
            // verifica la recensibilità dei due libri per il genitore
            assertTrue(recensioneDAO.getRecensibilita(libro1, genitore1));
            assertFalse(recensioneDAO.getRecensibilita(libro2, genitore1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}