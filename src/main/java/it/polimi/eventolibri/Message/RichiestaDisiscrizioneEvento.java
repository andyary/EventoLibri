package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

/** Messaggio inviato dal client al server per richiedere la disiscrizione di un figlio da un evento.
 */
public class RichiestaDisiscrizioneEvento extends Messaggio{
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    /** Costruttore della classe RichiestaDisiscrizioneEvento.
     * @param figlio Figlio da disiscrivere.
     * @param evento Evento da cui disiscrivere il figlio.
     * @param genitore Genitore che richiede la disiscrizione.
     */
    public RichiestaDisiscrizioneEvento(Figlio figlio, Evento evento, Genitore genitore) {
        this.figlio = figlio;
        this.evento = evento;
        this.genitore = genitore;
    }

    /** Restituisce il figlio da disiscrivere.
     * @return Figlio della richiesta.
     */
    public Figlio getFiglio() {
        return figlio;
    }

    /** Restituisce l'evento da cui disiscrivere il figlio.
     * @return Evento della richiesta.
     */
    public Evento getEvento() {
        return evento;
    }

    /** Restituisce il genitore che richiede la disiscrizione.
     * @return Genitore della richiesta.
     */
    public Genitore getGenitore() {
        return genitore;
    }
}
