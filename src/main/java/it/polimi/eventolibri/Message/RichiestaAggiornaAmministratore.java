package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Amministratore;

public class RichiestaAggiornaAmministratore extends Messaggio{
    private Amministratore amministratore;

    public RichiestaAggiornaAmministratore(Amministratore amministratore) {
        this.amministratore = amministratore;
    }

    public Amministratore getAmministratore() {
        return amministratore;
    }
}
