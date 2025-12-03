package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

public class RichiestaNextEventi extends Messaggio {
    private Evento ultimoEvento;

    public RichiestaNextEventi(Evento ultimoEvento) {
        this.ultimoEvento = ultimoEvento;
    }

    public Evento getUltimoEvento() {
        return ultimoEvento;
    }

    public void setUltimoEvento(Evento ultimoEvento) {
        this.ultimoEvento = ultimoEvento;
    }
}
