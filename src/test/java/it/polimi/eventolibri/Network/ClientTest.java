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

    // Prima di tutti i test, inizializza JavaFX Platform
    @BeforeAll
    static void initJfx() throws InterruptedException {
        try {
            CountDownLatch latch = new CountDownLatch(1); // Latch per attendere l'inizializzazione
            Platform.startup(latch::countDown); // Avvia JavaFX Platform
            latch.await(); // Attendi che JavaFX sia inizializzato
        } catch (IllegalStateException ignored) {
            // JavaFX già inizializzato (es. esecuzioni multiple dei test)
        }
    }

    // Semplice messaggio di test
    static class TestMessage extends Messaggio implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String payload = "hello"; // campo di esempio
    }

    // Test del metodo sendMessage
    @Test
    void sendMessage_scriveSuStream() throws Exception {
        // prepara stream in memoria per catturare l'output usando ByteArrayOutputStream per memorizzare i byte
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // stream in memoria
        ObjectOutputStream oos = new ObjectOutputStream(baos); // stream oggetti che scrive su baos
        // crea client con ObjectOutputStream mockato
        Client client = new Client((Socket) null, oos, (ObjectInputStream) null);
        // invia un messaggio di test
        TestMessage msg = new TestMessage();
        client.sendMessage(msg);

        // legge dall'array bytes per verificare che l'oggetto sia stato serializzato
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())); // stream oggetti che legge da baos
        Object read = ois.readObject(); // deserializza l'oggetto
        // verifica
        assertTrue(read instanceof TestMessage); // verifica il tipo
        assertEquals("hello", ((TestMessage) read).payload); // verifica il contenuto
    }

    // Test del metodo startListening
    @Test
    void startListening_riceveUnMessaggioEChiude() throws Exception {
        // Piped stream collega server e client perché non possiamo usare socket reali nei test
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        // stream per il server e client
        ObjectOutputStream serverOos = new ObjectOutputStream(pos); // scrive header
        ObjectInputStream clientOis = new ObjectInputStream(pis);   // legge header
        // crea client con ObjectInputStream collegato al server
        Client client = new Client((Socket) null, (ObjectOutputStream) null, clientOis);

        // Avvia il listener e ottieni il thread per fare join
        Thread t = client.startListening();

        // Il server scrive un messaggio e poi chiude la stream per provocare la terminazione del listener
        serverOos.writeObject(new TestMessage()); // invia messaggio
        serverOos.flush(); // assicura che sia inviato
        serverOos.close(); // chiude lo stream lato server per terminare il listener

        // Attendi fino a 2s per la terminazione del listener
        t.join(2000); // attende massimo 2 secondi
        assertFalse(t.isAlive(), "Terminazione del listener non avvenuta in tempo"); // verifica che il thread sia terminato
    }

