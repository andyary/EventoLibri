package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe astratta che rappresenta un utente del sistema.
 */
public abstract class Utente implements Serializable {

	private int id;
	private String nome;
	private String cognome;
	private String userName;

    /** Costruttore della classe Utente.
     *
     * @param nome Nome dell'utente.
     * @param cognome Cognome dell'utente.
     * @param userName Nome utente (username) dell'utente.
     */
    public Utente(String nome, String cognome, String userName) {
        this.nome = nome;
        this.cognome = cognome;
        this.userName = userName;
    }

    /** Costruttore della classe Utente con id.
     *
     * @param id Identificativo univoco dell'utente.
     * @param nome Nome dell'utente.
     * @param cognome Cognome dell'utente.
     * @param userName Nome utente (username) dell'utente.
     */
    public Utente(int id, String nome, String cognome, String userName) {
        this.nome = nome;
        this.cognome = cognome;
        this.userName = userName;
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getUserName() {
        return userName;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
