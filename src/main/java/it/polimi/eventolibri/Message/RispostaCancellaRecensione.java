package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

/** Messaggio dal server al client di risposta per la cancellazione di una recensione dal database.
 */
public class RispostaCancellaRecensione extends Messaggio {

    private boolean successo;
    private String messaggioErrore;
    private int id_recensione;

    /** Costruttore della classe RispostaCancellaRecensione.
     * @param r la recensione da cancellare.
     */
    public RispostaCancellaRecensione(Recensione r) {
        this.id_recensione = r.getId();
    }

    /** Ritorna lo stato di successo dell'operazione di cancellazione della recensione dal database.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di cancellazione della recensione dal database.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore associato all'operazione di cancellazione della recensione dal database.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore associato all'operazione di cancellazione della recensione dal database.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Ritorna l'id della recensione cancellata dal database.
     * @return l'id della recensione.
     */
    public int getId() {
        return id_recensione;
    }

    /** Imposta l'id della recensione cancellata dal database.
     * @param id_recensione l'id della recensione.
     */
    public  void setId(int id_recensione) {
        this.id_recensione = id_recensione;
    }
}
