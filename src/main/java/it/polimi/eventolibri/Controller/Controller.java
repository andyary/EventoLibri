package it.polimi.eventolibri.Controller;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Model.DAO.*;

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

    public RispostaDisiscrizioneEvento disiscriviFiglioEvento(Figlio figlio, Evento evento, Genitore genitore) {
        RispostaDisiscrizioneEvento risposta = new RispostaDisiscrizioneEvento(figlio, evento, genitore);
        try {
            figlioDAO.cancellaFiglioEvento(figlio, evento);
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

    public RispostaAggiornaGenitore aggiornaGenitore(Genitore genitore) {
        RispostaAggiornaGenitore risposta = new RispostaAggiornaGenitore();
        try {
            if (utenteDAO.aggiornaGenitore(genitore)==1) {
                risposta.setSuccesso(true);
                risposta.setGenitore(genitore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore aggiornamento genitore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaAggiungiFiglio aggiungiFiglio(Genitore genitore, Figlio nuovoFiglio) {
        RispostaAggiungiFiglio risposta = new RispostaAggiungiFiglio();
        try {
            int id = figlioDAO.creaFiglio(nuovoFiglio.getNome(), nuovoFiglio.getDataNascita(), genitore);
            if (id !=-1) {
                nuovoFiglio.setId(id);
                risposta.setSuccesso(true);
                risposta.setGenitore(genitore);
                risposta.setNuovoFiglio(nuovoFiglio);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione figlio su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaNuovoGenitore registraNuovoGenitore(Genitore nuovoGenitore, String psw) {
        RispostaNuovoGenitore risposta = new RispostaNuovoGenitore();
        try {
            int id = utenteDAO.creaGenitore(nuovoGenitore.getNome(), nuovoGenitore.getCognome(), nuovoGenitore.getUserName(), psw);
            if (id !=-1) {
                nuovoGenitore.setId(id);
                risposta.setSuccesso(true);
                risposta.setNuovoGenitore(nuovoGenitore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione nuovo genitore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaNuovoLettore registraNuovoLettore(Lettore nuovoLettore, String psw) {
        RispostaNuovoLettore risposta = new RispostaNuovoLettore();
        try {
            int id = utenteDAO.creaLettore(nuovoLettore.getNome(), nuovoLettore.getCognome(),
                    nuovoLettore.getUserName(), psw);
            if (id !=-1) {
                nuovoLettore.setId(id);
                risposta.setSuccesso(true);
                risposta.setNuovoLettore(nuovoLettore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione nuovo lettore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaLettoriELuoghi richiestaLettoriELuoghi() {
        RispostaLettoriELuoghi risposta = new RispostaLettoriELuoghi();
        try {
            risposta.setLuoghi(luogoDAO.getLuoghi());
            risposta.setLettori(utenteDAO.getLettori());
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaAggiornaLettore aggiornaLettore(Lettore lettore) {
        RispostaAggiornaLettore risposta = new RispostaAggiornaLettore();
        try {
            if (utenteDAO.aggiornaLettore(lettore)==1) {
                risposta.setSuccesso(true);
                risposta.setLettore(lettore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore aggiornamento lettore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

    public RispostaAggiornaAmministratore aggiornaAmministratore(Amministratore amministratore) {
        RispostaAggiornaAmministratore risposta = new RispostaAggiornaAmministratore();
        try {
            if (utenteDAO.aggiornaAmministratore(amministratore)==1) {
                risposta.setSuccesso(true);
                risposta.setAmministratore(amministratore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore aggiornamento lettore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            e.printStackTrace();
        }
        return risposta;
    }

}
