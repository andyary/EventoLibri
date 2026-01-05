package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

/** Messaggio di risposta dal server al client per la creazione o il salvataggio di un evento nel database.
 * Contiene informazioni sul successo dell'operazione e, in caso di successo,
 * i dettagli dell'evento creato o salvato.
 */
public class RispostaSalvaEvento extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Evento evento;

    /** Ritorna lo stato di successo dell'operazione di creazione o salvataggio dell'evento.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di creazione o salvataggio dell'evento.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore in caso di fallimento dell'operazione.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore in caso di fallimento dell'operazione.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Ritorna l'evento creato o salvato.
     * @return l'evento.
     */
    public Evento getEvento() {
        return evento;
    }

    /** Imposta l'evento creato o salvato.
     * @param evento l'evento.
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }
}
