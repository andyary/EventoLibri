package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;

import java.util.ArrayList;

public class RispostaNextEventi extends Messaggio {
    private boolean successo;
    private String messaggioErrore;
    private ArrayList<Evento> prossimiEventi;

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

    public ArrayList<Evento> getProssimiEventi() {
        return prossimiEventi;
    }

    public void setProssimiEventi(ArrayList<Evento> prossimiEventi) {
        this.prossimiEventi = prossimiEventi;
    }
}
