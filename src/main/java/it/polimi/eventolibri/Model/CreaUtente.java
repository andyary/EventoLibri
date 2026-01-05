package it.polimi.eventolibri.Model;

/** Classe astratta per la creazione di utenti. Implementa il pattern Factory Method.
 * @param <T> Tipo di utente che estende la classe Utente.
 */
public abstract class CreaUtente<T extends Utente> {

    /** Metodo parametrico per creare un nuovo utente senza ID.
     * @param nome Nome dell'utente.
     * @param cognome Cognome dell'utente.
     * @param userName Username dell'utente.
     * @return Nuovo utente di tipo parametrico T.
     */
	public T nuovoUtente(String nome, String cognome, String userName) {
        T utente = factory(nome, cognome, userName);
        return utente;
	}

    /** Metodo parametrico per creare un nuovo utente con ID.
     * @param id ID univoco dell'utente.
     * @param nome Nome dell'utente.
     * @param cognome Cognome dell'utente.
     * @param userName Username dell'utente.
     * @return Nuovo utente di tipo parametrico T.
     */
    public T nuovoUtente(int id, String nome, String cognome, String userName) {
        T utente = factory(id, nome, cognome, userName);
        return utente;
    }



    public abstract T factory(String nome, String cognome, String userName);

    public abstract T factory(int id, String nome, String cognome, String userName);

}
