package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Genitore extends Utente implements Listener, Serializable {

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

    public void addFiglio(Figlio figlio) {
        this.figli.add(figlio);
    }

    public void setFigli(ArrayList<Figlio> figli) {
        this.figli = figli;
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
