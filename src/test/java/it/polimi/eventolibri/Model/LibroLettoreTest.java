package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroLettoreTest {

    @Test
    void modificaLettore() {
        Lettore lettore1 = new Lettore(1, "Mario", "Rossi", "mariorossi");
        Lettore lettore2 = new Lettore( 2,"Luca", "Bianchi", "lucabianchi");
        Libro libro = new Libro("Il Nome della Rosa", 20, "link.com", "Umberto Eco", 1);
        LibroLettore libroLettore = new LibroLettore(libro, 1);
        libroLettore.modificaLettore(lettore1);
        assertEquals(lettore1, libroLettore.getLettore());
        assertNotEquals(lettore2, libroLettore.getLettore());
        LibroLettore libroLettore2 = new LibroLettore(libro, lettore2, 2);
        assertEquals(lettore2, libroLettore2.getLettore());
        assertNotEquals(lettore1, libroLettore2.getLettore());

    }

    @Test
    void getLibro() {

    }
}