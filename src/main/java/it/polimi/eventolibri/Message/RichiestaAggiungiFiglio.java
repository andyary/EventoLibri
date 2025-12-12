package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;

public class RichiestaAggiungiFiglio extends Messaggio{
    private Genitore genitore;
    private Figlio nuovoFiglio;

    public RichiestaAggiungiFiglio(Genitore genitore, Figlio nuovo) {
        this.genitore = genitore;
        this.nuovoFiglio = nuovo;
    }

    public Genitore getGenitore() {
        return genitore;
    }

    public Figlio getFiglioNuovo() {
        return nuovoFiglio;
    }
}
