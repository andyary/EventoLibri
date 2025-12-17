package it.polimi.eventolibri.Message;


import it.polimi.eventolibri.Model.Amministratore;

public class RichiestaNuovoAmministratore extends Messaggio{
    private Amministratore nuovoAmministratore;
    private String password;

    public Amministratore getNuovoAmministratore() {
        return nuovoAmministratore;
    }
    public String getPassword() {
        return password;
    }

    public RichiestaNuovoAmministratore(Amministratore nuovoAmministratore, String password) {
        this.password = password;
        this.nuovoAmministratore = nuovoAmministratore;
    }
}
