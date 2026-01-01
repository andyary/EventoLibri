package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class EventoAstratto  implements Serializable {

    private ArrayList<Listener> listeners = new ArrayList<Listener>();

	public void addListener(Listener listener) {
        // cicla su tutti i listeners per verificare se gia uno è equal a quello da aggiungere
        for (Listener l : listeners){
            if (l.equals(listener)){
                return; // esci dal metodo se lo trovi
            }
        }
        listeners.add(listener);
        // if (!listeners.contains(listener)){listeners.add(listener);}
    }

    public void addListeners(ArrayList<Genitore> listenersToAdd) {
        for (Genitore genitore : listenersToAdd) {
            addListener(genitore);
        }
    }


	public void removeListener(Listener listener) {
        for (Listener l : listeners){
            if (l.equals(listener)){
                listeners.remove(l);
                return; // esci dal metodo dopo aver rimosso
            }
        }
        // listeners.remove(listener);
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
