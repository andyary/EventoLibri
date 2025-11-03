package it.polimi.eventolibri.Model;

public class LibroLettore {

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
        return lettore;
    }

    public int getProgressivo() {
        return progressivo;
    }
}
