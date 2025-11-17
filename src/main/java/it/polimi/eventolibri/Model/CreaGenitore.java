package it.polimi.eventolibri.Model;

public class CreaGenitore extends CreaUtente {

    public Genitore factory(String nome, String cognome, String userName) {
        return new Genitore(nome, cognome, userName);
    }

    public Genitore factory(int id, String nome, String cognome, String userName) {
        return new Genitore(id, nome, cognome, userName);
    }

}
