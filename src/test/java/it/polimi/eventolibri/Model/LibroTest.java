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

    }
}