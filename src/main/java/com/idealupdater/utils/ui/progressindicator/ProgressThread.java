package com.idealupdater.utils.ui.progressindicator;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class ProgressThread extends Thread {
    RingProgressIndicator rpi;
    Label rpi_label;
    int progress;
    public ProgressThread(RingProgressIndicator rpi, Label rpi_label){
        this.rpi = rpi;
        this.rpi_label = rpi_label;
        progress = 0;
    }

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(100);
            }catch(InterruptedException ex){
                System.out.println(ex);
            }
            Platform.runLater(()->{
                rpi.setProgress(progress);
            });

            if(progress == 10){
                // do something at this interval
            };

            progress ++;
            /**
             * progress > 100 ensures the while loop runs only once
             * This is also very true for every action there is a reaction
             *
             *
             * */
            if(progress>100){
                Platform.runLater(()-> {
                    rpi_label.setText("No Updates found");
                });
                break;
            }

        }

    }
}

