package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class Richieste {

    @Test
    void Login() {
        RichiestaLogin r = new RichiestaLogin("mrossi","psw");
        // adattare i nomi dei setter/getter se diversi nella classe reale


        assertEquals("mrossi", r.getUsername());
        assertEquals("psw", r.getPassword());

        // toString non deve essere null (utile per log/debug)
        assertNotNull(r.toString());

        // creiamo un'altra istanza con gli stessi valori e controlliamo i getter
        RichiestaLogin r2 = new RichiestaLogin("mrossi", "psw");

        assertEquals(r.getUsername(), r2.getUsername());
        assertEquals(r.getPassword(), r2.getPassword());
    }

    @Test
    void NuovoLettore() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        RichiestaNuovoLettore r = new RichiestaNuovoLettore(lettore,"psw");
        // adattare i nomi dei setter/getter se diversi nella classe reale
        assertEquals("mrossi", r.getNuovoLettore().getUserName());
        assertEquals("psw", r.getPassword());
        assertEquals("Mario", r.getNuovoLettore().getNome());
        assertEquals("Rossi", r.getNuovoLettore().getCognome());
        // toString non deve essere null (utile per log/debug)
    }

    @Test
    void NuovoGenitore() {
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        RichiestaNuovoGenitore r = new RichiestaNuovoGenitore(genitore,"psw");


        assertEquals("mrossi", r.getNuovoGenitore().getUserName());
        assertEquals("psw", r.getPassword());
        assertEquals("Mario", r.getNuovoGenitore().getNome());
        assertEquals("Rossi", r.getNuovoGenitore().getCognome());

    }

    @Test
    void NuovoAmministratore() {
        Amministratore amministratore = new Amministratore("Mario", "Rossi", "mrossi");
        RichiestaNuovoAmministratore r = new RichiestaNuovoAmministratore(amministratore, "psw");


        assertEquals("mrossi", r.getNuovoAmministratore().getUserName());
        assertEquals("psw", r.getPassword());
        assertEquals("Mario", r.getNuovoAmministratore().getNome());
        assertEquals("Rossi", r.getNuovoAmministratore().getCognome());


        assertNotNull(r.toString());
    }

    @Test
    void AggiornaAmministratore() {
        Amministratore amministratore = new Amministratore("Mario", "Rossi", "mrossi");
        RichiestaAggiornaAmministratore r = new RichiestaAggiornaAmministratore(amministratore);


        assertEquals("mrossi", r.getAmministratore().getUserName());
        assertEquals("Mario", r.getAmministratore().getNome());
        assertEquals("Rossi", r.getAmministratore().getCognome());


        assertNotNull(r.toString());
    }

    @Test
    void AggiornaGenitore() {
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        RichiestaAggiornaGenitore r = new RichiestaAggiornaGenitore(genitore);


        assertEquals("mrossi", r.getGenitore().getUserName());
        assertEquals("Mario", r.getGenitore().getNome());
        assertEquals("Rossi", r.getGenitore().getCognome());


        assertNotNull(r.toString());
    }

    @Test
    void AggiornaLettore() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        RichiestaAggiornaLettore r = new RichiestaAggiornaLettore(lettore);


        assertEquals("mrossi", r.getLettore().getUserName());
        assertEquals("Mario", r.getLettore().getNome());
        assertEquals("Rossi", r.getLettore().getCognome());

        assertNotNull(r.toString());

    }

    @Test
    void AggiungiFiglio() {
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        RichiestaAggiungiFiglio r = new RichiestaAggiungiFiglio(genitore, figlio);


        assertEquals(r.getGenitore().getUserName(), "mrossi");
        assertEquals(r.getFiglioNuovo().getNome(), "figlio1");

        assertNotNull(r.toString());
    }

    @Test
    void AggiungiRecensione() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("Titolo", 3 , "link.com", "ISBN12345", 10);
        Recensione recensione = new Recensione(genitore, "Ottimo libro!", libro);
        RichiestaAggiungiRecensione r = new RichiestaAggiungiRecensione(recensione);


        assertEquals(r.getRecensione().getGenitore().getUserName(), "mrossi");
        assertEquals(r.getRecensione().getTesto(), "Ottimo libro!");
        assertEquals(r.getRecensione().getLibro().getId(), 10);

        assertNotNull(r.toString());
    }

    @Test
    void CancellaRecensione() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("Titolo", 3 , "link.com", "ISBN12345", 10);
        Recensione recensione = new Recensione(genitore, "Ottimo libro!", libro);
        RichiestaCancellaRecensione r = new RichiestaCancellaRecensione(recensione);


        assertEquals(r.getRecensione().getGenitore().getUserName(), "mrossi");
        assertEquals(r.getRecensione().getTesto(), "Ottimo libro!");
        assertEquals(r.getRecensione().getLibro().getId(), 10);

        assertNotNull(r.toString());
    }

    @Test
    void IscrizioneEvento() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        genitore.aggiungiFiglio(figlio);
        Luogo luogo = new Luogo("Biblioteca Centrale", 30,1);
        Evento evento = new Evento(lettore,"Evento1", luogo, LocalDateTime.of(2024, 7, 15, 10, 0 ));
        RichiestaIscrizioneEvento r = new RichiestaIscrizioneEvento(figlio, evento, genitore);

        assertEquals(r.getGenitore().getUserName(), "mrossi");
        assertEquals(r.getFiglio().getNome(), "figlio1");
        assertEquals(r.getEvento().getNome(), "Evento1");

        assertNotNull(r.toString());
    }

    @Test
    void DisiscrizioneEvento() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        genitore.aggiungiFiglio(figlio);
        Luogo luogo = new Luogo("Biblioteca Centrale", 30,1);
        Evento evento = new Evento(lettore,"Evento1", luogo, LocalDateTime.of(2024, 7, 15, 10, 0 ));
        RichiestaDisiscrizioneEvento r = new RichiestaDisiscrizioneEvento(figlio, evento, genitore);

        assertEquals(r.getGenitore().getUserName(), "mrossi");
        assertEquals(r.getFiglio().getNome(), "figlio1");
        assertEquals(r.getEvento().getNome(), "Evento1");

        assertNotNull(r.toString());
    }

    @Test
    void NexteSalvaEventi() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        genitore.aggiungiFiglio(figlio);
        Luogo luogo = new Luogo("Biblioteca Centrale", 30,1);
        Evento evento = new Evento(lettore,"Evento1", luogo, LocalDateTime.of(2024, 7, 15, 10, 0 ));
        RichiestaSalvaEvento r1 = new RichiestaSalvaEvento(evento);
        RichiestaNextEventi r2 = new RichiestaNextEventi(evento);


        assertEquals(r1.getEvento().getNome(), "Evento1");
        assertEquals(r2.getUltimoEvento().getNome(), "Evento1");

        assertNotNull(r1.toString());
        assertNotNull(r2.toString());
    }

    @Test
    void RecensioniERecensibilita() {
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("Titolo", 3 , "link.com", "ISBN12345", 10);
        RichiestaRecensioniERecensibilita r = new RichiestaRecensioniERecensibilita(libro, lettore);

        assertEquals(r.getLibro().getId(), 10);
        assertEquals(r.getUtente().getUserName(), "mrossi");

        assertNotNull(r.toString());
    }




}
