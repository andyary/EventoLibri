package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe che rappresenta un amministratore del sistema.
 * Estende la classe Utente.
 */
public class Amministratore extends  Utente implements Serializable {

    /** Costruttore della classe Amministratore.
     *
     * @param nome Nome dell'amministratore.
     * @param cognome Cognome dell'amministratore.
     * @param userName Nome utente (username) dell'amministratore.
     */
    public Amministratore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
    }

    /** Costruttore della classe Amministratore con id.
     *
     * @param id Identificativo univoco dell'amministratore.
     * @param nome Nome dell'amministratore.
     * @param cognome Cognome dell'amministratore.
     * @param userName Nome utente (username) dell'amministratore.
     */
    public Amministratore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
    }


}
