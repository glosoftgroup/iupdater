package com.idealupdater.utils.ui.controllers;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.ApplicationUtilities;
import com.idealupdater.utils.utils.FileDownloader;
import com.idealupdater.utils.utils.FileUtils;
import com.idealupdater.utils.utils.Prefs;
import com.idealupdater.utils.utils.animation.PulseTransition;
import com.jfoenix.controls.*;
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
import sun.tools.jar.CommandLine;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

public class UpdateViewController implements Initializable {
    public static UpdateViewController instance;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "CheckUpdate";
    @FXML public JFXButton statusBtn, clientUpdateBtn, serverUpdateBtn, settingsBtn, updateBtn, revertBtn,
            clientBrowseBtn, serverBrowseBtn, saveConfigBtn;
    @FXML AnchorPane headerAnchorPane, statusAnchorPane, updatesAnchorPane, noUpdatesAnchorPane,
            searchUpdatesAnchorPane, settingsAnchorPane;
    @FXML Label headerLabel, clientStatusLabel, serverStatusLabel, clientDirectoryPathLabel, serverDirectoryPathLabel;
    @FXML JFXProgressBar progressBar;
    @FXML VBox featureVBox;
    @FXML TextArea consoleField;
    @FXML FontAwesomeIconView clientStatusIcon, serverStatusIcon;
    @FXML JFXToggleButton clientToggleBtn, serverToggleBtn;
    @FXML JFXComboBox updateTimeoutCbx, serverVesionCbx, clientVesionCbx;
    private ArrayList<JFXButton> sidebarBtns = new ArrayList<>();
    private ArrayList<AnchorPane> anchorPanes = new ArrayList<>();
    private ArrayList<String> newFeatures = new ArrayList<>();
    PulseTransition clientIconStatusTrans;
    PulseTransition serverIconStatusTrans;
    private String pid = null;

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
        revertBtn.setVisible(false);

        // setup the toggle buttons listeners
        setupToggleBtnListener(clientToggleBtn, true);
        setupToggleBtnListener(serverToggleBtn, false);

        // set the local installation paths
        serverDirectoryPathLabel.setText( Prefs.getInstance().getLocalServerPath());
        clientDirectoryPathLabel.setText(Prefs.getInstance().getLocalClientPath());

        updateTimeoutCbx.getItems().addAll("30 Min","1 Day","1 Week",
                "1 Month", "3 Sec");
        updateTimeoutCbx.getSelectionModel().select(getTimeout());

    }

    @FXML
    public void checkStatus(Event evt){
        changeActiveClass(evt);
        headerLabel.setText("Status");
        progressBar.setVisible(false);

        setActivePane(statusAnchorPane.getId());

        try {
            // if client is running
            if(ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe"))
            {
                clientToggleBtn.setSelected(true);
            }else{
                clientToggleBtn.setSelected(false);
            }

            // if server is running
            pid = ApplicationUtilities.getProcessIdFromFile(Prefs.getInstance().getLocalServerPath() +
                    "/py-dist/proc.txt");

            if (ApplicationUtilities.isProcessIdRunning(pid)) {
                serverToggleBtn.setSelected(true);
            } else {
                serverToggleBtn.setSelected(false);
            }
        }catch(IOException|InterruptedException ex){
            ex.printStackTrace();
        }

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
                    Alert al = new Alert(Alert.AlertType.ERROR);
                    al.setTitle("Error");
                    al.setHeaderText("Incorrect installation folder");
                    al.show();
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
                Alert al = new Alert(Alert.AlertType.ERROR);
                al.setTitle("Error");
                al.setHeaderText("Incorrect installation folder");
                al.show();
            }
        }
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
            ex.printStackTrace();
        }
    }

    @FXML
    public void performRevert(){
        // revert the update process
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

    public void setupToggleBtnListener(JFXToggleButton toggleBtn, Boolean isClient){
        toggleBtn.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(toggleBtn.isSelected()==true){
                    toggleBtn.setText("ON");
                    if(isClient){
                        try {
                            ApplicationUtilities.runApplication("C:\\Program Files (x86)\\" +
                                    "ClassicPOS Client\\ClassicPOS Client\\ClassicPOS Client.exe");

                            if(ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe")){
                                clientStatusLabel.setText("Client Status: ON");
                                clientStatusIcon.getStyleClass().add("active-node-icon");
                                clientIconStatusTrans = startNodePulseAnimation(clientStatusIcon);
                                clientIconStatusTrans.play();
                            }else{
                                toggleBtn.setSelected(false);
                                toggleBtn.setText("OFF");
                            }
                        }catch(IOException|InterruptedException ex){
                            ex.printStackTrace();
                        }

                    }else{
                        try {
                            ApplicationUtilities.runApplication("C:\\Program Files (x86)" +
                                    "\\ClassicPOS Server\\ClassicPOS Server\\ClassicPOS Server.exe");
                            if(ApplicationUtilities.isProcessIdRunning(pid)){
                                serverStatusLabel.setText("Server Status: ON");
                                serverStatusIcon.getStyleClass().add("active-node-icon");
                                serverIconStatusTrans = startNodePulseAnimation(serverStatusIcon);
                                serverIconStatusTrans.play();
                            }else{
                                toggleBtn.setSelected(false);
                                toggleBtn.setText("OFF");
                            }
                        }catch(IOException|InterruptedException ex){
                            ex.printStackTrace();
                        }
                    }
                }else{
                    if(isClient){
                        clientStatusLabel.setText("Client Status: OFF");
                        clientStatusIcon.getStyleClass().remove("active-node-icon");
                        // stop the pulse animation
                        if(clientIconStatusTrans!=null) clientIconStatusTrans.stop();

                        try {
                            ApplicationUtilities.closeApplication("ClassicPOS Client.exe");
                            if(!ApplicationUtilities.isProcessRunning("ClassicPOS Client.exe")){
                                toggleBtn.setText("OFF");
                            }
                        }catch(IOException|InterruptedException ex){
                            ex.printStackTrace();
                        }
                    }else{
                        serverStatusLabel.setText("Server Status: OFF");
                        serverStatusIcon.getStyleClass().remove("active-node-icon");
                        // stop the pulse animation
                        if(serverIconStatusTrans!=null) serverIconStatusTrans.stop();

                        try {
                            if(pid != null) {
                                ApplicationUtilities.killProcessId(pid);
                                if(!ApplicationUtilities.isProcessIdRunning(pid)){
                                    toggleBtn.setText("OFF");
                                }
                            }
                        }catch(IOException ex){
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

    @FXML
    public void saveConfig(){

        String serverPath = serverDirectoryPathLabel.getText();
        String clientPath = clientDirectoryPathLabel.getText();

        if(serverPath != null){
            Prefs.getInstance().setLocalServerPath(serverPath);
        }

        if(clientPath != null){
            Prefs.getInstance().setLocalClientPath(serverPath);
        }

        Prefs.getInstance().setTimeOut(process_timeout(updateTimeoutCbx.getSelectionModel().getSelectedIndex()));
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
            case 4:
                value = 3000;
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
            case ((28 * 24 * 60 * 60 * 1000) | (29 * 24 * 60 * 60 * 1000) | (30 * 24 * 60 * 60 * 1000) | (31 * 24 * 60 * 60 * 1000)):
                index = 3;
                break;
            case 3000: index = 4; break;
            default: index = 0;
        }
        return index;
    }

}
