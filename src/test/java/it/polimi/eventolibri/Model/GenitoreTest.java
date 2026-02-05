package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GenitoreTest {

    // Test per il metodo aggiungiFiglio
    @Test
    void aggiungiFiglio() {
        // Crea un genitore e due figli
        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        Figlio figlio1 = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        Figlio figlio2 = new Figlio("Angela", LocalDate.of(2017, 7, 7));
        // Verifica il numero di figli prima e dopo l'aggiunta
        assertEquals(0, genitore1.getNumeroFigli());
        genitore1.aggiungiFiglio(figlio1);
        assertEquals(1, genitore1.getNumeroFigli());
        genitore1.aggiungiFiglio(figlio2);
        assertEquals(2, genitore1.getNumeroFigli());
    }

    // Test per il metodo getNumeroFigli
    @Test
    void getNumeroFigli() {
        // Tested in aggiungiFiglio test
    }

    // Test per il metodo update
    @Test
    void update() {
        // Crea un genitore e due figli
        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        Figlio figlio1 = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        Figlio figlio2 = new Figlio("Angela", LocalDate.of(2017, 7, 7));
        // Aggiungi i figli al genitore
        genitore1.aggiungiFiglio(figlio1);
        genitore1.aggiungiFiglio(figlio2);
        // Crea evento e iscrivi i figli
        Lettore creatore = new Lettore("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        Evento evento1 = new Evento(creatore, "Evento di prova", luogo, null, new ArrayList<>());
        figlio1.iscrivi(evento1, genitore1);
        figlio2.iscrivi(evento1, genitore1);
        // Verifica il numero di figli prima e dopo l'update
        assertEquals(2, genitore1.getFigli().get(0).getIscrizioni().get(0).getIscritti());
        // Crea un secondo genitore con un solo figlio iscritto
        Genitore genitore2 = new Genitore("Luigi", "Verdi", "luigiverdi");
        Figlio figlio3 = new Figlio("Carlo", LocalDate.of(2016, 3, 3));
        genitore2.aggiungiFiglio(figlio3);
        figlio3.iscrivi(evento1, genitore2);
        // Esegui l'update del primo genitore con i dati del secondo
        assertEquals(3, genitore1.getFigli().get(0).getIscrizioni().get(0).getIscritti());
        genitore1.update(evento1);
        assertEquals(3, genitore1.getFigli().get(0).getIscrizioni().get(0).getIscritti());
    }
}