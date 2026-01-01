package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Lettore extends Utente implements Listener, Serializable {

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
        int i;
        for (i = 0; i < eventiCreati.size(); i++) {
            if (eventiCreati.get(i).getId() == eventoCreato.getId()) {
                eventiCreati.get(i).removeListener(this);
                eventiCreati.set(i, eventoCreato);
                eventoCreato.addListener(this);
                eventiCreati.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
                return;
            }
        }
        eventiCreati.add(eventoCreato);
        eventiCreati.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
        eventoCreato.addListener(this);
    }

    public void aggiungiIscrizioneLettura(Evento eventoIscritto) {
        int i;
        for (i = 0; i < iscrizioniLettura.size(); i++) {
            if (iscrizioniLettura.get(i).getId() == eventoIscritto.getId()) {
                iscrizioniLettura.get(i).removeListener(this);
                iscrizioniLettura.set(i, eventoIscritto);
                eventoIscritto.addListener(this);
                iscrizioniLettura.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
                return;
            }
        }
        iscrizioniLettura.add(eventoIscritto);
        iscrizioniLettura.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
        eventoIscritto.addListener(this);
    }

    public void rimuoviIscrizioneLettura(Evento eventoIscritto) {
        int i;
        for (i = 0; i < iscrizioniLettura.size(); i++) {
            if (iscrizioniLettura.get(i).getId() == eventoIscritto.getId()) {
                iscrizioniLettura.get(i).removeListener(this);
                iscrizioniLettura.remove(i);
                return;
            }
        }
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

    public ArrayList<Evento> getEventiCreati() {
        return eventiCreati;
    }

    public ArrayList<Evento> getIscrizioniLettura() {
        return iscrizioniLettura;
    }

    @Override
    public boolean equals(Listener listener) {
        return this.getId() == listener.getId();
    }

}
