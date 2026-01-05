package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

/** Messaggio dal server al client di risposta per la disiscrizione (sul database) di un figlio da un evento.
 */
public class RispostaDisiscrizioneEvento extends Messaggio {

    private boolean successo;
    private String messaggioErrore;
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    /** Costruttore della classe RispostaDisiscrizioneEvento.
     * @param figlio il figlio da disiscrivere dall'evento.
     * @param evento l'evento da cui disiscrivere il figlio.
     * @param genitore il genitore del figlio.
     */
    public RispostaDisiscrizioneEvento(Figlio figlio, Evento evento, Genitore genitore) {
        this.genitore = genitore;
        this.figlio = figlio;
        this.evento = evento;
    }

    /** Ritorna lo stato di successo dell'operazione di disiscrizione del figlio dall'evento sul database.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di disiscrizione del figlio dall'evento sul database.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore associato all'operazione di disiscrizione del figlio dall'evento sul database.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore associato all'operazione di disiscrizione del figlio dall'evento sul database.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Imposta il figlio da disiscrivere dall'evento.
     * @param figlio il figlio.
     */
    public void setFiglio(Figlio figlio) {
        this.figlio = figlio;
    }

    /** Imposta l'evento da cui disiscrivere il figlio.
     * @param evento l'evento.
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    /** Imposta il genitore del figlio da disiscrivere dall'evento.
     * @param genitore il genitore.
     */
    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    /** Ritorna il figlio disiscritto dall'evento.
     * @return il figlio.
     */
    public Figlio getFiglio() {
        return figlio;
    }

    /** Ritorna l'evento da cui il figlio è stato disiscritto.
     * @return l'evento.
     */
    public Evento getEvento() {
        return evento;
    }

    /** Ritorna il genitore del figlio disiscritto dall'evento.
     * @return il genitore.
     */
    public Genitore getGenitore() {
        return genitore;
    }
}
