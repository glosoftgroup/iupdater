package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.ApplicationUtilities;
import com.idealupdater.utils.utils.FileDownloader;
import com.idealupdater.utils.utils.FileUtils;
import com.idealupdater.utils.utils.Prefs;
import com.idealupdater.utils.utils.animation.PulseTransition;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;

public class StatusController implements Initializable {
    public static StatusController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "StatusController";

    @FXML Label headerLabel, clientStatusLabel, serverStatusLabel;
    @FXML FontAwesomeIconView clientStatusIcon, serverStatusIcon;
    @FXML JFXToggleButton clientToggleBtn, serverToggleBtn;
    @FXML AnchorPane mainAnchorPane;

    PulseTransition clientIconStatusTrans;
    PulseTransition serverIconStatusTrans;
    private String pid = null;
    private Thread thread = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "status View controller", "message",
                "initialize status View controller");

        // adjust the screen accordingly
        mainAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        mainAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());

        // setup the toggle buttons listeners
        setupToggleBtnListener(clientToggleBtn, true);
        setupToggleBtnListener(serverToggleBtn, false);

        // thread to check the status
        startStatusCheckThread();
    }

    private void stopStatusCheckThread(){
        if(thread != null){
            thread.stop();
            thread = null;
        };
    }

    private void startStatusCheckThread(){
        thread = new Thread(() -> {
            while(true) {
                checkStatusWithNoEventParam();
                try {
                    thread.sleep(5000);

                } catch(InterruptedException e) { e.printStackTrace(); }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void checkStatusWithNoEventParam(){

        try {
            // if client is running
            Boolean clientProcessIsRunning = ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe");
            clientToggleBtn.setSelected(clientProcessIsRunning);

            // if server is running
            pid = ApplicationUtilities.getProcessIdFromFile(Prefs.getInstance().getLocalServerPath() +
                    "/py-dist/proc.txt");
            Boolean serverProcessIsRunning = ApplicationUtilities.isProcessIdRunning(pid);
            serverToggleBtn.setSelected(serverProcessIsRunning);
        }catch(IOException|InterruptedException ex){
            ex.printStackTrace();
        }
    }

    public void setupToggleBtnListener(JFXToggleButton toggleBtn, Boolean isClient){
        toggleBtn.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(toggleBtn.isSelected() == true){
                    if(isClient){
                        try {
                            Platform.runLater(()->{
                                stopStatusCheckThread();
                                try {
                                    ApplicationUtilities.runApplication(
                                            Prefs.getInstance().getLocalClientPath()+"\\ClassicPOS Client.exe");

                                    Thread.sleep(10000);

                                    // check if process name is running
                                    if(ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe")){
                                        toggleBtn.setText("ON");
                                        clientStatusLabel.setText("Client Status: ON");
                                        clientStatusIcon.getStyleClass().add("active-node-icon");
                                        clientIconStatusTrans = startNodePulseAnimation(clientStatusIcon);
                                        clientIconStatusTrans.play();
                                    }else{
                                        toggleBtn.setSelected(false);
                                        toggleBtn.setText("OFF");
                                    }
                                }catch (IOException|InterruptedException ex){ ex.printStackTrace(); }
                                startStatusCheckThread();
                            });

                        }catch(Exception ex){ ex.printStackTrace(); }

                    }else{
                        try {
                            if(ApplicationUtilities.isProcessIdRunning(pid)){
                                // if the processId is already running then change the status to on
                                Platform.runLater(()->{
                                    toggleBtn.setText("ON");
                                    serverStatusLabel.setText("Server Status: ON");
                                    serverStatusIcon.getStyleClass().add("active-node-icon");
                                    serverIconStatusTrans = startNodePulseAnimation(serverStatusIcon);
                                    serverIconStatusTrans.play();
                                });
                            }else{
                                stopStatusCheckThread();
                                // if the process id is not running, run the server application and check if
                                // it is running and the change the status, else let the status be OFF
                                ApplicationUtilities.runServerApplication(Prefs.getInstance().getLocalServerPath(), pid);
                                // run at some later time
                                Platform.runLater(()->{
                                    // introduce a delay of 10 seconds so that the pid can be read again
                                    try{
                                        Thread.sleep(10000);
                                    }catch (InterruptedException ex){ ex.printStackTrace(); }

                                    try {
                                        pid = ApplicationUtilities.getProcessIdFromFile(
                                                Prefs.getInstance().getLocalServerPath() +
                                                "/py-dist/proc.txt");

                                        // if the process id is running then set as required else set toggle false and
                                        // text OFF
                                        if (ApplicationUtilities.isProcessIdRunning(pid)) {
                                            toggleBtn.setText("ON");
                                            serverStatusLabel.setText("Server Status: ON");
                                            serverStatusIcon.getStyleClass().add("active-node-icon");
                                            serverIconStatusTrans = startNodePulseAnimation(serverStatusIcon);
                                            serverIconStatusTrans.play();
                                        } else {
                                            toggleBtn.setSelected(false);
                                            toggleBtn.setText("OFF");
                                        }
                                    }catch (IOException ex){ ex.printStackTrace(); }
                                    startStatusCheckThread();
                                });
                            }
                        }catch(IOException|InterruptedException ex){
                            ex.printStackTrace();
                        }
                    }
                }else{
                    if(isClient){
                        // stop the pulse animation
                        if(clientIconStatusTrans!=null) clientIconStatusTrans.stop();

                        try {
                            // run sometime later
                            Platform.runLater(()->{
                                try {
                                    stopStatusCheckThread();
                                    ApplicationUtilities.closeApplication("ClassicPOS Client.exe");
                                    // a delay to make sure the application has been closed
                                    Thread.sleep(10000);

                                    if (!ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe")) {
                                        toggleBtn.setText("OFF");
                                        clientStatusLabel.setText("Client Status: OFF");
                                        clientStatusIcon.getStyleClass().remove("active-node-icon");
                                    } else {
                                        toggleBtn.setText("ON");
                                    }
                                }catch(IOException|InterruptedException ex){
                                    ex.printStackTrace();
                                }
                                startStatusCheckThread();
                            });

                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }else{
                        // stop the pulse animation
                        if(serverIconStatusTrans!=null) serverIconStatusTrans.stop();

                        try {
                            if(pid != null) {
                                // run sometime later
                                Platform.runLater(()->{
                                    try {
                                        stopStatusCheckThread();
                                        ApplicationUtilities.killProcessId(pid);
                                        // a delay to make sure the application has been closed
                                        Thread.sleep(10000);

                                        if(!ApplicationUtilities.isProcessIdRunning(pid)){
                                            toggleBtn.setText("OFF");
                                            serverStatusLabel.setText("Server Status: OFF");
                                            serverStatusIcon.getStyleClass().remove("active-node-icon");
                                        }else{
                                            toggleBtn.setText("ON");
                                        }
                                    }catch(Exception ex){
                                        ex.printStackTrace();
                                    }
                                    startStatusCheckThread();
                                });
                            }
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public PulseTransition startNodePulseAnimation(Node node){
        PulseTransition ts = new PulseTransition(node);
        ts.setCycleCount(Timeline.INDEFINITE);
        ts.setAutoReverse(false);
        return ts;
    }

}
