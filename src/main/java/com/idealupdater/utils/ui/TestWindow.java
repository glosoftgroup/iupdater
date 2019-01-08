package com.idealupdater.utils.ui;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TestWindow extends Application {
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "TestWindow";


    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/views/TestWindow.fxml"));
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Test Window");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void main() {
        try {
            start(new Stage());
        } catch (IOException ex) {
            logger.error(LOG_TAG, "event", "launching stage", "custom_message", ex.getMessage());
        }
    }


}
