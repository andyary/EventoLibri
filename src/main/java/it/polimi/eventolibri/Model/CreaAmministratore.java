package it.polimi.eventolibri.Model;

public class CreaAmministratore extends CreaUtente<Amministratore> {

    public Amministratore factory(String nome, String cognome, String userName) {
        return new Amministratore(nome, cognome, userName);
    }

    public Amministratore factory(int id, String nome, String cognome, String userName) {
        return new Amministratore(id, nome, cognome, userName);
    }
}
