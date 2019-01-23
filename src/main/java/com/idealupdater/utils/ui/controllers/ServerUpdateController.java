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

public class ServerUpdateController implements Initializable {
    public static ServerUpdateController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ServerUpdateController";
    private ArrayList<AnchorPane> viewAnchorPanes = new ArrayList<>();

    BufferedReader gitInputStream;
    BufferedReader makeMigrationsInputStream;
    BufferedReader migrateInputStream;
    BufferedReader npmInputStream;
    BufferedReader yarnBuildInputStream;


    @FXML JFXProgressBar progressBar;
    @FXML VBox featureVBox;
    @FXML TextArea consoleField;
    @FXML public JFXButton updateBtn, revertBtn, refreshUpdatesBtn;
    @FXML AnchorPane mainAnchorPane, updatesAnchorPane, searchAnchorPane, noUpdatesAnchorPane;
    @FXML StackPane rootAnchorPane;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        logger.info(LOG_TAG, "event", "Server Update Controller", "message",
                "initialize ServerUpdateController");

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
            String url = Prefs.getInstance().getRemoteServerPath();
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

            try {
                /** check if the server is running and kill it */
                String pid = ApplicationUtilities.getProcessIdFromFile(Prefs.getInstance().getLocalServerPath() +
                        "/py-dist/proc.txt");
                Boolean serverProcessIsRunning = ApplicationUtilities.isProcessIdRunning(pid);
                if(serverProcessIsRunning){
                    if(new Notify().CreateConfirmDialog("Server Status", "Shutting down the server to apply the updates")) {
                        ApplicationUtilities.killProcessId(pid);
                    }
                }
            }catch(IOException ex){ ex.printStackTrace(); }

            consoleField.clear();
            progressBar.setVisible(true);
            updateBtn.setDisable(true);
            revertBtn.setDisable(true);
            refreshUpdatesBtn.setDisable(true);

            JSONObject remoteObject = getRemoteJSONObject();

            String appPath = Prefs.getInstance().getLocalServerPath() +"\\app";
            String pythonExe = Prefs.getInstance().getLocalServerPath() +"\\py-dist\\python-2.7.10\\python.exe";


            /** set streams to null to reset their state */
            gitInputStream = null;
            makeMigrationsInputStream = null;
            migrateInputStream = null;
            npmInputStream = null;
            yarnBuildInputStream = null;

