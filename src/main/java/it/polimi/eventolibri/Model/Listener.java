package it.polimi.eventolibri.Model;

/** Interfaccia che rappresenta un listener per gli eventi.
 */
public interface Listener {

    /** Metodo chiamato quando un evento viene aggiornato.
     * @param evento Evento che è stato aggiornato.
     */
	public abstract void update(Evento evento);

    /** Metodo per confrontare due listener.
     * @param listener Listener da confrontare con l'istanza corrente.
     * @return true se i listener sono uguali, false altrimenti.
     */
    public abstract boolean equals(Listener listener);

    /** Metodo per ottenere l'ID del listener.
        * @return ID del listener.
        */
    public abstract int getId();
}
