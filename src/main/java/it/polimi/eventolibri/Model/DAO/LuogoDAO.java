package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.Luogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Classe DAO per la gestione dei luoghi nel database.
 */
public class LuogoDAO {
    private Connection connection;

    /** Costruttore della classe LuogoDAO.
     * @param connection Connessione al database.
     */
    public LuogoDAO(Connection connection) {
        this.connection = connection;
    }

    /** Recupera un luogo specifico dal database in base all'ID.
     * @param idLuogo ID del luogo da recuperare.
     * @return Oggetto Luogo corrispondente all'ID, o null se non trovato.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public Luogo getLuogo(int idLuogo) throws SQLException {
        String query = "SELECT * FROM luoghi WHERE id = ?"; //query per prendere il luogo con l'id specificato
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, idLuogo); //imposto id del luogo
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati non esiste luogo con questo id
                    return null; //ritorno null
                else {
                    result.next(); //sposto il cursore al primo risultato
                    return new Luogo(result.getString("nome"), result.getInt("capienza"),result.getInt("id")); //creo e ritorno il luogo
                }
            }catch (SQLException ex) {
                System.out.println("105" + ex.getMessage()); //stampa messaggio di errore
                return null; //ritorna null in caso di errore
            }
        }catch (SQLException ex) {
            System.out.println("106" + ex.getMessage()); //stampa messaggio di errore
            return null; //ritorna null in caso di errore
        }
    }

    /** Recupera tutti i luoghi dal database.
     * @return ArrayList di oggetti Luogo presenti nel database, o null se non ci sono luoghi.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Luogo> getLuoghi() throws SQLException {
        String query = "SELECT * FROM luoghi"; //query per cereare la lista di tutti i luoghi
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono luoghi
                    return null; //ritorno null
                else {
                    ArrayList<Luogo> luoghi = new ArrayList<>(); //lista vuota di luoghi
                    while (result.next()) {
                        Luogo luogo = new Luogo(result.getString("nome"), result.getInt("capienza"),result.getInt("id")); //creo il luogo
                        luoghi.add(luogo); //aggiungo il luogo alla lista
                    }
                    return luoghi; //ritorno la lista di luoghi
                }
            }catch (SQLException ex) {
                System.out.println("1105" + ex.getMessage());  //stampa messaggio di errore
                return null; //ritorna null in caso di errore
            }
        }catch (SQLException ex) {
            System.out.println("1106" + ex.getMessage()); //stampa messaggio di errore
            return null; //ritorna null in caso di errore
        }
    }
}