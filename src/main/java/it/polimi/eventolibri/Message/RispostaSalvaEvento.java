package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

public class RispostaSalvaEvento extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Evento evento;

    public boolean isSuccesso() {
        return successo;
    }

    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    public String getMessaggioErrore() {
        return messaggioErrore;
    }

    public void setMessaggioErrore(String messaggioErrore) {
        this.messaggioErrore = messaggioErrore;
    }

    public Evento getEvento() {
        return evento;
    }
    public void setEvento(Evento evento) {
        this.evento = evento;
    }
}
