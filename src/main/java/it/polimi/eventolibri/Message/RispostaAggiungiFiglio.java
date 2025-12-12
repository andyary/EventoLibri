package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

public class RispostaAggiungiFiglio extends Messaggio{
    private boolean successo;
    private String messaggioErrore;
    private Genitore genitore;
    private Figlio figlio;

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

    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    public Genitore getGenitore() {
        return genitore;
    }

    public Figlio getNuovoFiglio() {
        return figlio;
    }

    public void setNuovoFiglio(Figlio figlio) {
        this.figlio = figlio;
    }
}
