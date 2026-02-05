package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Richieste {

    // test per la richiesta di login
    @Test
    void Login() {
        // istanza con valori di esempio
        RichiestaLogin r = new RichiestaLogin("mrossi", "psw");
        // controlliamo i getter
        assertEquals("mrossi", r.getUsername());
        assertEquals("psw", r.getPassword());

        // toString non deve essere null
        assertNotNull(r.toString());

        // creiamo un'altra istanza con gli stessi valori e controlliamo i getter
        RichiestaLogin r2 = new RichiestaLogin("mrossi", "psw");
        // i valori devono essere uguali
        assertEquals(r.getUsername(), r2.getUsername());
        assertEquals(r.getPassword(), r2.getPassword());
    }

    // test per la richiesta di creazione di un nuovo lettore
    @Test
    void NuovoLettore() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        RichiestaNuovoLettore r = new RichiestaNuovoLettore(lettore, "psw");
        // verifica
        assertEquals("mrossi", r.getNuovoLettore().getUserName());
        assertEquals("psw", r.getPassword());
        assertEquals("Mario", r.getNuovoLettore().getNome());
        assertEquals("Rossi", r.getNuovoLettore().getCognome());
    }

    // test per la richiesta di creazione di un nuovo genitore
    @Test
    void NuovoGenitore() {
        // istanza con valori di esempio
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        RichiestaNuovoGenitore r = new RichiestaNuovoGenitore(genitore, "psw");
        // verifica
        assertEquals("mrossi", r.getNuovoGenitore().getUserName());
        assertEquals("psw", r.getPassword());
        assertEquals("Mario", r.getNuovoGenitore().getNome());
        assertEquals("Rossi", r.getNuovoGenitore().getCognome());
    }

    // test per la richiesta di creazione di un nuovo amministratore
    @Test
    void NuovoAmministratore() {
        // istanza con valori di esempio
        Amministratore amministratore = new Amministratore("Mario", "Rossi", "mrossi");
        RichiestaNuovoAmministratore r = new RichiestaNuovoAmministratore(amministratore, "psw");
        // verifica
        assertEquals("mrossi", r.getNuovoAmministratore().getUserName());
        assertEquals("psw", r.getPassword());
        assertEquals("Mario", r.getNuovoAmministratore().getNome());
        assertEquals("Rossi", r.getNuovoAmministratore().getCognome());
        assertNotNull(r.toString());
    }

    // test per le richieste di aggiornamento di amminisratore
    @Test
    void AggiornaAmministratore() {
        // istanza con valori di esempio
        Amministratore amministratore = new Amministratore("Mario", "Rossi", "mrossi");
        RichiestaAggiornaAmministratore r = new RichiestaAggiornaAmministratore(amministratore);
        // verifica
        assertEquals("mrossi", r.getAmministratore().getUserName());
        assertEquals("Mario", r.getAmministratore().getNome());
        assertEquals("Rossi", r.getAmministratore().getCognome());
        assertNotNull(r.toString());
    }

    @Test
    void AggiornaGenitore() {
        // istanza con valori di esempio
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        RichiestaAggiornaGenitore r = new RichiestaAggiornaGenitore(genitore);
        // verifica
        assertEquals("mrossi", r.getGenitore().getUserName());
        assertEquals("Mario", r.getGenitore().getNome());
        assertEquals("Rossi", r.getGenitore().getCognome());
        assertNotNull(r.toString());
    }

    @Test
    void AggiornaLettore() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        RichiestaAggiornaLettore r = new RichiestaAggiornaLettore(lettore);
        // verifica
        assertEquals("mrossi", r.getLettore().getUserName());
        assertEquals("Mario", r.getLettore().getNome());
        assertEquals("Rossi", r.getLettore().getCognome());
        assertNotNull(r.toString());
    }

    // test per la richiesta di aggiunta di un figlio
    @Test
    void AggiungiFiglio() {
        // istanza con valori di esempio
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        RichiestaAggiungiFiglio r = new RichiestaAggiungiFiglio(genitore, figlio);
        // verifica
        assertEquals(r.getGenitore().getUserName(), "mrossi");
        assertEquals(r.getFiglioNuovo().getNome(), "figlio1");
        assertNotNull(r.toString());
    }

    // test per la richiesta di rimozione di un figlio
    @Test
    void AggiungiRecensione() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("Titolo", 3, "link.com", "ISBN12345", 10);
        Recensione recensione = new Recensione(genitore, "Ottimo libro!", libro);
        RichiestaAggiungiRecensione r = new RichiestaAggiungiRecensione(recensione);
        // verifica
        assertEquals(r.getRecensione().getGenitore().getUserName(), "mrossi");
        assertEquals(r.getRecensione().getTesto(), "Ottimo libro!");
        assertEquals(r.getRecensione().getLibro().getId(), 10);
        assertNotNull(r.toString());
    }

    // test per la richiesta di cancellazione di una recensione
    @Test
    void CancellaRecensione() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("Titolo", 3, "link.com", "ISBN12345", 10);
        Recensione recensione = new Recensione(genitore, "Ottimo libro!", libro);
        RichiestaCancellaRecensione r = new RichiestaCancellaRecensione(recensione);
        // verifica
        assertEquals(r.getRecensione().getGenitore().getUserName(), "mrossi");
        assertEquals(r.getRecensione().getTesto(), "Ottimo libro!");
        assertEquals(r.getRecensione().getLibro().getId(), 10);
        assertNotNull(r.toString());
    }

    // test per la richiesta di iscrizione ad un evento
    @Test
    void IscrizioneEvento() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        genitore.aggiungiFiglio(figlio);
        Luogo luogo = new Luogo("Biblioteca Centrale", 30, 1);
        Evento evento = new Evento(lettore, "Evento1", luogo, LocalDateTime.of(2024, 7, 15, 10, 0));
        RichiestaIscrizioneEvento r = new RichiestaIscrizioneEvento(figlio, evento, genitore);
        // verifica
        assertEquals(r.getGenitore().getUserName(), "mrossi");
        assertEquals(r.getFiglio().getNome(), "figlio1");
        assertEquals(r.getEvento().getNome(), "Evento1");
        assertNotNull(r.toString());
    }

    // test per la richiesta di disiscrizione da un evento
    @Test
    void DisiscrizioneEvento() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        genitore.aggiungiFiglio(figlio);
        Luogo luogo = new Luogo("Biblioteca Centrale", 30, 1);
        Evento evento = new Evento(lettore, "Evento1", luogo, LocalDateTime.of(2024, 7, 15, 10, 0));
        RichiestaDisiscrizioneEvento r = new RichiestaDisiscrizioneEvento(figlio, evento, genitore);
        // verifica
        assertEquals(r.getGenitore().getUserName(), "mrossi");
        assertEquals(r.getFiglio().getNome(), "figlio1");
        assertEquals(r.getEvento().getNome(), "Evento1");
        assertNotNull(r.toString());
    }

    // test per la richiesta di salvataggio di un evento e per la richiesta dei prossimi eventi
    @Test
    void NexteSalvaEventi() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Genitore genitore = new Genitore("Mario", "Rossi", "mrossi");
        Figlio figlio = new Figlio("figlio1", LocalDate.of(2015, 5, 20));
        genitore.aggiungiFiglio(figlio);
        Luogo luogo = new Luogo("Biblioteca Centrale", 30, 1);
        Evento evento = new Evento(lettore, "Evento1", luogo, LocalDateTime.of(2024, 7, 15, 10, 0));
        RichiestaSalvaEvento r1 = new RichiestaSalvaEvento(evento);
        RichiestaNextEventi r2 = new RichiestaNextEventi(evento);
        // verifica
        assertEquals(r1.getEvento().getNome(), "Evento1");
        assertEquals(r2.getUltimoEvento().getNome(), "Evento1");
        assertNotNull(r1.toString());
        assertNotNull(r2.toString());
    }

    // test per la richiesta di recensioni e recensibilità di un libro
    @Test
    void RecensioniERecensibilita() {
        // istanza con valori di esempio
        Lettore lettore = new Lettore("Mario", "Rossi", "mrossi");
        Libro libro = new Libro("Titolo", 3, "link.com", "ISBN12345", 10);
        RichiestaRecensioniERecensibilita r = new RichiestaRecensioniERecensibilita(libro, lettore);
        // verifica
        assertEquals(r.getLibro().getId(), 10);
        assertEquals(r.getUtente().getUserName(), "mrossi");
        assertNotNull(r.toString());
    }
}
