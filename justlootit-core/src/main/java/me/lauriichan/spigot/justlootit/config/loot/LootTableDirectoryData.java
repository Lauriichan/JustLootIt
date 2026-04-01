package me.lauriichan.spigot.justlootit.config.loot;

import org.bukkit.NamespacedKey;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.data.DirectoryDataExtension;
import me.lauriichan.minecraft.pluginbase.data.DirectoryDataWrapper;
import me.lauriichan.minecraft.pluginbase.data.IDataHandler;
import me.lauriichan.minecraft.pluginbase.data.handler.JsonDataHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.IOManager;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

@Extension
public class LootTableDirectoryData extends DirectoryDataExtension<IJson<?>> {

    private final Object2ObjectOpenHashMap<NamespacedKey, CustomLootTable> tables = new Object2ObjectOpenHashMap<>();

    private final IOManager ioManager;
    private final VersionHandler versionHandler;

    public LootTableDirectoryData(JustLootItPlugin plugin) {
        this.ioManager = plugin.ioManager();
        this.versionHandler = plugin.versionHandler();
    }

    public CustomLootTable get(NamespacedKey key) {
        return tables.get(key);
    }

    public ObjectCollection<CustomLootTable> getTables() {
        return tables.values();
    }

    @Override
    public String path() {
        return "data://loot/tables";
    }

    @Override
    public IDataHandler<IJson<?>> handler() {
        return JsonDataHandler.forKey("loot_table");
    }

    @Override
    public void onLoad(ISimpleLogger logger, FileData<IJson<?>> value) throws Exception {
        if (newData().contains(value.key())) {
            return;
        }
        IJson<?> json = value.value();
        if (json == null || !json.isObject()) {
            value.delete();
            return;
        }
        NamespacedKey id = value.key().location();
        ILootPoolProvider lootProvider = JsonIO.deserialize(ioManager, json.asJsonObject(), ILootPoolProvider.class);
        if (lootProvider == null) {
            tables.remove(id);
            logger.warning("No loot pool provider set for loot table '{0}'", id);
            return;
        }
        tables.put(id, new CustomLootTable(versionHandler, id, lootProvider));
    }

    @Override
    public void onDeleted(ISimpleLogger logger, FileKey key) {
        tables.remove(key.location());
    }

    @Override
    public void onDeleteDone(ISimpleLogger logger, DirectoryDataWrapper<?, ?> wrapper) {
        if (newData().isEmpty()) {
            return;
        }
        wrapper.saveDirectory(false);
    }

    @Override
    public void onSave(ISimpleLogger logger, FileData<IJson<?>> value) throws Exception {
        CustomLootTable table = tables.get(value.key().location());
        if (table == null) {
            value.delete();
            return;
        }
        value.value(JsonIO.serialize(ioManager, table.provider()));
    }

}
