package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

/** Messaggio inviato dal client al server per richiedere il salvataggio di un evento nuovo e modificato.
 */
public class RichiestaSalvaEvento extends Messaggio{
    private Evento evento;

    /** Costruttore della classe RichiestaSalvaEvento.
     * @param evento Evento da salvare (nuovo o modificato).
     */
    public RichiestaSalvaEvento(Evento evento) {
        this.evento = evento;
    }

    /** Restituisce l'evento da salvare (nuovo o modificato).
     * @return Evento specificato.
     */
    public Evento getEvento() {
        return evento;
    }
}
