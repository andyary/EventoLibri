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

    /** Costruttore della classe Evento senza creatore. Usato per creare eventi temporanei prima di assegnarli a un creatore. Id è impostato a 0.
     * @param nome Nome dell'evento.
     * @param luogo Luogo dell'evento.
     * @param data Data e ora dell'evento.
     */
    public Evento(String nome, Luogo luogo, LocalDateTime data) {
        this.id = 0;
        this.creatore = null;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = new ArrayList<>();
    }

    /** Imposta il creatore dell'evento. Usato per assegnare il creatore dopo aver creato l'evento temporaneo.
     * Aggiunge il creatore ai listener dell'evento.
     * @param creatore Lettore che ha creato l'evento.
     */
    public void setCreatore(Lettore creatore) {
        this.creatore = creatore;
        this.addListener(creatore);
    }

    /** Costruttore della classe Evento.
     * @param creatore Lettore che ha creato l'evento.
     * @param nome Nome dell'evento.
     * @param luogo Luogo dell'evento.
     * @param data Data e ora dell'evento.
     */
    public Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = new ArrayList<>();
        creatore.aggiungiEventiCreati(this);
    }

    /** Costruttore della classe Evento.
     * @param creatore Lettore che ha creato l'evento.
     * @param nome Nome dell'evento.
     * @param luogo Luogo dell'evento.
     * @param data Data e ora dell'evento.
     * @param scaletta Lista di libri e lettori associati all'evento.
     */
    public Evento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = scaletta;
        creatore.aggiungiEventiCreati(this);
        for (LibroLettore ll : scaletta) {
            this.addListener(ll.getLettore());
        }
    }

    /** Costruttore della classe Evento.
     * @param id ID univoco dell'evento.
     * @param creatore Lettore che ha creato l'evento.
     * @param nome Nome dell'evento.
     * @param luogo Luogo dell'evento.
     * @param data Data e ora dell'evento.
     * @param scaletta Lista di libri e lettori associati all'evento.
     */
    public Evento(int id, Lettore creatore, String nome, Luogo luogo, LocalDateTime data, ArrayList<LibroLettore> scaletta) {
        this.creatore = creatore;
        this.nome  = nome;
        this.luogo = luogo;
        this.data = data;
        this.scaletta = scaletta;
        creatore.aggiungiEventiCreati(this);
        for (LibroLettore ll : scaletta) {
            if (ll.getLettore() != null) this.addListener(ll.getLettore());
        }
        this.id = id;
    }

    /** Imposta l'ID univoco dell'evento.
     * @param id ID univoco dell'evento.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Restituisce il nome dell'evento.
     * @return Nome dell'evento.
     */
    public String getNome() {
        return nome;
    }

    /** Restituisce la data e ora dell'evento.
     * @return Data e ora dell'evento.
     */
    public LocalDateTime getData() {
        return data;
    }

    /** Restituisce il luogo dell'evento.
     * @return Luogo dell'evento.
     */
    public Luogo getLuogo() {
        return luogo;
    }

    /** Restituisce la scaletta dell'evento.
     * @return Scaletta dell'evento.
     */
    public ArrayList<LibroLettore> getScaletta() {
        return scaletta;
    }

    /** Restituisce il creatore dell'evento (che è un Lettore).
     * @return Creatore dell'evento.
     */
    public Lettore getCreatore() {
        return creatore;
    }

    /** Restituisce il numero di iscritti all'evento.
     * @return Numero di iscritti all'evento.
     */
    public int getIscritti() {
        return iscritti;
    }

    /** Restituisce l'ID univoco dell'evento.
     * @return ID univoco dell'evento.
     */
    public int getId() {
        return id;
    }

    /** Aggiorna i dettagli dell'evento con quelli di un evento passato.
     * @param evento Evento con i nuovi dettagli.
     */
    public void aggiornaEvento(Evento evento) {
        this.nome  = evento.nome;
        this.luogo = evento.luogo;
        this.data = evento.data;
        for (LibroLettore ll_old : this.scaletta) {
            int flag = 0;
            for (LibroLettore ll_new : evento.scaletta) {
                if (ll_old.getLettore().equals(ll_new.getLettore())) {flag=1;}
            }
            if (flag == 0) {
                // this.removeListener(ll_old.getLettore());
                ll_old.getLettore().rimuoviIscrizioneLettura(this);
            }
        }
        for (LibroLettore ll_new : evento.scaletta) {
            int flag = 0;
            for (LibroLettore ll_old : this.scaletta) {
                if (ll_old.getLettore().equals(ll_new.getLettore())) {flag=1;}
            }
            if (flag == 0) {
                // this.addListener(ll_new.getLettore());
                ll_new.getLettore().aggiungiIscrizioneLettura(this);
            }
        }
        this.scaletta = evento.scaletta;
        updateAll(this);
    }

    /** Calcola l'ora di fine dell'evento sommando le durate di lettura di tutti i libri nella scaletta alla ora di inizio.
     * @return Ora di fine dell'evento.
     */
    public LocalDateTime calcolaOraFine() {
        LocalDateTime oraFine = data;
        for (LibroLettore ll : scaletta) {
            // Somma la durata di ogni libro alla data di inizio
            oraFine = oraFine.plusMinutes(ll.getLibro().getTempoLettura());
        }
        return oraFine;
    }

    /** Imposta il numero di iscritti all'evento.
     * @param iscritti Numero di iscritti all'evento.
     */
    public void setIscritti(int iscritti) {
        this.iscritti = iscritti;
    }

    /** Imposta la scaletta dell'evento aggiungendo i lettori come listener.
     * @param scaletta Scaletta dell'evento.
     */
    public void setScaletta(ArrayList<LibroLettore> scaletta) {
        this.scaletta = scaletta;
        for (LibroLettore ll : scaletta) {
            if (ll.getLettore() != null) this.addListener(ll.getLettore());
        }
    }

    /** Verifica se un lettore è iscritto come Lettore all'evento.
     * @param lettore Lettore da verificare.
     * @return true se il lettore è iscritto, false altrimenti.
     */
    public boolean isIscritto(Lettore lettore) {
        for (LibroLettore ll : scaletta) {
            if (ll.getLettore() == null) continue;
            if (ll.getLettore().getId() == lettore.getId()) {
                return true;
            }
        }
        return false;
    }
}
