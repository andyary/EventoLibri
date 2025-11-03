package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    @Test
    void getTempoLettura() {
        Libro libro = new Libro("Titolo Esempio", 12, "http://linkesempio.com", "Autore Esempio", 1, null);
        assertEquals(12, libro.getTempoLettura());
    }
}