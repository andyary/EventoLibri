package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

public class RichiestaAggiornaGenitore extends Messaggio{
    private Genitore genitore;

    public RichiestaAggiornaGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    public Genitore getGenitore() {
        return genitore;
    }
}
