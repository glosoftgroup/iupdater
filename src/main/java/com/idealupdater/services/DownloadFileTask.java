package com.idealupdater.services;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

/***
 * This class is used with runnable task in thread and used with progress bar
 * see usage below
 *
 * Usage
 *
 * <code>
 *  Task<Void> task = new DownloadTask("https://some.url.com/path/file/location");
 *  ProgressBar progressBar = new ProgressBar();
 *  progressBar.setPrefWidth(350);
 *  progressBar.progressProperty().bind(task.progressProperty());
 *  root.getChildren().add(progressBar);
 *  Thread thread = new Thread(task);
 *  thread.setDaemon(true);
 *  thread.start();
 * </code>
 *
 * */

public class DownloadFileTask extends Task<Void> {
    public static final Logger logger = LoggerFactory.getLogger(DownloadFileTask.class);
    public static final String LOG_TAG = "DownloadFileTask";

    private String url;

    public DownloadFileTask(String url){
        this.url = url;
    }


    @Override
    protected Void call() throws Exception {
        InputStream in = null;
        OutputStream os = null;
        try {

            logger.info(LOG_TAG, "event", "Download_file", "message",
                    "Downloading file with url "+ url);

            String ext = url.substring(url.lastIndexOf("."), url.length());
            URLConnection connection = new URL(url).openConnection();
            long fileLength = connection.getContentLength();

            in = connection.getInputStream();
            os = Files.newOutputStream(Paths.get("downloadedfile" + ext));

            long nread = 0L;
            byte[] buf = new byte[8192];
            int n;

            while ((n = in.read(buf)) > 0){
                os.write(buf, 0, n);
                nread += n;
                updateProgress(nread, fileLength);
            }

        } catch (Exception e){
            logger.error(LOG_TAG, "event", "Download_file_error", "message",
                    "Downloading file error occurred");
        } finally {
            try {
                if (in != null) in.close();
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                in.close();
                os.close();
            }
        }

        return null;
    }

    @Override
    protected void failed(){

    }


    @Override
    protected  void succeeded(){

    }
}
