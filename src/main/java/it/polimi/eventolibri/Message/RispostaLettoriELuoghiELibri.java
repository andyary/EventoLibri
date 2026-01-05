package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;
import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Luogo;

import java.util.ArrayList;

/** Messaggio di risposta dal server al client che contiene liste di lettori, luoghi e libri prelevate dal databse.
 */
public class RispostaLettoriELuoghiELibri extends Messaggio {
    private boolean successo;
    private String messaggioerrore;
    private ArrayList<Lettore> lettori;
    private ArrayList<Luogo> luoghi;
    private ArrayList<Libro> elencolibri;

    /** Ritorna lo stato di successo dell'operazione di richiesta dati al database.
     * @return true se l'operazione è andata a buon fine, false altrimenti.
     */
    public boolean isSuccesso() {
        return successo;
    }

    /** Imposta lo stato di successo dell'operazione di richiesta dati al database.
     * @param successo true se l'operazione è andata a buon fine, false altrimenti.
     */
    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    /** Ritorna il messaggio di errore in caso di fallimento dell'operazione di richiesta dati al database.
     * @return messaggio di errore.
     */
    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    /** Imposta il messaggio di errore in caso di fallimento dell'operazione di richiesta dati al database.
     * @param messaggioerrore messaggio di errore.
     */
    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }

    /** Ritorna la lista di tutti i lettori prelevata dal database.
     * @return lista di lettori.
     */
    public ArrayList<Lettore> getLettori() {
        return lettori;
    }

    /** Imposta la lista di tutti i lettori prelevata dal database.
     * @param lettori lista di lettori.
     */
    public void setLettori(ArrayList<Lettore> lettori) {
        this.lettori = lettori;
    }

    /** Ritorna la lista di tutti i luoghi prelevata dal database.
     * @return lista di luoghi.
     */
    public ArrayList<Luogo> getLuoghi() {
        return luoghi;
    }

    /** Imposta la lista di tutti i luoghi prelevata dal database.
     * @param luoghi lista di luoghi.
     */
    public void setLuoghi(ArrayList<Luogo> luoghi) {
        this.luoghi = luoghi;
    }

    /** Ritorna la lista di tutti i libri prelevata dal database.
     * @return lista di libri.
     */
    public ArrayList<Libro> getElencolibri() {
        return elencolibri;
    }

    /** Imposta la lista di tutti i libri prelevata dal database.
     * @param elencolibri lista di libri.
     */
    public void setElencolibri(ArrayList<Libro> elencolibri) {
        this.elencolibri = elencolibri;
    }
}
