package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

/** Messaggio inviato dal client al server per richiedere l'iscrizione di un figlio a un evento.
 */
public class RichiestaIscrizioneEvento extends Messaggio {
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    /** Costruttore della classe RichiestaIscrizioneEvento.
     * @param figlio Figlio da iscrivere.
     * @param evento Evento a cui iscrivere il figlio.
     * @param genitore Genitore che richiede l'iscrizione.
     */
    public RichiestaIscrizioneEvento(Figlio figlio, Evento evento, Genitore genitore) {
        this.figlio = figlio;
        this.evento = evento;
        this.genitore = genitore;
    }

    /** Restituisce l'evento a cui iscrivere il figlio.
     * @return Evento della richiesta.
     */
    public Evento getEvento() {
        return evento;
    }

    /** Restituisce il figlio da iscrivere.
     * @return Figlio della richiesta.
     */
    public Figlio getFiglio() {
        return figlio;
    }

    /** Restituisce il genitore che richiede l'iscrizione.
     * @return Genitore della richiesta.
     */
    public Genitore getGenitore() {
        return genitore;
    }
}
