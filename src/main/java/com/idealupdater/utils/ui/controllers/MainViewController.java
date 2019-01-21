package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.Prefs;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    public static MainViewController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "MainViewController";
    private ArrayList<JFXButton> sidebarBtns = new ArrayList<>();

    @FXML public JFXButton statusBtn, clientUpdateBtn, serverUpdateBtn, settingsBtn;
    @FXML public AnchorPane holderPane;

    StackPane statusAnchorPane, clientUpdateAnchorPane, serverUpdateAnchorPane, settingsAnchorPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "mainView controller", "message",
                "initialize mainView controller");

        // get the clicked button from the side nav, default is status
        fireBtnEvent(Prefs.getInstance().getSideBarTargetBtn());

        // the sidebar buttons array list allows one to set specific button active while the rest remain inactive
        sidebarBtns.add(statusBtn);
        sidebarBtns.add(clientUpdateBtn);
        sidebarBtns.add(serverUpdateBtn);
        sidebarBtns.add(settingsBtn);

        //Load all fxmls in a cache
        setupAnchorNodes();
    }

    private void setupAnchorNodes(){
        try {
            statusAnchorPane = FXMLLoader.load(getClass().getResource("/views/Status.fxml"));
            clientUpdateAnchorPane = FXMLLoader.load(getClass().getResource("/views/ClientUpdate.fxml"));
            serverUpdateAnchorPane = FXMLLoader.load(getClass().getResource("/views/ServerUpdate.fxml"));
            settingsAnchorPane = FXMLLoader.load(getClass().getResource("/views/Settings.fxml"));
            setNode(statusAnchorPane);
        } catch (IOException ex) {
            logger.error(LOG_TAG, "event","assigning fxml loaders to anchorpanes", "error", ex.getMessage());
        }
    }

    //Set selected node to a content holder
    private void setNode(Node node) {
        holderPane.getChildren().clear();
        holderPane.getChildren().add(node);

        FadeTransition ft = new FadeTransition(Duration.millis(500));
        ft.setNode(node);
        ft.setFromValue(0.1);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();
    }



    @FXML
    public void checkStatus(Event evt){
        changeActiveClass(evt);
        setNode(statusAnchorPane);
    }

    @FXML
    public void checkClientUpdates(Event evt){
        changeActiveClass(evt);
        setNode(clientUpdateAnchorPane);
    }

    @FXML
    public void checkServerUpdates(Event evt){
        changeActiveClass(evt);
        setNode(serverUpdateAnchorPane);
    }

    @FXML
    public void settings(Event evt){
        changeActiveClass(evt);
        setNode(settingsAnchorPane);
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

}
