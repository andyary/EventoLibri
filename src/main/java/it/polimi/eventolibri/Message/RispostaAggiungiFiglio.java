package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

/** Risposta inviata dal server al client in risposta alla richiesta di aggiunta
 * di un figlio al profilo del genitore.
 */
public class RispostaAggiungiFiglio extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Genitore genitore;
    private Figlio figlio;

    /** Ritorna riscultato dell'operazione di aggiunta del figlio.
     * @return successo true se l'operazione è andata a buon fine, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta il risultato dell'operazione di aggiunta del figlio.
     * @param successo true se l'operazione è andata a buon fine, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore in caso di fallimento dell'operazione.
     * @return messaggioErrore il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore in caso di fallimento dell'operazione.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Imposta il genitore aggiornato.
     * @param genitore il genitore aggiornato.
     */
    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    /** Ritorna il genitore aggiornato.
     * @return genitore il genitore aggiornato.
     */
    public Genitore getGenitore() {
        return genitore;
    }

    /** Ritorna il nuovo figlio aggiunto.
     * @return figlio il nuovo figlio aggiunto.
     */
    public Figlio getNuovoFiglio() {
        return figlio;
    }

    /** Imposta il nuovo figlio aggiunto.
     * @param figlio il nuovo figlio aggiunto.
     */
    public void setNuovoFiglio(Figlio figlio) {
        this.figlio = figlio;
    }
}
