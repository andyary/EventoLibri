package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe che rappresenta un lettore del sistema.
 * Estende la classe Utente e implementa l'interfaccia Listener.
 */
public class Lettore extends Utente implements Listener, Serializable {

    private ArrayList<Evento> iscrizioniLettura; // eventi a cui il lettore è iscritto per leggere
    private ArrayList<Evento> eventiCreati; // eventi creati dal lettore

    /**
     * Costruttori della classe Lettore.
     *
     * @param nome     Nome del lettore.
     * @param cognome  Cognome del lettore.
     * @param userName Username del lettore.
     *                 Array di eventi a cui il lettore è iscritto per leggere e array di eventi creati dal lettore inizializzati vuoti.
     */
    public Lettore(String nome, String cognome, String userName) {
        super(nome, cognome, userName); // chiamata al costruttore della superclasse Utente
        this.iscrizioniLettura = new ArrayList<Evento>(); // inizializzazione array vuoto
        this.eventiCreati = new ArrayList<Evento>(); // inizializzazione array vuoto
    }

    /**
     * Costruttori della classe Lettore.
     *
     * @param nome     Nome del lettore.
     * @param cognome  Cognome del lettore.
     * @param userName Username del lettore.
     * @param id       ID univoco del lettore.
     *                 Array di eventi a cui il lettore è iscritto per leggere e array di eventi creati dal lettore inizializzati vuoti.
     */
    public Lettore(int id, String nome, String cognome, String userName) {
        this(nome, cognome, userName);
        this.setId(id); // chiamata al metodo setId della superclasse Utente
        this.iscrizioniLettura = new ArrayList<Evento>();
        this.eventiCreati = new ArrayList<Evento>(); // inizializzazione array vuoto
    }


    /**
     * Setters per le liste di eventi a cui il lettore si è iscritto per leggere, aggiornando anche i listener degli eventi.
     *
     * @param iscrizioniLettura Array di eventi a cui il lettore si è iscritto per leggere.
     */
    public void setIscrizioniLettura(ArrayList<Evento> iscrizioniLettura) {
        this.iscrizioniLettura = iscrizioniLettura; // aggiornamento della lista delle iscrizioni
        for (Evento evento : iscrizioniLettura) { // aggiunta del listener per ogni evento
            evento.addListener(this);
        }
    }

    /**
     * Setters per le liste di eventi creati dal lettore, aggiornando anche i listener degli eventi.
     *
     * @param eventiCreati Array di eventi creati dal lettore.
     */
    public void setEventiCreati(ArrayList<Evento> eventiCreati) {
        this.eventiCreati = eventiCreati;
        for (Evento evento : eventiCreati) { // aggiunta del listener per ogni evento
            evento.addListener(this);
        }
    }

    /**
     * Metodi per aggiungere eventi alle liste di eventi creati dal lettore, aggiornando anche i listener degli eventi.
     *
     * @param eventoCreato Evento creato dal lettore da aggiungere alla lista
     */
    public void aggiungiEventiCreati(Evento eventoCreato) {
        int i; // indice per il ciclo
        for (i = 0; i < eventiCreati.size(); i++) { // ciclo per controllare se l'evento è già presente
            if (eventiCreati.get(i).getId() == eventoCreato.getId()) { // confronto degli id degli eventi
                eventiCreati.get(i).removeListener(this); // rimozione del listener dall'evento esistente
                eventiCreati.set(i, eventoCreato); // aggiornamento dell'evento esistente
                eventoCreato.addListener(this); // aggiunta del listener all'evento aggiornato
                eventiCreati.sort((e1, e2) -> e1.getData().compareTo(e2.getData())); // ordinamento della lista degli eventi
                return; // evita duplicati
            }
        }
        eventiCreati.add(eventoCreato); // aggiunta del nuovo evento alla lista
        eventiCreati.sort((e1, e2) -> e1.getData().compareTo(e2.getData())); // ordinamento della lista degli eventi
        eventoCreato.addListener(this); // aggiunta del listener al nuovo evento
    }

