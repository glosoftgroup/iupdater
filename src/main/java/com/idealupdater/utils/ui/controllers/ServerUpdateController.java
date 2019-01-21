package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.FileDownloader;
import com.idealupdater.utils.utils.FileUtils;
import com.idealupdater.utils.utils.Notify;
import com.idealupdater.utils.utils.Prefs;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ServerUpdateController implements Initializable {
    public static ServerUpdateController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ServerUpdateController";
    private ArrayList<String> newFeatures = new ArrayList<>();
    BufferedReader inputStream = null;

    @FXML JFXProgressBar progressBar;
    @FXML VBox featureVBox;
    @FXML TextArea consoleField;
    @FXML public JFXButton updateBtn, revertBtn;
    @FXML AnchorPane mainAnchorPane;
    @FXML
    StackPane rootAnchorPane;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "Server Update Controller", "message",
                "initialize ServerUpdateController");

        // adjust the screen accordingly
        rootAnchorPane.prefHeightProperty().bind(MainViewController.instance.holderPane.heightProperty());
        rootAnchorPane.prefWidthProperty().bind(MainViewController.instance.holderPane.widthProperty());

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

    public JSONObject getFileObject(String appPath){
        JSONObject fileJsonObject = new JSONObject();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(appPath));
            fileJsonObject =  (JSONObject) obj;
        } catch (FileNotFoundException e) {
            logger.error(LOG_TAG, "event","returning_file_obj", "error", e.getMessage());
        } catch (IOException e) {
            logger.error(LOG_TAG, "event","returning_file_obj", "error", e.getMessage());
        } catch (ParseException e) {
            logger.error(LOG_TAG, "event","returning_file_obj", "error", e.getMessage());
        }
        return fileJsonObject;
    }

    public String getLocalVersion(){
        String localVersion = null;
        try {
            String appPath = Prefs.getInstance().getLocalServerFile();
            File f = new File(appPath);
            if (f.exists() && f.isFile()) {
                logger.info(LOG_TAG, "event", "Read_local_config_file_path", "custom_message",
                        "file exists", "path", appPath);

                try {
                    JSONParser parser = new JSONParser();
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

        String backendConfigFilePath = Prefs.getInstance().getLocalServerFile();
        String appPath = Prefs.getInstance().getLocalServerPath() +"\\app";
        String pythonExe = Prefs.getInstance().getLocalServerPath() +"\\py-dist\\python-2.7.10\\python.exe";

        try {
            /* git pull process */
            Process gitProcess = Runtime.getRuntime().exec(
                    "cmd /c \"cd "+appPath+" && cmd /c git pull 2>&1\"");
            BufferedReader gitInputStream = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));
            /* make migrations process */
            Process makeMigrationsProcess = Runtime.getRuntime().exec(
                    "cmd /c \"cd "+appPath+" && cmd /c \""+pythonExe+"\" manage.pyc makemigrations 2>&1\"");
            BufferedReader makeMigrationsInputStream = new BufferedReader(new InputStreamReader(makeMigrationsProcess.getInputStream()));
            /* migrate process */
            Process migrateProcess = Runtime.getRuntime().exec(
                    "cmd /c \"cd "+appPath+" && cmd /c \""+pythonExe+"\" manage.pyc migrate 2>&1\"");
            BufferedReader migrateInputStream = new BufferedReader(new InputStreamReader(migrateProcess.getInputStream()));
            /* npm install process */
            Process npmProcess = Runtime.getRuntime().exec(
                    "cmd /c \"cd "+appPath+" && cmd /c npm install 2>&1\"");
            BufferedReader npmInputStream = new BufferedReader(new InputStreamReader(npmProcess.getInputStream()));
            /* yarn build-assets */
            Process yarnBuildProcess = Runtime.getRuntime().exec("cmd /c \"cd "+appPath+" && cmd /c yarn build-assets\"");
            BufferedReader yarnBuildInputStream = new BufferedReader(new InputStreamReader(yarnBuildProcess.getInputStream()));

            Thread T = new Thread(new Runnable(){
                /** the outputLineFromCommand string variable disables the skipping of readlines */
                String outputLineFromCommand;
                @Override
                public void run() {
                    try {

                        appendText("Starting the update process ... \n");
                        /* write git pull stream to textarea */
                        appendText("Getting the new updates ... \n");
                        while ((outputLineFromCommand = gitInputStream.readLine()) != null) {
                            appendText(String.valueOf(outputLineFromCommand + "\n"));
                        }
                        appendText("\nMaking migrations ... \n");
                        /* write make-migrations stream to textarea */
                        while ((outputLineFromCommand = makeMigrationsInputStream.readLine()) != null) {
                            appendText(String.valueOf(outputLineFromCommand + "\n"));
                        }
                        appendText("\nPersisting migrations ... \n");
                        /* write migrate stream to textarea */
                        while ((outputLineFromCommand = migrateInputStream.readLine()) != null) {
                            appendText(String.valueOf(outputLineFromCommand + "\n"));
                        }

                        /* write npm install stream to textarea */
                        appendText("\nUpdating packages ... \n");
                        while ((outputLineFromCommand = npmInputStream.readLine()) != null) {
                            appendText(String.valueOf(outputLineFromCommand + "\n"));
                        }
                        /* write yarn build assets stream to textarea */
                        appendText("\nBundling packages ... \n");
                        while ((outputLineFromCommand = yarnBuildInputStream.readLine()) != null) {
                            appendText(String.valueOf(outputLineFromCommand + "\n"));
                        }
                        appendText("Ending the update process! \n");

                        progressBar.setVisible(false);
                        updateBtn.setDisable(false);
                        revertBtn.setVisible(true);
                        revertBtn.setDisable(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(LOG_TAG, "event","writing_out_put_to_textarea", "error", e.getMessage());
                        appendText("Error fetching updates! Please contact the Administrator. \n");
                    }
                }
            });

            T.start();
        }catch (IOException ex){
            ex.printStackTrace();
            logger.error(LOG_TAG, "event","perform_update", "error", ex.getMessage());
        }


    }

    @FXML
    public void performRevert(){
        // revert the update process
        new Notify().CreateConfirmDialog("Go back the previous version", "Revert Update");
    }

}
