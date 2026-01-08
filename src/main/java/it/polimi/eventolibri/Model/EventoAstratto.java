package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe astratta che rappresenta un evento generico nel sistema.
 * Implementa il pattern Observer per notificare i listener registrati
 * quando si modifica un evento.
 */
public abstract class EventoAstratto  implements Serializable {

    private ArrayList<Listener> listeners = new ArrayList<Listener>();

    /** Metodo per aggiungere un listener alla lista dei listener.
     * Controlla se il listener è già presente prima di aggiungerlo.
     */
	public void addListener(Listener listener) {
        // cicla su tutti i listeners per verificare se gia uno è equal a quello da aggiungere
        for (Listener l : listeners){
            if (l.getId() == listener.getId()){
                return; // esci dal metodo se lo trovi
            }
        }
        listeners.add(listener);
    }

    /** Metodo per aggiungere una lista di listener (tipo Genitori) alla lista dei listener.
     * Utilizza il metodo addListener per ogni listener nella lista fornita.
     */
    public void addListenersGenitori(ArrayList<Genitore> listenersToAdd) {
        for (Genitore genitore : listenersToAdd) {
            addListener(genitore);
        }
    }

    /** Metodo per aggiungere una lista di listener alla lista dei listener.
     * Utilizza il metodo addListener per ogni listener nella lista fornita.
     */
    public void addListeners(ArrayList<Listener> listenersToAdd) {
        for (Listener l : listenersToAdd) {
            addListener(l);
        }
    }

    /** Metodo per rimuovere un listener dalla lista dei listener.
     * Cerca il listener nella lista e lo rimuove se trovato.
     */
	public void removeListener(Listener listener) {
        for (Listener l : listeners){
            if (l.getId() == listener.getId()){
                listeners.remove(l);
                return; // esci dal metodo dopo aver rimosso
            }
        }
        // listeners.remove(listener);
	}

    /** Metodo per rimuovere tutti i listener dalla lista dei listener. */
    public void removeAllListeners() {
        listeners.clear();
    }

    /** Metodo per ottenere la lista dei listener registrati.
     * @return ArrayList di listener registrati.
     */
    public ArrayList<Listener> getListeners() {
        return listeners;
    }

    /** Metodo per ottenere la lista dei listener di tipo Genitore registrati.
     * @return ArrayList di listener di tipo Genitore registrati.
     */
    public ArrayList<Genitore> getListenersGenitori() {
        ArrayList<Genitore> genitori = new ArrayList<Genitore>();
        for (Listener l : listeners) {
            if (l instanceof Genitore) {
                genitori.add((Genitore) l);
            }
        }
        return genitori;
    }


    /** Metodo per notificare tutti i listener registrati di un aggiornamento.
     * Chiama il metodo update su ogni listener, passando l'evento aggiornato.
     */
	public void updateAll(Evento evento) {
        for (Listener listener : listeners) {
            listener.update(evento);
        }
	}

}
