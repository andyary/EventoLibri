package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibroTest {

    // Test per il metodo getTitolo
    @Test
    void getTempoLettura() {
        // Crea un oggetto Libro di esempio
        Libro libro = new Libro("Titolo Esempio", 12, "http://linkesempio.com", "Autore Esempio", 1);
        // Verifica che il metodo getTempoLettura restituisca il valore corretto
        assertEquals(12, libro.getTempoLettura());
    }

    // Test per il metodo aggiungiRecensione
    @Test
    void aggiungiRecensione() {
        // Crea un oggetto Libro di esempio
        Libro libro1 = new Libro("Titolo1", 12, "http://linkesempio.com", "Autore1", 1);
        // Crea due oggetti Recensione di esempio
        Genitore genitore1 = new Genitore("Mario", "Rossi", "mariorossi");
        Recensione recensione1 = new Recensione(1, genitore1, "Ottimo libro!", libro1);
        Recensione recensione2 = new Recensione(genitore1, "Ottimo libro2!", libro1);
        // Verifica che le recensioni vengano aggiunte correttamente
        assertEquals(0, libro1.getRecensioni().size());
        // Aggiungi la prima recensione e verifica
        libro1.aggiungiRecensione(recensione1);
        assertEquals(1, libro1.getRecensioni().size());
        assertEquals(recensione1, libro1.getRecensioni().get(0));
        // Aggiungi la seconda recensione e verifica
        libro1.aggiungiRecensione(recensione2);
        assertEquals(2, libro1.getRecensioni().size());
        assertEquals(recensione2, libro1.getRecensioni().get(1));
        assertEquals(0, libro1.getRecensioni().get(1).getId());
        // Imposta l'ID della seconda recensione e verifica nuovamente
        recensione2.setId(11);
        assertEquals(11, libro1.getRecensioni().get(1).getId());
        // Verifica le proprietà della seconda recensione
        assertEquals(genitore1, libro1.getRecensioni().get(1).getGenitore());
        assertEquals("Ottimo libro2!", libro1.getRecensioni().get(1).getTesto());
        assertEquals(libro1, libro1.getRecensioni().get(1).getLibro());
        // Crea un secondo oggetto Libro utilizzando il costruttore con ISBN e recensioni
        Libro libro2 = new Libro(libro1.getTitolo(), libro1.getTempoLettura(), libro1.getLink(), libro1.getAutore(), libro1.getId(), "ISBN", libro1.getRecensioni());
        // Verifica le proprietà del secondo libro
        assertEquals("ISBN", libro2.getIsbn());
        assertEquals(2, libro2.getRecensioni().size());
    }
}