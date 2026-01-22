package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Model.Libro;
import it.polimi.eventolibri.Network.Client;
import it.polimi.eventolibri.View.LibroView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibroViewTest extends ApplicationTest {

    private LibroView libroView;
    private Stage stage;

    private List<Libro> libri;


    @Override
    public void start(Stage stage) {
        // Crea alcuni libri di esempio
        libri = new ArrayList<>();
        libri.add(new Libro("Il Signore degli Anelli", 15, "link1.com", "F. Scott Fitzgerald", 1));
        libri.add(new Libro("Harry Potter", 12, "link2.com", "George Orwell", 2));
        libri.add(new Libro("Le Cronache di Narnia", 14, "link3.com", "Harper Lee", 3));


        // Inizializzazione della classe da testare
        libroView = new LibroView();
        libroView.showNonBlocking(stage, libri);
        this.stage = stage;
    }


    @Test
    void testFilterWithWildcard() {
        clickOn("#filterTextField").write("Harry*");

        // Aspetta che JavaFX aggiorni il filtro della lista
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();

        // Verifica che la ListView mostri solo il libro "Harry Potter"
        ListView<Libro> listView = lookup("#bookListView").queryAs(ListView.class);
        assertEquals(1, listView.getItems().size());
        assertEquals("Harry Potter", listView.getItems().get(0).getTitolo());
    }
//
//    @Test
//    void testSelectLibro() {
//
//        ListView<Libro> listView = lookup(".list-view").query();
//        clickOn(listView.lookupAll(".list-cell").stream().filter(node -> node instanceof ListCell && ((ListCell<Libro>) node).getItem() != null && ((ListCell<Libro>) node).getItem().getTitolo().equals("Il Signore degli Anelli")).findFirst().get());
//
//        // Clicca sul pulsante "Conferma"
//        clickOn("Conferma");
//
//        // Verifica che il libro selezionato sia "Il Signore degli Anelli"
//        Libro libroSelezionato = libroView.show(stage, libri);
//        assertNotNull(libroSelezionato);
//        assertEquals("Il Signore degli Anelli", libroSelezionato.getTitolo());
//    }
//
//    @Test
//    void testCancelSelection() {
//
//
//        // Clicca sul pulsante "Annulla"
//        clickOn("Annulla");
//
//        // Verifica che nessun libro sia stato selezionato
//        Libro libroSelezionato = libroView.show(stage, libri);
//        assertNull(libroSelezionato, "Nessun libro dovrebbe essere selezionato quando si clicca Annulla");
//    }
//
//    @Test
//    void testBuildPatternFromFilter() {
//        // Test per metodo interno di utilità
//        Pattern pattern = libroView.buildPatternFromFilter("har*");
//        assertTrue(pattern.matcher("harry").matches());
//        assertTrue(pattern.matcher("harold").matches());
//        assertFalse(pattern.matcher("potter").matches());
//
//        pattern = libroView.buildPatternFromFilter(null);
//        assertNull(pattern, "Il pattern dovrebbe essere null se il filtro è vuoto.");
//    }
}
