package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Utente;

public class RichiestaRecensioniERecensibilita extends Messaggio {
    private Libro libro;
    private Utente utente;

    public RichiestaRecensioniERecensibilita(Libro libro, Utente utente) {
        this.utente = utente;
        this.libro = libro;
    }

    public Libro getLibro() {
        return libro;
    }

    public Utente getUtente() {
        return utente;
    }
}

