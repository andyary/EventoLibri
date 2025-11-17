package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public class CreaLettore extends CreaUtente {

	public Lettore factory(String nome, String cognome, String userName) {
        return new Lettore(nome, cognome, userName);
	}

    public Lettore factory(int id, String nome, String cognome, String userName) {
        return new Lettore(id, nome, cognome, userName);
    }

}
