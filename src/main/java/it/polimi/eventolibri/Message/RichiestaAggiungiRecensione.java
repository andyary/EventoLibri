package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

/** Classe che rappresenta una richiesta di aggiunta di una recensione. Estende la classe Messaggio.
 */
public class RichiestaAggiungiRecensione extends Messaggio{
    private Recensione recensione;

    /** Costruttore della classe RichiestaAggiungiRecensione.
     * @param recensione Recensione da aggiungere.
     */
    public RichiestaAggiungiRecensione(Recensione recensione) {
        this.recensione = recensione;
    }

    /** Restituisce la recensione da aggiungere.
     * @return Recensione della richiesta.
     */
    public Recensione getRecensione() {
        return recensione;
    }

}
