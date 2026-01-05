package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

/** Messaggio dal server al client di risposta per la richiesta del numero di iscritti ad un evento ed
 * della lista di genitori i cui figli sono gli iscritti allo stesso evento.
 */
public class RispostaIscrittiEvento extends Messaggio{
    private Evento evento;
    private boolean successo;
    private String messaggioerrore;

    /** Costruttore della classe RispostaIscrittiEvento.
     * @param evento l'evento di cui si richiede il numero di iscritti e la lista di genitori i cui figli sono gli iscritti.
     */
    public RispostaIscrittiEvento(Evento evento) {
        this.evento = evento;
    }

    /** Ritorna l'evento di cui si richiede il numero di iscritti e la lista di genitori i cui figli sono gli iscritti.
     * @return l'evento.
     */
    public Evento getEvento() {
        return evento;
    }

    /** Ritorna lo stato di successo dell'operazione di richiesta del numero di iscritti e della lista di genitori i cui figli sono gli iscritti all'evento.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di richiesta del numero di iscritti e della lista di genitori i cui figli sono gli iscritti all'evento.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore associato all'operazione di richiesta del numero di iscritti e della lista di genitori i cui figli sono gli iscritti all'evento.
     * @return il messaggio di errore.
     */
    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    /** Imposta il messaggio di errore associato all'operazione di richiesta del numero di iscritti e della lista di genitori i cui figli sono gli iscritti all'evento.
     * @param messaggioerrore il messaggio di errore.
     */
    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }

    /** Imposta l'evento di cui si richiede il numero di iscritti e la lista di genitori i cui figli sono gli iscritti.
     * @param evento l'evento.
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }
}
