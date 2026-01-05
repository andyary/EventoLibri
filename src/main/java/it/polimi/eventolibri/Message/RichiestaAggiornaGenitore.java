package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

/**
 * Messaggio inviato dal client al server per richiedere l'aggiornamento delle informazioni di un genitore.
 */
public class RichiestaAggiornaGenitore extends Messaggio{
    private Genitore genitore;

    /** Costruttore della classe RichiestaAggiornaGenitore.
     * @param genitore Genitore con le informazioni aggiornate.
     */
    public RichiestaAggiornaGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    /** Restituisce il genitore con le informazioni aggiornate.
     * @return Genitore aggiornato.
     */
    public Genitore getGenitore() {
        return genitore;
    }
}
