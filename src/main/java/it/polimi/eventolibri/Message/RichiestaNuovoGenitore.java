package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

/** Messaggio inviato dal client al server per richiedere l'aggiunta di un nuovo genitore.
 */
public class RichiestaNuovoGenitore extends Messaggio{
    private Genitore nuovoGenitore;
    private String password;

    /** Restituisce il nuovo genitore da aggiungere.
     * @return Nuovo genitore.
     */
    public Genitore getNuovoGenitore() {
        return nuovoGenitore;
    }

    /** Restituisce la password del nuovo genitore.
     * @return Password del nuovo genitore.
     */
    public String getPassword() {
        return password;
    }

    /** Costruttore della classe RichiestaNuovoGenitore.
     * @param nuovoGenitore Nuovo genitore da aggiungere.
     * @param password Password del nuovo genitore.
     */
    public RichiestaNuovoGenitore(Genitore nuovoGenitore, String password) {
        this.password = password;
        this.nuovoGenitore = nuovoGenitore;
    }
}
