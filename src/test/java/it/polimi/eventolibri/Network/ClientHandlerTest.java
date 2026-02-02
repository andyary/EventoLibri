package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Controller.Controller;
import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test di unità per ClientHandler.
 * - I campi privati vengono sostituiti con mock tramite reflection per isolare la classe.
 * - I messaggi passano attraverso handleMessage (metodo privato invocato via reflection) per verificare i comportamenti.
 */
@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {

    @Mock
    Socket socketMock;

    @Mock
    Server serverMock;

    @Mock
    Controller controllerMock;

    // catturiamo l'oggetto scritto su ObjectOutputStream nelle verifiche
    @Captor
    ArgumentCaptor<Object> objectCaptor;

    ClientHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        // creazione handler con socket e server mock (controller verrà iniettato via reflection)
        handler = new ClientHandler(socketMock, serverMock);

        // inietto il controller mock nel campo privato 'controller'
        setPrivateField(handler, "controller", controllerMock);
    }

    // Utility per impostare campi privati via reflection
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // Utility per invocare il metodo privato handleMessage
    private void invokeHandleMessage(Messaggio msg) throws Exception {
        java.lang.reflect.Method m = ClientHandler.class.getDeclaredMethod("handleMessage", Messaggio.class);
        m.setAccessible(true);
        m.invoke(handler, msg);
    }

    @Test
    void testRichiestaNextEventi_scriveRisposta() throws Exception {
        // --- Arrange ---
        RichiestaNextEventi richiesta = mock(RichiestaNextEventi.class);
        Evento ultimo = mock(Evento.class);
        when(richiesta.getUltimoEvento()).thenReturn(ultimo);

        RispostaNextEventi risposta = mock(RispostaNextEventi.class);
        when(controllerMock.getNextEventi(ultimo)).thenReturn(risposta);

        // preparo un ObjectOutputStream mock per intercettare l'oggetto inviato
        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);

        // --- Act ---
        invokeHandleMessage(richiesta);

        // --- Assert ---
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();
    }

    @Test
    void testCloseUI_rimuoveClient_e_chiudeSocket() throws Exception {
        // --- Arrange ---
        CloseUI close = mock(CloseUI.class);

        // preparo una mappa reale per i clients e inserisco il handler
        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        clientsMap.put(handler, null);
        when(serverMock.getClients()).thenReturn(clientsMap);

        // rendiamo il socketMock verificabile su close()
        doNothing().when(socketMock).close();

        // out non necessario per questo test ma evitiamo NPE impostandolo
        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);

        // --- Act ---
        invokeHandleMessage(close);

        // --- Assert ---
        assertFalse(clientsMap.containsKey(handler), "Handler dovrebbe essere rimosso dalla mappa clients");
        verify(socketMock).close();
    }

    @Test
    void testRichiestaIscrizioneEvento_notificaAltriListener_seSuccesso() throws Exception {
        // --- Arrange ---
        RichiestaIscrizioneEvento richiesta = mock(RichiestaIscrizioneEvento.class);
        Evento evento = mock(Evento.class);
        Figlio figlio = mock(Figlio.class);
        Genitore genitore = mock(Genitore.class);

        when(richiesta.getEvento()).thenReturn(evento);
        when(richiesta.getFiglio()).thenReturn(figlio);
        when(richiesta.getGenitore()).thenReturn(genitore);

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

        // impostiamo un Utente non nullo per il handler reale così da evitare NPE durante il test
        Utente thisUtente = mock(Utente.class);
        when(thisUtente.getId()).thenReturn(1); // id diverso da 42
        clientsMap.put(handler, thisUtente);

        when(serverMock.getClients()).thenReturn(clientsMap);

        // impostiamo out per il nostro handler (evitiamo NPE)
        setPrivateField(handler, "out", mock(ObjectOutputStream.class));
        // iniettiamo anche il campo utente nel handler reale (coerenza)
        setPrivateField(handler, "utente", thisUtente);

        // --- Act ---
        invokeHandleMessage(richiesta);

        // --- Assert ---
        // Verifichiamo che l'altro handler abbia ricevuto la notifica di aggiornamento evento
        verify(otherHandler).sendMessage(any(NotificaAggiornamentoEvento.class));
    }

    @Test
    void testSendMessage_serializzaSenzaEccezioni() throws Exception {
        // --- Arrange ---
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream realOut = new ObjectOutputStream(baos);
        setPrivateField(handler, "out", realOut);

        Messaggio dummy = mock(Messaggio.class);

        // --- Act & Assert: non deve lanciare eccezioni
        handler.sendMessage(dummy);

        // controlliamo che qualcosa sia stato scritto nel buffer
        assertTrue(baos.size() > 0);
    }

    // java
    @Test
    void testRichiestaLogin_genitore_aggiungeClientEMandaRisposta() throws Exception {
        // Arrange
        RichiestaLogin richiesta = mock(RichiestaLogin.class);
        when(richiesta.getUsername()).thenReturn("userG");
        when(richiesta.getPassword()).thenReturn("passG");

        RispostaLogin risposta = mock(RispostaLogin.class);
        when(risposta.isSuccesso()).thenReturn(true);

        // utente restituito dal controller (dati che verranno passati al factory)
        Genitore genMsg = mock(Genitore.class);
        when(genMsg.getId()).thenReturn(11);
        when(genMsg.getNome()).thenReturn("Paolo");
        when(genMsg.getCognome()).thenReturn("Rossi");
        when(genMsg.getUserName()).thenReturn("userG");
        when(risposta.getUtente()).thenReturn(genMsg);

        when(controllerMock.controllaLogin("userG", "passG")).thenReturn(risposta);

        // mappa reale per verificare il put
        HashMap<ClientHandler, Utente> clientsMap = new HashMap<>();
        when(serverMock.getClients()).thenReturn(clientsMap);

        // mock out per intercettare sendMessage
        ObjectOutputStream outMock = mock(ObjectOutputStream.class);
        setPrivateField(handler, "out", outMock);

        // Act
        invokeHandleMessage(richiesta);

        // Assert
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();

        assertTrue(clientsMap.containsKey(handler));
        assertNotNull(clientsMap.get(handler));
        assertTrue(clientsMap.get(handler) instanceof Genitore, "L'utente salvato deve essere Genitore");
    }

    @Test
    void testRichiestaLogin_lettore_aggiungeClientEMandaRisposta() throws Exception {
        // Arrange
        RichiestaLogin richiesta = mock(RichiestaLogin.class);
        when(richiesta.getUsername()).thenReturn("userL");
        when(richiesta.getPassword()).thenReturn("passL");

        RispostaLogin risposta = mock(RispostaLogin.class);
        when(risposta.isSuccesso()).thenReturn(true);

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

        // Act
        invokeHandleMessage(richiesta);

        // Assert
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();

        assertTrue(clientsMap.containsKey(handler));
        assertNotNull(clientsMap.get(handler));
        assertTrue(clientsMap.get(handler) instanceof Lettore, "L'utente salvato deve essere Lettore");
    }

    @Test
    void testRichiestaLogin_amministratore_aggiungeClientEMandaRisposta() throws Exception {
        // Arrange
        RichiestaLogin richiesta = mock(RichiestaLogin.class);
        when(richiesta.getUsername()).thenReturn("userA");
        when(richiesta.getPassword()).thenReturn("passA");

        RispostaLogin risposta = mock(RispostaLogin.class);
        when(risposta.isSuccesso()).thenReturn(true);

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

        // Act
        invokeHandleMessage(richiesta);

        // Assert
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();

        assertTrue(clientsMap.containsKey(handler));
        assertNotNull(clientsMap.get(handler));
        assertTrue(clientsMap.get(handler) instanceof Amministratore, "L'utente salvato deve essere Amministratore");
    }

    // java
    @Test
    void testRichiestaDisiscrizioneEvento_notificaAltriListener_seSuccesso() throws Exception {
        // Arrange
        RichiestaDisiscrizioneEvento richiesta = mock(RichiestaDisiscrizioneEvento.class);
        Evento evento = mock(Evento.class);
        Figlio figlio = mock(Figlio.class);
        Genitore genitore = mock(Genitore.class);

        when(richiesta.getEvento()).thenReturn(evento);
        when(richiesta.getFiglio()).thenReturn(figlio);
        when(richiesta.getGenitore()).thenReturn(genitore);

        RispostaDisiscrizioneEvento risposta = mock(RispostaDisiscrizioneEvento.class);
        when(risposta.isSuccesso()).thenReturn(true);
        when(controllerMock.disiscriviFiglioEvento(figlio, evento, genitore)).thenReturn(risposta);

        Listener listener = mock(Listener.class);
        when(listener.getId()).thenReturn(42);
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(listener);
        when(evento.getListeners()).thenReturn(listeners);

        ClientHandler otherHandler = mock(ClientHandler.class);
        Utente otherUtente = mock(Utente.class);
        when(otherUtente.getId()).thenReturn(42);
        when(otherHandler.getUtente()).thenReturn(otherUtente);

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

        // Act
        invokeHandleMessage(richiesta);

        // Assert: risposta inviata e notifica inoltrata all'altro handler
        verify(outMock).writeObject(risposta);
        verify(outMock).flush();
        verify(outMock).reset();
        verify(otherHandler).sendMessage(any(NotificaAggiornamentoEvento.class));
    }

}

