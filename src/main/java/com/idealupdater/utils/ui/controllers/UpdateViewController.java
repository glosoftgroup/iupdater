package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.FileDownloader;
import com.idealupdater.utils.utils.FileUtils;
import com.idealupdater.utils.utils.Prefs;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class UpdateViewController implements Initializable {
    public static UpdateViewController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "CheckUpdate";
    @FXML public JFXButton statusBtn, clientUpdateBtn, serverUpdateBtn, settingsBtn, updateBtn;
    @FXML AnchorPane headerAnchorPane, statusAnchorPane, updatesAnchorPane, noUpdatesAnchorPane,
            searchUpdatesAnchorPane, settingsAnchorPane;
    @FXML Label headerLabel;
    @FXML JFXProgressBar progressBar;
    private ArrayList<JFXButton> sidebarBtns = new ArrayList<>();
    private ArrayList<AnchorPane> anchorPanes = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "updateView controller", "message",
                "initialize updateView controller");
        // the sidebar buttons array list allows one to set specific button active while the rest remain inactive
        sidebarBtns.add(statusBtn);
        sidebarBtns.add(clientUpdateBtn);
        sidebarBtns.add(serverUpdateBtn);
        sidebarBtns.add(settingsBtn);

        // the anchor panes array list allows one to set specific pane visible while the rest remain invisible
        anchorPanes.add(statusAnchorPane);
        anchorPanes.add(updatesAnchorPane);
        anchorPanes.add(noUpdatesAnchorPane);
        anchorPanes.add(searchUpdatesAnchorPane);
        anchorPanes.add(settingsAnchorPane);

        // get the clicked button from the side nav, default is status
        fireBtnEvent(Prefs.getInstance().getSideBarTargetBtn());
    }

    @FXML
    public void checkStatus(Event evt){
        changeActiveClass(evt);
        headerLabel.setText("Status");
        progressBar.setVisible(false);

        setActivePane(statusAnchorPane.getId());
    }
    @FXML
    public void checkClientUpdates(Event evt){
        changeActiveClass(evt);
        headerLabel.setText("Client Updates");
        progressBar.setVisible(true);
        setActivePane(searchUpdatesAnchorPane.getId());
    }

    @FXML
    public void checkServerUpdates(Event evt){
        changeActiveClass(evt);
        headerLabel.setText("Server Updates");

        // load the searching anchor pane first
        searchUpdatesAnchorPane.setVisible(true);
        progressBar.setVisible(true);

        String remoteVesion = getRemoteVersion();
        String localVersion = getLocalVersion();

        if(!remoteVesion.equals(localVersion)){
            // if updates available, show the updates page
            progressBar.setVisible(false);
            setActivePane(updatesAnchorPane.getId());
        }else{
            // if no updates available show the no updates page
            progressBar.setVisible(false);
            setActivePane(noUpdatesAnchorPane.getId());
        }

    }

    @FXML
    public void settings(Event evt){
        changeActiveClass(evt);
        headerLabel.setText("Settings");
        progressBar.setVisible(false);
        setActivePane(settingsAnchorPane.getId());
    }

    public void changeActiveClass(Event evt) {
        JFXButton btn = (JFXButton) evt.getSource();
        String name = btn.getText();

        Platform.runLater(() -> {
            for (JFXButton button : sidebarBtns) {
                if (button.getText().contains(name)) {
                    // if the button does not have the active class then add the class to it
                    if(!button.getStyleClass().contains("sidebarBtn-active")) {
                        button.getStyleClass().add("sidebarBtn-active");
                    }
                } else {
                    // if the button has the active class then remove the class from it
                    if(button.getStyleClass().contains("sidebarBtn-active")) {
                        button.getStyleClass().remove("sidebarBtn-active");
                    }
                }
            }
        });
    }

    public void setActivePane(String paneName) {
            for (AnchorPane pane : anchorPanes) {
                if (pane.getId().contains(paneName)) {
                    pane.setVisible(true);
                } else {
                    pane.setVisible(false);
                }
            }
    }

    public void fireBtnEvent(String targetName){
        Platform.runLater(() -> {
            for (JFXButton button : sidebarBtns) {
                if (button.getText().toLowerCase().contains(targetName)) {
                    // this simulates the action event when fired
                    button.fire();
                }
            }
        });
    }

    public String getLocalVersion(){
        String localVersion = null;
        try {
            String appPath = Prefs.getInstance().getLocalServerPath();
            File f = new File(appPath);
            if (f.exists() && f.isFile()) {
                logger.info(LOG_TAG, "event", "Read_local_config_file_path", "custom_message",
                        "file exists", "path", appPath);

                JSONParser parser = new JSONParser();
                try {
                    Object obj = parser.parse(new FileReader(appPath));
                    JSONObject fileJsonObject =  (JSONObject) obj;
                    // set the localVersion from the local file
                    localVersion = (String) fileJsonObject.get("version");
                    logger.info(LOG_TAG, "event", "Reading_local_file_obj", "custom_message",
                            "fetching local version", "version", localVersion);
                } catch (FileNotFoundException e) {
                    logger.error(LOG_TAG, "event","Reading_local_file_obj", "error", e.getMessage());
                } catch (IOException e) {
                    logger.error(LOG_TAG, "event","Reading_local_file_obj", "error", e.getMessage());
                } catch (ParseException e) {
                    logger.error(LOG_TAG, "event","Reading_local_file_obj", "error", e.getMessage());
                }
            }else{
                logger.info(LOG_TAG, "event", "Read_local_config_file_path", "custom_message",
                        "file not exist", "path", appPath);
            }
        }catch (Exception e){
            logger.error(LOG_TAG, "event","extracting_downloaded_file_obj", "error", e.getMessage());
        }

        return localVersion;
    }

    public String getRemoteVersion(){
        String remoteVersion = null;
        try {
            String url = Prefs.getInstance().getRemoteServerPath();
            String result = FileDownloader.downloadFromUrl(
                    new URL(url),
                    "frontend_updater_config.json");

            JSONObject jsonObject = new FileUtils().readConfigJsonObj(result);
            remoteVersion = (String) jsonObject.get("version");
            logger.info(LOG_TAG, "event", "Read_remote_config_file", "custom_message",
                    "fetching remote version", "version", remoteVersion);
        }catch (Exception e){
            logger.error(LOG_TAG, "event","extracting_downloaded_file_obj", "error", e.getMessage());
        }

        return remoteVersion;
    }
}
