package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Figlio implements Serializable {

    private int id;
	private ArrayList<Evento> iscrizioni;
	private String nome;
	private LocalDate dataNascita;

    public String getNome() {
        return nome;
    }

    public Figlio(String nome, LocalDate dataNascita) {
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = new ArrayList<>();
    }

    public Figlio(int id, String nome, LocalDate dataNascita) {
        this.id = id;
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = new ArrayList<>();
    }

    public Figlio(int id, String nome, LocalDate dataNascita, ArrayList<Evento> iscrizioni, Genitore genitore) {
        this.id = id;
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = iscrizioni;
        for (Evento evento : iscrizioni) {
            evento.addListener(genitore);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public void iscrivi(Evento evento, Genitore genitore) {
        iscrizioni.add(evento);
        evento.addListener(genitore);
    }

	public void disiscrivi(Evento evento, Genitore genitore) {
        iscrizioni.removeIf(e -> e.getId() == evento.getId());
        for (Figlio figlio : genitore.getFigli()) {
            if (figlio.getIscrizioni().stream().anyMatch(ev -> ev.getId() == evento.getId())) {
                return; // Un altro figlio del genitore è ancora iscritto all'evento
            }
        }
        evento.removeListener(genitore);
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

    public int getId() {
        return id;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }
}
