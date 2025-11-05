package it.polimi.eventolibri.Model;

public class CreaAmministratore extends CreaUtente {

    public Amministratore factory(String nome, String cognome, String userName) {
        return new Amministratore(nome, cognome, userName);
    }
}
