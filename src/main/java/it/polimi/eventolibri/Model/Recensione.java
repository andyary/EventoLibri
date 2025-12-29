package it.polimi.eventolibri.Model;

import java.io.Serializable;

public class Recensione  implements Serializable {

	private int id;
	private Genitore genitore;
	private String testo;
	private Libro libro;

    public Recensione(int id, Genitore genitore, String testo, Libro libro) {
        this.id = id;
        this.genitore = genitore;
        this.testo = testo;
        this.libro = libro;
    }

    public Recensione(Genitore genitore, String testo, Libro libro) {
        this.genitore = genitore;
        this.testo = testo;
        this.libro = libro;
    }

    public String getTesto() {
        return testo;
    }

    public Genitore getGenitore() {
        return genitore;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
