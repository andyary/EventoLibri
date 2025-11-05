package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreaLettoreTest {

    @Test
    void factory() {
        CreaUtente creaLettore = new CreaLettore();
        Utente lettore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        assertEquals("Mario", lettore.getNome());
        assertEquals("Rossi", lettore.getCognome());
        assertEquals("mrossi", lettore.getUserName());
    }
}