package it.polimi.eventolibri.Model;

public class Recensione {

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

}
