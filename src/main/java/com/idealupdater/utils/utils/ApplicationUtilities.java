package com.idealupdater.utils.utils;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.SystemTrayUtils;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApplicationUtilities {
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "ApplicationUtilities";

    public static void runApplication(String applicationFilePath) throws IOException, InterruptedException {
        File application = new File(applicationFilePath);
        String applicationName = application.getName();

        if (!isProcessRunning(applicationName)) {
            Desktop.getDesktop().open(application);
        }
    }

    public static void runServerApplication(String applicationFilePath, String processId) throws IOException, InterruptedException {
        // cd into the installation directory and call the exe file
        String command = "cmd /c \"cd "+applicationFilePath+" && \"ClassicPOS Server.exe\"\"";

        if (!isProcessIdRunning(processId)){
            Runtime.getRuntime().exec(command);
        }
    }

    public static boolean isProcessRunning(String processName) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("tasklist.exe");
        Process process = processBuilder.start();
        String tasksList = toString(process.getInputStream());

        return tasksList.contains(processName);
    }

    public static boolean isProcessIdRunning(String pid) {
        String command = "cmd /c \"tasklist /FI \"PID eq " + pid + "\" | findstr " + pid + "\"";
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);

            InputStreamReader isReader = new InputStreamReader(pr.getInputStream());
            BufferedReader bReader = new BufferedReader(isReader);
            String strLine = null;
            while ((strLine= bReader.readLine()) != null) {
                if (strLine.contains(" " + pid + " ")) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            logger.error(LOG_TAG, "event", "Got exception using system command [{}]",
                    "command", command, "exception", ex.getMessage());
            return true;
        }
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

    /**
     * Executes batch file for task kill process.
     * @param  processId the application process to kill
     * The bat file is executed with the paramater of the processId
     *  <pre>
     *       String processId = 12345;
     *       Runtime.getRuntime().exec("C:/User/Desktop/killProcessId.bat" +" " +processId);
     *  </pre>
     */
    public static void killProcessId(String processId) throws IOException {
        if (isProcessIdRunning(processId)){
            logger.info(LOG_TAG, "event", "checking if process id ["+processId+"] is running",
                    "command", Prefs.getInstance().getLocalServerPath() +
                            "/py-dist/killProcessId.bat" +" " +processId);

            Runtime.getRuntime().exec(
                    Prefs.getInstance().getLocalServerPath() +
                            "/py-dist/killProcessId.bat" +" " +processId);
        }
    }

    public static String getProcessIdFromFile(String procIdFilePath) throws IOException {
        String pid = null;
        String line;
        List<String> records = new ArrayList<>();
        File procIdFile = new File(procIdFilePath);

        if(procIdFile != null && procIdFile.isFile()) {
            BufferedReader reader = new BufferedReader(
                    new FileReader(procIdFilePath));
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
            pid = records.get(0);
        }
        return pid;
    }
}
