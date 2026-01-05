package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

/** Messaggio inviato dal client al server per richiedere i prossimi eventi
 * a partire da un evento specifico (per data successiva).
 */
public class RichiestaNextEventi extends Messaggio {
    private Evento ultimoEvento;

    /** Costruttore della classe RichiestaNextEventi.
     * @param ultimoEvento Evento a partire dal quale richiedere i prossimi eventi.
     */
    public RichiestaNextEventi(Evento ultimoEvento) {
        this.ultimoEvento = ultimoEvento;
    }

    /** Restituisce l'evento a partire dal quale richiedere i prossimi eventi.
     * @return Evento specificato.
     */
    public Evento getUltimoEvento() {
        return ultimoEvento;
    }

    /** Imposta l'evento a partire dal quale richiedere i prossimi eventi.
     * @param ultimoEvento Evento specificato.
     */
    public void setUltimoEvento(Evento ultimoEvento) {
        this.ultimoEvento = ultimoEvento;
    }
}
