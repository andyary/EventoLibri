package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Libro  implements Serializable {

	private String titolo;
	private int tempoLettura;
	private String link;
	private String autore;
	private int id;
    private String isbn;
	private ArrayList<Recensione> recensioni;;

    public Libro(String titolo, int tempoLettura, String link, String autore, int id, ArrayList<Recensione> recensioni) {
        this.titolo = titolo;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = recensioni;
    }

    public Libro(String titolo, int tempoLettura, String link, String autore, int id) {
        this.titolo = titolo;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = new ArrayList<>();
    }

    public Libro(String titolo, int tempoLettura, String link, String autore, int id, String isbn, ArrayList<Recensione> recensioni) {
        this.titolo = titolo;
        this.isbn = isbn;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = recensioni;
    }

    public Libro(String titolo, int tempoLettura, String link, String autore, int id, String isbn) {
        this.titolo = titolo;
        this.isbn = isbn;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = new ArrayList<>();
    }

    public int getTempoLettura() {
        return tempoLettura;
    }

    public void aggiungiRecensione(Recensione recensione) {
        recensioni.add(recensione);
    }

    public void aggiungiRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    public ArrayList<Recensione> getRecensioni() {
        return recensioni;
    }

    public int getId() { return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getAutore() {
        return autore;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getLink() {
        return link;
    }
}
