package it.polimi.eventolibri.Model;

/** Classe concreta per la creazione di oggetti Lettore. Implementa il pattern Factory Method.
 * Estende la classe astratta CreaUtente.
 */
public class CreaLettore extends CreaUtente<Lettore> {

    /** Implementazione del metodo factory per creare un nuovo Lettore senza ID.
     * @param nome Nome del lettore.
     * @param cognome Cognome del lettore.
     * @param userName Username del lettore.
     * @return Nuovo oggetto Lettore.
     */
	public Lettore factory(String nome, String cognome, String userName) {
        return new Lettore(nome, cognome, userName);
	}

    /** Implementazione del metodo factory per creare un nuovo Lettore con ID.
     * @param id ID univoco del lettore.
     * @param nome Nome del lettore.
     * @param cognome Cognome del lettore.
     * @param userName Username del lettore.
     * @return Nuovo oggetto Lettore.
     */
    public Lettore factory(int id, String nome, String cognome, String userName) {
        return new Lettore(id, nome, cognome, userName);
    }
}
