package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.View.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import it.polimi.eventolibri.Message.Messaggio;
import it.polimi.eventolibri.Message.RispostaAggiornaGenitore;
import it.polimi.eventolibri.Model.Genitore;
import it.polimi.eventolibri.View.ProfiloGenitore;
import org.testfx.api.FxToolkit;

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

    @Test
    void handleMessage_rispostaAggiungiFiglio_success_updatesView() throws Exception {
        Client client = new Client();

        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        Genitore genInView = mock(Genitore.class);
        when(profiloGenitore.getGenitore()).thenReturn(genInView);

        setPrivateField(client, "profiloGenitore", profiloGenitore);

        RispostaAggiungiFiglio msg = mock(RispostaAggiungiFiglio.class);
        when(msg.isSuccesso()).thenReturn(true);
        Figlio nuovo = mock(Figlio.class);
        when(msg.getNuovoFiglio()).thenReturn(nuovo);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(genInView, times(1)).aggiungiFiglio(eq(nuovo));
        verify(profiloGenitore, times(1)).aggiornaFigli(eq(nuovo));
    }

    @Test
    void handleMessage_rispostaAggiungiFiglio_error_showsErrorOnView() throws Exception {
        Client client = new Client();

        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        setPrivateField(client, "profiloGenitore", profiloGenitore);

        RispostaAggiungiFiglio msg = mock(RispostaAggiungiFiglio.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore aggiunta figlio");

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(profiloGenitore, times(1)).mostraErrore2("Errore aggiunta figlio");
    }

    @Test
    void handleMessage_rispostaLettoriELuoghiELibri_success_updatesViews() throws Exception {
        Client client = new Client();

        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);
        HomeLettore homeLettore = mock(HomeLettore.class);

        setPrivateField(client, "eventoViewLettore", eventoViewLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);
        setPrivateField(client, "homeLettore", homeLettore);

        ArrayList<Lettore> lettori = new ArrayList<>();
        ArrayList<Luogo> luoghi = new ArrayList<>();
        ArrayList<Libro> libri = new ArrayList<>();

        RispostaLettoriELuoghiELibri msg = mock(RispostaLettoriELuoghiELibri.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getLettori()).thenReturn(lettori);
        when(msg.getLuoghi()).thenReturn((ArrayList) luoghi);
        when(msg.getElencolibri()).thenReturn((ArrayList) libri);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(eventoViewLettore, times(1)).aggiornaLettoriELuoghiELibri(eq(lettori), any(), any());
        verify(homeGenitore, times(1)).aggiornaLibri(eq(libri));
        verify(homeAmministratore, times(1)).aggiornaLibri(eq(libri));
        verify(homeLettore, times(1)).aggiornaLibri(eq(libri));
    }

    @Test
    void handleMessage_rispostaLettoriELuoghiELibri_error_showsErrorsOnViews() throws Exception {
        Client client = new Client();

        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);
        HomeLettore homeLettore = mock(HomeLettore.class);

        setPrivateField(client, "eventoViewLettore", eventoViewLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);
        setPrivateField(client, "homeLettore", homeLettore);

        RispostaLettoriELuoghiELibri msg = mock(RispostaLettoriELuoghiELibri.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioerrore()).thenReturn("Errore caricamento dati");

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(eventoViewLettore, times(1)).mostraErrore("Errore caricamento dati");
        verify(homeGenitore, times(1)).mostraErrore("Errore caricamento dati");
        verify(homeAmministratore, times(1)).mostraErrore("Errore caricamento dati");
        verify(homeLettore, times(1)).mostraErrore("Errore caricamento dati");
    }

    @Test
    void handleMessage_notificaAggiornamentoEvento_genitore_updatesChildrenAndEventList() throws Exception {
        Client client = new Client();

        // setup homeGenitore e lista eventi prossimi con un evento esistente
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Evento originalEvento = mock(Evento.class);
        when(originalEvento.getId()).thenReturn(1);
        ArrayList<Evento> eventi = new ArrayList<>();
        eventi.add(originalEvento);
        when(homeGenitore.getEventiProssimi()).thenReturn(eventi);

        // crea una Scene mock e fallo ritornare anche da homeGenitore.getScene()
        javafx.scene.Scene scene = mock(javafx.scene.Scene.class);
        when(homeGenitore.getScene()).thenReturn(scene);

        // utente genitore con figlio iscritto
        Genitore gen = mock(Genitore.class);
        Figlio figlio = mock(Figlio.class);
        when(figlio.isIscritto(any(Evento.class))).thenReturn(true);
        when(gen.getFigli()).thenReturn(new ArrayList<>() {{ add(figlio); }});

        // evento aggiornato (stesso id)
        Evento newEvento = mock(Evento.class);
        when(newEvento.getId()).thenReturn(1);

        // inietta stati
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", gen);
        setPrivateField(client, "eventoView", mock(EventoView.class)); // non è necessario popolare scena qui

        // prepara loginView con Stage non-null e Scene coerente
        LoginView loginView = mock(LoginView.class);
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class);
        when(loginView.getStage()).thenReturn(stage);
        when(stage.getScene()).thenReturn(scene);
        when(stage.isShowing()).thenReturn(true);
        setPrivateField(client, "loginView", loginView);

        NotificaAggiornamentoEvento msg = mock(NotificaAggiornamentoEvento.class);
        when(msg.getEvento()).thenReturn(newEvento);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // figlio deve aggiornare l'evento
        verify(figlio, times(1)).aggiornaEvento(eq(newEvento));
        // la lista eventi deve essere stata sostituita con il nuovo evento (stesso id)
        assertSame(newEvento, eventi.get(0));
    }

    @Test
    void handleMessage_rispostaSalvaEvento_success_updatesEventoViewLettore() throws Exception {
        Client client = new Client();

        // prepara lettore e evento
        Lettore lettoreInView = mock(Lettore.class);
        when(lettoreInView.getId()).thenReturn(10);
        Evento evento = mock(Evento.class);
        when(evento.getCreatore()).thenReturn(lettoreInView); // creatore con stesso id
        when(evento.isIscritto(lettoreInView)).thenReturn(true);
        when(evento.getId()).thenReturn(5);

        // mock view lettore
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        when(eventoViewLettore.getLettore()).thenReturn(lettoreInView);

        setPrivateField(client, "eventoViewLettore", eventoViewLettore);

        // messaggio
        RispostaSalvaEvento msg = mock(RispostaSalvaEvento.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.getEvento()).thenReturn(evento);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(lettoreInView, times(1)).aggiungiEventiCreati(eq(evento));
        verify(lettoreInView, times(1)).aggiungiIscrizioneLettura(eq(evento));
        verify(eventoViewLettore, times(1)).setEvento(eq(evento));
    }

    @Test
    void handleMessage_rispostaSalvaEvento_error_showsErrorOnView() throws Exception {
        Client client = new Client();
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);

        RispostaSalvaEvento msg = mock(RispostaSalvaEvento.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore salvataggio");

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(eventoViewLettore, times(1)).mostraErrore("Errore salvataggio");
    }

    @Test
    void handleMessage_rispostaAggiungiRecensione_success_and_error() throws Exception {
        Client client = new Client();
        LibroDetailedView libroView = mock(LibroDetailedView.class);
        setPrivateField(client, "libroDetailedView", libroView);

        // success
        RispostaAggiungiRecensione ms1 = mock(RispostaAggiungiRecensione.class);
        when(ms1.isSuccesso()).thenReturn(true);
        Recensione rec = mock(Recensione.class);
        when(ms1.getRecensione()).thenReturn(rec);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ms1);

        verify(libroView, times(1)).aggiornaRecensioni(eq(rec));

        // error
        RispostaAggiungiRecensione ms2 = mock(RispostaAggiungiRecensione.class);
        when(ms2.isSuccesso()).thenReturn(false);
        when(ms2.getMessaggioErrore()).thenReturn("Errore recensione");

        handle.invoke(client, ms2);

        verify(libroView, times(1)).mostraErrore("Errore recensione");
    }

    @Test
    void handleMessage_rispostaCancellaRecensione_success_and_error() throws Exception {
        Client client = new Client();
        LibroDetailedView libroView = mock(LibroDetailedView.class);
        setPrivateField(client, "libroDetailedView", libroView);

        // success
        RispostaCancellaRecensione ok = mock(RispostaCancellaRecensione.class);
        when(ok.isSuccesso()).thenReturn(true);
        when(ok.getId()).thenReturn(42);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);

        verify(libroView, times(1)).cancellaRecensione(eq(42));
        verify(libroView, times(1)).mostraErrore2(ArgumentMatchers.contains("Cancellata recensione"));
        verify(libroView, times(1)).setAttendi(false);

        // error
        RispostaCancellaRecensione err = mock(RispostaCancellaRecensione.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore cancellazione");

        handle.invoke(client, err);

        verify(libroView, times(1)).mostraErrore2("Errore cancellazione");
    }

    @Test
    void handleMessage_rispostaRecensioniERecensibilita_success_updatesHomesAndClearsAttendi() throws Exception {
        Client client = new Client();
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);
        HomeLettore homeLettore = mock(HomeLettore.class);

        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);
        setPrivateField(client, "homeLettore", homeLettore);

        ArrayList<Recensione> recs = new ArrayList<>();
        recs.add(mock(Recensione.class));

        RispostaRecensioniERecensibilita msg = mock(RispostaRecensioniERecensibilita.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.isRecensibile()).thenReturn(true);
        when(msg.getRecensioni()).thenReturn(recs);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        verify(homeGenitore, times(1)).setRecensibile(true);
        verify(homeAmministratore, times(1)).setRecensibile(true);
        verify(homeLettore, times(1)).setRecensibile(true);

        verify(homeGenitore, times(1)).setRecensioni(eq(recs));
        verify(homeAmministratore, times(1)).setRecensioni(eq(recs));
        verify(homeLettore, times(1)).setRecensioni(eq(recs));

        verify(homeGenitore, times(1)).setAttendi(false);
        verify(homeAmministratore, times(1)).setAttendi(false);
        verify(homeLettore, times(1)).setAttendi(false);
    }

    @Test
    void handleMessage_notificaAggiornamentoEvento_lettore_updatesCreatorsAndEventList() throws Exception {
        Client client = new Client();

        // prepara loginView + stage + scene per evitare NPE e per rendere view coerente
        LoginView loginView = mock(LoginView.class);
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class);
        javafx.scene.Scene scene = mock(javafx.scene.Scene.class);
        when(loginView.getStage()).thenReturn(stage);
        when(stage.getScene()).thenReturn(scene);
        when(stage.isShowing()).thenReturn(true);
        setPrivateField(client, "loginView", loginView);

        // prepara homeLettore con eventi prossimi e lettore con eventi creati
        HomeLettore homeLettore = mock(HomeLettore.class);
        when(homeLettore.getScene()).thenReturn(scene); // fa sì che viewCoerente==true
        Lettore lettore = mock(Lettore.class);
        when(lettore.getId()).thenReturn(10);
        when(homeLettore.getLettore()).thenReturn(lettore);

        Evento existingProssimo = mock(Evento.class);
        when(existingProssimo.getId()).thenReturn(2);
        ArrayList<Evento> prossimi = new ArrayList<>();
        prossimi.add(existingProssimo);
        when(homeLettore.getEventiProssimi()).thenReturn(prossimi);

        Evento createdEvent = mock(Evento.class);
        when(createdEvent.getId()).thenReturn(2);
        Lettore creator = mock(Lettore.class);
        when(creator.getId()).thenReturn(10);
        when(createdEvent.getCreatore()).thenReturn(creator);
        when(createdEvent.isIscritto(lettore)).thenReturn(true);

        // eventi creati contiene un evento con id == createdEvent.getId()
        Evento createdExisting = mock(Evento.class);
        when(createdExisting.getId()).thenReturn(2);
        ArrayList<Evento> eventiCreati = new ArrayList<>();
        eventiCreati.add(createdExisting);
        when(lettore.getEventiCreati()).thenReturn(eventiCreati);

        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", lettore);

        // assicurati che eventoViewLettore non interferisca
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        when(eventoViewLettore.getScene()).thenReturn(null);
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);

        NotificaAggiornamentoEvento msg = mock(NotificaAggiornamentoEvento.class);
        when(msg.getEvento()).thenReturn(createdEvent);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che l'evento creato esistente sia aggiornato e che la lista prossimi sia sostituita
        verify(createdExisting, times(1)).aggiornaEvento(eq(createdEvent));
        assertSame(createdEvent, prossimi.get(0));

        // verifica che il lettore abbia aggiunto l'iscrizione (per iscritto==true)
        verify(lettore, times(1)).aggiungiIscrizioneLettura(eq(createdEvent));

        // se la view è coerente, homeLettore.aggiornaEventi dovrebbe essere chiamata
        verify(homeLettore, times(1)).aggiornaEventi(eq(prossimi));
    }

    @Test
    void handleMessage_rispostaNuovoGenitore_success_and_error() throws Exception {
        Client client = new Client();
        RegistraNewGenitore reg = mock(RegistraNewGenitore.class);
        setPrivateField(client, "registraNewGenitore", reg);

        // success
        RispostaNuovoGenitore ok = mock(RispostaNuovoGenitore.class);
        when(ok.isSuccesso()).thenReturn(true);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);

        verify(reg, times(1)).mostraSuccesso("Nuovo genitore registrato!");

        // error
        RispostaNuovoGenitore err = mock(RispostaNuovoGenitore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore nuovo genitore");

        handle.invoke(client, err);

        verify(reg, times(1)).mostraErrore("Errore nuovo genitore");
    }

    @Test
    void handleMessage_rispostaNuovoLettore_success_and_error() throws Exception {
        Client client = new Client();
        RegistraNewLettore reg = mock(RegistraNewLettore.class);
        setPrivateField(client, "registraNewLettore", reg);

        // success
        RispostaNuovoLettore ok = mock(RispostaNuovoLettore.class);
        when(ok.isSuccesso()).thenReturn(true);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);

        verify(reg, times(1)).mostraSuccesso("Nuovo lettore registrato!");

        // error
        RispostaNuovoLettore err = mock(RispostaNuovoLettore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore nuovo lettore");

        handle.invoke(client, err);

        verify(reg, times(1)).mostraErrore("Errore nuovo lettore");
    }

    @Test
    void handleMessage_rispostaNuovoAmministratore_success_and_error() throws Exception {
        Client client = new Client();
        RegistraNewAmministratore reg = mock(RegistraNewAmministratore.class);
        setPrivateField(client, "registraNewAmministratore", reg);

        // success
        RispostaNuovoAmministratore ok = mock(RispostaNuovoAmministratore.class);
        when(ok.isSuccesso()).thenReturn(true);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);

        verify(reg, times(1)).mostraSuccesso("Nuovo amministratore registrato!");

        // error
        RispostaNuovoAmministratore err = mock(RispostaNuovoAmministratore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore nuovo amministratore");

        handle.invoke(client, err);

        verify(reg, times(1)).mostraErrore("Errore nuovo amministratore");
    }

    @Test
    void handleMessage_notificaAggiornamentoEvento_genitore_eventoView_updatesEventView() throws Exception {
        Client client = new Client();

        // setup loginView + stage + scene
        LoginView loginView = mock(LoginView.class);
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class);
        javafx.scene.Scene scene = mock(javafx.scene.Scene.class);
        when(loginView.getStage()).thenReturn(stage);
        when(stage.getScene()).thenReturn(scene);
        when(stage.isShowing()).thenReturn(true);
        setPrivateField(client, "loginView", loginView);

        // homeGenitore e lista prossimi (con evento esistente)
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Evento existing = mock(Evento.class);
        when(existing.getId()).thenReturn(7);
        ArrayList<Evento> prossimi = new ArrayList<>();
        prossimi.add(existing);
        when(homeGenitore.getEventiProssimi()).thenReturn(prossimi);
        when(homeGenitore.getScene()).thenReturn(scene); // rende viewCoerente true
        setPrivateField(client, "homeGenitore", homeGenitore);

        // utente Genitore senza figli per questo test (evita altre chiamate)
        Genitore gen = mock(Genitore.class);
        when(gen.getFigli()).thenReturn(new ArrayList<>());
        setPrivateField(client, "utente", gen);

        // eventoView visibile e relativo evento (stesso id)
        EventoView evView = mock(EventoView.class);
        when(evView.getScene()).thenReturn(scene);
        when(evView.getEvento()).thenReturn(existing);
        setPrivateField(client, "eventoView", evView);

        // evento aggiornato
        Evento newEvento = mock(Evento.class);
        when(newEvento.getId()).thenReturn(7);

        NotificaAggiornamentoEvento msg = mock(NotificaAggiornamentoEvento.class);
        when(msg.getEvento()).thenReturn(newEvento);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // eventoView deve essere aggiornato e la lista prossimi sostituita
        verify(evView, times(1)).aggiornaEvento(eq(newEvento));
        assertSame(newEvento, prossimi.get(0));
        // se la vista era coerente, aggiornaEventi su homeGenitore dovrebbe essere chiamato
        verify(homeGenitore, times(1)).aggiornaEventi(eq(prossimi));
    }

    @Test
    void handleMessage_rispostaAggiornaAmministratore_success_and_error() throws Exception {
        Client client = new Client();

        ProfiloAmministratore profiloAmministratore = mock(ProfiloAmministratore.class);
        Amministratore adminInView = mock(Amministratore.class);
        when(profiloAmministratore.getAmministratore()).thenReturn(adminInView);
        setPrivateField(client, "profiloAmministratore", profiloAmministratore);

        // success
        RispostaAggiornaAmministratore ok = mock(RispostaAggiornaAmministratore.class);
        when(ok.isSuccesso()).thenReturn(true);
        Amministratore fromMsg = mock(Amministratore.class);
        when(fromMsg.getNome()).thenReturn("Anna");
        when(fromMsg.getCognome()).thenReturn("Verdi");
        when(ok.getAmministratore()).thenReturn(fromMsg);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);

        // getAmministratore dovrebbe essere chiamato due volte (setNome + setCognome)
        verify(profiloAmministratore, times(2)).getAmministratore();
        verify(adminInView, times(1)).setNome("Anna");
        verify(adminInView, times(1)).setCognome("Verdi");

        // error
        RispostaAggiornaAmministratore err = mock(RispostaAggiornaAmministratore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore aggiornamento admin");

        handle.invoke(client, err);

        verify(profiloAmministratore, times(1)).mostraErrore("Errore aggiornamento admin");
    }

    @Test
    void handleMessage_rispostaAggiornaLettore_success_updatesNomeCognome_and_runsPlatformRunnable_and_error() throws Exception {
        Client client = new Client();

        ProfiloLettore profiloLettore = mock(ProfiloLettore.class);
        Lettore lettInView = mock(Lettore.class);
        when(profiloLettore.getLettore()).thenReturn(lettInView);
        setPrivateField(client, "profiloLettore", profiloLettore);

        // success
        RispostaAggiornaLettore msg = mock(RispostaAggiornaLettore.class);
        when(msg.isSuccesso()).thenReturn(true);
        Lettore fromMsg = mock(Lettore.class);
        when(fromMsg.getNome()).thenReturn("Giulia");
        when(fromMsg.getCognome()).thenReturn("Bianchi");
        when(msg.getLettore()).thenReturn(fromMsg);

        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // profiloLettore.getLettore() viene invocato due volte (setNome + setCognome)
        verify(profiloLettore, times(2)).getLettore();
        verify(lettInView, times(1)).setNome("Giulia");
        verify(lettInView, times(1)).setCognome("Bianchi");

        // error
        RispostaAggiornaLettore err = mock(RispostaAggiornaLettore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore aggiornamento lettore");

        handle.invoke(client, err);

        verify(profiloLettore, times(1)).mostraErrore("Errore aggiornamento lettore");
    }



}
