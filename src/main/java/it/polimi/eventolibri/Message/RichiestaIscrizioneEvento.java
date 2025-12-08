package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

import java.io.Serializable;

public class RichiestaIscrizioneEvento extends Messaggio {
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    public RichiestaIscrizioneEvento(Figlio figlio, Evento evento, Genitore genitore) {
        this.figlio = figlio;
        this.evento = evento;
        this.genitore = genitore;
    }

    public Evento getEvento() {
        return evento;
    }

    public Figlio getFiglio() {
        return figlio;
    }

    public Genitore getGenitore() {
        return genitore;
    }

}
