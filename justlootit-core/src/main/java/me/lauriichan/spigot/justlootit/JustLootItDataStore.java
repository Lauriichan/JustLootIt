package me.lauriichan.spigot.justlootit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.platform.version.SimpleVersion;

public final class JustLootItDataStore {

    public static final JustLootItDataStore STORE = new JustLootItDataStore();

    private JustLootItDataStore() {
        if (STORE != null) {
            throw new UnsupportedOperationException();
        }
    }

    private SimpleVersion lastMinecraftVersion;

    private ISimpleLogger logger;
    private IDataSource source;
    private ICompoundTag dataTag;

    final void load(JustLootItPlugin plugin) {
        logger = plugin.logger();
        source = plugin.resource("data://store.dat");
        dataTag = plugin.versionHandler().nbtHelper().createCompound();
        try {
            if (!source.exists()) {
                return;
            }
            try (DataInputStream input = new DataInputStream(source.openReadableStream())) {
                dataTag.read(input);
            } catch (IOException e) {
                logger.warning("Failed to load JLI data store, can be ignored", e);
            }
        } finally {
            loadData();
        }
    }

    final void save() {
        if (source == null) {
            return;
        }
        dataTag.clear();
        saveData();
        try (DataOutputStream output = new DataOutputStream(source.openWritableStream())) {
            dataTag.write(output);
        } catch (IOException e) {
            logger.warning("Failed to save JLI data store, can be ignored", e);
        }
    }

    private void loadData() {
        String versionStr = dataTag.getString("minecraft.version");
        if (versionStr != null) {
            lastMinecraftVersion = SimpleVersion.of(versionStr);
        }
    }

    private void saveData() {
        if (lastMinecraftVersion != null) {
            dataTag.set("minecraft.version", lastMinecraftVersion.toString());
        }
    }

    public SimpleVersion lastMinecraftVersion() {
        return lastMinecraftVersion;
    }

    public void lastMinecraftVersion(SimpleVersion lastMinecraftVersion) {
        this.lastMinecraftVersion = lastMinecraftVersion;
    }

}
