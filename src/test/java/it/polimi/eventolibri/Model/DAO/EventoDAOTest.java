package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.DBGestore;
import it.polimi.eventolibri.Model.Evento;
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


}