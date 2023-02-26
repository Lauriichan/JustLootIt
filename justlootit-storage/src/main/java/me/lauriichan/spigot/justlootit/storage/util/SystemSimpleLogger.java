package me.lauriichan.spigot.justlootit.storage.util;

import me.lauriichan.laylib.logger.AbstractSimpleLogger;

public final class SystemSimpleLogger extends AbstractSimpleLogger {
    
    public static final SystemSimpleLogger SYSTEM = new SystemSimpleLogger();

    private static final String INFO_FORMAT = "[INFO] %s";
    private static final String WARNING_FORMAT = "[WARN] %s";
    private static final String ERROR_FORMAT = "[ERROR] %s";
    private static final String TRACK_FORMAT = "[TRACK] %s";
    private static final String DEBUG_FORMAT = "[DEBUG] %s";
    
    private SystemSimpleLogger() {}
    
    @Override
    protected void info(String message) {
        System.out.println(INFO_FORMAT.formatted(message));
    }

    @Override
    protected void warning(String message) {
        System.out.println(WARNING_FORMAT.formatted(message));
    }

    @Override
    protected void error(String message) {
        System.err.println(ERROR_FORMAT.formatted(message));
    }

    @Override
    protected void track(String message) {
        System.out.println(TRACK_FORMAT.formatted(message));
    }

    @Override
    protected void debug(String message) {
        System.out.println(DEBUG_FORMAT.formatted(message));
    }

}
