package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

/**
 * Messaggio di risposta per l'aggiornamento delle informazioni di un genitore.
 */
public class RispostaAggiornaGenitore extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Genitore genitore;

    /** Ritorna lo stato di successo dell'operazione.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore associato all'operazione.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore associato all'operazione.
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
     * @return il genitore aggiornato.
     */
    public Genitore getGenitore() {
        return genitore;
    }
}
