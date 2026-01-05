package it.polimi.eventolibri.Model;

/** Classe concreta per la creazione di oggetti Amministratore. Implementa il pattern Factory Method.
 * Estende la classe astratta CreaUtente.
 */
public class CreaAmministratore extends CreaUtente<Amministratore> {

    /** Implementazione del metodo factory per creare un nuovo Amministratore senza ID.
     * @param nome Nome dell'amministratore.
     * @param cognome Cognome dell'amministratore.
     * @param userName Username dell'amministratore.
     * @return Nuovo oggetto Amministratore.
     */
    public Amministratore factory(String nome, String cognome, String userName) {
        return new Amministratore(nome, cognome, userName);
    }

    /** Implementazione del metodo factory per creare un nuovo Amministratore con ID.
     * @param id ID univoco dell'amministratore.
     * @param nome Nome dell'amministratore.
     * @param cognome Cognome dell'amministratore.
     * @param userName Username dell'amministratore.
     * @return Nuovo oggetto Amministratore.
     */
    public Amministratore factory(int id, String nome, String cognome, String userName) {
        return new Amministratore(id, nome, cognome, userName);
    }
}
