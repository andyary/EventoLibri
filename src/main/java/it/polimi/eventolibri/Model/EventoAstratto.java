package it.polimi.eventolibri.Model;

import java.util.ArrayList;

public abstract class EventoAstratto {

	private ArrayList<Listener> listeners = new ArrayList<Listener>();

	public void addListener(Listener listener) {
        listeners.add(listener);
	}

	public void removeListener(Listener listener) {
        listeners.remove(listener);
	}

	public void updateAll(Evento evento) {
        for (Listener listener : listeners) {
            listener.update(evento);
        }
	}

}
