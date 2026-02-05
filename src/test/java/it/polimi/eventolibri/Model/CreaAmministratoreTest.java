package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreaAmministratoreTest {

    // Test per la creazione di un amministratore tramite factory pattern
    @Test
    void factory() {
        // Crea nuovo amministratore tramite factory pattern
        CreaUtente creaAmministratore = new CreaAmministratore();
        Utente Amministratore = creaAmministratore.nuovoUtente("Mario", "Rossi", "mrossi");
        // Verifica le proprietà dell'amministratore creato
        assertEquals("Mario", Amministratore.getNome());
        assertEquals("Rossi", Amministratore.getCognome());
        assertEquals("mrossi", Amministratore.getUserName());
    }
}