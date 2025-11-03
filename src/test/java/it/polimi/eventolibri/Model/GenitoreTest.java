package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GenitoreTest {

    @Test
    void aggiungiFiglio() {
        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        Figlio figlio1 = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        Figlio figlio2 = new Figlio("Angela", LocalDate.of(2017, 7, 7));
        assertEquals(0, genitore1.getNumeroFigli());
        genitore1.aggiungiFiglio(figlio1);
        assertEquals(1, genitore1.getNumeroFigli());
        genitore1.aggiungiFiglio(figlio2);
        assertEquals(2, genitore1.getNumeroFigli());
    }

    @Test
    void getNumeroFigli() {
        // Tested in aggiungiFiglio test
    }

    @Test
    void update() {
    }
}