package it.polimi.eventolibri.Model;

import java.io.Serializable;

public class Luogo  implements Serializable {

	private String nome;
	private int capienza;
	private int id;

    public Luogo(String nome, int capienza, int id) {
        this.nome = nome;
        this.capienza = capienza;
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
