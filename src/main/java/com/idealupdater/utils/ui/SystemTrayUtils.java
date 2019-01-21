package com.idealupdater.utils.ui;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.ui.controllers.MainViewController;
import com.idealupdater.utils.utils.Prefs;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;

public class SystemTrayUtils {

    private static SystemTrayUtils instance = null;
    public static final Logger logger = LoggerFactory.getLogger(SystemTrayUtils.class);
    public static final String LOG_TAG = "SystemTrayUtils";
    public static TrayIcon trayIcon =
            new TrayIcon(createImage("etraybulb.gif", "tray icon"));

    public SystemTrayUtils() {
        trayIcon.setImageAutoSize(true);
    }

    public enum Level {
        INFO,
        ERROR,
        WARNING
    }

    public static SystemTrayUtils getInstance () {
        if (instance == null) {
            instance = new SystemTrayUtils();
        }
        return instance;
    }

    public void viewTray() {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            logger.error(LOG_TAG, "event", "Not supported", "message", "SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem statusItem = new MenuItem("Status");

        Menu displayMenu = new Menu("Display");
        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem infoItem = new MenuItem("Info");
        MenuItem noneItem = new MenuItem("None");

        // Frontend Menu
        Menu frontEndMenu = new Menu("Client");
        MenuItem frUpdatesItem = new MenuItem("Check Updates");
        MenuItem frLogsItem = new MenuItem("Logs");

        // Backend Menu
        Menu backEndMenu = new Menu("Server");
        MenuItem bkUpdatesItem = new MenuItem("Check Updates");
        Menu bkLogsItem = new Menu("Logs");
        MenuItem bkWebAppLogItem = new MenuItem("Web App");
        MenuItem bkPackagerLogItem = new MenuItem("Packager");

        MenuItem settingsItem = new MenuItem("Settings");
        MenuItem exitItem = new MenuItem("Exit");


        //Add components to display menu item
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        displayMenu.add(infoItem);
        displayMenu.add(noneItem);

        //Add components to display frontend item
        frontEndMenu.add(frUpdatesItem);
        frontEndMenu.addSeparator();
        frontEndMenu.add(frLogsItem);

        //Add components to backend menu item
        backEndMenu.add(bkUpdatesItem);
        backEndMenu.addSeparator();
        backEndMenu.add(bkLogsItem);
        bkLogsItem.add(bkWebAppLogItem);
        bkLogsItem.addSeparator();
        bkLogsItem.add(bkPackagerLogItem);

        //Add components to popup menu
        popup.add(displayMenu);
        popup.add(statusItem);
        popup.add(frontEndMenu);
        popup.add(backEndMenu);
        popup.add(settingsItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            logger.error(LOG_TAG, "event", "add_trayIcon", "custom_message", e.getMessage());
            return;
        }
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new MainView().launch();

            }
        });


        statusItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Prefs.getInstance().setSideBarTargetBtn("status");
                    launchUpdateView("status");
                } catch (Exception ex) {
                    logger.error(LOG_TAG, "event", "open_status_view",
                            "custom_message", ex.getMessage());
                }
            }
        });

        settingsItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Prefs.getInstance().setSideBarTargetBtn("settings");
                    launchUpdateView("settings");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error(LOG_TAG, "event", "open_status_view",
                            "custom_message", ex.getMessage());
                }
            }
        });

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MenuItem item = (MenuItem)e.getSource();

                if ("Error".equals(item.getLabel())) {
                    trayIcon.displayMessage("Sun TrayIcon Demo",
                            "This is an error message", TrayIcon.MessageType.ERROR);
                } else if ("Warning".equals(item.getLabel())) {
                    //type = TrayIcon.MessageType.WARNING;
                    trayIcon.displayMessage("Sun TrayIcon Demo",
                            "This is a warning message", TrayIcon.MessageType.WARNING);
                } else if ("Info".equals(item.getLabel())) {
                    //type = TrayIcon.MessageType.INFO;
                    trayIcon.displayMessage("Sun TrayIcon Demo",
                            "This is an info message", TrayIcon.MessageType.INFO);
                } else if ("None".equals(item.getLabel())) {
                    //type = TrayIcon.MessageType.NONE;
                    trayIcon.displayMessage("Sun TrayIcon Demo",
                            "This is an ordinary message", TrayIcon.MessageType.NONE);
                }
            }
        };
        errorItem.addActionListener(listener);
        warningItem.addActionListener(listener);
        infoItem.addActionListener(listener);
        noneItem.addActionListener(listener);

        frUpdatesItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    Prefs.getInstance().setSideBarTargetBtn("client");
                    launchUpdateView("client");
                } catch (Exception ex) {
                    logger.error(LOG_TAG, "event", "open_client_update_view",
                            "custom_message", ex.getMessage());
                }
            }
        });


        frLogsItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                // search to find the path for the info.log
                String programFiles86Path = System.getenv("%programfiles% (x86)");
                try {
                    String home = System.getProperty("user.home");
                    String path = home + File.separator + "G-POS" + File.separator +  "Logs";

                    openFolder(path);
                } catch (Exception ex) {
                    logger.error(LOG_TAG, "event", "open_client_log_directory",
                            "custom_message", ex.getMessage());
                }
            }
        });

        bkWebAppLogItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String path = System.getenv("PUBLIC") + File.separator + "PosServer" +
                            File.separator +  "logs";

                    openFolder(path);
                } catch (Exception ex) {
                    logger.error(LOG_TAG, "event", "open_server_webapp_log_directory",
                            "custom_message", ex.getMessage());
                }
            }
        });

        bkPackagerLogItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String path = System.getenv("PUBLIC") + File.separator + "PosServer" +
                            File.separator +  "packager";

                    openFolder(path);
                } catch (Exception ex) {
                    logger.error(LOG_TAG, "event", "open_server_packager_log_directory",
                            "custom_message", ex.getMessage());
                }
            }
        });

        bkUpdatesItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    Prefs.getInstance().setSideBarTargetBtn("server");
                    launchUpdateView("server");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error(LOG_TAG, "event", "open_client_log_directory",
                            "custom_message", ex.getMessage());
                }
            }
        });

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //stop services
                tray.remove(trayIcon);
                System.exit(0);
            }
        });


    }

    private static void openFolder(String path){
        try {
            /**
             *  mkdirs() creates the directory named by the abstract pathname,
             *  including any necessary but nonexistent parent directories.
             *  Note that if this operation fails it may have succeeded in creating some of
             *  the necessary parent directories.
             *
             * Usage
             *
             * <code>
             *      File  f = new File("non_existing_dir/someDir");
             *      System.out.println(f.mkdir());  ->  yields false
             *      System.out.println(f.mkdirs()); ->  yields true
             * <code>
             * */
            new File(path).mkdirs();
            if(new File(path).exists()){

                // read the path
                File logFolder = new File(path);
                Desktop desktop = Desktop.getDesktop();

                // opens the folder using default windows explorer
                desktop.open(logFolder);
            }else{
                logger.error(LOG_TAG, "event", "find_path", "custom_message",
                        "path "+path+" doesn't exist");
                throw new IOException("Path doesn't exist");
            }

        } catch (IOException ex) {
            logger.error(LOG_TAG, "event", "open_frontend_log_directory",
                    "custom_message", ex.getMessage());
        }
    }

    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = SystemTrayUtils.class.getClassLoader().getResource(path);
        if (imageURL == null) {
            String message = "Resource not found: " + path;
            logger.error(LOG_TAG, "event", "Not_suported", "custom_message", message);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public void displayMessage(String message, Level level){
        TrayIcon.MessageType messageType;
        switch(level) {
            case INFO:
                messageType = TrayIcon.MessageType.INFO;
                break;
            case ERROR:
                messageType = TrayIcon.MessageType.ERROR;
                break;
            case WARNING:
                messageType = TrayIcon.MessageType.WARNING;
                break;
            default:
                  messageType = TrayIcon.MessageType.INFO;
        }

        trayIcon.displayMessage("Sun TrayIcon Demo",
                message, messageType);
    }

    public static void launchUpdateView(String action){
        if(MainViewController.instance == null){
            new MainView().launch();
        }else {
            MainViewController.instance.fireBtnEvent(action);
        }
    }

}
