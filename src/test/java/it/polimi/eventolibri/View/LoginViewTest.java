package it.polimi.eventolibri.View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class LoginViewTest {

    // Sostituisci con la tua interfaccia/servizio reale
    public interface AuthService {
        boolean authenticate(String username, String password);
    }

    // mock creato subito per poterlo iniettare nel controller nel metodo @Start
    private final AuthService authService = mock(AuthService.class);
    private Parent root;

    @BeforeAll
    static void headless() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_view.fxml"));
        loader.setControllerFactory(controllerClass -> {
            try {
                Object controller = controllerClass.getDeclaredConstructor().newInstance();
                // prova a iniettare il mock se il controller ha un campo 'authService'
                try {
                    Field f = controllerClass.getDeclaredField("authService");
                    f.setAccessible(true);
                    f.set(controller, authService);
                } catch (NoSuchFieldException ignored) {
                }
                return controller;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @Test
    void testSuccessfulLogin(FxRobot robot) {
        when(authService.authenticate("user", "pass")).thenReturn(true);

        robot.clickOn("#usernameField").write("user");
        robot.clickOn("#passwordField").write("pass");
        robot.clickOn("#loginButton");

        verify(authService).authenticate("user", "pass");

        Label error = robot.lookup("#errorLabel").queryAs(Label.class);
        assertNotNull(error);
        assertFalse(error.isVisible(), "L'etichetta errore non dovrebbe essere visibile su login riuscito");
    }

    @Test
    void testFailedLoginShowsError(FxRobot robot) {
        when(authService.authenticate("user", "bad")).thenReturn(false);

        robot.clickOn("#usernameField").write("user");
        robot.clickOn("#passwordField").write("bad");
        robot.clickOn("#loginButton");

        verify(authService).authenticate("user", "bad");

        Label error = robot.lookup("#errorLabel").queryAs(Label.class);
        assertNotNull(error);
        assertTrue(error.isVisible());
        assertTrue(error.getText().length() > 0);
    }

    @Test
    void testEmptyCredentialsDoNotCallService(FxRobot robot) {
        robot.clickOn("#loginButton");

        verify(authService, never()).authenticate(anyString(), anyString());

        Label error = robot.lookup("#errorLabel").queryAs(Label.class);
        assertNotNull(error);
        assertTrue(error.isVisible());
    }
}
