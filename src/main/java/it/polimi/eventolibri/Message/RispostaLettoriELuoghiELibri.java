package it.polimi.eventolibri.Message;

import it.polimi.eventolibri.Model.Lettore;
import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Model.Luogo;

import java.util.ArrayList;

public class RispostaLettoriELuoghiELibri extends Messaggio {
    private boolean successo;
    private String messaggioerrore;
    private ArrayList<Lettore> lettori;
    private ArrayList<Luogo> luoghi;
    private ArrayList<Libro> elencolibri;

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

    public ArrayList<Lettore> getLettori() {
        return lettori;
    }

    public void setLettori(ArrayList<Lettore> lettori) {
        this.lettori = lettori;
    }

    public ArrayList<Luogo> getLuoghi() {
        return luoghi;
    }

    public void setLuoghi(ArrayList<Luogo> luoghi) {
        this.luoghi = luoghi;
    }

    public ArrayList<Libro> getElencolibri() {
        return elencolibri;
    }

    public void setElencolibri(ArrayList<Libro> elencolibri) {
        this.elencolibri = elencolibri;
    }
}
