package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ClientUpdateController implements Initializable {
    public static ClientUpdateController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ClientUpdateController";
    private ArrayList<AnchorPane> viewAnchorPanes = new ArrayList<>();


    @FXML JFXProgressBar progressBar;
    @FXML VBox featureVBox;
    @FXML TextArea consoleField;
    @FXML public JFXButton updateBtn, revertBtn, refreshUpdatesBtn;
    @FXML AnchorPane mainAnchorPane, updatesAnchorPane, searchAnchorPane, noUpdatesAnchorPane;
    @FXML StackPane rootAnchorPane;

    BufferedReader gitInputStream;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "Client Update Controller", "message",
                "initialize ClientUpdateController");

        // adjust the screen accordingly
        rootAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        rootAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());


        // the anchor panes array list allows one to set specific pane visible while the rest remain invisible
        viewAnchorPanes.add(updatesAnchorPane);
        viewAnchorPanes.add(noUpdatesAnchorPane);
        viewAnchorPanes.add(searchAnchorPane);

        progressBar.setVisible(true);

        checkNewUpdates();
    }

    public JSONObject getLocalJSONObject(){
        JSONObject localJSONObject = new JSONObject();
        try {
            String appPath = Prefs.getInstance().getLocalClientFile();
            File f = new File(appPath);
            if (f.exists() && f.isFile()) {
                logger.info(LOG_TAG, "event", "Read_local_config_file_path", "custom_message",
                        "file exists", "path", appPath);

                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new FileReader(appPath));
                    JSONObject fileJsonObject =  (JSONObject) obj;
                    // set the localVersion from the local file
                    localJSONObject = fileJsonObject;
                    logger.info(LOG_TAG, "event", "Reading_local_file_obj", "custom_message",
                            "fetching local version", "version", localJSONObject.get("version"));
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

        return localJSONObject;
    }

    public JSONObject getRemoteJSONObject(){
        JSONObject remoteJSONObject = new JSONObject();
        try {
            String url = Prefs.getInstance().getRemoteClientPath();
            String result = FileDownloader.downloadFromUrl(
                    new URL(url),
                    "frontend_updater_config.json");

            JSONObject jsonObject = new FileUtils().readConfigJsonObj(result);
            remoteJSONObject = jsonObject;
            logger.info(LOG_TAG, "event", "Read_remote_config_file", "custom_message",
                    "fetching remote version", "version", remoteJSONObject.get("version"));
        }catch (Exception e){
            logger.error(LOG_TAG, "event","extracting_downloaded_file_obj", "error", e.getMessage());
        }

        return remoteJSONObject;
    }

    public void appendText(String str) {
        Platform.runLater(() -> consoleField.appendText(str));
    }

    @FXML
    public void performUpdate(){
        if(new Notify().CreateConfirmDialog("Apply updates?", "New Updates")) {
            /** check if the client is running and kill it */
            try {
                if (ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe")) {
                    if(new Notify().CreateConfirmDialog("Client Status", "Shutting down the client to apply the updates")) {
                        ApplicationUtilities.closeApplication("ClassicPOS Client.exe");
                    }
                }
            }catch(IOException|InterruptedException ex){ ex.printStackTrace(); }

            consoleField.clear();
            progressBar.setVisible(true);
            updateBtn.setDisable(true);
            revertBtn.setDisable(true);
            refreshUpdatesBtn.setDisable(true);

            String appPath = Prefs.getInstance().getLocalClientPath();

            // set streams to null to reset their state
            gitInputStream = null;

            try {
                /** git pull process */
                Process gitProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd " + appPath + " && cmd /c git pull 2>&1\"");
                gitInputStream = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));

                Thread T = new Thread(new Runnable() {
                    /**
                     * the outputLineFromCommand string variable disables the skipping of readlines
                     */
                    String outputLineFromCommand;

                    @Override
                    public void run() {
                        try {

                            appendText("START THE UPDATE PROCESS\n");
                            /** write git pull stream to textarea */
                            appendText("Getting the new updates\n");
                            appendText("-----------------------\n");

                            while ((outputLineFromCommand = gitInputStream.readLine()) != null) {
                                appendText(String.valueOf(outputLineFromCommand + "\n"));
                            }

                            appendText("END THE UPDATE PROCESS! \n");
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.error(LOG_TAG, "event", "writing_out_put_to_textarea", "error", e.getMessage());
                            appendText("Error fetching updates! Please contact the Administrator. \n");
                        }

                        progressBar.setVisible(false);
                        updateBtn.setDisable(false);
                        revertBtn.setVisible(true);
                        revertBtn.setDisable(false);
                        refreshUpdatesBtn.setDisable(false);
                    }
                });

                T.start();
            } catch (IOException ex) {
                logger.error(LOG_TAG, "event", "perform_update", "error", ex.getMessage());
            }
        }
    }

    @FXML
    public void performRevert(){
        if(new Notify().CreateConfirmDialog("Go back the previous version?", "Revert Changes")){
            /** check if the client is running and kill it */
            try {
                if (ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe")) {
                    if(new Notify().CreateConfirmDialog("Client Status", "Shutting down the client to apply the updates")) {
                        ApplicationUtilities.closeApplication("ClassicPOS Client.exe");
                    }
                }
            }catch(IOException|InterruptedException ex){ ex.printStackTrace(); }

            JSONObject remoteObject = getRemoteJSONObject();

            consoleField.clear();
            progressBar.setVisible(true);
            updateBtn.setDisable(true);
            revertBtn.setDisable(true);
            refreshUpdatesBtn.setDisable(true);

            String appPath = Prefs.getInstance().getLocalClientPath();

            // set streams to null to reset their state
            gitInputStream = null;

            try {
                /** git pull process */
                Process gitProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd "+appPath+" && cmd /c git reset --hard " +
                                remoteObject.get("commithash").toString()+" 2>&1\"");
                gitInputStream = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));

                Thread T = new Thread(new Runnable(){
                    /** the outputLineFromCommand string variable disables the skipping of readlines */
                    String outputLineFromCommand;
                    @Override
                    public void run() {
                        try {

                            appendText("START THE REVERT PROCESS\n");
                            /** write git pull stream to textarea */
                            appendText("Getting the old data\n");
                            appendText("--------------------\n");

                            while ((outputLineFromCommand = gitInputStream.readLine()) != null) {
                                appendText(String.valueOf(outputLineFromCommand + "\n"));
                            }

                            appendText("END THE REVERT PROCESS! \n");
                        } catch (IOException e) {
                            logger.error(LOG_TAG, "event","writing_out_put_to_textarea", "error", e.getMessage());
                            appendText("Error fetching previous data! Please contact the Administrator. \n");
                        }

                        progressBar.setVisible(false);
                        updateBtn.setDisable(false);
                        revertBtn.setVisible(true);
                        revertBtn.setDisable(false);
                        refreshUpdatesBtn.setDisable(false);
                    }
                });

                T.start();
            }catch (IOException ex){
                ex.printStackTrace();
                logger.error(LOG_TAG, "event","perform_revert", "error", ex.getMessage());
            }

        }
    }

    @FXML
    public void checkNewUpdates(){
        progressBar.setVisible(true);
        consoleField.clear();
        setActivePane(searchAnchorPane.getId());
        JSONObject remoteObject = getRemoteJSONObject();
        JSONObject localObject = getLocalJSONObject();

        if(!remoteObject.get("version").equals(localObject.get("version"))){
            // if updates available, show the updates page
            progressBar.setVisible(false);
            setActivePane(updatesAnchorPane.getId());

            Platform.runLater(()->{
                featureVBox.getChildren().clear();
                Label versionLabel = new Label("Version: " + remoteObject.get("version").toString());
                versionLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 18px;-fx-padding: 0px 0px 0px 10px");
                featureVBox.getChildren().add(versionLabel);

                if(remoteObject.get("features") != null){
                    JSONArray feaureArray = (JSONArray) remoteObject.get("features");
                    feaureArray.stream().forEach(obj -> {
                        JSONObject jsnObj = (JSONObject) obj;

                        if(jsnObj.get("title") != null && !jsnObj.get("title").toString().isEmpty()) {
                            Label label = new Label("➝ " + jsnObj.get("title").toString());
                            label.setStyle("-fx-font-size: 16px;-fx-font-weight: bold;-fx-padding: 0px 0px 0px 10px");
                            featureVBox.getChildren().add(label);
                        }

                        if(jsnObj.get("description") != null && !jsnObj.get("description").toString().isEmpty()) {
                            Label label = new Label(jsnObj.get("description").toString());
                            label.setStyle("-fx-font-size: 16px;-fx-padding: 0px 0px 0px 20px;");
                            label.setWrapText(true);
                            label.prefWidthProperty().bind(featureVBox.widthProperty());
                            featureVBox.getChildren().add(label);
                        }
                    });
                }

            });

        }else{
            // if no updates available show the no updates page
            progressBar.setVisible(false);
            setActivePane(noUpdatesAnchorPane.getId());
        }
    }

    public void setActivePane(String paneName) {
        for (AnchorPane pane : viewAnchorPanes) {
            if (paneName.contains(pane.getId())) {
                pane.setVisible(true);
            } else {
                pane.setVisible(false);
            }
        }
    }

}
