package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.jfoenix.controls.JFXProgressBar;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientUpdateController implements Initializable {
    public static ClientUpdateController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ClientUpdateController";

    @FXML JFXProgressBar progressBar;
    @FXML AnchorPane mainAnchorPane;
    @FXML
    StackPane rootAnchorPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "ClientUpdateController", "message",
                "initialize ClientUpdateController");
        // adjust the screen accordingly
        rootAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        rootAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());
    }



}
