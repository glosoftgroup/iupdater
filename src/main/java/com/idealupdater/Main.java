package com.idealupdater;

import com.idealupdater.services.CheckNewVersion;
import com.idealupdater.utils.structlog4j.GenericLoggableObject;
import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.StructLog4JConfig;
import com.idealupdater.utils.structlog4j.interfaces.Logger;
import com.idealupdater.utils.structlog4j.renderers.JSONRenderer;
import com.idealupdater.utils.ui.SystemTrayUtils;
import com.idealupdater.utils.utils.Prefs;


public class Main {

    public static final String VERSION = "1.0.0";
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String LOG_TAG = "Main";
    public static CheckNewVersion chknv;

    public static void init(){
        logger.bind("CheckUpdate", "CheckUpdate");
        // initialise background service
        chknv = new CheckNewVersion();

        logger.info(LOG_TAG, "event", "Initialize services");
    }

    public static void main(String[] args){
        StructLog4JConfig.setLogRenderer(new JSONRenderer());
        StructLog4JConfig.setContextSupplier(
                new GenericLoggableObject(new Object[]{"environment", "development"}));
        init();
        logger.info(LOG_TAG, "event", "Application started");

        SystemTrayUtils.getInstance().viewTray();

        while (true){
            try {
                chknv.runDaemon();
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
                int mills = new Prefs().getTimeOut();
                Thread.sleep(mills);
            } catch (Exception ex){
                System.err.println(ex.getMessage());
            }
        }

    }
}