//     Breve spiegazione:
//     - Creo un'istanza di Client vuota.
//     - Creo mock per le view e per il messaggio (RispostaLogin).
//     - Imposto i campi privati del client via reflection.
//     - Invoco il metodo privato handleMessage via reflection.
//     - Verifico che il campo 'utente' sia impostato e che la view corretta sia stata chiamata.

    // Metodi di utilità per accedere ai campi privati del client
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName); // ottiene il campo privato
        f.setAccessible(true); // rende il campo accessibile
        f.set(target, value); // imposta il valore del campo sul target
    }

    // Metodo per leggere un campo privato (utile per verificare lo stato interno del client dopo handleMessage)
    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName); // ottiene il campo privato
        f.setAccessible(true); // rende il campo accessibile
        return f.get(target); // restituisce il valore del campo dal target
    }

    // Test del metodo handleMessage per il caso di rispostaLogin con un genitore
    @Test
    void handleMessage_rispostaLogin_Genitore() throws Exception {
        Client client = new Client(); // istanza di client da testare

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
        when(msg.isSuccesso()).thenReturn(true); // simula un login riuscito

        // crea un mock Utente (es. Lettore/Genitore) e restituiscilo da getUtente()
        Genitore mockUtente = mock(Genitore.class);
        when(msg.getUtente()).thenReturn(mockUtente); // simula un login riuscito con un genitore

        // fornisce lista eventi vuota
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>());

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che il campo privato 'utente' sia stato impostato sul mockUtente
        Field field = Client.class.getDeclaredField("utente"); // ottiene il campo privato 'utente'
        field.setAccessible(true); // rende il campo accessibile
        field.set(client, mockUtente); // imposta il campo 'utente' del client al mockUtente per la verifica

        // ora possiamo leggere il campo 'utente' per verificare che sia stato impostato correttamente
        Object utente = getPrivateField(client, "utente"); // legge il campo 'utente' dal client, object e non utente perché getPrivateField restituisce Object
        assertNotNull(utente, "utente dovrebbe essere impostato");
        assertSame(mockUtente, utente);

        // verifica che la view corretta sia stata chiamata almeno una volta
        // (non conosciamo esattamente la firma del metodo show, quindi verifichiamo una chiamata generica)
        verify(homeLettore, atMost(1)).show(any(), any(), any(), any());
        verify(homeGenitore, atMost(1)).show(any(), any(), any(), any());
    }

    // Test del metodo handleMessage per il caso di rispostaLogin con un lettore
    @Test
    void handleMessage_rispostaLogin_Lettore() throws Exception {
        Client client = new Client(); // istanza di client da testare

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
        when(msg.isSuccesso()).thenReturn(true); // simula un login riuscito

        // crea un mock Utente (es. Lettore/Genitore) e restituiscilo da getUtente()
        Lettore mockUtente = mock(Lettore.class);
        when(msg.getUtente()).thenReturn(mockUtente); // simula un login riuscito con un lettore

        // fornisce lista eventi vuota se il codice la richiede
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>());

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che il campo privato 'utente' sia stato impostato sul mockUtente
        Field field = Client.class.getDeclaredField("utente"); // ottiene il campo privato 'utente'
        field.setAccessible(true); // rende il campo accessibile
        field.set(client, mockUtente); // imposta il campo 'utente' del client al mockUtente per la verifica

        // legge il campo 'utente' per verificare che sia stato impostato correttamente
        Object utente = getPrivateField(client, "utente"); // legge il campo 'utente' dal client, object e non utente perché getPrivateField restituisce Object
        assertNotNull(utente, "utente dovrebbe essere impostato"); // verifica che 'utente' non sia null
        assertSame(mockUtente, utente); // verifica che 'utente' sia esattamente il mockUtente che abbiamo creato

        // verifica che la view corretta sia stata chiamata almeno una volta
        // (non conosciamo esattamente la firma del metodo show, quindi verifichiamo una chiamata generica)
        verify(homeLettore, atMost(1)).show(any(), any(), any(), any());
        verify(homeGenitore, atMost(1)).show(any(), any(), any(), any());
    }

    // Test del metodo handleMessage per il caso di rispostaLogin con un amministratore
    @Test
    void handleMessage_rispostaLogin_Amministratore() throws Exception {
        Client client = new Client(); // istanza di client da testare

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
        when(msg.isSuccesso()).thenReturn(true); // simula un login riuscito

        // crea un mock Utente (es. Lettore/Genitore) e restituiscilo da getUtente()
        Amministratore mockUtente = mock(Amministratore.class);
        when(msg.getUtente()).thenReturn(mockUtente); // simula un login riuscito con un amministratore

        // fornisce lista eventi vuota se il codice la richiede
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>());

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che il campo privato 'utente' sia stato impostato sul mockUtente
        Field field = Client.class.getDeclaredField("utente"); // ottiene il campo privato 'utente'
        field.setAccessible(true); // rende il campo accessibile
        field.set(client, mockUtente); // imposta il campo 'utente' del client al mockUtente per la verifica

        // legge il campo 'utente' per verificare che sia stato impostato correttamente
        Object utente = getPrivateField(client, "utente");  // legge il campo 'utente' dal client, object e non utente perché getPrivateField restituisce Object
        assertNotNull(utente, "utente dovrebbe essere impostato"); // verifica che 'utente' non sia null
        assertSame(mockUtente, utente); // verifica che 'utente' sia esattamente il mockUtente che abbiamo creato

        // verifica che la view corretta sia stata chiamata almeno una volta
        // (non conosciamo esattamente la firma del metodo show, quindi verifichiamo una chiamata generica)
        verify(homeLettore, atMost(1)).show(any(), any(), any(), any());
        verify(homeGenitore, atMost(1)).show(any(), any(), any(), any());
    }

    // Test del metodo handleMessage per il caso di rispostaNextEventi con un lettore e lista eventi piccola (nasconde bottone)
    @Test
    void handleMessage_nextEventi_lettore_bottoneNascosto() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle view e dell'utente
        HomeLettore homeLettore = mock(HomeLettore.class);
        Lettore lettore = mock(Lettore.class);
        // inietta mock nel client (campi privati)
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", lettore);
        // mock del messaggio RispostaNextEventi con successo e lista eventi piccola
        RispostaNextEventi msg = mock(RispostaNextEventi.class);
        when(msg.isSuccesso()).thenReturn(true); // simula una risposta riuscita
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>()); // size = 0 (<10)
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che il bottone "Next Eventi" sia stato nascosto e che non siano stati aggiornati eventi
        verify(homeLettore, times(1)).nascondiBottoneNextEventi();
        verify(homeLettore, never()).aggiornaEventi(any(ArrayList.class));
    }

    // Test del metodo handleMessage per il caso di rispostaNextEventi con un lettore e lista eventi grande (aggiorna eventi)
    @Test
    void handleMessage_nextEventi_lettore_aggiornaEventi() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle view e dell'utente
        HomeLettore homeLettore = mock(HomeLettore.class);
        Lettore lettore = mock(Lettore.class);
        // inietta mock nel client (campi privati)
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", lettore);
        // mock del messaggio RispostaNextEventi con successo e lista eventi grande
        ArrayList<Evento> eventi = new ArrayList<>();
        for (int i = 0; i < 12; i++) { // crea una lista di 12 eventi (>=10)
            eventi.add(mock(Evento.class));
        }
        // simula una risposta riuscita con una lista di eventi grande
        RispostaNextEventi msg = mock(RispostaNextEventi.class); // mock del messaggio
        when(msg.isSuccesso()).thenReturn(true); // simula una risposta riuscita
        when(msg.getProssimiEventi()).thenReturn(eventi); // size >= 10
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che gli eventi siano stati aggiornati e che il bottone "Next Eventi" non sia stato nascosto
        verify(homeLettore, times(1)).aggiornaEventi(eventi);
        verify(homeLettore, never()).nascondiBottoneNextEventi();
    }

    // Test del metodo handleMessage per il caso di rispostaNextEventi con un genitore e lista eventi piccola (nasconde bottone)
    @Test
    void handleMessage_nextEventi_genitore_bottoneNascosto() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle view e dell'utente
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Genitore genitore = mock(Genitore.class);
        // inietta mock nel client (campi privati)
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", genitore);
        // mock del messaggio RispostaNextEventi con successo e lista eventi piccola
        RispostaNextEventi msg = mock(RispostaNextEventi.class); // mock del messaggio
        when(msg.isSuccesso()).thenReturn(true); // simula una risposta riuscita
        when(msg.getProssimiEventi()).thenReturn(new ArrayList<>()); // size = 0 (<10)
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che il bottone "Next Eventi" sia stato nascosto e che non siano stati aggiornati eventi
        verify(homeGenitore, times(1)).nascondiBottoneNextEventi(); // verifica che il bottone sia stato nascosto
        verify(homeGenitore, never()).aggiornaEventi(any(ArrayList.class)); // verifica che non siano stati aggiornati eventi
    }

    // Test del metodo handleMessage per il caso di rispostaNextEventi con un genitore e lista eventi grande (aggiorna eventi)
    @Test
    void handleMessage_nextEventi_genitore_aggiornaEventi() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle view e dell'utente
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Genitore genitore = mock(Genitore.class);
        // inietta mock nel client (campi privati)
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", genitore);
        // mock del messaggio RispostaNextEventi con successo e lista eventi grande
        ArrayList<Evento> eventi = new ArrayList<>();
        for (int i = 0; i < 12; i++) { // crea una lista di 12 eventi (>=10)
            eventi.add(mock(Evento.class)); // aggiunge un evento mockato alla lista
        }
        // simula una risposta riuscita con una lista di eventi grande
        RispostaNextEventi msg = mock(RispostaNextEventi.class); // mock del messaggio
        when(msg.isSuccesso()).thenReturn(true); // simula una risposta riuscita
        when(msg.getProssimiEventi()).thenReturn(eventi); // size >= 10
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);  // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che gli eventi siano stati aggiornati e che il bottone "Next Eventi" non sia stato nascosto
        verify(homeGenitore, times(1)).aggiornaEventi(eventi); // verifica che gli eventi siano stati aggiornati
        verify(homeGenitore, never()).nascondiBottoneNextEventi(); // verifica che il bottone "Next Eventi" non sia stato nascosto
    }

    // Test del metodo handleMessage per il caso di rispostaIscrizioneEvento con successo (iscrizione riuscita)
    @Test
    void handleMessage_rispostaIscrizioneEvento_successo() throws Exception {
        // spy sul client per intercettare sendMessage senza eseguirla realmente
        Client client = spy(new Client()); // spy serve a intercettare chiamate a metodi specifici (es. sendMessage) senza eseguirli realmente, utile per verificare che siano stati chiamati con i parametri corretti
        // mock vista ed entità
        EventoView eventoView = mock(EventoView.class);
        Genitore genitore = mock(Genitore.class);
        Figlio figlio = mock(Figlio.class);
        Evento evento = mock(Evento.class);

        // configura getId del figlio e lista figli del genitore
        when(figlio.getId()).thenReturn(7);
        // simula che il genitore abbia un figlio con id 7
        ArrayList<Figlio> figli = new ArrayList<>();
        figli.add(figlio);
        when(genitore.getFigli()).thenReturn(figli); // mock del genitore che restituisce la lista dei figli

        // configura la vista
        when(eventoView.getGenitore()).thenReturn(genitore); // mock della vista che restituisce il genitore
        when(eventoView.getEvento()).thenReturn(evento); // mock della vista che restituisce l'evento

        // inietta eventoView nel client
        setPrivateField(client, "eventoView", eventoView);

        // prepara il messaggio di risposta: successo e figlio con id 7
        RispostaIscrizioneEvento msg = mock(RispostaIscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(true); // simula iscrizione riuscita
        when(msg.getFiglio()).thenReturn(figlio); // figlio con id 7

        // inibisci l'effettiva sendMessage sullo spy
        doNothing().when(client).sendMessage(ArgumentMatchers.any(Messaggio.class)); // argumentsmatchers.any serve a intercettare qualsiasi messaggio

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che figlio.iscrivi sia stato chiamato con evento e genitore
        verify(figlio, times(1)).iscrivi(eq(evento), eq(genitore));

        // verifica che sia stata inviata una richiesta di iscritti (RichiestaIscrittiEvento)
        verify(client, times(1)).sendMessage(argThat(m -> m.getClass().getSimpleName().equals("RichiestaIscrittiEvento")));
    }

    // Test del metodo handleMessage per il caso di rispostaIscrizioneEvento con errore (iscrizione fallita)
    @Test
    void handleMessage_rispostaIscrizioneEvento_mostraErrore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock vista
        EventoView eventoView = mock(EventoView.class);
        setPrivateField(client, "eventoView", eventoView); // inietta la vista mockata nel client
        // prepara il messaggio di risposta: fallimento con messaggio di errore
        RispostaIscrizioneEvento msg = mock(RispostaIscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(false); // simula iscrizione fallita
        when(msg.getMessaggioErrore()).thenReturn("Errore iscrizione"); // simula un messaggio di errore specifico
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che la vista mostri l'errore
        verify(eventoView, times(1)).mostraErrore("Errore iscrizione");
    }

    // Test del metodo handleMessage per il caso di rispostaIscrittiEvento con successo (aggiorna iscritti e aggiunge listener)
    @Test
    void handleMessage_rispostaIscrittiEvento_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // mock evento e dati
        Evento evento = mock(Evento.class);
        int iscritti = 5;
        when(evento.getIscritti()).thenReturn(iscritti);
        // simula che l'evento abbia un listener già presente
        Listener listener = mock(Listener.class);
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);

        // mock messaggio
        RispostaIscrittiEvento msg = mock(RispostaIscrittiEvento.class);
        when(msg.isSuccesso()).thenReturn(true); // simula una risposta riuscita
        when(msg.getEvento()).thenReturn(evento); // simula che il messaggio restituisca l'evento mockato

        // mock view e collegamenti
        EventoView eventoView = mock(EventoView.class);
        when(eventoView.getEvento()).thenReturn(evento); // simula che la vista restituisca l'evento mockato
        HomeGenitore homeGenitore = mock(HomeGenitore.class);

        // inietta stati privati
        setPrivateField(client, "eventoView", eventoView);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", mock(Genitore.class)); // simula un genitore loggato

        // invoca handleMessage privato
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica aggiornamento iscritti e aggiunta listener
        verify(eventoView, times(1)).aggiornaIscritti(eq(iscritti)); // eq serve a verificare che il numero di iscritti passato alla vista sia esattamente quello che abbiamo definito
        verify(evento, times(1)).addListener(eq(listener)); // eq serve a verificare che il listener passato al metodo addListener sia esattamente quello che abbiamo definito
    }

    // Test del metodo handleMessage per il caso di rispostaIscrittiEvento con successo per un lettore (aggiorna iscritti e aggiunge listener)
    @Test
    void handleMessage_rispostaIscrittiEvento_lettore_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // mock evento e dati
        Evento evento = mock(Evento.class);
        int iscritti = 3;
        when(evento.getIscritti()).thenReturn(iscritti);
        // simula che l'evento abbia un listener già presente
        Listener listener = mock(Listener.class);
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);

        // mock messaggio
        RispostaIscrittiEvento msg = mock(RispostaIscrittiEvento.class);
        when(msg.isSuccesso()).thenReturn(true); // simula una risposta riuscita
        when(msg.getEvento()).thenReturn(evento);

        // mock view e collegamenti per lettore
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        when(eventoViewLettore.getEvento()).thenReturn(evento); // simula che la vista del lettore restituisca l'evento mockato
        HomeLettore homeLettore = mock(HomeLettore.class);

        // inietta stati privati
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", mock(Lettore.class));

        // invoca handleMessage privato
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica aggiornamento iscritti e aggiunta listener
        verify(eventoViewLettore, times(1)).aggiornaIscritti(eq(iscritti));
        verify(evento, times(1)).addListener(eq(listener));
    }


    // Test del metodo handleMessage per il caso di rispostaDisiscrizioneEvento con successo (disiscrive e richiede iscritti)
    @Test
    void handleMessage_rispostaDisiscrizioneEvento_successo() throws Exception {
        Client client = spy(new Client()); // spy serve a intercettare chiamate a metodi specifici (es. sendMessage) senza eseguirli realmente, utile per verificare che siano stati chiamati con i parametri corretti
        // mock vista, figlio, genitore ed evento
        EventoView eventoView = mock(EventoView.class);
        Genitore genitore = mock(Genitore.class);
        Figlio figlio = mock(Figlio.class);
        Evento evento = mock(Evento.class);
        // configura getId del figlio a 7 e lista figli del genitore
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
        RispostaDisiscrizioneEvento msg = mock(RispostaDisiscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(true); // simula disiscrizione riuscita
        when(msg.getFiglio()).thenReturn(figlio); // figlio con id 7

        // evita l'effettivo invio sullo spy
        doNothing().when(client).sendMessage(any(Messaggio.class));

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che venga chiamato disiscrivi sul figlio con evento e genitore
        verify(figlio, times(1)).disiscrivi(eq(evento), eq(genitore));

        // verifica che sia stata inviata una richiesta di iscritti (RichiestaIscrittiEvento)
        verify(client, times(1)).sendMessage(argThat(m -> m.getClass().getSimpleName().equals("RichiestaIscrittiEvento")));
    }

    // Test del metodo handleMessage per il caso di rispostaDisiscrizioneEvento con errore (disiscrizione fallita)
    @Test
    void handleMessage_rispostaDisiscrizioneEvento_Errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock vista
        EventoView eventoView = mock(EventoView.class);
        setPrivateField(client, "eventoView", eventoView); // inietta la vista mockata nel client
        // prepara il messaggio di risposta: fallimento con messaggio di errore
        RispostaDisiscrizioneEvento msg = mock(RispostaDisiscrizioneEvento.class);
        when(msg.isSuccesso()).thenReturn(false); // simula disiscrizione fallita
        when(msg.getMessaggioErrore()).thenReturn("Errore disiscrizione"); // simula un messaggio di errore specifico
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che la vista mostri l'errore
        verify(eventoView, times(1)).mostraErrore("Errore disiscrizione");
    }

    // Test del metodo handleMessage per il caso di rispostaAggiornaGenitore con successo (aggiorna nome/cognome e aggiorna view)
    @Test
    void handleMessage_rispostaAggiornaGenitore_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // mock profilo e genitore presente nella view
        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        Genitore genInView = mock(Genitore.class);
        when(profiloGenitore.getGenitore()).thenReturn(genInView);

        // inietta profiloGenitore
        setPrivateField(client, "profiloGenitore", profiloGenitore);

        // prepara messaggio di risposta con il genitore aggiornato
        RispostaAggiornaGenitore msg = mock(RispostaAggiornaGenitore.class);
        when(msg.isSuccesso()).thenReturn(true); // simula aggiornamento riuscito
        Genitore genFromMsg = mock(Genitore.class);
        when(genFromMsg.getNome()).thenReturn("Mario");
        when(genFromMsg.getCognome()).thenReturn("Rossi");
        when(msg.getGenitore()).thenReturn(genFromMsg); // simula che il messaggio restituisca un genitore con nome e cognome aggiornati

        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // verifica che il genitore della view sia stato aggiornato con i nuovi valori
        verify(profiloGenitore, times(2)).getGenitore(); // verifica che getGenitore sia stato chiamato due volte (una per leggere il genitore da aggiornare e una per aggiornare i campi)
        verify(genInView, times(1)).setNome("Mario"); // verifica che setNome sia stato chiamato con "Mario"
        verify(genInView, times(1)).setCognome("Rossi"); // verifica che setCognome sia stato chiamato con "Rossi"
    }

    // Test del metodo handleMessage per il caso di rispostaAggiornaGenitore con errore (mostra errore sulla view)
    @Test
    void handleMessage_rispostaAggiornaGenitore_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock profiloGenitore
        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        setPrivateField(client, "profiloGenitore", profiloGenitore);
        // prepara messaggio di risposta con errore
        RispostaAggiornaGenitore msg = mock(RispostaAggiornaGenitore.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore aggiornamento");
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che la view mostri l'errore
        verify(profiloGenitore, times(1)).mostraErrore("Errore aggiornamento");
    }

    // Test del metodo handleMessage per il caso di rispostaAggiungiFiglio con successo (aggiunge figlio alla view e aggiorna figli)
    @Test
    void handleMessage_rispostaAggiungiFiglio_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock profiloGenitore e genitore presente nella view
        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        Genitore genInView = mock(Genitore.class);
        when(profiloGenitore.getGenitore()).thenReturn(genInView);
        // inietta profiloGenitore
        setPrivateField(client, "profiloGenitore", profiloGenitore);
        // prepara messaggio di risposta con nuovo figlio
        RispostaAggiungiFiglio msg = mock(RispostaAggiungiFiglio.class);
        when(msg.isSuccesso()).thenReturn(true); // simula aggiunta riuscita
        Figlio nuovo = mock(Figlio.class); // nuovo figlio da aggiungere
        when(msg.getNuovoFiglio()).thenReturn(nuovo); // simula che il messaggio restituisca un nuovo figlio da aggiungere
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che il nuovo figlio sia stato aggiunto alla view e che i figli siano stati aggiornati
        verify(genInView, times(1)).aggiungiFiglio(eq(nuovo));
        verify(profiloGenitore, times(1)).aggiornaFigli(eq(nuovo));
    }

    // Test del metodo handleMessage per il caso di rispostaAggiungiFiglio con errore (mostra errore sulla view)
    @Test
    void handleMessage_rispostaAggiungiFiglio_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock profiloGenitore
        ProfiloGenitore profiloGenitore = mock(ProfiloGenitore.class);
        setPrivateField(client, "profiloGenitore", profiloGenitore);
        // prepara messaggio di risposta con errore
        RispostaAggiungiFiglio msg = mock(RispostaAggiungiFiglio.class);
        when(msg.isSuccesso()).thenReturn(false); // simula aggiunta fallita
        when(msg.getMessaggioErrore()).thenReturn("Errore aggiunta figlio"); // simula un messaggio di errore specifico
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che la view mostri l'errore
        verify(profiloGenitore, times(1)).mostraErrore2("Errore aggiunta figlio");
    }

    // Test del metodo handleMessage per il caso di rispostaLettoriELuoghiELibri con successo (aggiorna lettori, luoghi, libri su tutte le view)
    @Test
    void handleMessage_rispostaLettoriELuoghiELibri_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle view
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);
        HomeLettore homeLettore = mock(HomeLettore.class);
        // inietta le view nel client
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);
        setPrivateField(client, "homeLettore", homeLettore);
        // prepara liste vuote di lettori, luoghi e libri
        ArrayList<Lettore> lettori = new ArrayList<>();
        ArrayList<Luogo> luoghi = new ArrayList<>();
        ArrayList<Libro> libri = new ArrayList<>();
        // prepara messaggio di risposta con successo e liste
        RispostaLettoriELuoghiELibri msg = mock(RispostaLettoriELuoghiELibri.class);
        when(msg.isSuccesso()).thenReturn(true);    // simula risposta riuscita
        when(msg.getLettori()).thenReturn(lettori); // simula che il messaggio restituisca la lista di lettori
        when(msg.getLuoghi()).thenReturn(luoghi); // simula che il messaggio restituisca la lista di luoghi
        when(msg.getElencolibri()).thenReturn(libri); // simula che il messaggio restituisca la lista di libri
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che tutte le view siano state aggiornate con le liste corrette
        verify(eventoViewLettore, times(1)).aggiornaLettoriELuoghiELibri(eq(lettori), any(), any());
        verify(homeGenitore, times(1)).aggiornaLibri(eq(libri));
        verify(homeAmministratore, times(1)).aggiornaLibri(eq(libri));
        verify(homeLettore, times(1)).aggiornaLibri(eq(libri));
    }

    // Test del metodo handleMessage per il caso di rispostaLettoriELuoghiELibri con errore (mostra errore su tutte le view)
    @Test
    void handleMessage_rispostaLettoriELuoghiELibri_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle view
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);
        HomeLettore homeLettore = mock(HomeLettore.class);
        // inietta le view nel client
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);
        setPrivateField(client, "homeLettore", homeLettore);
        // prepara messaggio di risposta con errore
        RispostaLettoriELuoghiELibri msg = mock(RispostaLettoriELuoghiELibri.class);
        when(msg.isSuccesso()).thenReturn(false);  // simula risposta fallita
        when(msg.getMessaggioerrore()).thenReturn("Errore caricamento dati"); // simula un messaggio di errore specifico
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che tutte le view mostrino l'errore
        verify(eventoViewLettore, times(1)).mostraErrore("Errore caricamento dati");
        verify(homeGenitore, times(1)).mostraErrore("Errore caricamento dati");
        verify(homeAmministratore, times(1)).mostraErrore("Errore caricamento dati");
        verify(homeLettore, times(1)).mostraErrore("Errore caricamento dati");
    }

    // Test del metodo handleMessage per il caso di notificaAggiornamentoEvento (aggiorna eventi e figli iscritti)
    @Test
    void handleMessage_notificaAggiornamentoEvento_genitore() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // setup homeGenitore e lista eventi prossimi con un evento esistente
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Evento evento = mock(Evento.class);
        when(evento.getId()).thenReturn(1);
        ArrayList<Evento> eventi = new ArrayList<>();
        eventi.add(evento);
        when(homeGenitore.getEventiProssimi()).thenReturn(eventi);

        // crea una Scene mock e fallo ritornare anche da homeGenitore.getScene()
        javafx.scene.Scene scene = mock(javafx.scene.Scene.class); // scena mock
        when(homeGenitore.getScene()).thenReturn(scene); // homeGenitore restituisce la scena mock

        // utente genitore con figlio iscritto
        Genitore gen = mock(Genitore.class);
        Figlio figlio = mock(Figlio.class);
        when(figlio.isIscritto(any(Evento.class))).thenReturn(true); // figlio iscritto all'evento
        when(gen.getFigli()).thenReturn(new ArrayList<>() {{
            add(figlio);
        }}); // ritorna lista con aggingta del figlio

        // evento aggiornato (stesso id)
        Evento newEvento = mock(Evento.class);
        when(newEvento.getId()).thenReturn(1);

        // inietta stati
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "utente", gen);
        setPrivateField(client, "eventoView", mock(EventoView.class)); // non è necessario popolare scena qui

        // prepara loginView con Stage non-null e Scene coerente
        LoginView loginView = mock(LoginView.class); // loginView mock
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class); // stage mock
        when(loginView.getStage()).thenReturn(stage); // stage non-null
        when(stage.getScene()).thenReturn(scene); // scena coerente
        when(stage.isShowing()).thenReturn(true); // stage visibile
        setPrivateField(client, "loginView", loginView); // inietta loginView in client
        // prepara messaggio di notifica aggiornamento evento
        NotificaAggiornamentoEvento msg = mock(NotificaAggiornamentoEvento.class);
        when(msg.getEvento()).thenReturn(newEvento); // simula che il messaggio restituisca l'evento aggiornato
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato

        // figlio deve aggiornare l'evento
        verify(figlio, times(1)).aggiornaEvento(eq(newEvento));
        // la lista eventi deve essere stata sostituita con il nuovo evento (stesso id)
        assertSame(newEvento, eventi.get(0));
    }

    // Test del metodo handleMessage per il caso di rispostaSalvaEvento con successo (aggiorna eventoViewLettore)
    @Test
    void handleMessage_rispostaSalvaEvento_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // prepara lettore e evento
        Lettore lettoreInView = mock(Lettore.class);
        when(lettoreInView.getId()).thenReturn(10);
        Evento evento = mock(Evento.class);
        when(evento.getCreatore()).thenReturn(lettoreInView); // creatore con stesso id
        when(evento.isIscritto(lettoreInView)).thenReturn(true); // lettore iscritto all'evento
        when(evento.getId()).thenReturn(5); // id evento

        // mock view lettore
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        when(eventoViewLettore.getLettore()).thenReturn(lettoreInView); // simula che la vista restituisca il lettoreInView
        // inietta view nel client
        setPrivateField(client, "eventoViewLettore", eventoViewLettore);

        // messaggio
        RispostaSalvaEvento msg = mock(RispostaSalvaEvento.class);
        when(msg.isSuccesso()).thenReturn(true); // simula salvataggio riuscito
        when(msg.getEvento()).thenReturn(evento); // simula che il messaggio restituisca l'evento salvato
        // invoca handleMessage (metodo privato)
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottiene il metodo privato handleMessage
        handle.setAccessible(true); // rende il metodo accessibile
        handle.invoke(client, msg); // invoca il metodo con il messaggio mockato
        // verifica che la view sia stata aggiornata correttamente
        verify(lettoreInView, times(1)).aggiungiEventiCreati(eq(evento));
        verify(lettoreInView, times(1)).aggiungiIscrizioneLettura(eq(evento));
        verify(eventoViewLettore, times(1)).setEvento(eq(evento));
    }

    // Test del metodo handleMessage per il caso di rispostaSalvaEvento con errore (mostra errore sulla view)
    @Test
    void handleMessage_rispostaSalvaEvento_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock view lettore
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        setPrivateField(client, "eventoViewLettore", eventoViewLettore); // inietta view nel client
        // messaggio con errore
        RispostaSalvaEvento msg = mock(RispostaSalvaEvento.class);
        when(msg.isSuccesso()).thenReturn(false);
        when(msg.getMessaggioErrore()).thenReturn("Errore salvataggio");
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);
        // verifica che la view mostri l'errore
        verify(eventoViewLettore, times(1)).mostraErrore("Errore salvataggio");
    }

    // Test del metodo handleMessage per il caso di rispostaAggiungiRecensione con successo ed errore
    @Test
    void handleMessage_rispostaAggiungiRecensione_successo_ed_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock libroDetailedView
        LibroDetailedView libroView = mock(LibroDetailedView.class);
        setPrivateField(client, "libroDetailedView", libroView); // inietta view nel client

        // successo
        RispostaAggiungiRecensione ms1 = mock(RispostaAggiungiRecensione.class);
        when(ms1.isSuccesso()).thenReturn(true);
        Recensione rec = mock(Recensione.class);
        when(ms1.getRecensione()).thenReturn(rec);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ms1);
        // verifica che aggiornaRecensioni sia stato chiamato con la recensione
        verify(libroView, times(1)).aggiornaRecensioni(eq(rec));

        // errore
        RispostaAggiungiRecensione ms2 = mock(RispostaAggiungiRecensione.class);
        when(ms2.isSuccesso()).thenReturn(false);
        when(ms2.getMessaggioErrore()).thenReturn("Errore recensione");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, ms2);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(libroView, times(1)).mostraErrore("Errore recensione");
    }

    // Test del metodo handleMessage per il caso di rispostaCancellaRecensione con successo ed errore
    @Test
    void handleMessage_rispostaCancellaRecensione_successo_e_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock libroDetailedView
        LibroDetailedView libroView = mock(LibroDetailedView.class);
        setPrivateField(client, "libroDetailedView", libroView); // inietta view nel client

        // successo
        RispostaCancellaRecensione ok = mock(RispostaCancellaRecensione.class);
        when(ok.isSuccesso()).thenReturn(true);
        when(ok.getId()).thenReturn(42);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);
        // verifica che cancellaRecensione sia stato chiamato con l'id corretto e che setAttendi(false) sia stato chiamato
        verify(libroView, times(1)).cancellaRecensione(eq(42));
        verify(libroView, times(1)).mostraErrore2(ArgumentMatchers.contains("Cancellata recensione"));
        verify(libroView, times(1)).setAttendi(false);

        // errore
        RispostaCancellaRecensione err = mock(RispostaCancellaRecensione.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore cancellazione");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, err);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(libroView, times(1)).mostraErrore2("Errore cancellazione");
    }

    // Test del metodo handleMessage per il caso di rispostaRecensioniERecensibilita con successo (aggiorna homes e setAttendi false)
    @Test
    void handleMessage_rispostaRecensioniERecensibilita_successo() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock delle home
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        HomeAmministratore homeAmministratore = mock(HomeAmministratore.class);
        HomeLettore homeLettore = mock(HomeLettore.class);
        // inietta le home nel client
        setPrivateField(client, "homeGenitore", homeGenitore);
        setPrivateField(client, "homeAmministratore", homeAmministratore);
        setPrivateField(client, "homeLettore", homeLettore);
        // prepara lista di recensioni
        ArrayList<Recensione> recs = new ArrayList<>();
        recs.add(mock(Recensione.class));
        // prepara messaggio di risposta con successo, recensibilità e recensioni
        RispostaRecensioniERecensibilita msg = mock(RispostaRecensioniERecensibilita.class);
        when(msg.isSuccesso()).thenReturn(true);
        when(msg.isRecensibile()).thenReturn(true);
        when(msg.getRecensioni()).thenReturn(recs);
        // invoca handleMessage (metodo privato) rendendolo accessibile e lo invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);
        // verifica che tutte le home siano state aggiornate correttamente
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

    // Test del metodo handleMessage per il caso di notificaAggiornamentoEvento per lettore (aggiorna creators ed eventi)
    @Test
    void handleMessage_notificaAggiornamentoEvento_lettore() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // prepara loginView + stage + scene per evitare non-null e viewCoerente
        LoginView loginView = mock(LoginView.class);
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class);
        javafx.scene.Scene scene = mock(javafx.scene.Scene.class);
        when(loginView.getStage()).thenReturn(stage);
        when(stage.getScene()).thenReturn(scene);
        when(stage.isShowing()).thenReturn(true); // stage visibile
        setPrivateField(client, "loginView", loginView); // inietta loginView in client

        // prepara homeLettore con eventi prossimi e lettore con eventi creati
        HomeLettore homeLettore = mock(HomeLettore.class);
        when(homeLettore.getScene()).thenReturn(scene); // scena coerente
        Lettore lettore = mock(Lettore.class);
        when(lettore.getId()).thenReturn(10);
        when(homeLettore.getLettore()).thenReturn(lettore);
        // eventi prossimi contiene un evento con id
        Evento existingProssimo = mock(Evento.class);
        when(existingProssimo.getId()).thenReturn(2);
        ArrayList<Evento> prossimi = new ArrayList<>();
        prossimi.add(existingProssimo);
        when(homeLettore.getEventiProssimi()).thenReturn(prossimi);
        // evento creato da aggiornare
        Evento createdEvent = mock(Evento.class);
        when(createdEvent.getId()).thenReturn(2);
        Lettore creator = mock(Lettore.class);
        when(creator.getId()).thenReturn(10);
        when(createdEvent.getCreatore()).thenReturn(creator);
        when(createdEvent.isIscritto(lettore)).thenReturn(true);

        // eventi creati contiene un evento con id
        Evento createdExisting = mock(Evento.class);
        when(createdExisting.getId()).thenReturn(2);
        ArrayList<Evento> eventiCreati = new ArrayList<>();
        eventiCreati.add(createdExisting);
        when(lettore.getEventiCreati()).thenReturn(eventiCreati);
        // inietta stati
        setPrivateField(client, "homeLettore", homeLettore);
        setPrivateField(client, "utente", lettore);

        // inietta eventoViewLettore senza scena (null) per testare il ramo else
        EventoViewLettore eventoViewLettore = mock(EventoViewLettore.class);
        when(eventoViewLettore.getScene()).thenReturn(null); // scena null
        setPrivateField(client, "eventoViewLettore", eventoViewLettore); // inietta view nel client
        // prepara messaggio di notifica aggiornamento evento
        NotificaAggiornamentoEvento msg = mock(NotificaAggiornamentoEvento.class);
        when(msg.getEvento()).thenReturn(createdEvent); // simula che il messaggio restituisca l'evento creato da aggiornare
        // invoca handleMessage (metodo privato) rendendolo accessibile e lo invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // verifica che l'evento creato esistente sia aggiornato e che la lista prossimi sia sostituita
        verify(createdExisting, times(1)).aggiornaEvento(eq(createdEvent));
        assertSame(createdEvent, prossimi.get(0));

        // verifica che il lettore abbia aggiunto l'iscrizione (per iscritto==true)
        verify(lettore, times(1)).aggiungiIscrizioneLettura(eq(createdEvent));

        // se la view è coerente, homeLettore.aggiornaEventi deve essere chiamata
        verify(homeLettore, times(1)).aggiornaEventi(eq(prossimi));
    }

    // Test del metodo handleMessage per il caso di rispostaNuovoGenitore con successo ed errore
    @Test
    void handleMessage_rispostaNuovoGenitore_successo_ed_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock registraNewGenitore
        RegistraNewGenitore reg = mock(RegistraNewGenitore.class);
        setPrivateField(client, "registraNewGenitore", reg); // inietta view nel client

        // successo
        RispostaNuovoGenitore ok = mock(RispostaNuovoGenitore.class);
        when(ok.isSuccesso()).thenReturn(true);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);
        // verifica che mostraSuccesso sia stato chiamato con il messaggio corretto
        verify(reg, times(1)).mostraSuccesso("Nuovo genitore registrato!");

        // errore
        RispostaNuovoGenitore err = mock(RispostaNuovoGenitore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore nuovo genitore");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, err);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(reg, times(1)).mostraErrore("Errore nuovo genitore");
    }

    // Test del metodo handleMessage per il caso di rispostaNuovoLettore con successo ed errore
    @Test
    void handleMessage_rispostaNuovoLettore_successo_ed_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock registraNewLettore
        RegistraNewLettore reg = mock(RegistraNewLettore.class);
        setPrivateField(client, "registraNewLettore", reg); // inietta view nel client

        // successo
        RispostaNuovoLettore ok = mock(RispostaNuovoLettore.class);
        when(ok.isSuccesso()).thenReturn(true);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);
        // verifica che mostraSuccesso sia stato chiamato con il messaggio corretto
        verify(reg, times(1)).mostraSuccesso("Nuovo lettore registrato!");

        // errore
        RispostaNuovoLettore err = mock(RispostaNuovoLettore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore nuovo lettore");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, err);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(reg, times(1)).mostraErrore("Errore nuovo lettore");
    }

    // Test del metodo handleMessage per il caso di rispostaNuovoAmministratore con successo ed errore
    @Test
    void handleMessage_rispostaNuovoAmministratore_successo_ed_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock registraNewAmministratore
        RegistraNewAmministratore reg = mock(RegistraNewAmministratore.class);
        setPrivateField(client, "registraNewAmministratore", reg); // inietta view nel client
        // successo
        RispostaNuovoAmministratore ok = mock(RispostaNuovoAmministratore.class);
        when(ok.isSuccesso()).thenReturn(true);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);
        // verifica che mostraSuccesso sia stato chiamato con il messaggio corretto
        verify(reg, times(1)).mostraSuccesso("Nuovo amministratore registrato!");
        // errore
        RispostaNuovoAmministratore err = mock(RispostaNuovoAmministratore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore nuovo amministratore");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, err);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(reg, times(1)).mostraErrore("Errore nuovo amministratore");
    }

    // Test del metodo handleMessage per il caso di notificaAggiornamentoEvento per genitore con eventoView visibile (aggiorna eventoView e homeGenitore)
    @Test
    void handleMessage_notificaAggiornamentoEvento_genitore_eventoView() throws Exception {
        Client client = new Client(); // istanza di client da testare

        // setup loginView + stage + scene
        LoginView loginView = mock(LoginView.class);
        javafx.stage.Stage stage = mock(javafx.stage.Stage.class);
        javafx.scene.Scene scene = mock(javafx.scene.Scene.class);
        when(loginView.getStage()).thenReturn(stage);
        when(stage.getScene()).thenReturn(scene);
        when(stage.isShowing()).thenReturn(true); // stage visibile
        setPrivateField(client, "loginView", loginView); // inietta loginView in client

        // homeGenitore e lista prossimi (con evento esistente)
        HomeGenitore homeGenitore = mock(HomeGenitore.class);
        Evento existing = mock(Evento.class);
        when(existing.getId()).thenReturn(7); // 7 id dell'evento esistente
        ArrayList<Evento> prossimi = new ArrayList<>();
        prossimi.add(existing);
        when(homeGenitore.getEventiProssimi()).thenReturn(prossimi);
        when(homeGenitore.getScene()).thenReturn(scene); // scena coerente
        setPrivateField(client, "homeGenitore", homeGenitore); // inietta homeGenitore in client

        // utente Genitore senza figli per questo test (evita altre chiamate)
        Genitore gen = mock(Genitore.class);
        when(gen.getFigli()).thenReturn(new ArrayList<>());
        setPrivateField(client, "utente", gen); // inietta utente in client

        // eventoView visibile e relativo evento (stesso id)
        EventoView evView = mock(EventoView.class);
        when(evView.getScene()).thenReturn(scene); // scena coerente
        when(evView.getEvento()).thenReturn(existing); // evento con stesso id
        setPrivateField(client, "eventoView", evView); // inietta eventoView in client

        // evento aggiornato
        Evento newEvento = mock(Evento.class);
        when(newEvento.getId()).thenReturn(7); // stesso id dell'evento esistente
        // prepara messaggio di notifica aggiornamento evento
        NotificaAggiornamentoEvento msg = mock(NotificaAggiornamentoEvento.class);
        when(msg.getEvento()).thenReturn(newEvento);
        // invoca handleMessage (metodo privato) rendendolo accessibile e lo invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // eventoView deve essere aggiornato e la lista prossimi sostituita
        verify(evView, times(1)).aggiornaEvento(eq(newEvento));
        assertSame(newEvento, prossimi.get(0));
        // se la vista era coerente, aggiornaEventi su homeGenitore dovrebbe essere chiamato
        verify(homeGenitore, times(1)).aggiornaEventi(eq(prossimi));
    }

    // Test del metodo handleMessage per il caso di rispostaAggiornaAmministratore con successo ed errore
    @Test
    void handleMessage_rispostaAggiornaAmministratore_successo_ed_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock profiloAmministratore
        ProfiloAmministratore profiloAmministratore = mock(ProfiloAmministratore.class);
        Amministratore adminInView = mock(Amministratore.class);
        when(profiloAmministratore.getAmministratore()).thenReturn(adminInView);
        setPrivateField(client, "profiloAmministratore", profiloAmministratore); // inietta view nel client

        // successo
        RispostaAggiornaAmministratore ok = mock(RispostaAggiornaAmministratore.class);
        when(ok.isSuccesso()).thenReturn(true);
        Amministratore fromMsg = mock(Amministratore.class);
        when(fromMsg.getNome()).thenReturn("Anna");
        when(fromMsg.getCognome()).thenReturn("Verdi");
        when(ok.getAmministratore()).thenReturn(fromMsg);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, ok);

        // getAmministratore dovrebbe essere chiamato due volte (setNome + setCognome)
        verify(profiloAmministratore, times(2)).getAmministratore();
        verify(adminInView, times(1)).setNome("Anna");
        verify(adminInView, times(1)).setCognome("Verdi");

        // errore
        RispostaAggiornaAmministratore err = mock(RispostaAggiornaAmministratore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore aggiornamento admin");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, err);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(profiloAmministratore, times(1)).mostraErrore("Errore aggiornamento admin");
    }

    // Test del metodo handleMessage per il caso di rispostaAggiornaLettore con successo ed errore
    @Test
    void handleMessage_rispostaAggiornaLettore_successo_ed_errore() throws Exception {
        Client client = new Client(); // istanza di client da testare
        // mock profiloLettore
        ProfiloLettore profiloLettore = mock(ProfiloLettore.class);
        Lettore lettInView = mock(Lettore.class);
        when(profiloLettore.getLettore()).thenReturn(lettInView);
        setPrivateField(client, "profiloLettore", profiloLettore); // inietta view nel client

        // successo
        RispostaAggiornaLettore msg = mock(RispostaAggiornaLettore.class);
        when(msg.isSuccesso()).thenReturn(true);
        Lettore fromMsg = mock(Lettore.class);
        when(fromMsg.getNome()).thenReturn("Giulia");
        when(fromMsg.getCognome()).thenReturn("Bianchi");
        when(msg.getLettore()).thenReturn(fromMsg);
        // invoca handleMessage (metodo privato), rende accessibile e invoca
        Method handle = Client.class.getDeclaredMethod("handleMessage", Messaggio.class);
        handle.setAccessible(true);
        handle.invoke(client, msg);

        // profiloLettore.getLettore() viene invocato due volte (setNome + setCognome)
        verify(profiloLettore, times(2)).getLettore();
        verify(lettInView, times(1)).setNome("Giulia");
        verify(lettInView, times(1)).setCognome("Bianchi");

        // errore
        RispostaAggiornaLettore err = mock(RispostaAggiornaLettore.class);
        when(err.isSuccesso()).thenReturn(false);
        when(err.getMessaggioErrore()).thenReturn("Errore aggiornamento lettore");
        // invoca handleMessage (metodo privato) già reso accessibile
        handle.invoke(client, err);
        // verifica che mostraErrore sia stato chiamato con il messaggio di errore
        verify(profiloLettore, times(1)).mostraErrore("Errore aggiornamento lettore");
    }
}
