package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;

public class RispostaNuovoLettore extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Lettore nuovoLettore;

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

    public Lettore getNuovoLettore() {
        return nuovoLettore;
    }

    public void setNuovoLettore(Lettore nuovoLettore) {
        this.nuovoLettore = nuovoLettore;
    }
}
