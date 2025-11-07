package it.polimi.eventolibri.Model;

public abstract class CreaUtente {

	public Utente nuovoUtente(String nome, String cognome, String userName) {
        Utente utente = factory(nome, cognome, userName);
        return utente;
	}


    public abstract Utente factory(String nome, String cognome, String userName);

}
