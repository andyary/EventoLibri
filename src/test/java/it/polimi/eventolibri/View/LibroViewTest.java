package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Model.Libro;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    // Testa il filtro con un asterisco
    void testFilterConAsterisco() {
        clickOn("#filterTextField").write("Harry*");

        // Aspetta che JavaFX aggiorni il filtro della lista
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();

        // Verifica che la ListView mostri solo il libro "Harry Potter"
        ListView<Libro> listView = lookup("#bookListView").queryAs(ListView.class);
        assertEquals(1, listView.getItems().size());
        assertEquals("Harry Potter", listView.getItems().get(0).getTitolo());
    }
}
