package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

/** Messaggio di risposta per la creazione di un nuovo genitore.
 * Contiene informazioni sul successo dell'operazione, eventuali messaggi di errore
 * e il nuovo oggetto Genitore creato.
 */
public class RispostaNuovoGenitore extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Genitore nuovoGenitore;

    /* Getter per il campo successo.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /* Setter per il campo successo.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /* Getter per il campo messaggioErrore.
     * @return il messaggio di errore associato all'operazione.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /* Setter per il campo messaggioErrore.
     * @param messaggioErrore il messaggio di errore associato all'operazione.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /* Getter per il campo nuovoGenitore.
     * @return il nuovo oggetto Genitore creato.
     */
    public Genitore getNuovoGenitore() {
        return nuovoGenitore;
    }

    /* Setter per il campo nuovoGenitore.
     * @param nuovoGenitore il nuovo oggetto Genitore creato.
     */
    public void setNuovoGenitore(Genitore nuovoGenitore) {
        this.nuovoGenitore = nuovoGenitore;
    }
}
