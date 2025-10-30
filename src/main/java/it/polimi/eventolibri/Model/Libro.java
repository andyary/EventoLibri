package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public class Libro {

	private String titolo;
	private int tempoLettura;
	private String link;
	private String autore;
	private int id;
	private ArrayList<Recensione> recensioni;

    public Libro(String titolo, int tempoLettura, String link, String autore, int id, ArrayList<Recensione> recensioni) {
        this.titolo = titolo;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = recensioni;
    }

    public int getTempoLettura() {
        return tempoLettura;
    }


}
