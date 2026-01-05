package it.polimi.eventolibri.Model;

import java.io.Serializable;

/** Classe che rappresenta l'associazione tra un libro e un lettore. Implementa l'interfaccia Serializable.
 * Contiene informazioni sul libro, il lettore e un progressivo (rappresenta l'ordine di librolettore nella scaletta dell'evento).
 */
public class LibroLettore  implements Serializable {

	private Libro libro;
	private Lettore lettore;
	private int progressivo;

    /** Costruttori della classe LibroLettore.
     * @param libro Libro associato.
     * @param lettore Lettore associato.
     * @param progressivo Progressivo dell'associazione.
     */
	public LibroLettore(Libro libro, Lettore lettore, int progressivo) {
        this.libro = libro;
        this.lettore = lettore;
        this.progressivo = progressivo;
	}

    /** Costruttori della classe LibroLettore (senza Lettore associato).
     * @param libro Libro associato.
     * @param progressivo Progressivo dell'associazione.
     */
	public LibroLettore(Libro libro, int progressivo) {
        this.libro = libro;
        this.progressivo = progressivo;
	}

    /** Modifica il lettore associato.
     * @param lettore Nuovo lettore da associare.
     */
	public void modificaLettore(Lettore lettore) {
        this.lettore = lettore;
	}

    /** Restituisce il libro associato.
     * @return Libro associato.
     */
    public Libro getLibro() {
        return libro;
    }

    /** Restituisce il lettore associato.
     * @return Lettore associato.
     */
    public Lettore getLettore() {
        if (lettore == null) return null;
        if (lettore.getId() == 0) return null;
        else return lettore;
    }

    /** Restituisce il progressivo (rappresenta il progressivo nella scaletta dell'evento).
     * @return Progressivo dell'associazione.
     */
    public int getProgressivo() {
        return progressivo;
    }

    /** Imposta il progressivo (rappresenta il progressivo nella scaletta dell'evento).
     * @param progressivo Nuovo progressivo dell'associazione.
     */
    public void setProgressivo(int progressivo) {
        this.progressivo = progressivo;
    }
}
