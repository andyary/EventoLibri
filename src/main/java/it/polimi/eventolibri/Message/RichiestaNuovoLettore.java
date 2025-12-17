package it.polimi.eventolibri.Message;


import it.polimi.eventolibri.Model.Lettore;

public class RichiestaNuovoLettore extends Messaggio{
    private Lettore nuovoLettore;
    private String password;

    public Lettore getNuovoLettore() {
        return nuovoLettore;
    }
    public String getPassword() {
        return password;
    }

    public RichiestaNuovoLettore(Lettore nuovoLettore, String password) {
        this.password = password;
        this.nuovoLettore = nuovoLettore;
    }
}
