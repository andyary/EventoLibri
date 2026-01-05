package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe che rappresenta un amministratore del sistema.
 * Estende la classe Utente.
 */
public class Amministratore extends  Utente implements Serializable {

    /** Costruttori della classe Amministratore.
     * @param nome Nome dell'amministratore.
     * @param cognome Cognome dell'amministratore.
     * @param userName Username dell'amministratore.
     * @param id ID univoco dell'amministratore.
     */
    public Amministratore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
    }

    public Amministratore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
    }


}
