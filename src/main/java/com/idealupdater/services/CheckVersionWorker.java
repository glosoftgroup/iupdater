package com.idealupdater.services;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;

public class CheckVersionWorker implements Runnable{
    public static final Logger logger = LoggerFactory.getLogger(CheckVersionWorker.class);
    public static final String LOG_TAG = "CheckVersionWorker";

    @Override
    public void run() {

        logger.info(LOG_TAG, "event", "checking the version");

    }
}
