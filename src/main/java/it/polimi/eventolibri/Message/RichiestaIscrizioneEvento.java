package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;

import java.io.Serializable;

public class RichiestaIscrizioneEvento extends Messaggio {
    private Figlio figlio;
    private Evento evento;

    public RichiestaIscrizioneEvento(Figlio figlio, Evento evento) {
        this.figlio = figlio;
        this.evento = evento;
    }
}
