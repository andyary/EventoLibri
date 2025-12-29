package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

public class RispostaAggiungiRecensione extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Recensione recensione;

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

    public Recensione getRecensione() {
        return recensione;
    }

    public void setRecensione(Recensione recensione) {
        this.recensione = recensione;
    }
}
