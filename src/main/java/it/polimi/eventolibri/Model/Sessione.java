package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public class Sessione {

    private ArrayList<Evento> eventi;
	private Utente utente;

    public Sessione(Utente utente) {
        this.utente = utente;
        this.eventi = new ArrayList<>();
    }

    public Sessione(Utente utente, ArrayList<Evento> eventi) {
        this.utente = utente;
        this.eventi = eventi;
    }

    public void setEventi(ArrayList<Evento> eventi) {
        this.eventi.addAll(eventi);
    }

    public void aggiungiEvento(Evento evento) {
        this.eventi.add(evento);
    }

    public void rimuoviEvento(Evento evento) {
        this.eventi.remove(evento);
    }

    public ArrayList<Evento> getEventi() {
        return eventi;
    }

}
