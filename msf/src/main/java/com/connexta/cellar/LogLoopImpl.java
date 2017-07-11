package com.connexta.cellar;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connexta.cellar.api.LogLoop;

/**
 * Bundle with configurations for testing Cellar.
 */
public class LogLoopImpl implements LogLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogLoopImpl.class);

    private static final String DEFAULT_NAME = "DEFAULT_LOG_LOOP";

    private static final long WAIT_TIME = 5000L;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private AtomicBoolean logStuff;

    private String name;

    public LogLoopImpl() {
        this(DEFAULT_NAME);
    }

    public LogLoopImpl(final String name) {
        this.name = name;
        this.logStuff = new AtomicBoolean(true);
    }

    public void init() throws Exception {
        executor.execute(() -> {
            try {
                while (logStuff.get()) {
                    LOGGER.info("::: {} ::: {} :::", new Date(), name);
                    Thread.sleep(WAIT_TIME);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted. Terminating.");
            }
        });
    }

    public void destroy() throws Exception {
        this.logStuff.set(false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
