package com.idealupdater.services;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.utils.FileDownloader;
import com.idealupdater.utils.utils.FileUtils;
import org.json.simple.JSONObject;

import java.net.URL;

public class CheckVersionWorker implements Runnable{
    public static final Logger logger = LoggerFactory.getLogger(CheckVersionWorker.class);
    public static final String LOG_TAG = "CheckVersionWorker";

    @Override
    public void run() {
        logger.info(LOG_TAG, "event","check_version", "custom_message", "checking the version");
        try {
            String result = FileDownloader.downloadFromUrl(
                    new URL("https://github.com/glosoftgroup/updaterconfig/blob/master/frontend_updater_config.json"),
                    "frontend_updater_config.json");

            JSONObject jsonObject = new FileUtils().readConfigJsonObj(result);
            String version = (String) jsonObject.get("version");


        }catch (Exception e){

        }
    }
}
