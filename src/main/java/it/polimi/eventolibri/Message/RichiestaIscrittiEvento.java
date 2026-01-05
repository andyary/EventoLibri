package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

/** Messaggio inviato dal client al server per richiedere il numero di figli iscritti ad un evento
 * e la lista dei genitori iscritti allo stesso evento (per creazione listeners).
 */
public class RichiestaIscrittiEvento  extends Messaggio{
    private Evento evento;

    /** Costruttore della classe RichiestaIscrittiEvento.
     * @param evento Evento di cui si vogliono conoscere gli iscritti.
     */
    public RichiestaIscrittiEvento(Evento evento) {
        this.evento = evento;
    }

    /** Restituisce l'evento di cui si vogliono conoscere gli iscritti.
     * @return Evento della richiesta.
     */
    public Evento getEvento() {
        return evento;
    }
}
