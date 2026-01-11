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
        this.setId(id);
        this.setNome(nome);
        this.setCognome(cognome);
        this.setUserName(userName);
    }

    /** Imposta l'id dell'utente.
     *
     * @param id Identificativo univoco dell'utente.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Restituisce l'id dell'utente.
     *
     * @return Identificativo univoco dell'utente.
     */
    public int getId() {
        return id;
    }

    /** Restituisce il nome dell'utente.
     *
     * @return Nome dell'utente.
     */
    public String getNome() {
        return nome;
    }

    /** Restituisce il cognome dell'utente.
     *
     * @return Cognome dell'utente.
     */
    public String getCognome() {
        return cognome;
    }

    /** Restituisce lo username dell'utente.
     *
     * @return Nome utente (username) dell'utente.
     */
    public String getUserName() {
        return userName;
    }

    /** Imposta il nome dell'utente.
     *
     * @param nome Nome dell'utente.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /** Imposta il cognome dell'utente.
     *
     * @param cognome Cognome dell'utente.
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /** Imposta lo username dell'utente.
     *
     * @param userName Nome utente (username) dell'utente.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
