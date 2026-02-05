package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DBGestoreTest {

    // Test per il metodo getConnection
    @Test
    void getConnection() {
        try {
            // Connetti al database e crea una sessione per un lettore
            Connection conn = DBGestore.getConnection();
            Lettore lettore1 = new Lettore(2, "Luca", "Bianchi", "lucabianchi");
            Sessione sessione = new Sessione(lettore1);
            // Verifica che la connessione sia valida e che l'utente nella sessione sia corretto
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            DBGestore.closeConnection();
            assertTrue(conn.isClosed());
            assertEquals(lettore1, sessione.getUtente());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}