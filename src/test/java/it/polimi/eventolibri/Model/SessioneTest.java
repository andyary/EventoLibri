package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SessioneTest {

    // Test per il metodo setEventi
    @Test
    void setEventi() {
        // Crea due eventi di prova
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
        Evento evento1 = new Evento(creatore, "Evento di prova", luogo, date, scaletta);
        // Secondo evento
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
        Evento evento2 = new Evento(creatore2, "Evento di prova2", luogo2, date2, scaletta2);
        // Crea arrayList di eventi e una sessione
        ArrayList<Evento> eventi = new ArrayList<>();
        eventi.add(evento1);
        eventi.add(evento2);
        Utente utente = new Lettore("Test", "User", "testuser");
        Sessione sessione = new Sessione(utente);
        sessione.setEventi(eventi);
        // Verifica che gli eventi siano stati aggiunti correttamente
        assertEquals(2, sessione.getEventi().size());
        assertTrue(sessione.getEventi().contains(evento1));
        assertTrue(sessione.getEventi().contains(evento2));
        // Rimuovi e aggiungi eventi e verifica
        sessione.rimuoviEvento(evento1);
        assertEquals(1, sessione.getEventi().size());
        assertFalse(sessione.getEventi().contains(evento1));
        assertTrue(sessione.getEventi().contains(evento2));
        // Aggiungi di nuovo evento1
        sessione.aggiungiEvento(evento1);
        assertEquals(2, sessione.getEventi().size());
        assertTrue(sessione.getEventi().contains(evento1));
        assertTrue(sessione.getEventi().contains(evento2));
        // Crea una nuova sessione con gli eventi iniziali
        Sessione sessione2 = new Sessione(utente, eventi);
        // Verifica che gli eventi siano stati aggiunti correttamente
        assertEquals(2, sessione2.getEventi().size());
        assertTrue(sessione2.getEventi().contains(evento1));
        assertTrue(sessione2.getEventi().contains(evento2));
        // Rimuovi e aggiungi eventi e verifica
        sessione2.rimuoviEvento(evento1);
        assertEquals(1, sessione2.getEventi().size());
        assertFalse(sessione2.getEventi().contains(evento1));
        assertTrue(sessione2.getEventi().contains(evento2));
        // Aggiungi di nuovo evento1
        sessione2.aggiungiEvento(evento1);
        assertEquals(2, sessione2.getEventi().size());
        assertTrue(sessione2.getEventi().contains(evento1));
        assertTrue(sessione2.getEventi().contains(evento2));
    }

    // Test per il metodo aggiungiEvento
    @Test
    void aggiungiEvento() {
    }

    @Test
    void rimuoviEvento() {
    }
}