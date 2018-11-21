package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class TestWindowController implements Initializable {
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "MainWindow";
    @FXML JFXButton btn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info(LOG_TAG, "event", "test window controller", "message",
                "initialize test window controller");
    }

    @FXML
    public void doSomething(){
        btn.setText("Test Just Button Clicked");
    }
}
