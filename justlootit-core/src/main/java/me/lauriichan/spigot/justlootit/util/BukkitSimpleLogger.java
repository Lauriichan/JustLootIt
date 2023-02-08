package me.lauriichan.spigot.justlootit.util;

import java.util.logging.Logger;

import me.lauriichan.laylib.logger.AbstractSimpleLogger;

public final class BukkitSimpleLogger extends AbstractSimpleLogger {

    private final Logger logger;

    public BukkitSimpleLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void info(String message) {
        logger.info(message);
    }

    @Override
    protected void warning(String message) {
        logger.warning(message);
    }

    @Override
    protected void error(String message) {
        logger.severe(message);
    }

    @Override
    protected void track(String message) {
        logger.fine(message);
    }

    @Override
    protected void debug(String message) {
        logger.info("[DEBUG] " + message);
    }

}
