package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

/**
 * Messaggio dal server al client di risposta per l'aggiunta al database di una nuova recensione.
 */
public class RispostaAggiungiRecensione extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Recensione recensione;

    /** Ritorna lo stato di successo dell'operazione di aggiunta della recensione al database.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di aggiunta della recensione al database.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore associato all'operazione di aggiunta della recensione al database.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore associato all'operazione di aggiunta della recensione al database.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Ritorna la recensione aggiunta al database.
     * @return la recensione.
     */
    public Recensione getRecensione() {
        return recensione;
    }

    /** Imposta la recensione aggiunta al database.
     * @param recensione la recensione.
     */
    public void setRecensione(Recensione recensione) {
        this.recensione = recensione;
    }
}
