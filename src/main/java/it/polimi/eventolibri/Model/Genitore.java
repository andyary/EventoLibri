package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public class Genitore extends Utente implements Listener {

	private ArrayList<Figlio> figli= new ArrayList<>();

    public Genitore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
    }

    public Genitore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
    }

    public void aggiungiFiglio(Figlio figlio) {
        // Aggiungi il figlio all'array figli
        figli.add(figlio);
	}

    public int getNumeroFigli() {
        return figli.size();
    }

    public ArrayList<Figlio> getFigli() {
        return figli;
    }

    public void update(Evento evento) {
        for (Figlio figlio : figli) {
            figlio.aggiornaEvento(evento);
        }

	}

}
