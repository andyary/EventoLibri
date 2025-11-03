package it.polimi.eventolibri.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Figlio {

	private ArrayList<Evento> iscrizioni;
	private String nome;
	private LocalDate dataNascita;



    public Figlio(String nome, LocalDate dataNascita) {
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = new ArrayList<>();
    }

	public void iscrivi(Evento evento) {
        iscrizioni.add(evento);
    }

	public void disicrivi(Evento evento) {
        iscrizioni.remove(evento);
	}

    public int getNumeroIscrizioni() {
        return iscrizioni.size();
    }

    public ArrayList<Evento> getIscrizioni() {
        return iscrizioni;
    }
}
