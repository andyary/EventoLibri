package it.polimi.eventolibri.Model;

import java.io.Serializable;

/** Classe che rappresenta una recensione di un libro scritta da un genitore. Implementa l'interfaccia Serializable.
 * Contiene informazioni sull'id della recensione, il genitore che l'ha scritta, il testo della recensione e il libro recensito.
 */
public class Recensione  implements Serializable {

	private int id;
	private Genitore genitore;
	private String testo;
	private Libro libro;

    /** Costruttori della classe Recensione.
     * @param id ID univoco della recensione.
     * @param genitore Genitore che ha scritto la recensione.
     * @param testo Testo della recensione.
     * @param libro Libro recensito.
     */
    public Recensione(int id, Genitore genitore, String testo, Libro libro) {
        this.id = id;
        this.genitore = genitore;
        this.testo = testo;
        this.libro = libro;
    }

    /** Costruttori della classe Recensione.
     * @param genitore Genitore che ha scritto la recensione.
     * @param testo Testo della recensione.
     * @param libro Libro recensito.
     */
    public Recensione(Genitore genitore, String testo, Libro libro) {
        this.genitore = genitore;
        this.testo = testo;
        this.libro = libro;
    }

    /** Restituisce il testo della recensione.
     * @return Testo della recensione.
     */
    public String getTesto() {
        return testo;
    }

    /** Restituisce il genitore che ha scritto la recensione.
     * @return Genitore della recensione.
     */
    public Genitore getGenitore() {
        return genitore;
    }

    /** Restituisce il libro recensito.
     * @return Libro recensito.
     */
    public Libro getLibro() {
        return libro;
    }

    /** Imposta il genitore che ha scritto la recensione.
     * @param genitore Genitore della recensione.
     */
    public void setGenitore(Genitore genitore) {
        this.genitore = genitore;
    }

    /** Imposta il testo della recensione.
     * @param testo Testo della recensione.
     */
    public void setTesto(String testo) {
        this.testo = testo;
    }

    /** Imposta il libro recensito.
     * @param libro Libro recensito.
     */
    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    /** Imposta l'ID univoco della recensione.
     * @param id ID univoco della recensione.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Restituisce l'ID univoco della recensione.
     * @return ID univoco della recensione.
     */
    public int getId() {
        return id;
    }
}
