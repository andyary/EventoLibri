package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Figlio;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.Model.Recensione;

public class RichiestaAggiungiRecensione extends Messaggio{
    private Recensione recensione;

    public RichiestaAggiungiRecensione(Recensione recensione) {
        this.recensione = recensione;
    }

    public Recensione getRecensione() {
        return recensione;
    }

}
