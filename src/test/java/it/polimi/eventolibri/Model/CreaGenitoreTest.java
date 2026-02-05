package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreaGenitoreTest {

    // Test per la creazione di un genitore tramite factory pattern
    @Test
    void factory() {
        // Crea nuovo genitore tramite factory pattern
        CreaUtente creaGenitore = new CreaGenitore();
        Utente Genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        // Verifica le proprietà del genitore creato
        assertEquals("Mario", Genitore.getNome());
        assertEquals("Rossi", Genitore.getCognome());
        assertEquals("mrossi", Genitore.getUserName());
    }
}