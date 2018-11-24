package com.idealupdater.utils.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

public class FileUtils {

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

        // TODO: 1. read json file
        // TODO: 2. parse data form map

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filename));

            jsonObject = (JSONObject) obj;

        } catch (Exception e) {

        }

        return jsonObject;

    }


}
