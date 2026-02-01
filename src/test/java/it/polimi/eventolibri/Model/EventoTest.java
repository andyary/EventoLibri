package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EventoTest {

    @Test
    void evento() {
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
        Evento evento1= new Evento(creatore, "Evento di prova1", luogo, date, scaletta);
        assertNotNull(evento1);
        Evento evento2 = new Evento(1, creatore, "Evento di prova2", luogo, date, scaletta);
        assertNotNull(evento2);
        assertEquals("Evento di prova1", evento1.getNome()); // verifica nome evento
        assertEquals(luogo, evento1.getLuogo()); // verifica luogo evento
    }

    @Test
    void setId() {
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
        Evento evento= new Evento(creatore, "Evento di prova", luogo, date, scaletta);
        evento.setId(5);
        assertEquals(5, evento.getId());
    }

    @Test
    void aggiornaEvento() {
        Lettore creatore = new Lettore(1,"Mario", "Rossi", "mariorossi");
        Luogo luogo = new Luogo("Sala A", 15, 2);
        LocalDateTime date = LocalDateTime.of(2026, 6, 15, 18, 0);
        Libro libro1 = new Libro("Libro1", 10, "link1.com", "Autore1", 1);
        Libro libro2 = new Libro("Libro2", 9, "link2.com", "Autore2", 2);
        Lettore lettore1 = new Lettore(2,"Luca", "Bianchi", "lucabianchi");
        Lettore lettore2 = new Lettore(3,"Anna", "Verdi", "annaverdi");
        Lettore lettoreX = new Lettore(4,"Paolo", "Neri", "paoloneri");
        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        ArrayList<LibroLettore> scaletta = new ArrayList<LibroLettore>();
        scaletta.add(ll1);
        scaletta.add(ll2);
        Evento evento1= new Evento(creatore, "Evento di prova", luogo, date, scaletta);
        evento1.setId(5);

        assertEquals(1, creatore.getEventiCreati().size());
        assertEquals(1, lettore1.getIscrizioniLettura().size());


        assertEquals("Sala A", evento1.getLuogo().getNome());
        assertEquals(15, evento1.getLuogo().getCapienza());
        assertEquals(2, evento1.getLuogo().getId());
        assertTrue(evento1.isIscritto(lettore1));
        assertTrue(evento1.isIscritto(lettore2));
        assertFalse(evento1.isIscritto(lettoreX));
        assertEquals(creatore, evento1.getListeners().get(0));
        assertEquals(lettore1, evento1.getListeners().get(1));
        assertEquals(lettore2, evento1.getListeners().get(2));


        Genitore genitore1 = new Genitore(4,"Mario", "Rossi", "mariorossi");
        Genitore genitore2 = new Genitore(5,"Luca", "Bianchi", "lucabianchi");
        Figlio figlio1 = new Figlio("Mario", LocalDate.of(2015, 1, 1));
        Figlio figlio2 = new Figlio("Luca", LocalDate.of(2015, 1, 2));
        genitore1.aggiungiFiglio(figlio1);
        genitore2.aggiungiFiglio(figlio2);
        figlio1.iscrivi(evento1, genitore1);
        assertEquals(genitore1, evento1.getListeners().get(3));
        figlio2.iscrivi(evento1, genitore2);
        assertEquals(genitore2, evento1.getListeners().get(4));

        // evento1.addListener(genitore1);
        // dobbiamo testare observer pattern lato lettore e verificare lato figli/genitori

        assertEquals(2, evento1.getListenersGenitori().size());
        ArrayList<Listener> Listeners = new ArrayList<>(evento1.getListeners());
        assertEquals(5, Listeners.size());
        evento1.removeAllListeners();
        assertEquals(0, evento1.getListeners().size());
        evento1.addListener(creatore);
        assertEquals(1, evento1.getListeners().size());
        evento1.addListeners(Listeners);
        assertEquals(5, evento1.getListeners().size());



        Lettore creatore2 = new Lettore(6,"Mario2", "Rossi2", "mariorossi2");
        Luogo luogo2 = new Luogo("Sala B", 10, 3);
        LocalDateTime date2 = LocalDateTime.of(2026, 5, 15, 18, 0);
        Libro libro3 = new Libro("Libro3", 12, "link3.com", "Autore3", 3);
        Libro libro4 = new Libro("Libro4", 5, "link4.com", "Autore4", 4);
        Lettore lettore3 = new Lettore(7,"Luca2", "Bianchi2", "lucabianchi2");
        Lettore lettore4 = new Lettore(8,"Anna2", "Verdi2", "annaverdi2");
        LibroLettore ll3 = new LibroLettore(libro3, lettore3, 1);
        LibroLettore ll4 = new LibroLettore(libro4, lettore4, 2);
        ArrayList<LibroLettore> scaletta2 = new ArrayList<LibroLettore>();
        scaletta2.add(ll3);
        scaletta2.add(ll4);
        Evento evento2= new Evento(creatore2, "Evento di prova2", luogo2, date2, scaletta2);
        evento2.setId(2);
        evento1.aggiornaEvento(evento2);

        assertNotEquals(evento1, evento2);
        assertEquals(5, evento1.getId());
        assertNotEquals(evento2.getId(), evento1.getId());
        assertEquals(creatore, evento1.getCreatore());
        assertEquals(evento2.getNome(), evento1.getNome());
        assertEquals(evento2.getLuogo(), evento1.getLuogo());
        assertEquals(evento2.getData(), evento1.getData());
        assertEquals(evento2.getScaletta(), evento1.getScaletta());


    }

    @Test
    void calcolaOraFine() {
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
        LocalDateTime oraFine = evento1.calcolaOraFine();
        assertEquals(18, oraFine.getHour());
        assertEquals(19, oraFine.getMinute());

    }

    @Test
    void setIscritti() {
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
        evento1.setIscritti(10);
        assertEquals(10, evento1.getIscritti());
    }
}