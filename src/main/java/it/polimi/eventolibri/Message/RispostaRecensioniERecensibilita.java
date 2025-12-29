package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Recensione;

import java.util.ArrayList;

public class RispostaRecensioniERecensibilita extends Messaggio {
    private boolean successo;
    private String messaggioerrore;
    private ArrayList<Recensione> recensioni;
    private boolean recensibile;

    public boolean isSuccesso() {
        return successo;
    }

    public void setSuccesso(boolean successo) {
        this.successo = successo;
    }

    public String getMessaggioerrore() {
        return messaggioerrore;
    }

    public void setMessaggioerrore(String messaggioerrore) {
        this.messaggioerrore = messaggioerrore;
    }

    public ArrayList<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(ArrayList<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    public boolean isRecensibile() {
        return recensibile;
    }

    public void setRecensibile(boolean recensibile) {
        this.recensibile = recensibile;
    }

}
