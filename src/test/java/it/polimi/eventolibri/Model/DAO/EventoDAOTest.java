package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.DBGestore;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Lettore;
import it.polimi.eventolibri.Model.Luogo;
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
            eventoDAO.creaEvento(creatore, "EventoTest", luogo, LocalDateTime.of(2026, 6, 15, 18, 0));
            // arrivati qui


            utenteDAO.cancellaLettore(creatore);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}