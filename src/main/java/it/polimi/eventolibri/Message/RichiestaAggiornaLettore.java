package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;

/** Classe che rappresenta una richiesta di aggiornamento delle informazioni di un lettore.
 * Estende la classe Messaggio.
 */
public class RichiestaAggiornaLettore extends Messaggio{
    private Lettore lettore;

    /** Costruttore della classe RichiestaAggiornaLettore.
     * @param lettore Lettore con le informazioni aggiornate.
     */
    public RichiestaAggiornaLettore(Lettore lettore) {
        this.lettore = lettore;
    }

    /** Restituisce il lettore con le informazioni aggiornate.
     * @return Lettore aggiornato.
     */
    public Lettore getLettore() {
        return lettore;
    }
}
