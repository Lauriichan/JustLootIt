package me.lauriichan.spigot.justlootit.config.loot;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.JsonType;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.data.DirectoryDataExtension;
import me.lauriichan.minecraft.pluginbase.data.DirectoryDataWrapper;
import me.lauriichan.minecraft.pluginbase.data.IDataHandler;
import me.lauriichan.minecraft.pluginbase.data.handler.JsonDataHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.io.IOManager;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.LootModification;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

@Extension
public class LootModificationDirectoryData extends DirectoryDataExtension<IJson<?>> {

    private final Object2ObjectOpenHashMap<NamespacedKey, LootModification> modifications = new Object2ObjectOpenHashMap<>();

    private final IOManager ioManager;

    public LootModificationDirectoryData(JustLootItPlugin plugin) {
        this.ioManager = plugin.ioManager();
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
        JsonObject object = json.asJsonObject();
        ILootCondition condition = object.has("condition", JsonType.OBJECT)
            ? JsonIO.deserialize(ioManager, object.getAsObject("condition"), ILootCondition.class)
            : null;
        ILootModifier modifier = JsonIO.deserialize(ioManager, object.getAsObject("modifier"), ILootModifier.class);
        if (modifier == null) {
            modifications.remove(id);
            logger.warning("No loot modifier set for loot modification '{0}'", id);
            return;
        }
        modifications.put(id, new LootModification(id, condition, modifier));
    }

    @Override
    public void onDeleted(ISimpleLogger logger, FileKey key) {
        modifications.remove(key.location());
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
        LootModification modification = modifications.get(value.key().location());
        if (modification == null) {
            value.delete();
            return;
        }
        JsonObject object = new JsonObject();
        if (modification.condition() != null) {
            object.put("condition", JsonIO.serialize(ioManager, modification.condition()));
        }
        object.put("modifier", JsonIO.serialize(ioManager, modification.modifier()));
    }

    public void applyModifications(Container container, PlayerAdapter player, Location location, Inventory inventory, NamespacedKey lootTableKey, long seed) {
        for (LootModification modification : modifications.values()) {
            if (modification.isApplicable(container, player, location, lootTableKey)) {
                modification.apply(player.versionHandler(), inventory, seed);
            }
        }
    }

    public ItemStack applyModifications(Container container, PlayerAdapter player, Location location, ItemStack itemStack, NamespacedKey lootTableKey, long seed) {
        for (LootModification modification : modifications.values()) {
            if (modification.isApplicable(container, player, location, lootTableKey)) {
                itemStack = modification.apply(player.versionHandler(), itemStack, seed);
            }
        }
        return itemStack;
    }

}
