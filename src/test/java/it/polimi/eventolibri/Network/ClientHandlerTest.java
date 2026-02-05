package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Controller.Controller;
import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test di unità per ClientHandler.
 * - I campi privati vengono sostituiti con mock per isolare la classe.
 * - I messaggi passano attraverso handleMessage (metodo privato) per verificare i comportamenti.
 */
@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {
    // mock delle dipendenze
    @Mock
    Socket socketMock;

    @Mock
    Server serverMock;

    @Mock
    Controller controllerMock;

    // cattura dell'oggetto scritto su ObjectOutputStream nelle verifiche
    @Captor
    ArgumentCaptor<Object> objectCaptor;

    // istanza della classe sotto test
    ClientHandler handler;

    @BeforeEach
    // setup prima di ogni test
    void setUp() throws Exception {
        // creazione handler con socket e server mock
        handler = new ClientHandler(socketMock, serverMock);

        // inietto il controller mock nel campo privato 'controller'
        setPrivateField(handler, "controller", controllerMock);
    }

    // Utility per impostare campi privati
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName); // ottengo il campo
        f.setAccessible(true); // rendo accessibile
        f.set(target, value); // imposto il valore
    }

    // Utility per invocare il metodo privato handleMessage
    private void invokeHandleMessage(Messaggio msg) throws Exception {
        java.lang.reflect.Method m = ClientHandler.class.getDeclaredMethod("handleMessage", Messaggio.class); // ottengo il metodo
        m.setAccessible(true); // rendo accessibile
        m.invoke(handler, msg); // invoco il metodo
    }

    // test per RichiestaEventi
    @Test
    // test per RichiestaNextEventi
    void testRichiestaNextEventi_scriveRisposta() throws Exception {
        // impostazioni iniziali
        RichiestaNextEventi richiesta = mock(RichiestaNextEventi.class);
        Evento ultimo = mock(Evento.class);
        when(richiesta.getUltimoEvento()).thenReturn(ultimo);

        RispostaNextEventi risposta = mock(RispostaNextEventi.class);
        when(controllerMock.getNextEventi(ultimo)).thenReturn(risposta);

        // preparo un ObjectOutputStream mock per intercettare l'oggetto inviato
        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);

        // invocazione del metodo
        invokeHandleMessage(richiesta);

        // verifiche
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();
    }

    // test per CloseUI
    @Test
    void testCloseUI_rimuoveClient_e_chiudeSocket() throws Exception {
        // impostazioni iniziali
        CloseUI close = mock(CloseUI.class);

        // preparo una mappa reale per i clients e inserisco il handler
        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        clientsMap.put(handler, null); // utente nullo per semplicità
        when(serverMock.getClients()).thenReturn(clientsMap); // inietto la mappa mockata

        // rendiamo il socketMock verificabile su close()
        doNothing().when(socketMock).close();

        // out non necessario per questo test ma evitiamo NPE impostandolo
        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock); // impostazione del campo privato

        // invocazione del metodo
        invokeHandleMessage(close);

        // verifiche
        assertFalse(clientsMap.containsKey(handler), "Handler dovrebbe essere rimosso dalla mappa clients");
        verify(socketMock).close();
    }

    // test per RichiestaIscrizioneEvento
    @Test
    void testRichiestaIscrizioneEvento_notificaAltriListener_seSuccesso() throws Exception {
        // impostazioni iniziali
        RichiestaIscrizioneEvento richiesta = mock(RichiestaIscrizioneEvento.class);
        Evento evento = mock(Evento.class);
        Figlio figlio = mock(Figlio.class);
        Genitore genitore = mock(Genitore.class);

        // configuro la richiesta
        when(richiesta.getEvento()).thenReturn(evento);
        when(richiesta.getFiglio()).thenReturn(figlio);
        when(richiesta.getGenitore()).thenReturn(genitore);

        // configuro la risposta del controller
        RispostaIscrizioneEvento risposta = mock(RispostaIscrizioneEvento.class);
        when(risposta.isSuccesso()).thenReturn(true);
        when(controllerMock.iscriviFiglioEvento(figlio, evento, genitore)).thenReturn(risposta);

        // preparo listener con id = 42
        Listener listener = mock(Listener.class);
        when(listener.getId()).thenReturn(42);

        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);

        // preparo una mappa clients che contiene un altro ClientHandler (mock) con utente id 42
        ClientHandler otherHandler = mock(ClientHandler.class);
        Utente otherUtente = mock(Utente.class);
        when(otherUtente.getId()).thenReturn(42);
        when(otherHandler.getUtente()).thenReturn(otherUtente);

        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        clientsMap.put(otherHandler, otherUtente);

        // impostiamo un Utente non nullo per il handler per evitare NPE
        Utente thisUtente = mock(Utente.class);
        when(thisUtente.getId()).thenReturn(1); // id diverso da 42
        clientsMap.put(handler, thisUtente);

        when(serverMock.getClients()).thenReturn(clientsMap);

        // impostazione ad out per il nostro handler (evitiamo NPE)
        setPrivateField(handler, "out", mock(ObjectOutputStream.class));
        // iniettiamo anche il campo utente nel handler reale (coerenza)
        setPrivateField(handler, "utente", thisUtente);

        // invocazione del metodo
        invokeHandleMessage(richiesta);

        // Verifichiamo che l'altro handler abbia ricevuto la notifica di aggiornamento evento
        verify(otherHandler).sendMessage(any(NotificaAggiornamentoEvento.class));
    }

    // test per sendMessage
    @Test
    void testSendMessage_serializzaSenzaEccezioni() throws Exception {
        // impostazioni iniziali
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream realOut = new ObjectOutputStream(baos);
        setPrivateField(handler, "out", realOut);

        // messaggio dummy
        Messaggio dummy = mock(Messaggio.class);

        // invocazione del metodo sendMessage
        handler.sendMessage(dummy);

        // controllo che qualcosa sia stato scritto nel buffer
        assertTrue(baos.size() > 0);
    }

    @Test
    // test per RichiestaLogin con utente Genitore
    void testRichiestaLogin_genitore_aggiungeClientEMandaRisposta() throws Exception {
        // impostazioni iniziali
        RichiestaLogin richiesta = mock(RichiestaLogin.class);
        when(richiesta.getUsername()).thenReturn("userG");
        when(richiesta.getPassword()).thenReturn("passG");
        // risposta mockata
        RispostaLogin risposta = mock(RispostaLogin.class);
        when(risposta.isSuccesso()).thenReturn(true);

        // utente restituito dal controller (dati che verranno passati al factory)
        Genitore genMsg = mock(Genitore.class);
        when(genMsg.getId()).thenReturn(11);
        when(genMsg.getNome()).thenReturn("Paolo");
        when(genMsg.getCognome()).thenReturn("Rossi");
        when(genMsg.getUserName()).thenReturn("userG");
        when(risposta.getUtente()).thenReturn(genMsg);
        // configurazione del controller mock
        when(controllerMock.controllaLogin("userG", "passG")).thenReturn(risposta);

        // creazione mappa clients reale
        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        when(serverMock.getClients()).thenReturn(clientsMap);

        // mock out per intercettare sendMessage
        ObjectOutputStream outMock = mock(ObjectOutputStream.class); // mock out
        setPrivateField(handler, "out", outMock); // inietto nel handler

        // invocazione del metodo
        invokeHandleMessage(richiesta);

        // verifiche
        verify(outMock).writeObject(risposta); // verifica invio risposta
        verify(outMock).flush(); // verifica flush
        verify(outMock).reset(); // verifica reset
        // controllo che il client sia stato aggiunto alla mappa con l'utente corretto
        assertTrue(clientsMap.containsKey(handler)); // verifica presenza handler
        assertNotNull(clientsMap.get(handler)); // verifica utente non nullo
        assertTrue(clientsMap.get(handler) instanceof Genitore, "L'utente salvato deve essere Genitore"); // verifica tipo utente
    }

    // test per RichiestaLogin con utente Lettore
    @Test
    void testRichiestaLogin_lettore_aggiungeClientEMandaRisposta() throws Exception {
        // impostazioni iniziali
        RichiestaLogin richiesta = mock(RichiestaLogin.class);
        when(richiesta.getUsername()).thenReturn("userL");
        when(richiesta.getPassword()).thenReturn("passL");
        // risposta mockata
        RispostaLogin risposta = mock(RispostaLogin.class);
        when(risposta.isSuccesso()).thenReturn(true);
        // utente restituito dal controller (dati che verranno passati al factory)
        Lettore lettMsg = mock(Lettore.class);
        when(lettMsg.getId()).thenReturn(22);
        when(lettMsg.getNome()).thenReturn("Laura");
        when(lettMsg.getCognome()).thenReturn("Bianchi");
        when(lettMsg.getUserName()).thenReturn("userL");
        when(risposta.getUtente()).thenReturn(lettMsg);

        when(controllerMock.controllaLogin("userL", "passL")).thenReturn(risposta);

        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        when(serverMock.getClients()).thenReturn(clientsMap);

        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);

        // esecuzione del metodo
        invokeHandleMessage(richiesta);

        // verifiche
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();
        assertTrue(clientsMap.containsKey(handler));
        assertNotNull(clientsMap.get(handler));
        assertTrue(clientsMap.get(handler) instanceof Lettore, "L'utente salvato deve essere Lettore");
    }

    // test per RichiestaLogin con utente Amministratore
    @Test
    void testRichiestaLogin_amministratore_aggiungeClientEMandaRisposta() throws Exception {
        // impostazioni iniziali
        RichiestaLogin richiesta = mock(RichiestaLogin.class);
        when(richiesta.getUsername()).thenReturn("userA");
        when(richiesta.getPassword()).thenReturn("passA");
        // risposta mockata
        RispostaLogin risposta = mock(RispostaLogin.class);
        when(risposta.isSuccesso()).thenReturn(true);
        // utente restituito dal controller (dati che verranno passati al factory)
        Amministratore adminMsg = mock(Amministratore.class);
        when(adminMsg.getId()).thenReturn(33);
        when(adminMsg.getNome()).thenReturn("Marco");
        when(adminMsg.getCognome()).thenReturn("Verdi");
        when(adminMsg.getUserName()).thenReturn("userA");
        when(risposta.getUtente()).thenReturn(adminMsg);

        when(controllerMock.controllaLogin("userA", "passA")).thenReturn(risposta);

        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        when(serverMock.getClients()).thenReturn(clientsMap);

        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);

        // invocazione del metodo
        invokeHandleMessage(richiesta);

        // verifiche
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();

        assertTrue(clientsMap.containsKey(handler));
        assertNotNull(clientsMap.get(handler));
        assertTrue(clientsMap.get(handler) instanceof Amministratore, "L'utente salvato deve essere Amministratore");
    }

    // test per RichiestaDisiscrizioneEvento
    @Test
    void testRichiestaDisiscrizioneEvento_notificaAltriListener_seSuccesso() throws Exception {
        // impostazioni iniziali
        RichiestaDisiscrizioneEvento richiesta = mock(RichiestaDisiscrizioneEvento.class);
        Evento evento = mock(Evento.class);
        Figlio figlio = mock(Figlio.class);
        Genitore genitore = mock(Genitore.class);
        // configuro la richiesta
        when(richiesta.getEvento()).thenReturn(evento);
        when(richiesta.getFiglio()).thenReturn(figlio);
        when(richiesta.getGenitore()).thenReturn(genitore);
        // configuro la risposta del controller
        RispostaDisiscrizioneEvento risposta = mock(RispostaDisiscrizioneEvento.class);
        when(risposta.isSuccesso()).thenReturn(true);
        when(controllerMock.disiscriviFiglioEvento(figlio, evento, genitore)).thenReturn(risposta);
        // preparo listener con id = 42
        Listener listener = mock(Listener.class);
        when(listener.getId()).thenReturn(42);
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);
        // preparo una mappa clients che contiene un altro ClientHandler (mock) con utente id 42
        ClientHandler otherHandler = mock(ClientHandler.class);
        Utente otherUtente = mock(Utente.class);
        when(otherUtente.getId()).thenReturn(42);
        when(otherHandler.getUtente()).thenReturn(otherUtente);
        // inserisco anche il handler reale per evitare NPE
        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        // inserisco sia l'altro handler che il handler reale
        clientsMap.put(otherHandler, otherUtente);
        Utente thisUtente = mock(Utente.class);
        when(thisUtente.getId()).thenReturn(1);
        clientsMap.put(handler, thisUtente);

        when(serverMock.getClients()).thenReturn(clientsMap);

        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);
        setPrivateField(handler, "utente", thisUtente);

        // execuzione del metodo
        invokeHandleMessage(richiesta);

        // verifiche: risposta inviata e notifica inoltrata all'altro handler
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();
        verify(otherHandler).sendMessage(any(NotificaAggiornamentoEvento.class));
    }
}

