package me.lauriichan.spigot.justlootit.config.loot;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.data.DirectoryDataExtension;
import me.lauriichan.minecraft.pluginbase.data.DirectoryDataWrapper;
import me.lauriichan.minecraft.pluginbase.data.IDataHandler;
import me.lauriichan.minecraft.pluginbase.data.handler.JsonDataHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.io.IOManager;
import me.lauriichan.minecraft.pluginbase.io.serialization.SerializationException;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.config.io.JsonIO;
import me.lauriichan.spigot.justlootit.inventory.Textures;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.loot.io.LootRegistry;
import me.lauriichan.spigot.justlootit.loot.modifier.InsertionMode;
import me.lauriichan.spigot.justlootit.loot.modifier.SetAmountModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.SetEnchantmentModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.UpdateNameModifier;
import me.lauriichan.spigot.justlootit.loot.provider.ChancedPoolProvider;
import me.lauriichan.spigot.justlootit.loot.provider.CombinedPoolProvider;
import me.lauriichan.spigot.justlootit.loot.provider.ModifiedItemProvider;
import me.lauriichan.spigot.justlootit.loot.provider.NbtItemProvider;
import me.lauriichan.spigot.justlootit.loot.provider.SelectorPoolProvider;
import me.lauriichan.spigot.justlootit.loot.provider.SimpleItemProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.util.WeightedList;

@Extension
public class LootTableDirectoryData extends DirectoryDataExtension<IJson<?>> {

    private final Object2ObjectOpenHashMap<NamespacedKey, CustomLootTable> tables = new Object2ObjectOpenHashMap<>();

    private final IOManager ioManager;
    private final VersionHandler versionHandler;

    public LootTableDirectoryData(JustLootItPlugin plugin) {
        this.ioManager = plugin.ioManager();
        this.versionHandler = plugin.versionHandler();
        populateExample(plugin);
    }

    private void populateExample(JustLootItPlugin plugin) {
        FileData<IJson<?>> wrapper = new FileData<>(null, null);
        wrapper.value(JsonIO.serialize(ioManager, new CombinedPoolProvider(ObjectList.of(new ILootPoolProvider[] {
            new ChancedPoolProvider(new SelectorPoolProvider(
                WeightedList.<ILootPoolProvider>builder().add(5.0, new SimpleItemProvider(Material.DIAMOND, 2))
                    .add(0.1, new SimpleItemProvider(Material.NETHER_STAR, 1)).add(7, new SimpleItemProvider(Material.EMERALD, 1)).build(),
                2, 3), 30, 100),
            new SelectorPoolProvider(WeightedList.<ILootPoolProvider>builder().add(12, new SimpleItemProvider(Material.STONE, 3))
                .add(3, new ModifiedItemProvider(new SimpleItemProvider(Material.AMETHYST_SHARD, 1), ObjectList.of(new ILootModifier[] {
                    new UpdateNameModifier("&cItem Name!", InsertionMode.SET),
                    new SetAmountModifier(3, 23),
                    new SetEnchantmentModifier(LootRegistry.REGISTRY.enchUnbreaking(), 5)
                })))
                .add(5,
                    new NbtItemProvider(ItemEditor.ofHead(Textures.GEODE_BLANK).setName("&5Blank Geode Head")
                        .setEnchantment(Enchantment.BINDING_CURSE, 1, true).lore().add(new String[] {
                            "&cThis item is cursed"
                        }).apply().asItemStack())).build(), 1, 5)
        }))));
        wrapper.version(0);
        try {
            handler().save(wrapper, plugin.resource("data://loot/example_loot_table.json"));
        } catch (Exception e) {
            plugin.logger().warning("Failed to write loot table example", e);
        }
    }

    @Override
    public boolean searchSupportedDirectories() {
        return true;
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
        ILootPoolProvider lootProvider;
        try {
            lootProvider = JsonIO.deserialize(ioManager, json.asJsonObject(), ILootPoolProvider.class);
        } catch (IllegalStateException ise) {
            if (ise.getCause() instanceof SerializationException err) {
                if (err.getCause() != null) {
                    logger.error("Invalid loot pool provider set for loot table '{0}': {1}", err.getCause(), id, err.getMessage());
                    return;
                }
                logger.warning("Invalid loot pool provider set for loot table '{0}': {1}", id, err.getMessage());
                return;
            }
            logger.error(ise);
            return;
        }
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
