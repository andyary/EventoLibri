package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

public class RichiestaCancellaRecensione extends Messaggio{
    private Recensione recensione;

    public RichiestaCancellaRecensione(Recensione recensione) {
        this.recensione = recensione;
    }

    public Recensione getRecensione() {
        return recensione;
    }

}
