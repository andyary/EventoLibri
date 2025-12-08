package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

public class RispostaIscrizioneEvento extends Messaggio {

    private boolean successo;
    private String messaggioErrore;
    private Figlio figlio;
    private Evento evento;
    private Genitore genitore;

    public RispostaIscrizioneEvento() {
    }

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

    public void setFiglio(Figlio figlio) {
        this.figlio = figlio;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public void setGenitore(Genitore genitore) {
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
