package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

import java.util.ArrayList;

/** Classe che rappresenta la risposta del server alla richiesta di eventi successivi.
 * Contiene un flag di successo, un messaggio di errore (se presente) e una lista di eventi.
 */
public class RispostaNextEventi extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private ArrayList<Evento> prossimiEventi;

    /** Ritorna il flag di successo della risposta.
     * @return true se la richiesta è andata a buon fine, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta il flag di successo della risposta.
     * @param successo true se la richiesta è andata a buon fine, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore della risposta.
     * @return il messaggio di errore, o null se non c'è stato errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore della risposta.
     * @param messaggioErrore il messaggio di errore da impostare.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Ritorna la lista degli eventi successivi.
     * @return la lista degli eventi successivi.
     */
    public ArrayList<Evento> getProssimiEventi() {
        return prossimiEventi;
    }

    /** Imposta la lista degli eventi successivi.
     * @param prossimiEventi la lista degli eventi successivi da impostare.
     */
    public void setProssimiEventi(ArrayList<Evento> prossimiEventi) {
        this.prossimiEventi = prossimiEventi;
    }
}
