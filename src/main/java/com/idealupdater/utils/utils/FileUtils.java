package com.idealupdater.utils.utils;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;

public class FileUtils {
    public static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static final String LOG_TAG = "FileUtils";

    public void FileUtils(){}

    /**
     * Opens and reads a file, and returns the contents as one String.
     */
    public static String readFileAsString(String filename)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        reader.close();
        return sb.toString();
    }



    public JSONObject readConfigJsonObj(String filename) {

        JSONObject jsonObject = null;

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filename));

            jsonObject = (JSONObject) obj;

        } catch (Exception e) {
            logger.error(LOG_TAG, "event","read_config_obj", "error", e.getMessage());
        }

        return jsonObject;

    }


}
