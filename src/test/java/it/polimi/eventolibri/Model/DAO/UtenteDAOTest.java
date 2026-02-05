package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class UtenteDAOTest {

    Connection conn; // Connessione al database

    {
        try {
            conn = DBGestore.getConnection(); // Ottieni la connessione al database
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per il metodo checkCredentials
    @Test
    void checkCredentials() {
        try {
            // Crea un'istanza di UtenteDAO
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            // Verifica le credenziali per un genitore valido
            Genitore utentevalido = (Genitore) utenteDAO.checkCredentials("genitore2", "2222");
            // Controlli sulle proprietà del genitore
            assertNotNull(utentevalido);
            assertEquals("genitore2", utentevalido.getUserName());
            assertEquals(3, utentevalido.getId());
            assertEquals(2, utentevalido.getNumeroFigli());
            assertEquals(3, utentevalido.getFigli().get(0).getId());
            assertEquals(4, utentevalido.getFigli().get(1).getId());
            // Verifica le credenziali per un lettore valido
            Lettore lettorevalido = (Lettore) utenteDAO.checkCredentials("lettore13", "3333");
            // Controlli sulle proprietà del lettore
            assertNotNull(lettorevalido);
            // Verifica le credenziali per un amministratore valido
            Amministratore amministratorevalido = (Amministratore) utenteDAO.checkCredentials("admin2", "1111");
            // Controlli sulle proprietà dell'amministratore
            assertNotNull(amministratorevalido);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per il metodo checkUserName
    @Test
    void checkUserName() {
        try {
            // Crea un'istanza di UtenteDAO
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            // Verifica se il nome utente "admin" esiste
            assertEquals(-1, utenteDAO.checkUserName("admin"));
            // Verifica se il nome utente "admin1" esiste
            assertEquals(1, utenteDAO.checkUserName("admin1"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per la creazione, aggiornamento e cancellazione di un lettore
    @Test
    void creaLettore() {
        try {
            // Crea un'istanza di UtenteDAO e crea un nuovo lettore
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            utenteDAO.creaLettore("Luca", "Castagna", "lettore99", "3333");

            Lettore lettorevalido = (Lettore) utenteDAO.checkCredentials("lettore99", "3333");
            // Verifica le proprietà del lettore creato
            assertNotNull(lettorevalido);
            assertEquals("lettore99", lettorevalido.getUserName());
            assertEquals("Castagna", lettorevalido.getCognome());
            assertEquals("Luca", lettorevalido.getNome());
            // Aggiorna il nome del lettore e verifica l'aggiornamento
            lettorevalido.setNome("Andrea");
            utenteDAO.aggiornaLettore(lettorevalido);
            // Verifica che il nome sia stato aggiornato correttamente
            assertEquals("Andrea", lettorevalido.getNome());
            // Cancella il lettore e verifica che non esista più
            utenteDAO.cancellaLettore(lettorevalido);
            assertNull(utenteDAO.checkCredentials("lettore99", "3333"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per la creazione, aggiornamento e cancellazione di un amministratore
    @Test
    void creaAmministratore() {
        try {
            // Crea un'istanza di UtenteDAO e crea un nuovo amministratore
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            utenteDAO.creaAmministratore("Luca", "Castagna", "admin99", "1111");
            // Verifica le proprietà dell'amministratore creato
            Amministratore amministratorevalido = (Amministratore) utenteDAO.checkCredentials("admin99", "1111");
            assertNotNull(amministratorevalido);
            assertEquals("admin99", amministratorevalido.getUserName());
            assertEquals("Castagna", amministratorevalido.getCognome());
            assertEquals("Luca", amministratorevalido.getNome());
            // Aggiorna il nome dell'amministratore e verifica l'aggiornamento
            amministratorevalido.setNome("Andrea");
            utenteDAO.aggiornaAmministratore(amministratorevalido);
            assertEquals("Andrea", utenteDAO.checkCredentials("admin99", "1111").getNome());
            // Cancella l'amministratore e verifica che non esista più
            utenteDAO.cancellaAmministratore(amministratorevalido);
            assertNull(utenteDAO.checkCredentials("admin99", "1111"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test per la creazione, aggiornamento e cancellazione di un genitore
    @Test
    void creaGenitore() {
        try {
            // Crea un'istanza di UtenteDAO e crea un nuovo genitore
            UtenteDAO utenteDAO = new UtenteDAO(conn);
            FiglioDAO figlioDAO = new FiglioDAO(conn);
            int id_genitore = utenteDAO.creaGenitore("Alberto", "Castagna", "genitore99", "2222");

            CreaUtente<Genitore> creaGenitore = new CreaGenitore();
            Genitore genitorevalido11 = creaGenitore.nuovoUtente(id_genitore, "Alberto", "Castagna", "genitore99");
            // Aggiungi figli al genitore
            int id_figlio1 = figlioDAO.creaFiglio("Marco99", new Date(2015 - 01 - 03).toLocalDate(), (Genitore) utenteDAO.checkCredentials("genitore99", "2222"));
            int id_figlio2 = figlioDAO.creaFiglio("Alice99", new Date(2015 - 01 - 03).toLocalDate(), (Genitore) utenteDAO.checkCredentials("genitore99", "2222"));

            Figlio figlio1 = new Figlio(id_figlio1, "Marco99", new Date(2015 - 01 - 03).toLocalDate());
            Figlio figlio2 = new Figlio(id_figlio2, "Alice99", new Date(2015 - 01 - 03).toLocalDate());
            genitorevalido11.aggiungiFiglio(figlio1);
            genitorevalido11.aggiungiFiglio(figlio2);
            // Verifica le proprietà del genitore creato
            assertNotNull(genitorevalido11);
            assertEquals("genitore99", genitorevalido11.getUserName());
            assertEquals("Alberto", genitorevalido11.getNome());
            assertEquals("Castagna", genitorevalido11.getCognome());
            assertEquals(2, genitorevalido11.getNumeroFigli());
            // Recupera il genitore dal database e verifica le proprietà
            Genitore genitorevalido = (Genitore) utenteDAO.checkCredentials("genitore99", "2222");
            assertNotNull(genitorevalido);
            assertEquals("genitore99", genitorevalido.getUserName());
            assertEquals("Alberto", genitorevalido.getNome());
            assertEquals("Castagna", genitorevalido.getCognome());
            assertEquals(2, genitorevalido.getNumeroFigli());
            EventoDAO eventoDAO = new EventoDAO(conn);
            // Iscrivi i figli del genitore a un evento
            figlioDAO.iscriviFiglioEvento(genitorevalido.getFigli().get(0), eventoDAO.getEventoDaId(1));
            figlioDAO.iscriviFiglioEvento(genitorevalido.getFigli().get(1), eventoDAO.getEventoDaId(1));
            // Verifica le iscrizioni dei figli all'evento
            assertEquals(1, figlioDAO.getIscrizioni(genitorevalido.getFigli().get(0).getId()).size());
            assertEquals(1, figlioDAO.getIscrizioni(genitorevalido.getFigli().get(1).getId()).size());
            // Cancella l'iscrizione del primo figlio all'evento e verifica
            figlioDAO.cancellaFiglioEvento(genitorevalido.getFigli().get(0), eventoDAO.getEventoDaId(1));
            assertEquals(0, figlioDAO.getIscrizioni(genitorevalido.getFigli().get(0).getId()).size());
            // Aggiorna il nome del genitore e verifica l'aggiornamento
            genitorevalido.setNome("Andrea");
            utenteDAO.aggiornaGenitore(genitorevalido);
            assertEquals("Andrea", genitorevalido.getNome());
            // Cancella il genitore e verifica che non esista più
            utenteDAO.cancellaGenitore(genitorevalido);
            assertNull(utenteDAO.checkCredentials("genitore99", "2222"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}