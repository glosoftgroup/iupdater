package com.idealupdater.services;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.FileDownloader;
import com.idealupdater.utils.utils.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class CheckVersionWorker implements Runnable{
    public static final Logger logger = LoggerFactory.getLogger(CheckVersionWorker.class);
    public static final String LOG_TAG = "CheckVersionWorker";

    @Override
    public void run() {
        logger.info(LOG_TAG, "event","check_version", "custom_message", "checking the version");
        try {
            String result = FileDownloader.downloadFromUrl(
                    new URL("https://raw.githubusercontent.com/glosoftgroup/updaterconfig/master/backend_updater_config.json"),
                    "frontend_updater_config.json");

            JSONObject jsonObject = new FileUtils().readConfigJsonObj(result);
            String remoteVersion = (String) jsonObject.get("version");
            logger.info(LOG_TAG, "event", "Read_remote_config_file", "custom_message",
                    "fetching remote version", "version", remoteVersion);
            // search the file dynamically through C:\\ and return the path
            // choose the path that is specified through the settings interface
            String appPath = System.getenv("ProgramFiles(X86)") + "\\ClassicPOS Server\\ClassicPOS Server\\backend_updater_config.json";
            File f = new File(appPath);
            if (f.exists() && f.isFile()) {
                logger.info(LOG_TAG, "event", "Read_local_config_file_path", "custom_message",
                        "file exists", "path", appPath);

                String localVersion = remoteVersion;
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

                // uncomment after solving the run once inside the thread
                if(!remoteVersion.equals(localVersion)){
                    // show the new updates message
                    new SystemTrayUtils().displayMessage("New updates detected", SystemTrayUtils.Level.INFO);
                }
            }else{
                logger.info(LOG_TAG, "event", "Read_local_config_file_path", "custom_message",
                        "file not exist", "path", appPath);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error(LOG_TAG, "event","extracting_downloaded_file_obj", "error", e.getMessage());
        }
    }
}
