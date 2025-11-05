package it.polimi.eventolibri.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreaAmministratoreTest {

    @Test
    void factory() {
        CreaUtente creaAmministratore = new CreaAmministratore();
        Utente Amministratore = creaAmministratore.nuovoUtente("Mario", "Rossi", "mrossi");
        assertEquals("Mario", Amministratore.getNome());
        assertEquals("Rossi", Amministratore.getCognome());
        assertEquals("mrossi", Amministratore.getUserName());
    }
}