package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Amministratore;


public class RispostaNuovoAmministratore extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private Amministratore nuovoAmministratore;

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

    public Amministratore getNuovoAmministratore() {
        return nuovoAmministratore;
    }

    public void setNuovoAmministratore(Amministratore nuovoAmministratore) {
        this.nuovoAmministratore = nuovoAmministratore;
    }
}
