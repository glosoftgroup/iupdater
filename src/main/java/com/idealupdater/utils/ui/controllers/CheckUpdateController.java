package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.progressindicator.ProgressThread;
import com.idealupdater.utils.ui.progressindicator.RingProgressIndicator;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.ui.TestWindow;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CheckUpdateController implements Initializable {
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "CheckUpdate";
    @FXML JFXButton test_btn;
    @FXML Label rpi_label;
    @FXML StackPane stackPane;
    RingProgressIndicator ringProgressIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info(LOG_TAG, "event", "main window controller", "message",
                "initialize main window controller");
        ringProgressIndicator = new RingProgressIndicator();
        ringProgressIndicator.setRingWidth(10);
        ringProgressIndicator.makeIndeterminate();

        stackPane.getChildren().add(ringProgressIndicator);

        ProgressThread pt = new ProgressThread(ringProgressIndicator, rpi_label);
        pt.start();
    }

    @FXML
    public void doSomething(){
        /* display the tray icon message */
//        new SystemTrayUtils().displayMessage("new Test", SystemTrayUtils.Level.INFO);

        /* call the test window */
//        new TestWindow().main();
        // for manual prompt of update
        ProgressThread pt = new ProgressThread(ringProgressIndicator, rpi_label);
        pt.start();
    }
}