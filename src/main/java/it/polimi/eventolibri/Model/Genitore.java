package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public class Genitore extends Utente implements Listener {

	private ArrayList<Figlio> figli;

    public Genitore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
    }

    public void aggiungiFiglio(Figlio figlio) {
        // Aggiungi il figlio all'array figli
        figli.add(figlio);
	}

	public void update() {

	}

}
