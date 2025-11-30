package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Utente;

public class RispostaLogin extends Messaggio {

    private boolean successo;
    private Utente utente;
    private String messaggioerrore;

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

}
