package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public class Lettore extends  Utente implements Listener {

    private ArrayList<Evento> iscrizioniLettura;
    private ArrayList<Evento> eventiCreati;

    public Lettore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
        this.iscrizioniLettura = new ArrayList<Evento>();
        this.eventiCreati = new ArrayList<Evento>();
    }

    public Lettore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
        this.iscrizioniLettura = new ArrayList<Evento>();
        this.eventiCreati = new ArrayList<Evento>();
    }


    public void setIscrizioniLettura(ArrayList<Evento> iscrizioniLettura) {
        this.iscrizioniLettura = iscrizioniLettura;
        for (Evento evento : iscrizioniLettura) {
            evento.addListener(this);
        }
    }

    public void setEventiCreati(ArrayList<Evento> eventiCreati) {
        this.eventiCreati = eventiCreati;
        for (Evento evento : eventiCreati) {
            evento.addListener(this);
        }
    }

    public void aggiungiEventiCreati(Evento eventoCreato) {
        this.eventiCreati.add(eventoCreato);
        eventoCreato.addListener(this);
    }

    public void aggiungiIscrizioneLettura(Evento eventoIscritto) {
        this.iscrizioniLettura.add(eventoIscritto);
        eventoIscritto.addListener(this);
    }

    public void rimuoviIscrizioneLettura(Evento eventoIscritto) {
        this.iscrizioniLettura.remove(eventoIscritto);
        if (!this.equals(eventoIscritto.getCreatore()) && !iscrizioniLettura.contains(eventoIscritto)) {
            eventoIscritto.removeListener(this);}
    }

    public void update(Evento eventoAggiornato) {
        for (int i = 0; i < iscrizioniLettura.size(); i++) {
            if (iscrizioniLettura.get(i).getId() == eventoAggiornato.getId()) {
                iscrizioniLettura.set(i, eventoAggiornato);
                break;
            }
        }
        for (int i = 0; i < eventiCreati.size(); i++) {
            if (eventiCreati.get(i).getId() == eventoAggiornato.getId()) {
                eventiCreati.set(i, eventoAggiornato);
                break;
            }
        }
    }
}
