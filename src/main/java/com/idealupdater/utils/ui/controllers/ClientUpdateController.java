package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.ApplicationUtilities;
import com.idealupdater.utils.utils.Prefs;
import com.idealupdater.utils.utils.animation.PulseTransition;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientUpdateController implements Initializable {
    public static ClientUpdateController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ClientUpdateController";

    @FXML JFXProgressBar progressBar;
    @FXML AnchorPane mainAnchorPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "ClientUpdateController", "message",
                "initialize ClientUpdateController");
        // adjust the screen accordingly
        mainAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        mainAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());
    }



}
