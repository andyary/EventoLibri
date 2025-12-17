package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;

public class RichiestaAggiornaLettore extends Messaggio{
    private Lettore lettore;

    public RichiestaAggiornaLettore(Lettore lettore) {
        this.lettore = lettore;
    }

    public Lettore getLettore() {
        return lettore;
    }
}
