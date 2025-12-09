package it.polimi.eventolibri.Controller;

import it.polimi.eventolibri.Message.RispostaIscrittiEvento;
import it.polimi.eventolibri.Message.RispostaIscrizioneEvento;
import it.polimi.eventolibri.Message.RispostaLogin;
import it.polimi.eventolibri.Message.RispostaNextEventi;
import it.polimi.eventolibri.Model.DAO.*;
import it.polimi.eventolibri.Model.DBGestore;
import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

import java.sql.Connection;
import java.time.LocalDateTime;

public class Controller {

    private UtenteDAO utenteDAO;
    private EventoDAO eventoDAO;
    private FiglioDAO figlioDAO;
    private LibroDAO libroDAO;
    private LibroLettoreDAO libroLettoreDAO;
    private LuogoDAO luogoDAO;
    private RecensioneDAO recensioneDAO;

    public Controller() {
        try {
            Connection con = DBGestore.getConnection();
            utenteDAO = new UtenteDAO(con);
            eventoDAO = new EventoDAO(con);
            figlioDAO = new FiglioDAO(con);
            libroDAO = new LibroDAO(con);
            libroLettoreDAO = new LibroLettoreDAO(con);
            luogoDAO = new LuogoDAO(con);
            recensioneDAO = new RecensioneDAO(con);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RispostaLogin controllaLogin(String username, String password) {
        RispostaLogin risposta = new RispostaLogin();
        try {
            if (utenteDAO.checkCredentials(username, password) != null) {
                risposta.setSuccesso(true);
                risposta.setUtente(utenteDAO.checkCredentials(username, password));
                risposta.setProssimiEventi(eventoDAO.getNextEventi(LocalDateTime.now()));
            } else {
                risposta.setSuccesso(false);
                risposta.setMessaggioerrore("Credenziali non valide.");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioerrore("Errore richiesta al server.");
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaNextEventi getNextEventi(Evento ultimoEvento) {
        RispostaNextEventi risposta = new RispostaNextEventi();
        try {
            risposta.setProssimiEventi(eventoDAO.getNextEventi(ultimoEvento));
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server.");
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaIscrizioneEvento iscriviFiglioEvento(Figlio figlio, Evento evento, Genitore genitore) {
        RispostaIscrizioneEvento risposta = new RispostaIscrizioneEvento(figlio, evento, genitore);
        try {
            figlioDAO.iscriviFiglioEvento(figlio, evento);
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaIscrittiEvento getIscrittiEvento(Evento evento) {
        RispostaIscrittiEvento risposta = new RispostaIscrittiEvento(evento);
        try {
            risposta.getEvento().setIscritti(eventoDAO.getIscrittiEvento(evento));
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioerrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }
}
