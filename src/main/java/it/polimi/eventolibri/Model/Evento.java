package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Evento extends EventoAstratto implements Serializable {

    private String nome;
    private LocalDateTime data;
    private Luogo luogo;
    private int id;
    private ArrayList<LibroLettore> scaletta;
    private Lettore creatore;
    private int iscritti;


    // costruttore senza creatore (usato per creare eventi temporanei prima di assegnarli a un creatore), impostato Id a 0
    public Evento(String nome, Luogo luogo, LocalDateTime data) {
        this.id = 0;
        this.creatore = null;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = new ArrayList<>();
    }

    // setter per il creatore (usato per assegnare il creatore dopo aver creato l'evento temporaneo)
    public void setCreatore(Lettore creatore) {
        this.creatore = creatore;
    }

    public Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = new ArrayList<>();
        creatore.aggiungiEventiCreati(this);
    }


    public Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = scaletta;
        creatore.aggiungiEventiCreati(this);
        for (LibroLettore ll : scaletta) {
            this.addListener(ll.getLettore());
        }
    }

    public Evento(int id, Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = scaletta;
        creatore.aggiungiEventiCreati(this);
        for (LibroLettore ll : scaletta) {
            this.addListener(ll.getLettore());
        }
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Luogo getLuogo() {
        return luogo;
    }

    public ArrayList<LibroLettore> getScaletta() {
        return scaletta;
    }

    public Lettore getCreatore() {
        return creatore;
    }

    public int getIscritti() {
        return iscritti;
    }


    public int getId() {
        return id;
    }

    public void aggiornaEvento(Evento evento) {
        this.nome  = evento.nome;
        this.luogo = evento.luogo;
        this.data = evento.data;
        for (LibroLettore ll_old : this.scaletta) {
            int flag = 0;
            for (LibroLettore ll_new : evento.scaletta) {
                if (ll_old.getLettore().equals(ll_new.getLettore())) {flag=1;}
            }
            if (flag == 0) {
                // this.removeListener(ll_old.getLettore());
                ll_old.getLettore().rimuoviIscrizioneLettura(this);
            }
        }
        for (LibroLettore ll_new : evento.scaletta) {
            int flag = 0;
            for (LibroLettore ll_old : this.scaletta) {
                if (ll_old.getLettore().equals(ll_new.getLettore())) {flag=1;}
            }
            if (flag == 0) {
                // this.addListener(ll_new.getLettore());
                ll_new.getLettore().aggiungiIscrizioneLettura(this);
            }
        }
        this.scaletta = evento.scaletta;
        updateAll(this);
    }



    public LocalDateTime calcolaOraFine() {
        LocalDateTime oraFine = data;
        for (LibroLettore ll : scaletta) {
            // Somma la durata di ogni libro alla data di inizio
            oraFine = oraFine.plusMinutes(ll.getLibro().getTempoLettura());
        }
        return oraFine;
    }

    public void setIscritti(int iscritti) {
        this.iscritti = iscritti;
    }

    public void setScaletta(ArrayList<LibroLettore> scaletta) {
        this.scaletta = scaletta;
    }
}
