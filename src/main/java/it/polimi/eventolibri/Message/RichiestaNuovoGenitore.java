package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Genitore;

public class RichiestaNuovoGenitore extends Messaggio{
    private Genitore nuovoGenitore;
    private String password;

    public Genitore getNuovoGenitore() {
        return nuovoGenitore;
    }
    public String getPassword() {
        return password;
    }

    public RichiestaNuovoGenitore(Genitore nuovoGenitore, String password) {
        this.password = password;
        this.nuovoGenitore = nuovoGenitore;
    }
}
