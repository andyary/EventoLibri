package it.polimi.eventolibri.Model;

public abstract class CreaUtente<T extends Utente> {

	public T nuovoUtente(String nome, String cognome, String userName) {
        T utente = factory(nome, cognome, userName);
        return utente;
	}

    public T nuovoUtente(int id, String nome, String cognome, String userName) {
        T utente = factory(id, nome, cognome, userName);
        return utente;
    }



    public abstract T factory(String nome, String cognome, String userName);

    public abstract T factory(int id, String nome, String cognome, String userName);

}
