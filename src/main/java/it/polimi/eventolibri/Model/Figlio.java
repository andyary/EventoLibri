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

    public Figlio(String nome, LocalDate dataNascita, ArrayList<Evento> iscrizioni) {
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = iscrizioni;
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

    public void aggiornaEvento(Evento eventoAggiornato) {
        for (int i = 0; i < iscrizioni.size(); i++) {
            if (iscrizioni.get(i).getId() == eventoAggiornato.getId()) {
                iscrizioni.set(i, eventoAggiornato);
                break;
            }
        }
    }
}
