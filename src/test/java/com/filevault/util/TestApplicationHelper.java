package com.filevault.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Helfer-Klasse für das Testen von JavaFX-Komponenten in einer Headless-Umgebung.
 * Diese Klasse initialisiert die JavaFX-Umgebung für Tests ohne einen angeschlossenen Bildschirm.
 */
public class TestApplicationHelper implements BeforeAllCallback {

    private static volatile boolean initialized = false;
    private static final CountDownLatch initLatch = new CountDownLatch(1);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!initialized) {
            // Erkenne CI-Umgebung
            boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;
            
            if (isCI) {
                System.setProperty("java.awt.headless", "true");
                System.setProperty("testfx.robot", "glass");
                System.setProperty("testfx.headless", "true");
                System.setProperty("prism.order", "sw");
                System.setProperty("prism.text", "t2k");
            }
            
            try {
                // JavaFX-Toolkit starten
                Thread javafxThread = new Thread(() -> {
                    try {
                        // Leere JavaFX-Anwendung starten
                        javafx.application.Application.launch(DummyApplication.class);
                    } catch (Exception e) {
                        // Fehler bei der JavaFX-Initialisierung
                        System.err.println("JavaFX-Initialisierung fehlgeschlagen: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                javafxThread.setDaemon(true);
                javafxThread.start();
                
                // Warten auf die Initialisierung (höchstens 10 Sekunden)
                if (!initLatch.await(10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("JavaFX wurde nicht rechtzeitig initialisiert");
                }
                
                initialized = true;
            } catch (Exception e) {
                System.err.println("Fehler bei der Initialisierung der JavaFX-Umgebung: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Führt eine Aktion im JavaFX-Thread aus und wartet auf deren Abschluss.
     *
     * @param action Die auszuführende Aktion
     * @throws Exception Wenn während der Ausführung Fehler auftreten
     */
    public static void runAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        doneLatch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Dummy-JavaFX-Anwendung für Tests.
     */
    public static class DummyApplication extends javafx.application.Application {
        @Override
        public void start(Stage primaryStage) {
            // Eine leere, minimale JavaFX-Anwendung
            primaryStage.setScene(new Scene(new StackPane(new Label("Test")), 100, 100));
            
            // Die Bühne nicht anzeigen in headless-Umgebungen
            if (!"true".equals(System.getProperty("testfx.headless"))) {
                primaryStage.show();
            }
            
            // Signal, dass JavaFX initialisiert wurde
            initLatch.countDown();
        }
    }
} 