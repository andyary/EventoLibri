package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

/** Classe che rappresenta una richiesta di aggiunta di un nuovo figlio da parte di un genitore.
 * Estende la classe Messaggio.
 */
public class RichiestaAggiungiFiglio extends Messaggio{
    private Genitore genitore;
    private Figlio nuovoFiglio;

    /** Costruttori della classe RichiestaAggiungiFiglio.
     * @param genitore Genitore che richiede l'aggiunta del figlio.
     * @param nuovo Nuovo figlio da aggiungere.
     */
    public RichiestaAggiungiFiglio(Genitore genitore, Figlio nuovo) {
        this.genitore = genitore;
        this.nuovoFiglio = nuovo;
    }

    /** Restituisce il genitore che ha effettuato la richiesta.
     * @return Genitore richiedente.
     */
    public Genitore getGenitore() {
        return genitore;
    }

    /** Restituisce il nuovo figlio da aggiungere.
     * @return Nuovo figlio.
     */
    public Figlio getFiglioNuovo() {
        return nuovoFiglio;
    }
}
