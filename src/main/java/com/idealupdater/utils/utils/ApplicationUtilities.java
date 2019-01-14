package com.idealupdater.utils.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ApplicationUtilities {
    public static void runApplication(String applicationFilePath) throws IOException, InterruptedException {
        File application = new File(applicationFilePath);
        String applicationName = application.getName();

        if (!isProcessRunning(applicationName)) {
            Desktop.getDesktop().open(application);
        }
    }

    public static boolean isProcessRunning(String processName) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("tasklist.exe");
        Process process = processBuilder.start();
        String tasksList = toString(process.getInputStream());
        System.err.println("task list " + tasksList);

        return tasksList.contains(processName);
    }

    private static String toString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
        String string = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        return string;
    }

    public static void closeApplication(String processName) throws IOException, InterruptedException {
        String KILL = "taskkill /F /IM ";
        if (isProcessRunning(processName)){
            Runtime.getRuntime().exec(KILL + "\""+processName+"\"");
        }
    }
}
