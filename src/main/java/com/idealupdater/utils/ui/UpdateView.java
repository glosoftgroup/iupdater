package com.idealupdater.utils.ui;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.controllers.UpdateViewController;
import com.idealupdater.utils.utils.Prefs;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javax.swing.*;
import java.util.Optional;

public class UpdateView {
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ViewUpdate";

    public void launch(){
        JFrame window = new JFrame();
        JFXPanel jfxPanel = new JFXPanel();

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UpdateView.fxml"));
                Parent root = loader.load();
                jfxPanel.setScene(new Scene(root));

                SwingUtilities.invokeLater(() -> {
                    window.add(jfxPanel);
                    window.pack();
                    window.setLocationRelativeTo(null); // centers the window
                    window.setVisible(true);
                });
            } catch (Exception e) {
                logger.error(LOG_TAG, "event", "show main window", "custom_message", e.getMessage());
            }
        });

        /* JFrame on close action */
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                UpdateViewController.instance = null;
            }
        });
    }

}
