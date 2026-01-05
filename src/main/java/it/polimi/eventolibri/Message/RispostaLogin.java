package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Utente;

import java.util.ArrayList;

/** Classe che rappresenta la risposta al tentativo di login.
 * Contiene informazioni sul successo del login, l'utente loggato,
 * eventuali messaggi di errore e una lista di prossimi eventi.
 */
public class RispostaLogin extends Messaggio {

    private boolean successo;
    private Utente utente;
    private String messaggioerrore;
    private ArrayList<Evento> prossimiEventi;

    /** Ritorna se il login è avvenuto con successo.
     * @return true se il login è riuscito, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo del login.
     * @param successo true se il login è riuscito, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna l'utente loggato.
     * @return l'utente loggato.
     */
    public Utente getUtente() {
        return utente;
    }

    /** Imposta l'utente loggato.
     * @param utente l'utente loggato.
     */
    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    /** Ritorna il messaggio di errore alla richiesta di dati al database.
     * @return il messaggio di errore.
     */
    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    /** Imposta il messaggio di errore alla richiesta di dati al database.
     * @param messaggioerrore il messaggio di errore.
     */
    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }

    /** Ritorna la lista dei prossimi eventi.
     * @return la lista dei prossimi eventi.
     */
    public ArrayList<Evento> getProssimiEventi() {
        return prossimiEventi;
    }

    /** Imposta la lista dei prossimi eventi.
     * @param prossimiEventi la lista dei prossimi eventi.
     */
    public void setProssimiEventi(ArrayList<Evento> prossimiEventi) {
        this.prossimiEventi = prossimiEventi;
    }
}
