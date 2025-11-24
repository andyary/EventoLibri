package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EventoDAOTest {

    @Test
    void getNextEventi() {
        try{
            Connection conn = DBGestore.getConnection();
            EventoDAO eventoDAO = new EventoDAO(conn);
            ArrayList<Evento> result1 = eventoDAO.getNextEventi(LocalDateTime.parse("2025-01-02T00:00:00"));
            assertNotNull(result1);
            if (result1.size() > 2) {
                ArrayList<Evento> result2 = eventoDAO.getNextEventi(result1.get(result1.size()-2));
                assertTrue(result2.getFirst().getId() == result1.getLast().getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void CRUD_Evento()  {
        Connection conn = null;
        try {
            conn = DBGestore.getConnection();
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            utenteDAO.creaLettore("Luca", "Castagna", "lettore99", "3333");
            Lettore creatore = (Lettore) utenteDAO.checkCredentials("lettore99", "3333");
            LuogoDAO luogoDAO = new LuogoDAO(conn);
            Luogo luogo = luogoDAO.getLuogo(3);
            EventoDAO eventoDAO = new EventoDAO(conn);
            int id_evento = eventoDAO.creaEvento(creatore, "Evento_Test", luogo, LocalDateTime.of(2026, 6, 15, 18, 0));
            // arrivati qui
            Lettore lettore1 = (Lettore) utenteDAO.checkCredentials("lettore1", "3333");
            Lettore lettore2 = (Lettore) utenteDAO.checkCredentials("lettore2", "3333");
            LibroDAO libroDAO = new LibroDAO(conn);
            Libro libro1 = libroDAO.getLibro(1);
            Libro libro2 = libroDAO.getLibro(2);
            ArrayList<LibroLettore> scaletta = new ArrayList<>();
            LibroLettore ll11 = new LibroLettore(libro1, lettore1,1);
            LibroLettore ll22 = new LibroLettore(libro2, lettore2,2);
            scaletta.add(ll11);
            scaletta.add(ll22);
            Evento evento = eventoDAO.getEventoDaId(id_evento);
            assertEquals(0, evento.getScaletta().size());

            LibroLettoreDAO librolettoreDAO = new LibroLettoreDAO(conn);
            librolettoreDAO.creaScaletta(evento, scaletta);
            Evento evento1 = eventoDAO.getEventoDaId(id_evento);
            assertEquals(2, evento1.getScaletta().size());

            LibroLettore ll2 = new LibroLettore(libro1, lettore2,1);
            LibroLettore l21 = new LibroLettore(libro2, lettore1,2);
            scaletta.add(l21);
            scaletta.add(ll2);
            evento.setScaletta(scaletta);
            eventoDAO.modificaEvento(evento);
            Evento evento2 = eventoDAO.getEventoDaId(id_evento);
            assertEquals(4, evento2.getScaletta().size());

            eventoDAO.cancellaEvento(id_evento);
            utenteDAO.cancellaLettore(creatore);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}