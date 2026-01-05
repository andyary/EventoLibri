package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

/** Messaggio inviato dal client al server per richiedere la cancellazione di una recensione.
 */
public class RichiestaCancellaRecensione extends Messaggio{
    private Recensione recensione;

    /** Costruttore della classe RichiestaCancellaRecensione.
     * @param recensione Recensione da cancellare.
     */
    public RichiestaCancellaRecensione(Recensione recensione) {
        this.recensione = recensione;
    }

    /** Restituisce la recensione da cancellare.
     * @return Recensione della richiesta.
     */
    public Recensione getRecensione() {
        return recensione;
    }

}
