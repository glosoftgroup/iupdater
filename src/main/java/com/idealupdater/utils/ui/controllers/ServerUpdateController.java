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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ServerUpdateController implements Initializable {
    public static ServerUpdateController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ServerUpdateController";
    private ArrayList<String> newFeatures = new ArrayList<>();

    @FXML JFXProgressBar progressBar;
    @FXML VBox featureVBox;
    @FXML TextArea consoleField;
    @FXML public JFXButton updateBtn, revertBtn;
    @FXML AnchorPane mainAnchorPane;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "Server Update Controller", "message",
                "initialize ServerUpdateController");

        // adjust the screen accordingly
        mainAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        mainAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());

        progressBar.setVisible(true);

        String remoteVesion = getRemoteVersion();
        String localVersion = getLocalVersion();

        if(!remoteVesion.equals(localVersion)){
            // if updates available, show the updates page
            progressBar.setVisible(false);

            Platform.runLater(()->{
                featureVBox.getChildren().clear();
                Label versionLabel = new Label(remoteVesion);
                versionLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 16px;");
                featureVBox.getChildren().add(versionLabel);
                newFeatures.stream().forEach(obj -> {
                    Label label = new Label("➝ " + obj);
                    label.setStyle("-fx-font-size: 16px;");
                    featureVBox.getChildren().add(label);
                });
            });

        }else{
            // if no updates available show the no updates page
            progressBar.setVisible(false);
        }
    }

    public String getLocalVersion(){
        String localVersion = null;
        try {
            String appPath = Prefs.getInstance().getLocalServerFile();
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

            JSONArray feaureArray = (JSONArray) jsonObject.get("changedfiles");
            newFeatures.clear();
            feaureArray.stream().forEach(ob -> newFeatures.add(ob.toString()) );
        }catch (Exception e){
            logger.error(LOG_TAG, "event","extracting_downloaded_file_obj", "error", e.getMessage());
        }

        return remoteVersion;
    }

    public void appendText(String str) {
        Platform.runLater(() -> consoleField.appendText(str));
    }

    @FXML
    public void performUpdate(){
        consoleField.clear();
        progressBar.setVisible(true);
        updateBtn.setDisable(true);
        revertBtn.setDisable(true);
        try {

            Process p = Runtime.getRuntime().exec("ping localhost -n 6");
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
            /** run the console output as a thread to avoid the system inactivity and reduce the delay time before you
             *  see the output *
             **/
            Thread T = new Thread(new Runnable(){
                /** the outputLineFromCommand string variable disables the skipping of readlines */
                String outputLineFromCommand;
                @Override
                public void run() {
                    try {
                        while ((outputLineFromCommand = inputStream.readLine()) != null) {
                            appendText(String.valueOf(outputLineFromCommand + "\n"));
                        }
                        progressBar.setVisible(false);
                        updateBtn.setDisable(false);
                        revertBtn.setVisible(true);
                        revertBtn.setDisable(false);
                    } catch (IOException e) {
                        logger.error(LOG_TAG, "event","writing_out_put_to_textarea", "error", e.getMessage());
                    }
                }
            });

            T.start();
        }catch (IOException ex){
            logger.error(LOG_TAG, "event","perform_update", "error", ex.getMessage());
        }
    }

    @FXML
    public void performRevert(){
        // revert the update process
    }

}
