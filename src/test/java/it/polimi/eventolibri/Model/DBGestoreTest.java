package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DBGestoreTest {

    @Test
    void getConnection() {
        try {
            Connection conn = DBGestore.getConnection();
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            DBGestore.closeConnection();
            assertTrue(conn.isClosed());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void closeConnection() {
    }
}