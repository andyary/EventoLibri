package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

/** Messaggio di risposta all'iscrizione ad un evento.
 * Contiene informazioni sull'esito dell'iscrizione, eventuali messaggi di errore,
 * e i dettagli del figlio, dell'evento e del genitore coinvolti.
 */
public class RispostaIscrizioneEvento extends Messaggio {

    private boolean successo;
    private String messaggioErrore;
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    /** Costruttore della classe RispostaIscrizioneEvento.
     *
     * @param figlio Il figlio che si è iscritto all'evento.
     * @param evento L'evento a cui il figlio si è iscritto.
     * @param genitore Il genitore del figlio.
     */
    public RispostaIscrizioneEvento(Figlio figlio, Evento evento, Genitore genitore) {
        this.genitore = genitore;
        this.figlio = figlio;
        this.evento = evento;
    }

    /** Ritorna lo stato di successo dell'operazione di iscrizione su database.
     * @return true se l'iscrizione è avvenuta con successo, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di iscrizione su database.
     * @param successo true se l'iscrizione è avvenuta con successo, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore associato all'iscrizione.
     * @return Il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore associato all'iscrizione.
     * @param messaggioErrore Il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Imposta il figlio associato all'iscrizione.
     * @param figlio Il figlio.
     */
    public void setFiglio(Figlio figlio) {
        this.figlio = figlio;
    }

    /** Imposta l'evento associato all'iscrizione.
     * @param evento L'evento.
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    /** Imposta il genitore associato all'iscrizione.
     * @param genitore Il genitore.
     */
    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    /** Ritorna il figlio associato all'iscrizione.
     * @return Il figlio.
     */
    public Figlio getFiglio() {
        return figlio;
    }

    /** Ritorna l'evento associato all'iscrizione.
     * @return L'evento.
     */
    public Evento getEvento() {
        return evento;
    }

    /** Ritorna il genitore associato all'iscrizione.
     * @return Il genitore.
     */
    public Genitore getGenitore() {
        return genitore;
    }
}
