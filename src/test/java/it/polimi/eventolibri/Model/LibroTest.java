package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    @Test
    void getTempoLettura() {
        Libro libro = new Libro("Titolo Esempio", 12, "http://linkesempio.com", "Autore Esempio", 1);
        assertEquals(12, libro.getTempoLettura());
    }

    @Test
    void aggiungiRecensione() {
        Libro libro1 = new Libro("Titolo1", 12, "http://linkesempio.com", "Autore1", 1);
        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        Recensione recensione1 = new Recensione(1,genitore1, "Ottimo libro!", libro1);
        Recensione recensione2 = new Recensione(genitore1, "Ottimo libro2!", libro1);
        assertEquals(0, libro1.getRecensioni().size());
        libro1.aggiungiRecensione(recensione1);
        assertEquals(1, libro1.getRecensioni().size());
        assertEquals(recensione1, libro1.getRecensioni().get(0));
        libro1.aggiungiRecensione(recensione2);
        assertEquals(2, libro1.getRecensioni().size());
        assertEquals(recensione2, libro1.getRecensioni().get(1));
        assertEquals(0,libro1.getRecensioni().get(1).getId());
        recensione2.setId(11);
        assertEquals(11,libro1.getRecensioni().get(1).getId());
        assertEquals(genitore1, libro1.getRecensioni().get(1).getGenitore());
        assertEquals("Ottimo libro2!", libro1.getRecensioni().get(1).getTesto());
        assertEquals(libro1, libro1.getRecensioni().get(1).getLibro());

        Libro libro2 = new Libro(libro1.getTitolo(), libro1.getTempoLettura(), libro1.getLink(), libro1.getAutore(), libro1.getId(), "ISBN", libro1.getRecensioni());
        assertEquals("ISBN", libro2.getIsbn());
        assertEquals(2, libro2.getRecensioni().size());




    }
}