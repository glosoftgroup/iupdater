package com.idealupdater.services;

import com.idealupdater.utils.structlog4j.LoggerFactory;
import com.idealupdater.utils.structlog4j.interfaces.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckNewVersion {
    public static final Logger logger = LoggerFactory.getLogger(CheckNewVersion.class);
    public static final String LOG_TAG = "CheckNewVersion";

    public static void CheckNewVersion(){}

    public void runDaemon(){
        synchronizeTasks();
    }

    private synchronized void synchronizeTasks(){
        executeTask();
    }

    private void executeTask(){
        // create runnable task
        ExecutorService executor = Executors.newFixedThreadPool(1);

        logger.info(LOG_TAG, "event","create_Executor_task");

        Runnable task = createTask();
        executor.execute(task);

        shutdownAndAwaitTermination(executor);

    }

    private void shutdownAndAwaitTermination(final ExecutorService pool){
        pool.shutdown();
        try {

            if (!pool.awaitTermination(5, TimeUnit.MINUTES)) {
                logger.info(LOG_TAG, "event","Executor_Shutdown_wait", "message",
                        "waiting for some unfinished task to complete");
                pool.shutdownNow();
            }else{
                logger.info(LOG_TAG, "event","Executor_Shutdown_successful", "message",
                        "Executor pool completed all tasks, shutting down");
            }
        } catch (Exception e){
            logger.error(LOG_TAG, "event", "Executor_Shutdown_error", "message",
                    "error shutting down the thread pool", e.getMessage());
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private Runnable createTask(){
        return new CheckVersionWorker();
    }


}
