package it.polimi.eventolibri.Model;

import java.io.Serializable;

/**
 * Classe che rappresenta un luogo dove si svolgono gli eventi. Implementa l'interfaccia Serializable.
 * Contiene informazioni sul nome, la capienza e l'id del luogo.
 */
public class Luogo implements Serializable {

    private String nome;
    private int capienza;
    private int id;

    /**
     * Costruttori della classe Luogo.
     *
     * @param nome     Nome del luogo.
     * @param capienza Capienza del luogo.
     * @param id       ID univoco del luogo.
     */
    public Luogo(String nome, int capienza, int id) {
        this.nome = nome;
        this.capienza = capienza;
        this.id = id;
    }

    /**
     * Restituisce l'id del luogo.
     *
     * @return ID del luogo.
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce il nome del luogo.
     *
     * @return Nome del luogo.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce la capienza del luogo.
     *
     * @return Capienza del luogo.
     */
    public int getCapienza() {
        return capienza;
    }
}
