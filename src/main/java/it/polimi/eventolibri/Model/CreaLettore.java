package it.polimi.eventolibri.Model;

public class CreaLettore extends CreaUtente {

	public Lettore factory(String nome, String cognome, String userName) {
        return new Lettore(nome, cognome, userName);
	}

}
