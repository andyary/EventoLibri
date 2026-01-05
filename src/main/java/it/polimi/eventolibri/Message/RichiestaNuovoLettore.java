package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;

/** Messaggio inviato dal client al server per richiedere l'aggiunta di un nuovo lettore.
 */
public class RichiestaNuovoLettore extends Messaggio{
    private Lettore nuovoLettore;
    private String password;

    /** Restituisce il nuovo lettore da aggiungere.
     * @return Nuovo lettore.
     */
    public Lettore getNuovoLettore() {
        return nuovoLettore;
    }

    /** Restituisce la password del nuovo lettore.
     * @return Password del nuovo lettore.
     */
    public String getPassword() {
        return password;
    }

    /** Costruttore della classe RichiestaNuovoLettore.
     * @param nuovoLettore Nuovo lettore da aggiungere.
     * @param password Password del nuovo lettore.
     */
    public RichiestaNuovoLettore(Lettore nuovoLettore, String password) {
        this.password = password;
        this.nuovoLettore = nuovoLettore;
    }
}
