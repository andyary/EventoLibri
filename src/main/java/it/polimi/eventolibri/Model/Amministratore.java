package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Amministratore extends  Utente implements Serializable {

    public Amministratore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
    }

    public Amministratore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
    }

}
