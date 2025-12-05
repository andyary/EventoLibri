package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;

public class RichiestaDisiscrizioneEvento extends Messaggio{
    private Figlio figlio;
    private Evento evento;

    public RichiestaDisiscrizioneEvento(Figlio figlio, Evento evento) {
        this.figlio = figlio;
        this.evento = evento;
    }
}
