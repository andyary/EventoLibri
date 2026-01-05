package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

import java.util.ArrayList;

/** Messaggio di risposta dal server al clint contenente le recensioni di un libro prelevate dal database e l'informazione se l'utente può recensirlo
 * Estende la classe Messaggio
 */
public class RispostaRecensioniERecensibilita extends Messaggio {
    private boolean successo;
    private String messaggioerrore;
    private ArrayList<Recensione> recensioni;
    private boolean recensibile;

    /** Ritorna lo stato di successo dell'operazione
    * @return successo true se l'operazione è andata a buon fine, false altrimenti
    */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione
    * @param successo true se l'operazione è andata a buon fine, false altrimenti
    */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore in caso di fallimento dell'operazione
    * @return messaggioerrore il messaggio di errore
    */
    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    /** Imposta il messaggio di errore in caso di fallimento dell'operazione
    * @param messaggioerrore il messaggio di errore
    */
    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }

    /** Ritorna la lista delle recensioni del libro
    * @return recensioni la lista delle recensioni
    */
    public ArrayList<Recensione> getRecensioni() {
        return recensioni;
    }

    /** Imposta la lista delle recensioni del libro
    * @param recensioni la lista delle recensioni
    */
    public void setRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    /** Ritorna se l'utente può recensire il libro
    * @return recensibile true se l'utente può recensire il libro, false altrimenti
    */
    public boolean isRecensibile() {
        return recensibile;
    }

    /** Imposta se l'utente può recensire il libro
    * @param recensibile true se l'utente può recensire il libro, false altrimenti
    */
    public void setRecensibile(boolean recensibile) {
        this.recensibile = recensibile;
    }
}
