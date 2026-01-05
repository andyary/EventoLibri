package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe che rappresenta un genitore nel sistema.
 * Estende la classe Utente e implementa l'interfaccia Listener.
 */
public class Genitore extends Utente implements Listener, Serializable {

	private ArrayList<Figlio> figli= new ArrayList<>();

    /** Costruttori della classe Genitore.
     * @param nome Nome del genitore.
     * @param cognome Cognome del genitore.
     * @param userName Username del genitore.
     */
    public Genitore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
    }

    /** Costruttori della classe Genitore.
     * @param nome Nome del genitore.
     * @param cognome Cognome del genitore.
     * @param userName Username del genitore.
     * @param id ID univoco del genitore.
     */
    public Genitore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
    }

    /**
     * Aggiunge un figlio al genitore.
     * @param figlio Il figlio da aggiungere.
     */
    public void aggiungiFiglio(Figlio figlio) {
        // Aggiungi il figlio all'array figli
        figli.add(figlio);
	}

    /**
     * Imposta la lista dei figli del genitore.
     * @param figli La lista dei figli da impostare.
     */
    public void setFigli(ArrayList<Figlio> figli) {
        this.figli = figli;
    }

    /**
     * Restituisce il numero di figli del genitore.
     * @return Il numero di figli.
     */
    public int getNumeroFigli() {
        return figli.size();
    }

    /**
     * Restituisce la lista dei figli del genitore.
     * @return La lista dei figli.
     */
    public ArrayList<Figlio> getFigli() {
        return figli;
    }

    @Override
    public void update(Evento evento) {
        for (Figlio figlio : figli) {
            figlio.aggiornaEvento(evento);
        }

	}

    /**
     * Confronta questo genitore con un altro listener confrontando il loro id.
     * @param listener Il listener da confrontare.
     * @return true se i due listener sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Listener listener) {
        return this.getId() == listener.getId();
    }

}
