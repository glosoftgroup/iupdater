package com.idealupdater.utils.utils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class Notify {
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
        JFXButton headerBtn = new JFXButton();
        headerBtn.setPadding(new Insets(8, 16, 8, 17));
        headerBtn.getStyleClass().add("close-icon");
        // add the close icon to the button
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setIcon(FontAwesomeIcon.TIMES);
        icon.setSize("17px");
        icon.setStyle("-fx-fill: white;");
        headerBtn.setGraphic(icon);

        HBox hbox = new HBox();
        hbox.setPrefWidth(content.getPrefWidth());
        hbox.getChildren().add(headerLbl);
        hbox.getChildren().add(headerBtn);

        content.setHeading(hbox);
        content.setBody(new Label(message));

        JFXDialog dialog = new JFXDialog(rootAnchorPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("CLOSE");
        button.getStyleClass().add("dialog-button");
        Label lbl = new Label();
        lbl.setPrefWidth(content.getPrefWidth());
        lbl.setPrefWidth(140);

        headerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        dialog.setOnDialogClosed(closeEvent -> {
            System.out.println("I am closed!");
        });

        content.setActions(button, lbl);
        content.getStylesheets().add("/styles/alerts/"+type+".css");
        dialog.setOverlayClose(false);
        dialog.show();
    }
}
