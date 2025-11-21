package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UtenteDAOTest {

    @Test
    void checkCredentials() {
        try {
            Connection conn = DBGestore.getConnection();
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            Genitore utentevalido = (Genitore) utenteDAO.checkCredentials("genitore2", "2222");
            assertNotNull(utentevalido);
            assertEquals("genitore2", utentevalido.getUserName());
            assertEquals(3, utentevalido.getId());
            assertEquals(2, utentevalido.getNumeroFigli());
            assertEquals(3, utentevalido.getFigli().get(0).getId());
            assertEquals(4, utentevalido.getFigli().get(1).getId());

            Lettore lettorevalido = (Lettore) utenteDAO.checkCredentials("lettore13", "3333");
            assertNotNull(lettorevalido);
            // valutarer altri controlli

            Amministratore amministratorevalido = (Amministratore) utenteDAO.checkCredentials("admin2", "1111");
            assertNotNull(amministratorevalido);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}