    /**
     * Metodi per aggiungere eventi alle liste di eventi a cui il lettore si è iscritto per leggere, aggiornando anche i listener degli eventi.
     *
     * @param eventoIscritto Evento a cui il lettore si è iscritto per leggere da aggiungere alla lista
     */
    public void aggiungiIscrizioneLettura(Evento eventoIscritto) {
        int i; // indice per il ciclo
        for (i = 0; i < iscrizioniLettura.size(); i++) { // ciclo per controllare se l'evento è già presente
            if (iscrizioniLettura.get(i).getId() == eventoIscritto.getId()) { // confronto degli id degli eventi
                iscrizioniLettura.get(i).removeListener(this); // rimozione del listener dall'evento esistente
                iscrizioniLettura.set(i, eventoIscritto); // aggiornamento dell'evento esistente
                eventoIscritto.addListener(this); // aggiunta del listener all'evento aggiornato
                iscrizioniLettura.sort((e1, e2) -> e1.getData().compareTo(e2.getData())); // ordinamento della lista degli eventi
                return; // evita duplicati
            }
        }
        iscrizioniLettura.add(eventoIscritto); // aggiunta del nuovo evento alla lista
        iscrizioniLettura.sort((e1, e2) -> e1.getData().compareTo(e2.getData())); // ordinamento della lista degli eventi
        eventoIscritto.addListener(this); // aggiunta del listener al nuovo evento
    }

    /**
     * Metodi per rimuovere eventi dalle liste di eventi a cui il lettore si era iscritto per leggere, aggiornando anche i listener degli eventi.
     *
     * @param eventoIscritto Evento a cui il lettore si era iscritto per leggere da rimuovere dalla lista
     */
    public void rimuoviIscrizioneLettura(Evento eventoIscritto) {
        int i; // indice per il ciclo
        for (i = 0; i < iscrizioniLettura.size(); i++) { // ciclo per trovare l'evento da rimuovere
            if (iscrizioniLettura.get(i).getId() == eventoIscritto.getId()) { // confronto degli id degli eventi
                iscrizioniLettura.get(i).removeListener(this); // rimozione del listener dall'evento
                iscrizioniLettura.remove(i); // rimozione dell'evento dalla lista
                return; // esce dal metodo dopo la rimozione
            }
        }
    }

    /**
     * Metodo per aggiornare le informazioni di un evento di cui il lettore è listeners perchè iscritto o che ha creato.
     *
     * @param eventoAggiornato Evento aggiornato con le nuove informazioni.
     */
    @Override
    public void update(Evento eventoAggiornato) {
        for (int i = 0; i < iscrizioniLettura.size(); i++) { // Cicla su tutte le iscrizioni per trovare l'evento da aggiornare
            if (iscrizioniLettura.get(i).getId() == eventoAggiornato.getId()) { // Trova l'evento da aggiornare
                iscrizioniLettura.set(i, eventoAggiornato); // Sostituisce l'evento con quello aggiornato
                break; // Esce dal ciclo dopo aver aggiornato l'evento
            }
        }
        for (int i = 0; i < eventiCreati.size(); i++) {  // Cicla su tutti gli eventi creati per trovare l'evento da aggiornare
            if (eventiCreati.get(i).getId() == eventoAggiornato.getId()) { // Trova l'evento da aggiornare
                eventiCreati.set(i, eventoAggiornato); // Sostituisce l'evento con quello aggiornato
                break; // Esce dal ciclo dopo aver aggiornato l'evento
            }
        }
    }

    /**
     * Getters per le liste di eventi creati dal lettore.
     *
     * @return Array di eventi creati dal lettore.
     */
    public ArrayList<Evento> getEventiCreati() {
        return eventiCreati;
    }

    /**
     * Getters per le liste di eventi a cui il lettore si è iscritto per leggere.
     *
     * @return Array di eventi a cui il lettore si è iscritto per leggere.
     */
    public ArrayList<Evento> getIscrizioniLettura() {
        return iscrizioniLettura;
    }

    /**
     * Confronta questo lettore con un altro listener confrontando il loro id.
     *
     * @param listener Il listener da confrontare.
     * @return true se i due listener sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Listener listener) {
        return this.getId() == listener.getId();
    }

}
