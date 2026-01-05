package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Amministratore;

/** Classe che rappresenta una richiesta di aggiornamento delle informazioni di un amministratore.
 * Estende la classe Messaggio.
 */
public class RichiestaAggiornaAmministratore extends Messaggio{
    private Amministratore amministratore;

    /** Costruttore della classe RichiestaAggiornaAmministratore.
     * @param amministratore Amministratore con le informazioni aggiornate.
     */
    public RichiestaAggiornaAmministratore(Amministratore amministratore) {
        this.amministratore = amministratore;
    }

    /** Restituisce l'amministratore con le informazioni aggiornate.
     * @return Amministratore aggiornato.
     */
    public Amministratore getAmministratore() {
        return amministratore;
    }
}
