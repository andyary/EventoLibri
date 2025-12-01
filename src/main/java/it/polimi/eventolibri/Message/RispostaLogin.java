package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Evento;
import it.polimi.eventolibri.Model.Utente;

import java.util.ArrayList;

public class RispostaLogin extends Messaggio {

    private boolean successo;
    private Utente utente;
    private String messaggioerrore;
    private ArrayList<Evento> prossimiEventi;

    public boolean isSuccesso() {
        return successo;
    }

    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }

    public ArrayList<Evento> getProssimiEventi() {
        return prossimiEventi;
    }

    public void setProssimiEventi(ArrayList<Evento> prossimiEventi) {
        this.prossimiEventi = prossimiEventi;
    }
}