            try {
                /** git pull process */
                Process gitProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd " + appPath + " && cmd /c git pull 2>&1\"");
                gitInputStream = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));

                /** check if migrations were changed */
                if ((Boolean) remoteObject.get("migrations")) {
                    /** make migrations process */
                    Process makeMigrationsProcess = Runtime.getRuntime().exec(
                            "cmd /c \"cd " + appPath + " && cmd /c \"" + pythonExe +
                                    "\" manage.pyc makemigrations 2>&1\"");
                    makeMigrationsInputStream = new BufferedReader(new InputStreamReader(makeMigrationsProcess.getInputStream()));
                    /** migrate process */
                    Process migrateProcess = Runtime.getRuntime().exec(
                            "cmd /c \"cd " + appPath + " && cmd /c \"" + pythonExe +
                                    "\" manage.pyc migrate --fake 2>&1\"");
                    migrateInputStream = new BufferedReader(new InputStreamReader(migrateProcess.getInputStream()));
                }

                /** check if package.json file changed */
                if ((Boolean) remoteObject.get("packagejson")) {
                    /** npm install process */
                    Process npmProcess = Runtime.getRuntime().exec(
                            "cmd /c \"cd " + appPath + " && cmd /c npm install 2>&1\"");
                    npmInputStream = new BufferedReader(new InputStreamReader(npmProcess.getInputStream()));
                }

                /** check if package.json file or webpack.config.js file changed */
                if ((Boolean) remoteObject.get("webpackconfig")) {
                    /** yarn build-assets */
                    Process yarnBuildProcess = Runtime.getRuntime().exec("cmd /c \"cd " + appPath + " && cmd /c yarn build-assets\"");
                    yarnBuildInputStream = new BufferedReader(new InputStreamReader(yarnBuildProcess.getInputStream()));
                }

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

                            /** write make-migrations and migrate streams to textarea */
                            if ((Boolean) remoteObject.get("migrations")) {
                                appendText("\nMaking migrations\n");
                                appendText("-----------------\n");
                                while ((outputLineFromCommand = makeMigrationsInputStream.readLine()) != null) {
                                    appendText(String.valueOf(outputLineFromCommand + "\n"));
                                }

                                /** write migrate stream to textarea */
                                appendText("\nPersisting migrations\n");
                                appendText("---------------------\n");
                                while ((outputLineFromCommand = migrateInputStream.readLine()) != null) {
                                    appendText(String.valueOf(outputLineFromCommand + "\n"));
                                }
                            }

                            /** write npm install stream to textarea */
                            if ((Boolean) remoteObject.get("packagejson")) {
                                appendText("\nUpdating packages\n");
                                appendText("-----------------\n");
                                while ((outputLineFromCommand = npmInputStream.readLine()) != null) {
                                    appendText(String.valueOf(outputLineFromCommand + "\n"));
                                }
                            }

                            /** write yarn build assets stream to textarea */
                            if ((Boolean) remoteObject.get("packagejson") == true || (Boolean) remoteObject.get("webpackconfig") == true) {
                                appendText("\nBundling packages\n");
                                appendText("-----------------\n");
                                while ((outputLineFromCommand = yarnBuildInputStream.readLine()) != null) {
                                    appendText(String.valueOf(outputLineFromCommand + "\n"));
                                }
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
                ex.printStackTrace();
                logger.error(LOG_TAG, "event", "perform_update", "error", ex.getMessage());
            }
        }
    }

    @FXML
    public void performRevert(){
        if(new Notify().CreateConfirmDialog("Go back the previous version?", "Revert Changes")){
            /** check if the server is running and kill it */
            try {
                String pid = ApplicationUtilities.getProcessIdFromFile(Prefs.getInstance().getLocalServerPath() +
                        "/py-dist/proc.txt");
                Boolean serverProcessIsRunning = ApplicationUtilities.isProcessIdRunning(pid);
                if(serverProcessIsRunning){
                    if(new Notify().CreateConfirmDialog("Server Status", "Shutting down the server to apply the updates")) {
                        ApplicationUtilities.killProcessId(pid);
                    }
                }
            }catch(IOException ex){ ex.printStackTrace(); }

            // do git revert
            JSONObject remoteObject = getRemoteJSONObject();

            consoleField.clear();
            progressBar.setVisible(true);
            updateBtn.setDisable(true);
            revertBtn.setDisable(true);
            refreshUpdatesBtn.setDisable(true);

            String appPath = Prefs.getInstance().getLocalServerPath() +"\\app";
            String pythonExe = Prefs.getInstance().getLocalServerPath() +"\\py-dist\\python-2.7.10\\python.exe";

            // set streams to null to reset their state
            gitInputStream = null;
            makeMigrationsInputStream = null;
            migrateInputStream = null;
            npmInputStream = null;
            yarnBuildInputStream = null;

            try {
                /* git pull process */
                Process gitProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd "+appPath+" && cmd /c git reset --hard " +
                                remoteObject.get("commithash").toString()+" 2>&1\"");
                gitInputStream = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));

                /** check if migrations were changed
                    make migrations process
                 */
                Process makeMigrationsProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd " + appPath + " && cmd /c \"" +
                                pythonExe + "\" manage.pyc makemigrations 2>&1\"");
                makeMigrationsInputStream = new BufferedReader(new InputStreamReader(makeMigrationsProcess.getInputStream()));

                /** migrate process */
                Process migrateProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd " + appPath + " && cmd /c \"" + pythonExe +
                                "\" manage.pyc migrate --fake 2>&1\"");
                migrateInputStream = new BufferedReader(new InputStreamReader(migrateProcess.getInputStream()));

                /** check if package.json file changed
                    npm install process
                 */
                Process npmProcess = Runtime.getRuntime().exec(
                        "cmd /c \"cd " + appPath + " && cmd /c npm install 2>&1\"");
                npmInputStream = new BufferedReader(new InputStreamReader(npmProcess.getInputStream()));

                /** check if package.json file or webpack.config.js file changed
                    yarn build-assets
                 */
                Process yarnBuildProcess = Runtime.getRuntime().exec("cmd /c \"cd " + appPath +
                        " && cmd /c yarn build-assets\"");
                yarnBuildInputStream = new BufferedReader(new InputStreamReader(yarnBuildProcess.getInputStream()));

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

                            /** write make-migrations and migrate streams to textarea */
                            appendText("\nMaking migrations\n");
                            appendText("-----------------\n");
                            while ((outputLineFromCommand = makeMigrationsInputStream.readLine()) != null) {
                                appendText(String.valueOf(outputLineFromCommand + "\n"));
                            }

                            /** write migrate stream to textarea */
                            appendText("\nPersisting migrations\n");
                            appendText("---------------------\n");
                            while ((outputLineFromCommand = migrateInputStream.readLine()) != null) {
                                appendText(String.valueOf(outputLineFromCommand + "\n"));
                            }

                            /** write npm install stream to textarea */
                            appendText("\nUpdating packages\n");
                            appendText("-----------------\n");
                            while ((outputLineFromCommand = npmInputStream.readLine()) != null) {
                                appendText(String.valueOf(outputLineFromCommand + "\n"));
                            }

                            /** write yarn build assets stream to textarea */
                            appendText("-----------------\n");
                            while ((outputLineFromCommand = yarnBuildInputStream.readLine()) != null) {
                                appendText(String.valueOf(outputLineFromCommand + "\n"));
                            }appendText("\nBundling packages\n");

                            appendText("END THE REVERT PROCESS! \n");
                        } catch (IOException e) {
                            e.printStackTrace();
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
