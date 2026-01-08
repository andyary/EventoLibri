package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe che rappresenta un lettore del sistema.
 * Estende la classe Utente e implementa l'interfaccia Listener.
 */
public class Lettore extends Utente implements Listener, Serializable {

    private ArrayList<Evento> iscrizioniLettura;
    private ArrayList<Evento> eventiCreati;

    /** Costruttori della classe Lettore.
     * @param nome Nome del lettore.
     * @param cognome Cognome del lettore.
     * @param userName Username del lettore.
     * Array di eventi a cui il lettore è iscritto per leggere e array di eventi creati dal lettore inizializzati vuoti.
     */
    public Lettore(String nome, String cognome, String userName) {
        super(nome, cognome, userName);
        this.iscrizioniLettura = new ArrayList<Evento>();
        this.eventiCreati = new ArrayList<Evento>();
    }

    /** Costruttori della classe Lettore.
     * @param nome Nome del lettore.
     * @param cognome Cognome del lettore.
     * @param userName Username del lettore.
     * @param id ID univoco del lettore.
     * Array di eventi a cui il lettore è iscritto per leggere e array di eventi creati dal lettore inizializzati vuoti.
     */
    public Lettore(int id, String nome, String cognome, String userName) {
        super(id, nome, cognome, userName);
        this.iscrizioniLettura = new ArrayList<Evento>();
        this.eventiCreati = new ArrayList<Evento>();
    }


    /** Setters per le liste di eventi a cui il lettore si è iscritto per leggere, aggiornando anche i listener degli eventi.
     *  @param iscrizioniLettura Array di eventi a cui il lettore si è iscritto per leggere.
     */
    public void setIscrizioniLettura(ArrayList<Evento> iscrizioniLettura) {
        this.iscrizioniLettura = iscrizioniLettura;
        for (Evento evento : iscrizioniLettura) {
            evento.addListener(this);
        }
    }

    /** Setters per le liste di eventi creati dal lettore, aggiornando anche i listener degli eventi.
     *  @param eventiCreati Array di eventi creati dal lettore.
     */
    public void setEventiCreati(ArrayList<Evento> eventiCreati) {
        this.eventiCreati = eventiCreati;
        for (Evento evento : eventiCreati) {
            evento.addListener(this);
        }
    }

    /** Metodi per aggiungere eventi alle liste di eventi creati dal lettore, aggiornando anche i listener degli eventi.
     *  @param eventoCreato Evento creato dal lettore da aggiungere alla lista
     */
    public void aggiungiEventiCreati(Evento eventoCreato) {
        int i;
        for (i = 0; i < eventiCreati.size(); i++) {
            if (eventiCreati.get(i).getId() == eventoCreato.getId()) {
                eventiCreati.get(i).removeListener(this);
                eventiCreati.set(i, eventoCreato);
                eventoCreato.addListener(this);
                eventiCreati.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
                return; // evita duplicati
            }
        }
        eventiCreati.add(eventoCreato);
        eventiCreati.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
        eventoCreato.addListener(this);
    }

    /** Metodi per aggiungere eventi alle liste di eventi a cui il lettore si è iscritto per leggere, aggiornando anche i listener degli eventi.
     *  @param eventoIscritto Evento a cui il lettore si è iscritto per leggere da aggiungere alla lista
     */
    public void aggiungiIscrizioneLettura(Evento eventoIscritto) {
        int i;
        for (i = 0; i < iscrizioniLettura.size(); i++) {
            if (iscrizioniLettura.get(i).getId() == eventoIscritto.getId()) {
                iscrizioniLettura.get(i).removeListener(this);
                iscrizioniLettura.set(i, eventoIscritto);
                eventoIscritto.addListener(this);
                iscrizioniLettura.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
                return;
            }
        }
        iscrizioniLettura.add(eventoIscritto);
        iscrizioniLettura.sort((e1, e2) -> e1.getData().compareTo(e2.getData()));
        eventoIscritto.addListener(this);
    }

    /** Metodi per rimuovere eventi dalle liste di eventi a cui il lettore si era iscritto per leggere, aggiornando anche i listener degli eventi.
     *  @param eventoIscritto Evento a cui il lettore si era iscritto per leggere da rimuovere dalla lista
     */
    public void rimuoviIscrizioneLettura(Evento eventoIscritto) {
        int i;
        for (i = 0; i < iscrizioniLettura.size(); i++) {
            if (iscrizioniLettura.get(i).getId() == eventoIscritto.getId()) {
                iscrizioniLettura.get(i).removeListener(this);
                iscrizioniLettura.remove(i);
                return;
            }
        }
    }

    /** Metodo per aggiornare le informazioni di un evento di cui il lettore è listeners perchè iscritto o che ha creato.
     *  @param eventoAggiornato Evento aggiornato con le nuove informazioni.
     */
    @Override
    public void update(Evento eventoAggiornato) {
        for (int i = 0; i < iscrizioniLettura.size(); i++) {
            if (iscrizioniLettura.get(i).getId() == eventoAggiornato.getId()) {
                iscrizioniLettura.set(i, eventoAggiornato);
                break;
            }
        }
        for (int i = 0; i < eventiCreati.size(); i++) {
            if (eventiCreati.get(i).getId() == eventoAggiornato.getId()) {
                eventiCreati.set(i, eventoAggiornato);
                break;
            }
        }
    }

    /** Getters per le liste di eventi creati dal lettore.
     *  @return Array di eventi creati dal lettore.
     */
    public ArrayList<Evento> getEventiCreati() {
        return eventiCreati;
    }

    /** Getters per le liste di eventi a cui il lettore si è iscritto per leggere.
     *  @return Array di eventi a cui il lettore si è iscritto per leggere.
     */
    public ArrayList<Evento> getIscrizioniLettura() {
        return iscrizioniLettura;
    }

    /** Confronta questo lettore con un altro listener confrontando il loro id.
     *  @param listener Il listener da confrontare.
     *  @return true se i due listener sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Listener listener) {
        return this.getId() == listener.getId();
    }

}
