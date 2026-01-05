package it.polimi.eventolibri.Model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

/** Classe che rappresenta un figlio di un genitore, implementa l'interfaccia Serializable.
 * Contiene informazioni sul nome, data di nascita e le iscrizioni agli eventi.
 */
public class Figlio implements Serializable {

    private int id;
	private ArrayList<Evento> iscrizioni;
	private String nome;
	private LocalDate dataNascita;

    /** Restituisce il nome del figlio.
     * @return Nome del figlio.
     */
    public String getNome() {
        return nome;
    }

    /** Costruttori della classe Figlio.
     * @param nome Nome del figlio.
     * @param dataNascita Data di nascita del figlio.
     */
    public Figlio(String nome, LocalDate dataNascita) {
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = new ArrayList<>();
    }

    /** Costruttori della classe Figlio.
     * @param id ID univoco del figlio.
     * @param nome Nome del figlio.
     * @param dataNascita Data di nascita del figlio.
     */
    public Figlio(int id, String nome, LocalDate dataNascita) {
        this.id = id;
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = new ArrayList<>();
    }

    /** Costruttori della classe Figlio.
     * @param id ID univoco del figlio.
     * @param nome Nome del figlio.
     * @param dataNascita Data di nascita del figlio.
     * @param iscrizioni Array di eventi a cui il figlio è iscritto.
     * @param genitore Genitore del figlio, per aggiungere i listener agli eventi.
     */
    public Figlio(int id, String nome, LocalDate dataNascita, ArrayList<Evento> iscrizioni, Genitore genitore) {
        this.id = id;
        this.nome = nome;
        this.dataNascita = dataNascita;
        this.iscrizioni = iscrizioni;
        for (Evento evento : iscrizioni) {
            evento.addListener(genitore);
        }
    }

    /** Imposta l'ID del figlio.
     * @param id ID univoco del figlio.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Metodo per iscrivere il figlio ad un evento, aggiungendo anche il listener del genitore all'evento.
     *  @param evento Evento a cui iscrivere il figlio.
     *  @param genitore Genitore del figlio, per aggiungere il listener all'evento.
     */
    public void iscrivi(Evento evento, Genitore genitore) {
        iscrizioni.add(evento);
        evento.addListener(genitore);
    }

    /** Metodo per disiscrivere il figlio da un evento, rimuovendo il listener del genitore dall'evento
     *  solo se nessun altro figlio del genitore è iscritto all'evento.
     *  @param evento Evento da cui disiscrivere il figlio.
     *  @param genitore Genitore del figlio, per rimuovere il listener dall'evento se necessario.
     */
	public void disiscrivi(Evento evento, Genitore genitore) {
        iscrizioni.removeIf(e -> e.getId() == evento.getId());
        for (Figlio figlio : genitore.getFigli()) {
            if (figlio.getIscrizioni().stream().anyMatch(ev -> ev.getId() == evento.getId())) {
                return; // Un altro figlio del genitore è ancora iscritto all'evento
            }
        }
        evento.removeListener(genitore);
	}

    /** Restituisce il numero di iscrizioni del figlio.
     * @return Numero di iscrizioni del figlio.
     */
    public int getNumeroIscrizioni() {
        return iscrizioni.size();
    }

    /** Restituisce la lista delle iscrizioni del figlio.
     * @return Lista delle iscrizioni del figlio.
     */
    public ArrayList<Evento> getIscrizioni() {
        return iscrizioni;
    }

    /** Aggiorna un evento nella lista delle iscrizioni del figlio.
     * @param eventoAggiornato Evento aggiornato da sostituire nella lista delle iscrizioni.
     */
    public void aggiornaEvento(Evento eventoAggiornato) {
        for (int i = 0; i < iscrizioni.size(); i++) {
            if (iscrizioni.get(i).getId() == eventoAggiornato.getId()) {
                iscrizioni.set(i, eventoAggiornato);
                break;
            }
        }
    }

    /** Restituisce l'ID del figlio.
     * @return ID del figlio.
     */
    public int getId() {
        return id;
    }

    /** Restituisce la data di nascita del figlio.
     * @return Data di nascita del figlio.
     */
    public LocalDate getDataNascita() {
        return dataNascita;
    }
}
