package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

public class RispostaNuovoGenitore extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Genitore nuovoGenitore;

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

    public Genitore getNuovoGenitore() {
        return nuovoGenitore;
    }

    public void setNuovoGenitore(Genitore nuovoGenitore) {
        this.nuovoGenitore = nuovoGenitore;
    }
}
