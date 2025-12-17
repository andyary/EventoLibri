package it.polimi.eventolibri.Message;


import it.polimi.eventolibri.Model.Lettore;

public class RispostaAggiornaLettore extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Lettore lettore;


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

    public void setLettore(Lettore lettore) {
        this.lettore = lettore;
    }

    public Lettore getLettore() {
        return lettore;
    }


}
