package it.polimi.eventolibri.Message;

public class RichiestaLogin extends Messaggio {
    private final String username;
    private final String password;

    public RichiestaLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
