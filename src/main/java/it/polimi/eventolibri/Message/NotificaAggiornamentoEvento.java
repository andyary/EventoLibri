package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

/** Messaggio inviato dal server ai client per notificare l'aggiornamento di un evento.
 */
public class NotificaAggiornamentoEvento extends Messaggio{

    private Evento evento;

    /** Costruttore della classe NotificaAggiornamentoEvento.
     * @param evento L'evento aggiornato da notificare ai client.
     */
    public NotificaAggiornamentoEvento(Evento evento) {
        this.evento = evento;
    }

    /** Restituisce l'evento aggiornato.
     * @return L'evento aggiornato.
     */
    public Evento getEvento() {
        return evento;
    }
}
