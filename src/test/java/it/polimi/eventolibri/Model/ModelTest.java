package it.polimi.eventolibri.Model;

import it.polimi.eventolibri.Model.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
    @Test
    void test() {
        CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
        Amministratore admin1 = creaAmministratore.nuovoUtente(1, "Andrea", "Bianchi", "admin1");
        Amministratore admin2 = creaAmministratore.nuovoUtente(2,"Luca", "Rossi", "admin2");
        CreaUtente<Lettore> creaLettore = new CreaLettore();
        Lettore lettore1 = creaLettore.nuovoUtente(11,"Mario", "Verdi", "lettore1");
        Lettore lettore2 = creaLettore.nuovoUtente(12,"Giulia", "Neri", "lettore2");
        Lettore lettore3 = creaLettore.nuovoUtente(13,"Francesca", "Rosa", "lettore3");
        Lettore lettore4 = creaLettore.nuovoUtente(14,"Alessandro", "Marroni", "lettore4");
        // TODO aggiungere eventiCreati e iscrizioniLettura

        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
        Genitore genitore1 = creaGenitore.nuovoUtente(21,"Paolo", "Gialli", "genitore1");
        Genitore genitore2 = creaGenitore.nuovoUtente(22,"Sara", "Blu", "genitore2");
        Genitore genitore3 = creaGenitore.nuovoUtente(23,"Elena", "Viola", "genitore3");
        Genitore genitore4 = creaGenitore.nuovoUtente(24,"Marco", "Arancioni", "genitore4");

        Figlio figlio1 = new Figlio(1, "Figlio1", LocalDate.of(2016, 1, 1));
        Figlio figlio2 = new Figlio(2, "Figlio2", LocalDate.of(2014, 2, 2));
        Figlio figlio3 = new Figlio(3, "Figlio3", LocalDate.of(2017, 3, 3));
        Figlio figlio4 = new Figlio(4, "Figlio4", LocalDate.of(2018, 1, 1));
        Figlio figlio5 = new Figlio(5, "Figlio5", LocalDate.of(2015, 5, 5));
        Figlio figlio6 = new Figlio(6, "Figlio6", LocalDate.of(2020, 1, 1));
        Figlio figlio7 = new Figlio(7, "Figlio7", LocalDate.of(2019, 7, 7));
        Figlio figlio8 = new Figlio(8, "Figlio8", LocalDate.of(2013, 9, 8));

        genitore1.aggiungiFiglio(figlio1);
        genitore1.aggiungiFiglio(figlio2);
        genitore1.aggiungiFiglio(figlio3);
        genitore2.aggiungiFiglio(figlio4);
        genitore2.aggiungiFiglio(figlio5);
        genitore3.aggiungiFiglio(figlio6);
        genitore3.aggiungiFiglio(figlio7);
        genitore4.aggiungiFiglio(figlio8);

        Luogo luogo1 = new Luogo("Sala 1", 10, 1);
        Luogo luogo2 = new Luogo("Sala 2", 10, 2);
        Luogo luogo3 = new Luogo("Sala 3", 10, 3);
        Luogo luogo4 = new Luogo("Sala 4", 10, 4);

        Libro libro1 = new Libro("Il Grande Gatsby", 15, "link1.com", "F. Scott Fitzgerald", 1);
        Libro libro2 = new Libro("1984", 12, "link2.com", "George Orwell", 2);
        Libro libro3 = new Libro("To Kill a Mockingbird", 14, "link3.com", "Harper Lee", 3);
        Libro libro4 = new Libro("Pride and Prejudice", 11, "link4.com", "Jane Austen", 4);
        Libro libro5 = new Libro("Moby Dick", 13, "link5.com", "Herman Melville", 5);
        Libro libro7 = new Libro("War and Peace", 19, "link7.com", "Leo Tolstoy", 7);
        Libro libro8 = new Libro("The Odyssey", 17, "link8.com", "Homer", 8);

        Recensione recensione1 = new Recensione(1, genitore1, "Molto bello", libro1);
        libro1.aggiungiRecensione(recensione1);
        Recensione recensione2 = new Recensione(2, genitore2, "Interessante", libro2);
        libro2.aggiungiRecensione(recensione2);
        Recensione recensione3 = new Recensione(3, genitore3, "Coinvolgente", libro3);
        libro3.aggiungiRecensione(recensione3);
        Recensione recensione4 = new Recensione(4, genitore4, "Emozionante", libro4);
        libro4.aggiungiRecensione(recensione4);
        Recensione recensione5 = new Recensione(5, genitore1, "Avvincente", libro5);
        libro5.aggiungiRecensione(recensione5);
        Recensione recensione6 = new Recensione(6, genitore2, "Avvincente", libro7);
        libro7.aggiungiRecensione(recensione6);
        Recensione recensione7 = new Recensione(7, genitore3, "Avvincente", libro8);
        libro8.aggiungiRecensione(recensione7);
        Recensione recensione8 = new Recensione(8, genitore4, "Avvincente", libro1);
        libro1.aggiungiRecensione(recensione8);
        Recensione recensione9 = new Recensione(9, genitore1, "Avvincente", libro2);
        libro2.aggiungiRecensione(recensione9);
        Recensione recensione10 = new Recensione(10, genitore2, "Avvincente", libro3);
        libro3.aggiungiRecensione(recensione10);
        Recensione recensione11 = new Recensione(11, genitore2, "Avvincente", libro4);
        libro4.aggiungiRecensione(recensione11);
        Recensione recensione12 = new Recensione(12, genitore3, "Avvincente", libro5);
        libro5.aggiungiRecensione(recensione12);
        Recensione recensione13 = new Recensione(13, genitore4, "Avvincente", libro7);
        libro7.aggiungiRecensione(recensione13);
        Recensione recensione14 = new Recensione(14, genitore1, "Avvincente", libro8);
        libro8.aggiungiRecensione(recensione14);
        Recensione recensione15 = new Recensione(15, genitore2, "Avvincente", libro1);
        libro1.aggiungiRecensione(recensione15);
        Recensione recensione16 = new Recensione(16, genitore3, "Avvincente", libro2);
        libro2.aggiungiRecensione(recensione16);
        Recensione recensione17 = new Recensione(17, genitore4, "Avvincente", libro3);
        libro3.aggiungiRecensione(recensione17);
        Recensione recensione18 = new Recensione(18, genitore1, "Avvincente", libro4);
        libro4.aggiungiRecensione(recensione18);
        Recensione recensione19 = new Recensione(19, genitore2, "Avvincente", libro5);
        libro5.aggiungiRecensione(recensione19);
        Recensione recensione20 = new Recensione(20, genitore3, "Avvincente", libro7);
        libro7.aggiungiRecensione(recensione20);

        LibroLettore ll1 = new LibroLettore(libro1, lettore1, 1);
        LibroLettore ll2 = new LibroLettore(libro2, lettore2, 2);
        LibroLettore ll3 = new LibroLettore(libro3, lettore3, 3);
        LibroLettore ll4 = new LibroLettore(libro4, lettore4, 4);
        ArrayList<LibroLettore> scaletta1 = new ArrayList<LibroLettore>();
        scaletta1.add(ll1);
        scaletta1.add(ll2);
        scaletta1.add(ll3);
        scaletta1.add(ll4);
        Evento evento1= new Evento(1, lettore1, "Evento 1", luogo1, LocalDateTime.of(2026,01,01,10,00), scaletta1);
        genitore1.getFigli().get(0).iscrivi(evento1, genitore1);
        genitore2.getFigli().get(0).iscrivi(evento1, genitore2);
        genitore1.getFigli().get(1).iscrivi(evento1, genitore1);
        genitore3.getFigli().get(0).iscrivi(evento1, genitore3);
        genitore3.getFigli().get(1).iscrivi(evento1, genitore3);

        LibroLettore ll11 = new LibroLettore(libro5, lettore2, 1);
        LibroLettore ll12 = new LibroLettore(libro7, lettore3, 2);
        LibroLettore ll13 = new LibroLettore(libro8, lettore4, 3);
        ArrayList<LibroLettore> scaletta2 = new ArrayList<LibroLettore>();
        scaletta2.add(ll11);
        scaletta2.add(ll12);
        scaletta2.add(ll13);
        Evento evento2= new Evento(2, lettore2, "Evento 2", luogo2, LocalDateTime.of(2026,02,02,11,00), scaletta2);
        genitore2.getFigli().get(0).iscrivi(evento2, genitore2);
        genitore2.getFigli().get(1).iscrivi(evento2, genitore2);
        genitore4.getFigli().get(0).iscrivi(evento2, genitore4);

        LibroLettore ll21 = new LibroLettore(libro1, lettore3, 1);
        LibroLettore ll22 = new LibroLettore(libro2, lettore4, 2);
        LibroLettore ll23 = new LibroLettore(libro3, lettore4, 3);
        LibroLettore ll24 = new LibroLettore(libro4, lettore1, 4);
        ArrayList<LibroLettore> scaletta3 = new ArrayList<LibroLettore>();
        scaletta3.add(ll21);
        scaletta3.add(ll22);
        scaletta3.add(ll23);
        scaletta3.add(ll24);
        Evento evento3= new Evento(3, lettore3, "Evento 3", luogo3, LocalDateTime.of(2026,03,03,12,00), scaletta3);
        genitore3.getFigli().get(0).iscrivi(evento3, genitore3);
        genitore3.getFigli().get(1).iscrivi(evento3, genitore3);
        genitore4.getFigli().get(0).iscrivi(evento3, genitore4);
        genitore1.getFigli().get(1).iscrivi(evento3, genitore1);

        LibroLettore ll31 = new LibroLettore(libro5, lettore4, 1);
        LibroLettore ll32 = new LibroLettore(libro7, lettore1, 2);
        LibroLettore ll33 = new LibroLettore(libro8, lettore2, 3);
        ArrayList<LibroLettore> scaletta4 = new ArrayList<LibroLettore>();
        scaletta4.add(ll31);
        scaletta4.add(ll32);
        scaletta4.add(ll33);
        Evento evento4= new Evento(4, lettore4, "Evento 4", luogo4, LocalDateTime.of(2026,04,04,13,00), scaletta4);
        genitore4.getFigli().get(0).iscrivi(evento4, genitore4);
        genitore1.getFigli().get(0).iscrivi(evento4, genitore4);
        genitore1.getFigli().get(1).iscrivi(evento4, genitore1);
        genitore2.getFigli().get(0).iscrivi(evento4, genitore2);
        genitore3.getFigli().get(0).iscrivi(evento4, genitore3);
        genitore2.getFigli().get(1).iscrivi(evento4, genitore2);



        assertNotNull(admin1);
        assertNotNull(admin2);
        assertNotNull(lettore1);
        assertNotNull(lettore2);
        assertNotNull(genitore1);
        assertNotNull(genitore2);
        assertNotNull(genitore3);
        assertNotNull(genitore4);
        assertNotNull(figlio1);
        assertNotNull(figlio2);
        assertNotNull(figlio3);
        assertNotNull(figlio4);
        assertNotNull(figlio5);
        assertNotNull(figlio6);
        assertNotNull(figlio7);
        assertNotNull(figlio8);
        assertEquals(figlio1, genitore1.getFigli().get(0));
        assertEquals(figlio2, genitore1.getFigli().get(1));
        assertEquals(figlio3, genitore1.getFigli().get(2));
        assertEquals(figlio4, genitore2.getFigli().get(0));
        assertEquals(figlio5, genitore2.getFigli().get(1));
        assertEquals(figlio6, genitore3.getFigli().get(0));
        assertEquals(figlio7, genitore3.getFigli().get(1));
        assertEquals(figlio8, genitore4.getFigli().get(0));
        assertNotNull(luogo1);
        assertNotNull(luogo2);
        assertNotNull(luogo3);
        assertNotNull(luogo4);
        assertNotNull(libro1);
        assertNotNull(libro2);
        assertNotNull(libro3);
        assertNotNull(libro4);
        assertNotNull(libro5);
        assertNotNull(libro7);
        assertNotNull(libro8);
        assertNotNull(recensione1);
        assertNotNull(recensione2);
        assertNotNull(recensione3);
        assertNotNull(recensione4);
        assertNotNull(recensione5);
        assertNotNull(recensione6);
        assertNotNull(recensione7);
        assertNotNull(recensione8);
        assertNotNull(recensione9);
        assertNotNull(recensione10);
        assertNotNull(recensione11);
        assertNotNull(recensione12);
        assertNotNull(recensione13);
        assertNotNull(recensione14);
        assertNotNull(recensione15);
        assertNotNull(recensione16);
        assertNotNull(recensione17);
        assertNotNull(recensione18);
        assertNotNull(recensione19);
        assertNotNull(recensione20);
        assertNotNull(evento1);
        assertNotNull(evento2);
        assertNotNull(evento3);
        assertNotNull(evento4);


    }

}
