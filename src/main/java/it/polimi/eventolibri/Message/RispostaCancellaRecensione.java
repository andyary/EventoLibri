package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

public class RispostaCancellaRecensione extends Messaggio {

    private boolean successo;
    private String messaggioErrore;
    private int id_recensione;

    public RispostaCancellaRecensione(Recensione r) {
        this.id_recensione = r.getId();
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

    public int getId() {
        return id_recensione;
    }

    public  void setId(int id_recensione) {
        this.id_recensione = id_recensione;
    }
}
