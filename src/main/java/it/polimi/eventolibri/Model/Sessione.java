package it.polimi.eventolibri.Model;

import java.util.ArrayList;

/**
 * Classe che rappresenta una sessione utente, contenente informazioni sugli eventi associati e l'utente stesso.
 */
public class Sessione {

    private ArrayList<Evento> eventi; // Lista di eventi associati alla sessione
    private Utente utente; // Utente associato alla sessione

    /**
     * Costruttori della classe Sessione. Inizializza arrayList di eventi vuota.
     *
     * @param utente Utente associato alla sessione.
     */
    public Sessione(Utente utente) {
        this.utente = utente;
        this.eventi = new ArrayList<>();
    }

    /**
     * Costruttori della classe Sessione.
     *
     * @param utente Utente associato alla sessione.
     * @param eventi Array di eventi associati alla sessione.
     */
    public Sessione(Utente utente, ArrayList<Evento> eventi) {
        this.utente = utente;
        this.eventi = eventi;
    }

    /**
     * Restituisce l'utente associato alla sessione.
     *
     * @return Utente della sessione.
     */
    public Utente getUtente() {
        return utente;
    }


    /**
     * Imposta lista di eventi della sessione associati all'utente.
     *
     * @param eventi Array di eventi da impostare.
     */
    public void setEventi(ArrayList<Evento> eventi) {
        this.eventi.addAll(eventi);
    }

    /**
     * Aggiunge un evento alla lista di eventi della sessione.
     *
     * @param evento Evento da aggiungere.
     */
    public void aggiungiEvento(Evento evento) {
        this.eventi.add(evento);
    }

    /**
     * Rimuove un evento dalla lista di eventi della sessione.
     *
     * @param evento Evento da rimuovere.
     */
    public void rimuoviEvento(Evento evento) {
        this.eventi.remove(evento);
    }

    /**
     * Restituisce la lista di eventi associati alla sessione.
     *
     * @return Array di eventi della sessione.
     */
    public ArrayList<Evento> getEventi() {
        return eventi;
    }
}
