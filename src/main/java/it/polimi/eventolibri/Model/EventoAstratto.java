package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class EventoAstratto  implements Serializable {

    private ArrayList<Listener> listeners = new ArrayList<Listener>();

	public void addListener(Listener listener) {
        if (!listeners.contains(listener)){
        listeners.add(listener);
	}
    }

	public void removeListener(Listener listener) {
        listeners.remove(listener);
	}

    public ArrayList<Listener> getListeners() {
        return listeners;
    }

	public void updateAll(Evento evento) {
        for (Listener listener : listeners) {
            listener.update(evento);
        }
	}

}
