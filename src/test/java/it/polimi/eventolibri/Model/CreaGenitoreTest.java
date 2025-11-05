package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreaGenitoreTest {

    @Test
    void factory() {
        CreaUtente creaGenitore = new CreaGenitore();
        Utente Genitore = creaGenitore.nuovoUtente("Mario", "Rossi", "mrossi");
        assertEquals("Mario", Genitore.getNome());
        assertEquals("Rossi", Genitore.getCognome());
        assertEquals("mrossi", Genitore.getUserName());
    }
}