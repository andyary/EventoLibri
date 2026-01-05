package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Amministratore;

/** Messaggio di risposta alla richiesta di creazione di un nuovo amministratore.
 * Contiene informazioni sul successo dell'operazione e, in caso di successo,
 * i dettagli del nuovo amministratore creato.
 */
public class RispostaNuovoAmministratore extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Amministratore nuovoAmministratore;

    /** Ritorna il successo dell'operazione di creazione del nuovo amministratore.
     * @return true se l'operazione è riuscita, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Setta il successo dell'operazione di creazione del nuovo amministratore.
     * @param successo true se l'operazione è riuscita, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore in caso di fallimento dell'operazione.
     * @return il messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Setta il messaggio di errore in caso di fallimento dell'operazione.
     * @param messaggioErrore il messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Ritorna il nuovo amministratore creato.
     * @return il nuovo amministratore.
     */
    public Amministratore getNuovoAmministratore() {
        return nuovoAmministratore;
    }

    /** Setta il nuovo amministratore creato.
     * @param nuovoAmministratore il nuovo amministratore.
     */
    public void setNuovoAmministratore(Amministratore nuovoAmministratore) {
        this.nuovoAmministratore = nuovoAmministratore;
    }
}
