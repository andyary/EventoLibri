package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Network.Client;
import it.polimi.eventolibri.Message.RichiestaLettoriELuoghiELibri;
import it.polimi.eventolibri.Model.Amministratore;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

// estensione di ApplicationExtension per testare componenti JavaFX con TestFX
@ExtendWith(ApplicationExtension.class)
public class HomeAmministratoreTest {
    // mock delle dipendenze
    private Client mockClient;
    private ProfiloAmministratore mockProfilo;
    private RegistraNewLettore mockRegLettore;
    private RegistraNewAmministratore mockRegAdmin;
    private LibroDetailedView mockLibroDetail;

    // setup iniziale prima di ogni test
    @Start
    public void start(Stage stage) {
        mockClient = mock(Client.class); // mock del client di rete
        try {
            doNothing().when(mockClient).sendMessage(any()); // mock del metodo sendMessage, che non fa nulla
        } catch (Exception e) {
        }
        //  mock delle altre dipendenze
        mockProfilo = mock(ProfiloAmministratore.class);
        mockRegLettore = mock(RegistraNewLettore.class);
        mockRegAdmin = mock(RegistraNewAmministratore.class);
        mockLibroDetail = mock(LibroDetailedView.class);

        Amministratore admin = mock(Amministratore.class);
        when(admin.getNome()).thenReturn("admin");

        HomeAmministratore home = new HomeAmministratore(
                mockClient,
                mockProfilo,
                mockRegLettore,
                mockRegAdmin,
                mockLibroDetail
        );
        home.show(stage, admin, () -> {
        }); // mostra la view dell'amministratore
    }

    @Test
    // verifica che venga inviata la richiesta di lettori, luoghi e libri all'avvio
    public void invia_richiesta_avvio() throws Exception {
        // verifica che la view invii la richiesta all'avvio
        verify(mockClient, timeout(1000)).sendMessage(isA(RichiestaLettoriELuoghiELibri.class));
    }
}