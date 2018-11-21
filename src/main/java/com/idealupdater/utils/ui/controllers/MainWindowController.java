package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.ui.TestWindow;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "MainWindow";
    @FXML JFXButton test_btn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info(LOG_TAG, "event", "main window controller", "message",
                "initialize main window controller");
    }

    @FXML
    public void doSomething(){
        /* display the tray icon message */
        new SystemTrayUtils().displayMessage("new Test", SystemTrayUtils.Level.INFO);

        /* call the test window */
        new TestWindow().main();
    }
}
