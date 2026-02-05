package it.polimi.eventolibri.Model;

/** Classe concreta per la creazione di oggetti Genitore. Implementa il pattern Factory Method.
 * Estende la classe astratta CreaUtente.
 */
public class CreaGenitore extends CreaUtente<Genitore> {

    /** Implementazione del metodo factory per creare un nuovo Genitore senza ID.
     * @param nome Nome del genitore.
     * @param cognome Cognome del genitore.
     * @param userName Username del genitore.
     * @return Nuovo oggetto Genitore.
     */
    public Genitore factory(String nome, String cognome, String userName) {
        return new Genitore(nome, cognome, userName); // Chiamata al costruttore di Genitore senza ID
    }

    /** Implementazione del metodo factory per creare un nuovo Genitore con ID.
     * @param id ID univoco del genitore.
     * @param nome Nome del genitore.
     * @param cognome Cognome del genitore.
     * @param userName Username del genitore.
     * @return Nuovo oggetto Genitore.
     */
    public Genitore factory(int id, String nome, String cognome, String userName) {
        return new Genitore(id, nome, cognome, userName); // Chiamata al costruttore di Genitore con ID
    }

}
