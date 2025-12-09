package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

public class RichiestaIscrittiEvento  extends Messaggio{
    private Evento evento;

    public RichiestaIscrittiEvento(Evento evento) {
        this.evento = evento;
    }

    public Evento getEvento() {
        return evento;
    }
}
