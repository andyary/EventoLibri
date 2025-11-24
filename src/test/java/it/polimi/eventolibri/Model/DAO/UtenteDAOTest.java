package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

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

    @Test
    void creaLettore() {
        try {
            Connection conn = DBGestore.getConnection();
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            utenteDAO.creaLettore("Luca", "Castagna", "lettore99", "3333");

            Lettore lettorevalido = (Lettore) utenteDAO.checkCredentials("lettore99", "3333");
            assertNotNull(lettorevalido);
            assertEquals("lettore99", lettorevalido.getUserName());
            assertEquals("Castagna", lettorevalido.getCognome());
            assertEquals("Luca", lettorevalido.getNome());
            utenteDAO.cancellaLettore(lettorevalido);
            assertNull(utenteDAO.checkCredentials("lettore99", "3333"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void creaGenitore() {
        try {
            Connection conn = DBGestore.getConnection();
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            int id_genitore = utenteDAO.creaGenitore("Alberto", "Castagna", "genitore99", "2222");

            CreaUtente<Genitore> creaGenitore = new CreaGenitore();
            Genitore genitorevalido11= creaGenitore.nuovoUtente(id_genitore,"Alberto", "Castagna", "genitore99");

            FiglioDAO figlioDAO = new FiglioDAO(conn);
            int id_figlio1 = figlioDAO.creaFiglio("Marco99", new Date(2015-01-03), (Genitore) utenteDAO.checkCredentials("genitore99", "2222"));
            int id_figlio2 = figlioDAO.creaFiglio("Alice99", new Date(2015-01-03), (Genitore) utenteDAO.checkCredentials("genitore99", "2222"));

            Figlio figlio1 = new Figlio(id_figlio1,"Marco99", new Date(2015-01-03).toLocalDate());
            Figlio figlio2 = new Figlio(id_figlio2,"Alice99", new Date(2015-01-03).toLocalDate());
            genitorevalido11.aggiungiFiglio(figlio1);
            genitorevalido11.aggiungiFiglio(figlio2);
            assertNotNull(genitorevalido11);
            assertEquals("genitore99", genitorevalido11.getUserName());
            assertEquals("Alberto", genitorevalido11.getNome());
            assertEquals("Castagna", genitorevalido11.getCognome());
            assertEquals(2, genitorevalido11.getNumeroFigli());

            Genitore genitorevalido = (Genitore) utenteDAO.checkCredentials("genitore99", "2222");
            assertNotNull(genitorevalido);
            assertEquals("genitore99", genitorevalido.getUserName());
            assertEquals("Alberto", genitorevalido.getNome());
            assertEquals("Castagna", genitorevalido.getCognome());
            assertEquals(2, genitorevalido.getNumeroFigli());
            EventoDAO eventoDAO = new EventoDAO(conn);

            figlioDAO.iscriviFiglioEvento(genitorevalido.getFigli().get(0), eventoDAO.getEventoDaId(1));
            figlioDAO.iscriviFiglioEvento(genitorevalido.getFigli().get(1), eventoDAO.getEventoDaId(1));

            utenteDAO.cancellaGenitore(genitorevalido);
            assertNull(utenteDAO.checkCredentials("genitore99", "2222"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}