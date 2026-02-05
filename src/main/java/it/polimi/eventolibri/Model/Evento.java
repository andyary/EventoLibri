package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Classe che rappresenta un evento di lettura di libri.
 * Estende la classe astratta EventoAstratto e implementa l'interfaccia Serializable.
 */
public class Evento extends EventoAstratto implements Serializable {

    private String nome;
    private LocalDateTime data;
    private Luogo luogo;
    private int id;
    private ArrayList<LibroLettore> scaletta;
    private Lettore creatore;
    private int iscritti;

    /**
     * Costruttore della classe Evento senza creatore. Usato per creare eventi temporanei prima di assegnarli a un creatore. Id è impostato a 0.
     *
     * @param nome  Nome dell'evento.
     * @param luogo Luogo dell'evento.
     * @param data  Data e ora dell'evento.
     */
    public Evento(String nome, Luogo luogo, LocalDateTime data) {
        this.id = 0;
        this.creatore = null;
        this.nome = nome;
        this.luogo = luogo;
        this.data = data;
        this.iscritti = 0;
        this.scaletta = new ArrayList<>();
    }

    /**
     * Imposta il creatore dell'evento. Usato per assegnare il creatore dopo aver creato l'evento temporaneo.
     * Aggiunge il creatore ai listener dell'evento.
     *
     * @param creatore Lettore che ha creato l'evento.
     */
    public void setCreatore(Lettore creatore) {
        this.creatore = creatore;
        // creatore.aggiungiEventiCreati(this);
        this.addListener(creatore); // aggiungo il creatore come listener dell'evento
    }

