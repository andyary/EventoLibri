package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreaLettoreTest {

    // Test per la creazione di un lettore tramite factory pattern
    @Test
    void factory() {
        // Crea nuovo lettore tramite factory pattern
        CreaUtente creaLettore = new CreaLettore();
        Utente lettore = creaLettore.nuovoUtente("Mario", "Rossi", "mrossi");
        // Verifica le proprietà del lettore creato
        assertEquals("Mario", lettore.getNome());
        assertEquals("Rossi", lettore.getCognome());
        assertEquals("mrossi", lettore.getUserName());
    }
}