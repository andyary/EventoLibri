package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Utente;

/** Messaggio inviato dal server al client per richiedere recensioni e recensibilita di un libro da parte di un utente.
 */
public class RichiestaRecensioniERecensibilita extends Messaggio {
    private Libro libro;
    private Utente utente;

    /** Costruttore della classe RichiestaRecensioniERecensibilita.
     * @param libro Libro di cui richiedere recensioni.
     * @param utente Utente di cui valutare la recensibilita (per questo libro).
     */
    public RichiestaRecensioniERecensibilita(Libro libro, Utente utente) {
        this.utente = utente;
        this.libro = libro;
    }

    /** Restituisce il libro di cui richiedere recensioni e recensibilita.
     * @return Libro specificato.
     */
    public Libro getLibro() {
        return libro;
    }

    /** Restituisce l'utente a cui richiedere recensioni e recensibilita.
     * @return Utente specificato.
     */
    public Utente getUtente() {
        return utente;
    }
}

