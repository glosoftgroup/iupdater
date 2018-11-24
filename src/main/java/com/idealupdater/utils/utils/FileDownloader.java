package com.idealupdater.utils.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/***
 * This class is used to download file and store locally give name
 * Returns : output file path
 * see usage below
 *
 * Usage
 *
 * <code>
 *      String result = FileDownloader.downloadFromUrl(new URL("http://google.de/images/path/image.png"),
 *      "image.png");
 *      File file = new File(result);
 *      // To delete the file
 *      Boolean fileExists = file.exists();
 * </code>
 *
 * */

public class FileDownloader {

    public void  FileDownloader(){}

    public static String downloadFromUrl(URL url, String saveAsLocalFileName){
        InputStream ins = null;
        FileOutputStream fos = null;

        String tempDir = System.getProperty("user.dir");
        String outputPath = tempDir + "/tmp/" + saveAsLocalFileName;

        try {
            URLConnection urlConnection = url.openConnection();
            ins = urlConnection.getInputStream();

            fos = new FileOutputStream(outputPath);

            byte[] buf = new byte[4096];
            int length;
            while ((length = ins.read(buf)) > 0){
                fos.write(buf, 0, length);
            }

        } catch (Exception e){

        } finally {
            try {
                if (ins != null) ins.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputPath;

    }


}
