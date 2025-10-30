package it.polimi.eventolibri.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Evento extends EventoAstratto {

    private String nome;
    private LocalDateTime data;
    private Luogo luogo;
    private int id;
    private ArrayList<LibroLettore> scaletta;
    private Lettore creatore;
    private int iscritti;

    public void Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = scaletta;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void aggiornaEvento(Evento evento) {
        this.nome  = evento.nome;
        this.luogo = evento.luogo;
        this.data = evento.data;
        this.scaletta = evento.scaletta;
    }

    public LocalDateTime calcolaOraFine() {
        LocalDateTime oraFine = data;
        for (LibroLettore ll : scaletta) {
            // Somma la durata di ogni libro alla data di inizio
            oraFine = data.plusMinutes(ll.getLibro().getTempoLettura());
        }
        return oraFine;
    }

}
