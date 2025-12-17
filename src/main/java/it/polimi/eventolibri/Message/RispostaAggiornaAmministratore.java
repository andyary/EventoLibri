package it.polimi.eventolibri.Message;


import it.polimi.eventolibri.Model.Amministratore;

public class RispostaAggiornaAmministratore extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Amministratore amministratore;


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

    public void setAmministratore(Amministratore amministratore) {
        this.amministratore = amministratore;
    }

    public Amministratore getAmministratore() {
        return amministratore;
    }


}
