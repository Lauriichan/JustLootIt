package me.lauriichan.spigot.justlootit.command.provider;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IProviderArgumentType;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class LoggerProvider implements IProviderArgumentType<ISimpleLogger> {

    private final ISimpleLogger logger;

    public LoggerProvider(final ISimpleLogger logger) {
        this.logger = logger;
    }

    @Override
    public ISimpleLogger provide(final Actor<?> actor) {
        return logger;
    }

}
