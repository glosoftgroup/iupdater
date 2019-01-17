package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.Notify;
import com.idealupdater.utils.utils.Prefs;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    public static SettingsController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "SettingsController";

    @FXML JFXComboBox updateTimeoutCbx;
    @FXML public JFXButton clientBrowseBtn, serverBrowseBtn, saveConfigBtn;
    @FXML JFXProgressBar progressBar;
    @FXML Label headerLabel, clientDirectoryPathLabel, serverDirectoryPathLabel;
    @FXML AnchorPane mainAnchorPane;
    @FXML StackPane rootAnchorPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "SettingsController", "message",
                "initialize SettingsController");

        // adjust the screen accordingly
        rootAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        rootAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());

        // set the local installation paths
        serverDirectoryPathLabel.setText( Prefs.getInstance().getLocalServerPath());
        clientDirectoryPathLabel.setText(Prefs.getInstance().getLocalClientPath());

        updateTimeoutCbx.getItems().addAll("30 Min","1 Day","1 Week",
                "1 Month");
        updateTimeoutCbx.getSelectionModel().select(getTimeout());
    }

    @FXML
    public void saveConfig(){
        String serverPath = serverDirectoryPathLabel.getText();
        String clientPath = clientDirectoryPathLabel.getText();

        if(serverPath != null){
            Prefs.getInstance().setLocalServerPath(serverPath);
        }

        if(clientPath != null){
            Prefs.getInstance().setLocalClientPath(clientPath);
        }

        Prefs.getInstance().setTimeOut(process_timeout(updateTimeoutCbx.getSelectionModel().getSelectedIndex()));
        new Notify().showSuccess(rootAnchorPane, "Success", "Settings saved successfully!");
    }

    @FXML
    public void chooseServerDirectory(){
        try {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            File file = directoryChooser.showDialog(headerLabel.getScene().getWindow());

            if(file != null && file.isDirectory()){
                // check for log file inside of the path
                File f = new File(file.getAbsolutePath()+"/backend_updater_config.json");
                if (f.exists() && f.isFile()) {
                    Platform.runLater(()->{
                        serverDirectoryPathLabel.setText(file.getAbsolutePath());
                    });
                }else{
                    new Notify().showError(rootAnchorPane, "Error", "Incorrect server installation folder");
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @FXML
    public void chooseClientDirectory(){
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(headerLabel.getScene().getWindow());

        if(file != null && file.isDirectory()){
            // check for log file inside of the path
            File f = new File(file.getAbsolutePath()+"/frontend_updater_config.json");
            if (f.exists() && f.isFile()) {
                Platform.runLater(()->{
                    clientDirectoryPathLabel.setText(file.getAbsolutePath());
                });
            }else{
                new Notify().showError(rootAnchorPane, "Error", "Incorrect client installation folder");
            }
        }
    }

    private int process_timeout(int index){
        int value = 60;
        switch (index){
            case 0:
                value = 30 * 60 * 1000;
                break;
            case 1:
                value = 24 * 60 * 60 * 1000;
                break;
            case 2:
                value = 7 * 24 * 60 * 60 * 1000;
                break;
            case 3:
                Calendar c = Calendar.getInstance();
                int monthMaxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                value = monthMaxDays * 24 * 60 * 60 * 1000;
                break;
        }
        return value;
    }

    private int getTimeout(){
        int index = 0;
        int value = new Prefs().getTimeOut();
        switch(value){
            case 30 * 60 * 1000: index = 0; break;
            case 24 * 60 * 60 * 1000: index = 1; break;
            case 7 * 24 * 60 * 60 * 1000: index = 2; break;
            case ((28 * 24 * 60 * 60 * 1000) | (29 * 24 * 60 * 60 * 1000) | (30 * 24 * 60 * 60 * 1000) |
                    (31 * 24 * 60 * 60 * 1000)): index = 3; break;
            default: index = 0;
        }
        return index;
    }


}
