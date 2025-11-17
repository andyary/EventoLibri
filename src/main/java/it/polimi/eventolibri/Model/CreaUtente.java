package it.polimi.eventolibri.Model;

public abstract class CreaUtente {

	public Utente nuovoUtente(String nome, String cognome, String userName) {
        Utente utente = factory(nome, cognome, userName);
        return utente;
	}

    public Utente nuovoUtente(int id, String nome, String cognome, String userName) {
        Utente utente = factory(id, nome, cognome, userName);
        return utente;
    }



    public abstract Utente factory(String nome, String cognome, String userName);

    public abstract Utente factory(int id, String nome, String cognome, String userName);

}
