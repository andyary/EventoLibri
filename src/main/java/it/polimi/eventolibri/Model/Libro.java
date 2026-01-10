package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/** Classe che rappresenta un libro. Implementa l'interfaccia Serializable.
 * Contiene informazioni sul titolo, tempo di lettura, link, autore, id, isbn e recensioni del libro.
 */
public class Libro  implements Serializable {

	private String titolo;
	private int tempoLettura;
	private String link;
	private String autore;
	private int id;
    private String isbn;
	private ArrayList<Recensione> recensioni;;

    /** Costruttori della classe Libro.
     * @param titolo Titolo del libro.
     * @param tempoLettura Tempo di lettura del libro.
     * @param link Link al libro.
     * @param autore Autore del libro.
     * @param id ID univoco del libro.
     * @param recensioni Array di recensioni del libro.
     */
    public Libro(String titolo, int tempoLettura, String link, String autore, int id, ArrayList<Recensione> recensioni) {
        this(titolo, tempoLettura, link, autore, id);
        this.aggiungiRecensioni(recensioni);
    }

    /** Costruttori della classe Libro.
     * @param titolo Titolo del libro.
     * @param tempoLettura Tempo di lettura del libro.
     * @param link Link al libro.
     * @param autore Autore del libro.
     * @param id ID univoco del libro.
     */
    public Libro(String titolo, int tempoLettura, String link, String autore, int id) {
        this.titolo = titolo;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = new ArrayList<>();
    }

    /** Costruttori della classe Libro.
     * @param titolo Titolo del libro.
     * @param tempoLettura Tempo di lettura del libro.
     * @param link Link al libro.
     * @param autore Autore del libro.
     * @param id ID univoco del libro.
     * @param isbn ISBN del libro.
     * @param recensioni Array di recensioni del libro.
     */
    public Libro(String titolo, int tempoLettura, String link, String autore, int id, String isbn, ArrayList<Recensione> recensioni) {
        this(titolo, tempoLettura, link, autore, id, recensioni);
        this.isbn = isbn;
    }

    /** Costruttori della classe Libro.
     * @param titolo Titolo del libro.
     * @param tempoLettura Tempo di lettura del libro.
     * @param link Link al libro.
     * @param autore Autore del libro.
     * @param id ID univoco del libro.
     * @param isbn ISBN del libro.
     */
    public Libro(String titolo, int tempoLettura, String link, String autore, int id, String isbn) {
        this.titolo = titolo;
        this.isbn = isbn;
        this.tempoLettura = tempoLettura;
        this.link = link;
        this.autore = autore;
        this.id = id;
        this.recensioni = new ArrayList<>();
    }

    /** Restituisce il tempo di lettura del libro.
     * @return Tempo di lettura del libro.
     */
    public int getTempoLettura() {
        return tempoLettura;
    }

    /** Aggiunge una recensione al libro.
     * @param recensione Recensione da aggiungere.
     */
    public void aggiungiRecensione(Recensione recensione) {
        recensioni.add(recensione);
    }

    /** Aggiunge una lista di recensioni al libro.
     * @param recensioni Array di recensioni da aggiungere.
     */
    public void aggiungiRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    /** Restituisce la lista delle recensioni del libro.
     * @return Array di recensioni del libro.
     */
    public ArrayList<Recensione> getRecensioni() {
        return recensioni;
    }

    /** Restituisce l'ID del libro.
     * @return ID del libro.
     */
    public int getId() { return id;
    }

    /** Restituisce il titolo del libro.
     * @return Titolo del libro.
     */
    public String getTitolo() {
        return titolo;
    }

    /** Restituisce l'autore del libro.
     * @return Autore del libro.
     */
    public String getAutore() {
        return autore;
    }

    /** Restituisce l'ISBN del libro.
     * @return ISBN del libro.
     */
    public String getIsbn() {
        return isbn;
    }

    /** Restituisce il link del libro.
     * @return Link del libro.
     */
    public String getLink() {
        return link;
    }
}
