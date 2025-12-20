package it.polimi.eventolibri.Message;


import it.polimi.eventolibri.Model.Evento;

public class RichiestaSalvaEvento extends Messaggio{
    private Evento evento;

    public RichiestaSalvaEvento(Evento evento) {
        this.evento = evento;
    }

    public Evento getEvento() {
        return evento;
    }
}
