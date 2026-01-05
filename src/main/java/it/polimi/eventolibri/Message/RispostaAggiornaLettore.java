package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;

/** Risposta inviata dal server al client in risposta alla richiesta di aggiornamento
 * delle informazioni del lettore.
 */
public class RispostaAggiornaLettore extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Lettore lettore;

    /** Ritorna riscultato dell'operazione di aggiornamento delle informazioni del lettore.
     * @return successo true se l'operazione è andata a buon fine, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta il risultato dell'operazione di aggiornamento delle informazioni del lettore.
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

    /** Imposta il lettore aggiornato.
     * @param lettore il lettore aggiornato.
     */
    public void setLettore(Lettore lettore) {
        this.lettore = lettore;
    }

    /** Ritorna il lettore aggiornato.
     * @return lettore il lettore aggiornato.
     */
    public Lettore getLettore() {
        return lettore;
    }
}
