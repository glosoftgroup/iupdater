package com.idealupdater.utils.utils;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Optional;

public class Notify {
    public StackPane stackPane;
    public boolean status = false;

    public void showSuccess(StackPane rootAnchorPane, String title, String message){
        alert(rootAnchorPane, title, message, "success");
    }

    public void showError(StackPane rootAnchorPane, String title, String message){
        alert(rootAnchorPane, title, message, "error");
    }

    public void showInfo(StackPane rootAnchorPane, String title, String message){
        alert(rootAnchorPane, title, message, "info");
    }

    public void alert(StackPane rootAnchorPane, String title, String message, String type){
        JFXDialogLayout content = new JFXDialogLayout();
        Label headerLbl = new Label(title.toUpperCase());
        headerLbl.setPrefWidth(330);

        // the close button on the header
        JFXButton closeIconBtn = new JFXButton();
        closeIconBtn.setPadding(new Insets(8, 16, 8, 17));
        closeIconBtn.getStyleClass().add("close-icon");
        // add the close icon to the button
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setIcon(FontAwesomeIcon.TIMES);
        icon.setSize("17px");
        icon.setStyle("-fx-fill: white;");
        closeIconBtn.setGraphic(icon);

        HBox hbox = new HBox();
        hbox.setPrefWidth(content.getPrefWidth());
        hbox.getChildren().add(headerLbl);
        hbox.getChildren().add(closeIconBtn);

        content.setHeading(hbox);
        content.setBody(new Label(message));

        JFXDialog dialog = new JFXDialog(rootAnchorPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton closeBtn = new JFXButton("CLOSE");
        closeBtn.getStyleClass().add("dialog-button");
        Label spaceLabel = new Label();
        spaceLabel.setPrefWidth(content.getPrefWidth());
        spaceLabel.setPrefWidth(140);

        closeIconBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });

        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });

        content.setActions(closeBtn, spaceLabel);
        content.getStylesheets().add("/styles/alerts/"+type+".css");
        dialog.setOverlayClose(false);
        dialog.show();
    }



    public boolean AlertConfirm(StackPane stackPane, String msg, String Title){
        JFXAlert<Boolean> alert = new JFXAlert<>((Stage) stackPane.getScene().getWindow());
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Label(Title));
        content.setBody(new Label(msg));

        content.getStylesheets().add("/styles/alerts/info.css");

        JFXButton closeButton = new JFXButton("CLOSE");
        closeButton.getStyleClass().add("close-button");

        JFXButton yesButton = new JFXButton("YES");
        yesButton.getStyleClass().add("dialog-button");

        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event)->{
            status = false;
            alert.close();
        });
        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event)->{
            status = true;
            alert.close();
        });

        content.setActions(closeButton, yesButton);
        alert.setContent(content);
        alert.showAndWait();
        return status;
    }

    public void CreateAlert(String msg, String Title,Alert.AlertType type){
        Platform.runLater(()->{
            Alert alert=new Alert(type);
            alert.setHeaderText(msg);
            alert.setTitle(Title);
            alert.showAndWait();
        });
    }

    public boolean CreateConfirmDialog(String msg, String Title){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Title);
        alert.setHeaderText(msg);
        Optional<ButtonType> results=alert.showAndWait();
        if(results.get()==ButtonType.OK){
            return true;
        }
        return false;
    }

    public int CreateInputDialogue(){
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Change Quantity");
        dialog.setHeaderText("Change product Quantity");
        dialog.setContentText("Enter new Value:");
        dialog.getEditor().setOnKeyTyped(e->{
            if(!e.getCharacter().matches("[0-9]")){
                e.consume();
            }
        });
        Optional<String> result = dialog.showAndWait();

        if(result.isPresent())
            return Integer.parseInt(result.get());

        return 0;

    }
}
