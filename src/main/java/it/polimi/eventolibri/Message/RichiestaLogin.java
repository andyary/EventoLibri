package it.polimi.eventolibri.Message;

/**
 * Messaggio inviato dal client al server per richiedere il login.
 */
public class RichiestaLogin extends Messaggio {
    private final String username;
    private final String password;

    /**
     * Costruttore della classe RichiestaLogin.
     * @param username Username dell'utente che effettua il login.
     * @param password Password dell'utente che effettua il login.
     */
    public RichiestaLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Restituisce l'username dell'utente che effettua il login.
     * @return Username dell'utente.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce la password dell'utente che effettua il login.
     * @return Password dell'utente.
     */
    public String getPassword() {
        return password;
    }
}
