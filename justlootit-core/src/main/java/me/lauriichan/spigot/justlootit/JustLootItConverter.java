package me.lauriichan.spigot.justlootit;

import me.lauriichan.laylib.reflection.StackTracker;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;

public final class JustLootItConverter {
    
    private JustLootItConverter() {
        throw new UnsupportedOperationException();
    }
    
    public static void convert(VersionHandler versionHandler) {
        Class<?> clazz = StackTracker.getCallerClass().orElse(null);
        if (clazz != JustLootItPlugin.class) {
            throw new UnsupportedOperationException();
        }
        ConversionAdapter conversionAdapter = versionHandler.conversionAdapter();
        // TODO: Implement conversion
    }
    
}
