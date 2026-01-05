package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Amministratore;

/** Messaggio inviato dal server al client in risposta alla richiesta di aggiornamento dei dati di un amministratore.
 */
public class RispostaAggiornaAmministratore extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Amministratore amministratore;

    /** Restituisce se l'aggiornamento è avvenuto con successo.
     * @return true se l'aggiornamento è avvenuto con successo, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta se l'aggiornamento è avvenuto con successo.
     * @param successo true se l'aggiornamento è avvenuto con successo, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Restituisce il messaggio di errore in caso di fallimento dell'aggiornamento.
     * @return Messaggio di errore.
     */
    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    /** Imposta il messaggio di errore in caso di fallimento dell'aggiornamento.
     * @param messaggioErrore Messaggio di errore.
     */
    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    /** Imposta l'amministratore aggiornato.
     * @param amministratore Amministratore aggiornato.
     */
    public void setAmministratore(Amministratore amministratore) {
        this.amministratore = amministratore;
    }

    /** Restituisce l'amministratore aggiornato.
     * @return Amministratore aggiornato.
     */
    public Amministratore getAmministratore() {
        return amministratore;
    }
}
