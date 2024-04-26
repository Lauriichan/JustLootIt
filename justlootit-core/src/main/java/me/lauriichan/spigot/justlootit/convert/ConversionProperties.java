package me.lauriichan.spigot.justlootit.convert;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import me.lauriichan.laylib.logger.ISimpleLogger;

public final class ConversionProperties {

    private final ISimpleLogger logger;

    private final File file;
    private final Properties properties;

    public ConversionProperties(final ISimpleLogger logger, final File file, final boolean create) {
        this.file = file;
        this.logger = logger;
        if (create) {
            this.properties = new Properties();
        } else {
            if (!file.exists()) {
                this.properties = null;
            } else {
                Properties properties = new Properties();
                try (FileReader reader = new FileReader(file)) {
                    properties.load(reader);
                } catch (IOException e) {
                    logger.error("Failed to load conversion properties!", e);
                    logger.error("Deleting conversion properties as a result, please retry the process.");
                    file.delete();
                    properties = null;
                }
                this.properties = properties;
            }
        }
    }

    public boolean isAvailable() {
        return properties != null;
    }

    public boolean isProperty(String name) {
        return isProperty(name, false);
    }

    public boolean isProperty(String name, boolean defaultState) {
        String propertyValue = properties.getProperty(name);
        if (propertyValue == null) {
            return defaultState;
        }
        return Boolean.valueOf(propertyValue);
    }

    public void setProperty(String name, boolean state) {
        properties.setProperty(name, Boolean.toString(state));
    }

    public void save() {
        try {
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                if (!file.createNewFile()) {
                    logger.error("Failed to save conversion properties!");
                    return;
                }
            }
            try (FileWriter writer = new FileWriter(file)) {
                properties.store(writer, "THIS FILE IS NOT MEANT TO BE EDITED MANUALLY\nPROCEED AT OWN RISK");
            }
        } catch (IOException e) {
            logger.error("Failed to save conversion properties!", e);
        }
    }

    public void delete() {
        if (!file.exists()) {
            return;
        }
        file.delete();
    }

}
