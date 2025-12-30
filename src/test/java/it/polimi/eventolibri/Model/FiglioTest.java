package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FiglioTest {

    @Test
    void iscrivi() {
        Figlio figlio1 = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        Figlio figlio2 = new Figlio("Angela", LocalDate.of(2017, 7, 7));
        Lettore creatore = new Lettore("Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
        Lettore lettore1 = new Lettore("Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = new Lettore("Anna", "Verdi", "annaverdi");
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento evento1= new Evento(creatore, "Evento di prova", luogo, date, scaletta);
        evento1.setId(5);

        Lettore creatore2 = new Lettore("Mario2", "Rossi2", "mariorossi2");
        Luogo luogo2 = new Luogo("Sala B", 10, 3);
        LocalDateTime date2 = LocalDateTime.of(2026, 5, 15, 18, 0);
        Libro libro3 = new Libro("Libro3", 12, "link3.com", "Autore3", 3);
        Libro libro4 = new Libro("Libro4", 5, "link4.com", "Autore4", 4);
        Lettore lettore3 = new Lettore("Luca2", "Bianchi2", "lucabianchi2");
        Lettore lettore4 = new Lettore("Anna2", "Verdi2", "annaverdi2");
        LibroLettore ll3 = new LibroLettore(libro3, lettore3, 1);
        LibroLettore ll4 = new LibroLettore(libro4, lettore4, 2);
        ArrayList<LibroLettore> scaletta2 = new ArrayList<LibroLettore>();
        scaletta2.add(ll3);
        scaletta2.add(ll4);
        Evento evento2= new Evento(creatore2, "Evento di prova2", luogo2, date2, scaletta2);
        evento2.setId(2);

        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        genitore1.aggiungiFiglio(figlio1);
        genitore1.aggiungiFiglio(figlio2);

        figlio1.iscrivi(evento1, genitore1);
        figlio1.iscrivi(evento2, genitore1);
        figlio2.iscrivi(evento2, genitore1);
        assertEquals(evento1, figlio1.getIscrizioni().get(0));
        assertEquals(evento2, figlio1.getIscrizioni().get(1));
        assertEquals(evento2, figlio2.getIscrizioni().get(0));
        assertEquals(2, figlio1.getNumeroIscrizioni());
        assertEquals(1, figlio2.getNumeroIscrizioni());

    }

    @Test
    void disiscrivi() {
        Figlio figlio1 = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        Figlio figlio2 = new Figlio("Angela", LocalDate.of(2017, 7, 7));
        Lettore creatore = new Lettore(1,"Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
        Lettore lettore1 = new Lettore(2,"Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = new Lettore(3,"Anna", "Verdi", "annaverdi");
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento evento1= new Evento(creatore, "Evento di prova", luogo, date, scaletta);
        evento1.setId(5);

        Lettore creatore2 = new Lettore(4,"Mario2", "Rossi2", "mariorossi2");
        Luogo luogo2 = new Luogo("Sala B", 10, 3);
        LocalDateTime date2 = LocalDateTime.of(2026, 5, 15, 18, 0);
        Libro libro3 = new Libro("Libro3", 12, "link3.com", "Autore3", 3);
        Libro libro4 = new Libro("Libro4", 5, "link4.com", "Autore4", 4);
        Lettore lettore3 = new Lettore(5,"Luca2", "Bianchi2", "lucabianchi2");
        Lettore lettore4 = new Lettore(6,"Anna2", "Verdi2", "annaverdi2");
        LibroLettore ll3 = new LibroLettore(libro3, lettore3, 1);
        LibroLettore ll4 = new LibroLettore(libro4, lettore4, 2);
        ArrayList<LibroLettore> scaletta2 = new ArrayList<LibroLettore>();
        scaletta2.add(ll3);
        scaletta2.add(ll4);
        Evento evento2= new Evento(creatore2, "Evento di prova2", luogo2, date2, scaletta2);
        evento2.setId(2);
        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        genitore1.aggiungiFiglio(figlio1);
        genitore1.aggiungiFiglio(figlio2);
        figlio1.iscrivi(evento1, genitore1);
        figlio1.iscrivi(evento2, genitore1);
        figlio2.iscrivi(evento2, genitore1);
        assertEquals(genitore1, evento1.getListeners().get(3));
        assertEquals(genitore1, evento2.getListeners().get(3));

        figlio1.disiscrivi(evento1, genitore1);
        assertFalse(evento1.getListeners().contains(genitore1));
        assertTrue(evento2.getListeners().contains(genitore1));

        assertEquals(evento2, figlio1.getIscrizioni().get(0));
        assertEquals(evento2, figlio2.getIscrizioni().get(0));
        assertEquals(1, figlio1.getNumeroIscrizioni());
        assertEquals(1, figlio2.getNumeroIscrizioni());

        figlio1.disiscrivi(evento2, genitore1);
        assertTrue(evento2.getListeners().contains(genitore1));

        figlio2.disiscrivi(evento2, genitore1);
        assertFalse(evento2.getListeners().contains(genitore1));


    }

    @Test
    void getNumeroIscrizioni() {
        // Testato nel metodo iscrivi e disiscrivi
    }
}