package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Amministratore;

/** Messaggio inviato dal client al server per richiedere l'aggiunta di un nuovo amministratore.
 */
public class RichiestaNuovoAmministratore extends Messaggio{
    private Amministratore nuovoAmministratore;
    private String password;

    /** Restituisce il nuovo amministratore da aggiungere.
     * @return Nuovo amministratore.
     */
    public Amministratore getNuovoAmministratore() {
        return nuovoAmministratore;
    }

    /** Restituisce la password del nuovo amministratore.
     * @return Password del nuovo amministratore.
     */
    public String getPassword() {
        return password;
    }

    /** Costruttore della classe RichiestaNuovoAmministratore.
     * @param nuovoAmministratore Nuovo amministratore da aggiungere.
     * @param password Password del nuovo amministratore.
     */
    public RichiestaNuovoAmministratore(Amministratore nuovoAmministratore, String password) {
        this.password = password;
        this.nuovoAmministratore = nuovoAmministratore;
    }
}
