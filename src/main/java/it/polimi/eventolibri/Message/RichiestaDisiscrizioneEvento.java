package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

public class RichiestaDisiscrizioneEvento extends Messaggio{
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    public RichiestaDisiscrizioneEvento(Figlio figlio, Evento evento, Genitore genitore) {
        this.figlio = figlio;
        this.evento = evento;
        this.genitore = genitore;
    }

    public Figlio getFiglio() {
        return figlio;
    }

    public Evento getEvento() {
        return evento;
    }

    public Genitore getGenitore() {
        return genitore;
    }
}
