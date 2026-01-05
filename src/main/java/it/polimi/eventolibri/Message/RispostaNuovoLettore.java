package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;

/** Messaggio di risposta dal server al client per la creazione di un nuovo lettore nel databse.
 * Contiene informazioni sul successo dell'operazione e, in caso di successo,
 * i dettagli del nuovo lettore creato.
 */
public class RispostaNuovoLettore extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Lettore nuovoLettore;

    /* Ritorna lo stato di successo dell'operazione di creazione del lettore.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /* Imposta lo stato di successo dell'operazione di creazione del lettore.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /* Ritorna il messaggio di errore in caso di fallimento dell'operazione.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /* Imposta il messaggio di errore in caso di fallimento dell'operazione.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /* Ritorna il nuovo lettore creato.
     * @return il nuovo lettore.
     */
    public Lettore getNuovoLettore() {
        return nuovoLettore;
    }

    /* Imposta il nuovo lettore creato.
     * @param nuovoLettore il nuovo lettore.
     */
    public void setNuovoLettore(Lettore nuovoLettore) {
        this.nuovoLettore = nuovoLettore;
    }
}