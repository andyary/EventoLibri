package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroLettoreTest {

    // Test per il metodo modificaLettore
    @Test
    void modificaLettore() {
        // Crea due lettori e un libro
        Lettore lettore1 = new Lettore(1, "Mario", "Rossi", "mariorossi");
        Lettore lettore2 = new Lettore(2, "Luca", "Bianchi", "lucabianchi");
        Libro libro = new Libro("Il Nome della Rosa", 20, "link.com", "Umberto Eco", 1);
        // Crea un oggetto LibroLettore e modifica il lettore associato
        LibroLettore libroLettore = new LibroLettore(libro, 1);
        libroLettore.modificaLettore(lettore1);
        // Verifica che il lettore sia stato modificato correttamente
        assertEquals(lettore1, libroLettore.getLettore());
        assertNotEquals(lettore2, libroLettore.getLettore());
        // Crea un secondo oggetto LibroLettore con il secondo lettore
        LibroLettore libroLettore2 = new LibroLettore(libro, lettore2, 2);
        // Verifica che il lettore sia stato impostato correttamente
        assertEquals(lettore2, libroLettore2.getLettore());
        assertNotEquals(lettore1, libroLettore2.getLettore());
    }

    // Test per il metodo getLibro
    @Test
    void getLibro() {
        // Crea un oggetto LibroLettore
        Libro libro = new Libro("Il Nome della Rosa", 20, "link.com", "Umberto Eco", 1);
        LibroLettore libroLettore = new LibroLettore(libro, 1);
        // Verifica che il libro associato sia corretto
        assertEquals(libro, libroLettore.getLibro());
    }
}