    /**
     * Costruttore della classe Evento.
     *
     * @param creatore Lettore che ha creato l'evento.
     * @param nome     Nome dell'evento.
     * @param luogo    Luogo dell'evento.
     * @param data     Data e ora dell'evento.
     */
    public Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) {
        this(nome, luogo, data); // chiamo il costruttore senza creatore
        this.setCreatore(creatore); // imposto il creatore dell'evento
        this.iscritti = 0; // inizializzazione del numero di iscritti a 0
        this.scaletta = new ArrayList<>(); // inizializzazione della scaletta come lista vuota
        creatore.aggiungiEventiCreati(this); // aggiunta del'evento alla lista di eventi creati del creatore
        this.addListener(creatore); // aggiunta del creatore come listener dell'evento
    }

    /**
     * Costruttore della classe Evento.
     *
     * @param creatore Lettore che ha creato l'evento.
     * @param nome     Nome dell'evento.
     * @param luogo    Luogo dell'evento.
     * @param data     Data e ora dell'evento.
     * @param scaletta Lista di libri e lettori associati all'evento.
     */
    public Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this(creatore, nome, luogo, data); // chiamata al costruttore senza scaletta
        this.setScaletta(scaletta); //impostazione della scaletta
    }

    /**
     * Costruttore della classe Evento.
     *
     * @param id       ID univoco dell'evento.
     * @param creatore Lettore che ha creato l'evento.
     * @param nome     Nome dell'evento.
     * @param luogo    Luogo dell'evento.
     * @param data     Data e ora dell'evento.
     * @param scaletta Lista di libri e lettori associati all'evento.
     */
    public Evento(int id, Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this(creatore, nome, luogo, data, scaletta); // chiamata al costruttore senza id
        this.setId(id); // impostazione dell'id
    }

    /**
     * Imposta l'ID univoco dell'evento.
     *
     * @param id ID univoco dell'evento.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome dell'evento.
     *
     * @return Nome dell'evento.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce la data e ora dell'evento.
     *
     * @return Data e ora dell'evento.
     */
    public LocalDateTime getData() {
        return data;
    }

    /**
     * Restituisce il luogo dell'evento.
     *
     * @return Luogo dell'evento.
     */
    public Luogo getLuogo() {
        return luogo;
    }

    /**
     * Restituisce la scaletta dell'evento.
     *
     * @return Scaletta dell'evento.
     */
    public ArrayList<LibroLettore> getScaletta() {
        return scaletta;
    }

    /**
     * Restituisce il creatore dell'evento (che è un Lettore).
     *
     * @return Creatore dell'evento.
     */
    public Lettore getCreatore() {
        return creatore;
    }

    /**
     * Restituisce il numero di iscritti all'evento.
     *
     * @return Numero di iscritti all'evento.
     */
    public int getIscritti() {
        return iscritti;
    }

    /**
     * Restituisce l'ID univoco dell'evento.
     *
     * @return ID univoco dell'evento.
     */
    public int getId() {
        return id;
    }

    /**
     * Aggiorna i dettagli dell'evento con quelli di un evento passato.
     *
     * @param evento Evento con i nuovi dettagli.
     */
    public void aggiornaEvento(Evento evento) {
        this.nome = evento.nome; // aggiornamento del nome
        this.luogo = evento.luogo; // aggiornamento del luogo
        this.data = evento.data; // aggiornamento della data

        // se il lettore che c'era nella vecchia scaletta non c'è piu nella nuova scaletta toglilo dai listeners
        for (LibroLettore ll_old : this.scaletta) { // scorro la vecchia scaletta
            int flag = 0; // flag di controllo impostato a 0
            if (ll_old.getLettore() != null) { // se il lettore non è null
                // scorro la nuova scaletta per vedere se il lettore è presente
                for (LibroLettore ll_new : evento.scaletta) {
                    // se il lettore è presente imposto il flag a 1
                    if (ll_new.getLettore() != null) {
                        // confronto gli id dei lettori
                        if (ll_old.getLettore().getId() == (ll_new.getLettore().getId())) {
                            flag = 1; // lettore trovato nella nuova scaletta
                        }
                    }
                }
                // se il flag è ancora 0 significa che il lettore non è piu nella nuova scaletta
                if (flag == 0) {
                    // this.removeListener(ll_old.getLettore());
                    ll_old.getLettore().rimuoviIscrizioneLettura(this); // rimuovo l'evento dalla lista di eventi di lettura del lettore
                }
            }
        }
        // se il lettore che non c'era nella vecchia scaletta c'è nella nuova, aggiungilo ai listeners
        for (LibroLettore ll_new : evento.scaletta) {
            int flag = 0;
            if (ll_new.getLettore() != null) { // se il lettore non è null
                for (LibroLettore ll_old : this.scaletta) { // scorrimento della vecchia scaletta
                    if (ll_old.getLettore() != null) { // se il lettore non è null
                        if (ll_old.getLettore().getId() == (ll_new.getLettore().getId())) { // se il lettore è presente
                            flag = 1; // lettore trovato nella vecchia scaletta
                        }

                    }
                    if (flag == 0) { // se il flag è ancora 0 significa che il lettore non era nella vecchia scaletta
                        // this.addListener(ll_new.getLettore());
                        ll_new.getLettore().aggiungiIscrizioneLettura(this); // aggiunta dell'evento alla lista di eventi di lettura del lettore
                    }
                }
            }
        }
        this.scaletta = evento.scaletta; // aggiornamento della scaletta
        // Notifica i listener dell'evento riguardo l'aggiornamento
        updateAll(this);
    }

    /**
     * Calcola l'ora di fine dell'evento sommando le durate di lettura di tutti i libri nella scaletta alla ora di inizio.
     *
     * @return Ora di fine dell'evento.
     */
    public LocalDateTime calcolaOraFine() {
        // Inizializza l'ora di fine con la data di inizio dell'evento
        LocalDateTime oraFine = data;
        // Itera attraverso ogni libro nella scaletta
        for (LibroLettore ll : scaletta) {
            // Somma la durata di ogni libro alla data di inizio
            oraFine = oraFine.plusMinutes(ll.getLibro().getTempoLettura());
        }
        return oraFine; // Ritorna l'ora di fine calcolata
    }

    /**
     * Imposta il numero di iscritti all'evento.
     *
     * @param iscritti Numero di iscritti all'evento.
     */
    public void setIscritti(int iscritti) {
        this.iscritti = iscritti;
        updateAll(this); // notifica i listener dell'evento riguardo l'aggiornamento
    }

    /**
     * Imposta la scaletta dell'evento aggiungendo i lettori come listener.
     *
     * @param scaletta Scaletta dell'evento.
     */
    public void setScaletta(ArrayList<LibroLettore> scaletta) {
        this.scaletta = scaletta;
        // aggiungo i lettori della scaletta ai listener dell'evento
        for (LibroLettore ll : scaletta) {
            // se il lettore non è null lo aggiungo come listener
            if (ll.getLettore() != null) {
                // this.addListener(ll.getLettore());
                ll.getLettore().aggiungiIscrizioneLettura(this); // aggiungo l'evento alla lista di eventi di lettura del lettore
            }
        }
    }

    /**
     * Verifica se un lettore è iscritto come Lettore all'evento.
     *
     * @param lettore Lettore da verificare.
     * @return true se il lettore è iscritto, false altrimenti.
     */
    public boolean isIscritto(Lettore lettore) {
        // scorro la scaletta per verificare se il lettore è presente
        for (LibroLettore ll : scaletta) {
            if (ll.getLettore() == null) continue; // salto se il lettore è null
            if (ll.getLettore().getId() == lettore.getId()) { // se il lettore è trovato
                return true;
            } // ritorno true se il lettore è trovato
        }
        return false; // ritorno false se il lettore non è trovato
    }

    /**
     * Imposta il luogo dell'evento.
     *
     * @param luogo Luogo dell'evento.
     */
    public void setLuogo(Luogo luogo) {
        this.luogo = luogo;
    }
}
