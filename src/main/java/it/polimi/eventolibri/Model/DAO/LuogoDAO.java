package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.Luogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LuogoDAO {
    private Connection connection;
    public LuogoDAO(Connection connection) {
        this.connection = connection;
    }

    public Luogo getLuogo(int idLuogo) throws SQLException {
        String query = "SELECT * FROM luoghi WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, idLuogo);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return null;
                else {
                    result.next();
                    return new Luogo(result.getString("nome"), result.getInt("capienza"),result.getInt("id"));
                }
            }catch (SQLException ex) {
                System.out.println("105" + ex.getMessage());
                return null;
            }
        }catch (SQLException ex) {
            System.out.println("106" + ex.getMessage());
            return null;
        }
    }

    public ArrayList<Luogo> getLuoghi() throws SQLException {
        String query = "SELECT * FROM luoghi";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono luoghi
                    return null;
                else {
                    ArrayList<Luogo> luoghi = new ArrayList<>();
                    while (result.next()) {
                        Luogo luogo = new Luogo(result.getString("nome"), result.getInt("capienza"),result.getInt("id"));
                        luoghi.add(luogo);
                    }
                    return luoghi;
                }
            }catch (SQLException ex) {
                System.out.println("1105" + ex.getMessage());
                return null;
            }
        }catch (SQLException ex) {
            System.out.println("1106" + ex.getMessage());
            return null;
        }
    }
}
