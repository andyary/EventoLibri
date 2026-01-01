package it.polimi.eventolibri.Model;

public interface Listener {

	public abstract void update(Evento evento);

    public abstract boolean equals(Listener listener);

    public abstract int getId();
}
