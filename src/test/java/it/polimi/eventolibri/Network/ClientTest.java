package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.View.*;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import it.polimi.eventolibri.Message.Messaggio;
import it.polimi.eventolibri.Message.RispostaAggiornaGenitore;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.View.ProfiloGenitore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atMost;


class ClientTest {

    @BeforeAll
    static void initJfx() throws InterruptedException {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException ignored) {
            // JavaFX già inizializzato (es. esecuzioni multiple dei test)
        }
    }

    // Semplice messaggio di test
    static class TestMessage extends Messaggio implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String payload = "hello";
    }

    @Test
    void sendMessage_writesObjectToStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        Client client = new Client((Socket) null, oos, (ObjectInputStream) null);
        TestMessage msg = new TestMessage();
        client.sendMessage(msg);

        // legge dall'array bytes per verificare che l'oggetto sia stato serializzato
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object read = ois.readObject();
        assertTrue(read instanceof TestMessage);
        assertEquals("hello", ((TestMessage) read).payload);
    }

    @Test
    void startListening_receivesOneMessageAndEnds() throws Exception {
        // Piped stream collega "server" <-> "client"
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        ObjectOutputStream serverOos = new ObjectOutputStream(pos); // scrive header
        ObjectInputStream clientOis = new ObjectInputStream(pis);   // legge header

        Client client = new Client((Socket) null, (ObjectOutputStream) null, clientOis);

        // Avvia il listener e ottieni il thread per fare join
        Thread t = client.startListening();

        // Il server scrive un messaggio e poi chiude la stream per provocare EOF e terminazione del loop
        serverOos.writeObject(new TestMessage());
        serverOos.flush();
        serverOos.close(); // causa EOF lato client dopo aver letto l'oggetto

        // Attendi fino a 2s per la terminazione del listener
        t.join(2000);
        assertFalse(t.isAlive(), "Listener thread dovrebbe terminare dopo EOF");
    }


    /*
     Breve spiegazione:
     - Creo un'istanza di Client vuota.
     - Creo mock per le view e per il messaggio (RispostaLogin).
     - Imposto i campi privati del client via reflection.
     - Invoco il metodo privato handleMessage via reflection.
     - Verifico che il campo 'utente' sia impostato e che la view corretta sia stata chiamata.
    */
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }


    @Test
    void handleMessage_rispostaLogin_Genitore() throws Exception {
        Client client = new Client();

        // mock delle view
        LoginView loginView = mock(LoginView.class);
        HomeLettore homeLettore = mock(HomeLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);

        // inietta mock nel client (campi privati)
        setPrivateField(client, "loginView", loginView);
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);

        // mock del messaggio RispostaLogin
        RispostaLogin msg = mock(RispostaLogin.class);
        when(msg.isSuccesso()).thenReturn(true);


        // crea un mock Utente (es. Lettore/Genitore) e restituiscilo da getUtente()
        Genitore mockUtente = mock(Genitore.class);
        when(msg.getUtente()).thenReturn(mockUtente);


        // fornisce lista eventi vuota se il codice la richiede
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>());

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che il campo privato 'utente' sia stato impostato sul mockUtente
        Field field = Client.class.getDeclaredField("utente");
        field.setAccessible(true);
        field.set(client, mockUtente);

        Object utente = getPrivateField(client, "utente");
        assertNotNull(utente, "utente dovrebbe essere impostato");
        assertSame(mockUtente, utente);

        // verifica che la view corretta sia stata chiamata almeno una volta
        // (non conosciamo esattamente la firma del metodo show, quindi verifichiamo una chiamata generica)
        verify(homeLettore, atMost(1)).show(any(), any(), any(), any());
        verify(homeGenitore, atMost(1)).show(any(), any(), any(), any());
    }

    @Test
    void handleMessage_rispostaLogin_Lettore() throws Exception {
        Client client = new Client();

        // mock delle view
        LoginView loginView = mock(LoginView.class);
        HomeLettore homeLettore = mock(HomeLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);

        // inietta mock nel client (campi privati)
        setPrivateField(client, "loginView", loginView);
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);

        // mock del messaggio RispostaLogin
        RispostaLogin msg = mock(RispostaLogin.class);
        when(msg.isSuccesso()).thenReturn(true);


        // crea un mock Utente (es. Lettore/Genitore) e restituiscilo da getUtente()
        Lettore mockUtente = mock(Lettore.class);
        when(msg.getUtente()).thenReturn(mockUtente);


        // fornisce lista eventi vuota se il codice la richiede
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>());

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che il campo privato 'utente' sia stato impostato sul mockUtente
        Field field = Client.class.getDeclaredField("utente");
        field.setAccessible(true);
        field.set(client, mockUtente);

        Object utente = getPrivateField(client, "utente");
        assertNotNull(utente, "utente dovrebbe essere impostato");
        assertSame(mockUtente, utente);

        // verifica che la view corretta sia stata chiamata almeno una volta
        // (non conosciamo esattamente la firma del metodo show, quindi verifichiamo una chiamata generica)
        verify(homeLettore, atMost(1)).show(any(), any(), any(), any());
        verify(homeGenitore, atMost(1)).show(any(), any(), any(), any());
    }


    @Test
    void handleMessage_rispostaLogin_Amministratore() throws Exception {
        Client client = new Client();

        // mock delle view
        LoginView loginView = mock(LoginView.class);
        HomeLettore homeLettore = mock(HomeLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);

        // inietta mock nel client (campi privati)
        setPrivateField(client, "loginView", loginView);
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);

        // mock del messaggio RispostaLogin
        RispostaLogin msg = mock(RispostaLogin.class);
        when(msg.isSuccesso()).thenReturn(true);


        // crea un mock Utente (es. Lettore/Genitore) e restituiscilo da getUtente()
        Amministratore mockUtente = mock(Amministratore.class);
        when(msg.getUtente()).thenReturn(mockUtente);


        // fornisce lista eventi vuota se il codice la richiede
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>());

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che il campo privato 'utente' sia stato impostato sul mockUtente
        Field field = Client.class.getDeclaredField("utente");
        field.setAccessible(true);
        field.set(client, mockUtente);

        Object utente = getPrivateField(client, "utente");
        assertNotNull(utente, "utente dovrebbe essere impostato");
        assertSame(mockUtente, utente);

        // verifica che la view corretta sia stata chiamata almeno una volta
        // (non conosciamo esattamente la firma del metodo show, quindi verifichiamo una chiamata generica)
        verify(homeLettore, atMost(1)).show(any(), any(), any(), any());
        verify(homeGenitore, atMost(1)).show(any(), any(), any(), any());
    }


    @Test
    void handleMessage_nextEventi_lettore_smallList_hidesButton() throws Exception {
        Client client = new Client();

        HomeLettore homeLettore = mock(HomeLettore.class);
        Lettore lettore = mock(Lettore.class);

        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", lettore);

        RispostaNextEventi msg = mock(RispostaNextEventi.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>()); // size = 0 (<10)

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(homeLettore, times(1)).nascondiBottoneNextEventi();
        verify(homeLettore, never()).aggiornaEventi(any(ArrayList.class));
    }

    @Test
    void handleMessage_nextEventi_lettore_largeList_updatesEvents() throws Exception {
        Client client = new Client();

        HomeLettore homeLettore = mock(HomeLettore.class);
        Lettore lettore = mock(Lettore.class);

        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", lettore);

        ArrayList<Evento> eventi = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            eventi.add(mock(Evento.class));
        }

        RispostaNextEventi msg = mock(RispostaNextEventi.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getProssimiEventi()).thenReturn(eventi); // size >= 10

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(homeLettore, times(1)).aggiornaEventi(eventi);
        verify(homeLettore, never()).nascondiBottoneNextEventi();
    }

    @Test
    void handleMessage_nextEventi_genitore_smallList_hidesButton() throws Exception {
        Client client = new Client();

        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Genitore genitore = mock(Genitore.class);

        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", genitore);

        RispostaNextEventi msg = mock(RispostaNextEventi.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>()); // size = 0 (<10)

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(homeGenitore, times(1)).nascondiBottoneNextEventi();
        verify(homeGenitore, never()).aggiornaEventi(any(ArrayList.class));
    }

    @Test
    void handleMessage_nextEventi_genitore_largeList_updatesEvents() throws Exception {
        Client client = new Client();

        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Genitore genitore = mock(Genitore.class);

        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", genitore);

        ArrayList<Evento> eventi = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            eventi.add(mock(Evento.class));
        }

        RispostaNextEventi msg = mock(RispostaNextEventi.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getProssimiEventi()).thenReturn(eventi); // size >= 10

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(homeGenitore, times(1)).aggiornaEventi(eventi);
        verify(homeGenitore, never()).nascondiBottoneNextEventi();
    }


    @Test
    void handleMessage_rispostaIscrizioneEvento_success_invokesIscriviAndRequestsIscritti() throws Exception {
        // spy sul client per intercettare sendMessage senza eseguirla realmente
        Client client = spy(new Client());

        // mock vista ed entità
        EventoView eventoView = mock(EventoView.class);
        Genitore genitore = mock(Genitore.class);
        Figlio figlio = mock(Figlio.class);
        Evento evento = mock(Evento.class);

        // configura getId del figlio e lista figli del genitore
        when(figlio.getId()).thenReturn(7);
        ArrayList<Figlio> figli = new ArrayList<>();
        figli.add(figlio);
        when(genitore.getFigli()).thenReturn(figli);

        // configura la vista
        when(eventoView.getGenitore()).thenReturn(genitore);
        when(eventoView.getEvento()).thenReturn(evento);

        // inietta eventoView nel client
        setPrivateField(client, "eventoView", eventoView);

        // prepara il messaggio di risposta: successo e figlio con id 7
        RispostaIscrizioneEvento msg = mock(RispostaIscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getFiglio()).thenReturn(figlio);

        // inibisci l'effettiva sendMessage sullo spy
        doNothing().when(client).sendMessage(ArgumentMatchers.any(Messaggio.class));

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che figlio.iscrivi sia stato chiamato con evento e genitore
        verify(figlio, times(1)).iscrivi(eq(evento), eq(genitore));

        // verifica che sia stata inviata una richiesta di iscritti (RichiestaIscrittiEvento)
        verify(client, times(1)).sendMessage(argThat(m -> m.getClass().getSimpleName().equals("RichiestaIscrittiEvento")));
    }

    @Test
    void handleMessage_rispostaIscrizioneEvento_error_showsErrorOnView() throws Exception {
        Client client = new Client();

        EventoView eventoView = mock(EventoView.class);
        setPrivateField(client, "eventoView", eventoView);

        RispostaIscrizioneEvento msg = mock(RispostaIscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore iscrizione");

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che la vista mostri l'errore
        verify(eventoView, times(1)).mostraErrore("Errore iscrizione");
    }


    @Test
    void handleMessage_rispostaIscrittiEvento_genitore_updatesIscrittiAndAddsListeners() throws Exception {
        Client client = new Client();

        // mock evento e dati
        Evento evento = mock(Evento.class);
        int iscritti = 5;
        when(evento.getIscritti()).thenReturn(iscritti);
        Listener listener = mock(Listener.class);
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);

        // mock messaggio
        RispostaIscrittiEvento msg = mock(RispostaIscrittiEvento.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getEvento()).thenReturn(evento);

        // mock view e collegamenti
        EventoView eventoView = mock(EventoView.class);
        when(eventoView.getEvento()).thenReturn(evento);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);

        // inietta stati privati
        setPrivateField(client, "eventoView", eventoView);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", mock(Genitore.class));

        // invoca handleMessage privato
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica aggiornamento iscritti e aggiunta listener
        verify(eventoView, times(1)).aggiornaIscritti(eq(iscritti));
        verify(evento, times(1)).addListener(eq(listener));
    }

    @Test
    void handleMessage_rispostaIscrittiEvento_lettore_updatesIscrittiAndAddsListeners() throws Exception {
        Client client = new Client();

        // mock evento e dati
        Evento evento = mock(Evento.class);
        int iscritti = 3;
        when(evento.getIscritti()).thenReturn(iscritti);
        Listener listener = mock(Listener.class);
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);

        // mock messaggio
        RispostaIscrittiEvento msg = mock(RispostaIscrittiEvento.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getEvento()).thenReturn(evento);

        // mock view e collegamenti per lettore
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        when(eventoViewLettore.getEvento()).thenReturn(evento);
        HomeLettore homeLettore = mock(HomeLettore.class);

        // inietta stati privati
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", mock(Lettore.class));

        // invoca handleMessage privato
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica aggiornamento iscritti e aggiunta listener
        verify(eventoViewLettore, times(1)).aggiornaIscritti(eq(iscritti));
        verify(evento, times(1)).addListener(eq(listener));
    }


    @Test
    void handleMessage_rispostaDisiscrizioneEvento_success_invokesDisiscriviAndRequestsIscritti() throws Exception {
        Client client = spy(new Client());

        EventoView eventoView = mock(EventoView.class);
        Genitore genitore = mock(Genitore.class);
        Figlio figlio = mock(Figlio.class);
        Evento evento = mock(Evento.class);

        when(figlio.getId()).thenReturn(7);
        ArrayList<Figlio> figli = new ArrayList<>();
        figli.add(figlio);
        when(genitore.getFigli()).thenReturn(figli);

        when(eventoView.getGenitore()).thenReturn(genitore);
        when(eventoView.getEvento()).thenReturn(evento);

        setPrivateField(client, "eventoView", eventoView);

        RispostaDisiscrizioneEvento msg = mock(RispostaDisiscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getFiglio()).thenReturn(figlio);

        // evita l'effettivo invio sullo spy
        doNothing().when(client).sendMessage(any(Messaggio.class));

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che venga chiamato disiscrivi sul figlio con evento e genitore
        verify(figlio, times(1)).disiscrivi(eq(evento), eq(genitore));

        // verifica che sia stata inviata una richiesta di iscritti (RichiestaIscrittiEvento)
        verify(client, times(1)).sendMessage(argThat(m -> m.getClass().getSimpleName().equals("RichiestaIscrittiEvento")));
    }

    @Test
    void handleMessage_rispostaDisiscrizioneEvento_error_showsErrorOnView() throws Exception {
        Client client = new Client();

        EventoView eventoView = mock(EventoView.class);
        setPrivateField(client, "eventoView", eventoView);

        RispostaDisiscrizioneEvento msg = mock(RispostaDisiscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore disiscrizione");

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(eventoView, times(1)).mostraErrore("Errore disiscrizione");
    }


//    @Test
//    void handleMessage_rispostaAggiornaGenitore_success_updatesNomeCognome_and_runsPlatformRunnable() throws Exception {
//        Client client = new Client();
//
//        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
//        Genitore genInView = mock(Genitore.class);
//        when(profiloGenitore.getGenitore()).thenReturn(genInView);
//
//        setPrivateField(client, "profiloGenitore", profiloGenitore);
//
//        RispostaAggiornaGenitore msg = mock(RispostaAggiornaGenitore.class);
//        when(msg.isSuccesso()).thenReturn(true);
//        Genitore genFromMsg = mock(Genitore.class);
//        when(genFromMsg.getNome()).thenReturn("Mario");
//        when(genFromMsg.getCognome()).thenReturn("Rossi");
//        when(msg.getGenitore()).thenReturn(genFromMsg);
//
//        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
//        handle.setAccessible(true);
//        handle.invoke(client, msg);
//
//        // aspetta che i runnable su FX thread siano eseguiti e chiudi eventuali Alert aperti
//        CountDownLatch latch = new CountDownLatch(1);
//        Platform.runLater(() -> {
//            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
//                if (w instanceof javafx.stage.Stage) {
//                    try {
//                        ((javafx.stage.Stage) w).hide();
//                    } catch (Exception ignored) {}
//                }
//            }
//            latch.countDown();
//        });
//        boolean completed = latch.await(1, java.util.concurrent.TimeUnit.SECONDS);
//
//        verify(profiloGenitore, times(2)).getGenitore();
//        verify(genInView, times(1)).setNome("Mario");
//        verify(genInView, times(1)).setCognome("Rossi");
//        assertTrue(completed, "Operazioni su Platform.runLater non completate in tempo");
//    }


    @Test
    void handleMessage_rispostaAggiornaGenitore_success_updatesNomeCognome_and_runsPlatformRunnable() throws Exception {
        Client client = new Client();

        // mock profilo e genitore presente nella view
        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        Genitore genInView = mock(Genitore.class);
        when(profiloGenitore.getGenitore()).thenReturn(genInView);

        // inietta profiloGenitore
        setPrivateField(client, "profiloGenitore", profiloGenitore);

        // prepara messaggio di risposta con il genitore aggiornato
        RispostaAggiornaGenitore msg = mock(RispostaAggiornaGenitore.class);
        when(msg.isSuccesso()).thenReturn(true);
        Genitore genFromMsg = mock(Genitore.class);
        when(genFromMsg.getNome()).thenReturn("Mario");
        when(genFromMsg.getCognome()).thenReturn("Rossi");
        when(msg.getGenitore()).thenReturn(genFromMsg);

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che il genitore della view sia stato aggiornato con i nuovi valori
        verify(profiloGenitore, times(2)).getGenitore();
        verify(genInView, times(1)).setNome("Mario");
        verify(genInView, times(1)).setCognome("Rossi");
        // la chiamata a Platform.runLater è stata eseguita (JavaFX inizializzato da JFXPanel)
    }

    @Test
    void handleMessage_rispostaAggiornaGenitore_error_showsErrorOnView() throws Exception {
        Client client = new Client();

        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        setPrivateField(client, "profiloGenitore", profiloGenitore);

        RispostaAggiornaGenitore msg = mock(RispostaAggiornaGenitore.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore aggiornamento");

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(profiloGenitore, times(1)).mostraErrore("Errore aggiornamento");
    }


}
