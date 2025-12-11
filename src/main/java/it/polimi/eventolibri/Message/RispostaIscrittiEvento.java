package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

public class RispostaIscrittiEvento extends Messaggio{
    private Evento evento;
    private boolean successo;
    private String messaggioerrore;

    public RispostaIscrittiEvento(Evento evento) {
        this.evento = evento;
    }

    public Evento getEvento() {
        return evento;
    }

    public boolean isSuccesso() {
        return successo;
    }

    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }


    public void setEvento(Evento evento) {
        this.evento = evento;
    }
}
