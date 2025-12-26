package it.polimi.eventolibri.Model;

import java.io.Serializable;

public class LibroLettore  implements Serializable {

	private Libro libro;
	private Lettore lettore;
	private int progressivo;

	public LibroLettore(Libro libro, Lettore lettore, int progressivo) {
        this.libro = libro;
        this.lettore = lettore;
        this.progressivo = progressivo;
	}

	public LibroLettore(Libro libro, int progressivo) {
        this.libro = libro;
        this.progressivo = progressivo;
	}

	public void modificaLettore(Lettore lettore) {
        this.lettore = lettore;
	}

    public Libro getLibro() {
        return libro;
    }

    public Lettore getLettore() {
        if (lettore == null) return null;
        if (lettore.getId() == 0) return null;
        else return lettore;
    }

    public int getProgressivo() {
        return progressivo;
    }

    public void setProgressivo(int progressivo) {
        this.progressivo = progressivo;
    }
}
