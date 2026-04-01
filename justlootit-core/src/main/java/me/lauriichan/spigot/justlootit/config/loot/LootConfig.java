package me.lauriichan.spigot.justlootit.config.loot;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.inventory.Textures;
import me.lauriichan.spigot.justlootit.loot.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.loot.modifier.InsertionMode;
import me.lauriichan.spigot.justlootit.loot.modifier.SetAmountFunc;
import me.lauriichan.spigot.justlootit.loot.modifier.SetEnchantmentFunc;
import me.lauriichan.spigot.justlootit.loot.modifier.UpdateNameFunc;
import me.lauriichan.spigot.justlootit.loot.provider.ModifiedItemProvider;
import me.lauriichan.spigot.justlootit.loot.provider.NbtItemProvider;
import me.lauriichan.spigot.justlootit.loot.provider.SelectorPoolProvider;
import me.lauriichan.spigot.justlootit.loot.provider.SimpleItemProvider;
import me.lauriichan.spigot.justlootit.util.TypeName;
import me.lauriichan.spigot.justlootit.util.WeightedList;

public class LootConfig implements IConfigExtension {

    private final Object2ObjectOpenHashMap<String, CustomLootTable> tables = new Object2ObjectOpenHashMap<>();
    private volatile boolean modified = false;

    private final JustLootItPlugin plugin;

    public LootConfig(JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String name() {
        return TypeName.ofConfig(this);
    }

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    /*
     * Propergate
     */

    @Override
    public void onPropergate(ISimpleLogger logger, Configuration configuration) throws Exception {
        WeightedList<ILootPoolProvider> poolProviders = new WeightedList<>();
        poolProviders.add(12, new SimpleItemProvider(Material.STONE, 3));
        ObjectArrayList<ILootModifierFunc> modifiers = new ObjectArrayList<>();
        modifiers.add(new UpdateNameFunc("&cItem Name!", InsertionMode.SET));
        modifiers.add(new SetAmountFunc(3, 23));
        modifiers.add(new SetEnchantmentFunc(Enchantment.UNBREAKING, 5));
        poolProviders.add(3, new ModifiedItemProvider(new SimpleItemProvider(Material.AMETHYST_SHARD, 1), modifiers));
        poolProviders.add(5,
            new NbtItemProvider(ItemEditor.ofHead(Textures.GEODE_BLANK).setName("&5Blank Geode Head")
                .setEnchantment(Enchantment.BINDING_CURSE, 1, true).lore().add(new String[] {
                    "&cThis item is cursed"
                }).apply().asItemStack()));
        configuration.set("example", new SelectorPoolProvider(poolProviders, 1, 5));
    }

    /*
     * Load
     */

    @Override
    public void onLoad(ISimpleLogger logger, Configuration configuration) throws Exception {
        for (String key : configuration.keySet()) {
            ILootPoolProvider provider = configuration.get(key, ILootPoolProvider.class);
            if (provider == null) {
                continue;
            }
            NamespacedKey id = new NamespacedKey(plugin, key.replace('.', '/').replace(':', '/'));
            if (tables.containsKey(id.getKey())) {
                String newKey = id.getKey();
                int count = 1;
                while (tables.containsKey(newKey)) {
                    newKey = key + '_' + count;
                }
                logger.warning("There is already a loot table called '{0}', to avoid conflict the new table was renamed to '{1}'",
                    id.getKey(), newKey);
                id = new NamespacedKey(plugin, newKey);
            }
            tables.put(id.getKey(), new CustomLootTable(plugin.versionHandler(), id, provider));
        }
    }

    /*
     * Save
     */

    @Override
    public void onSave(ISimpleLogger logger, Configuration configuration) throws Exception {
        configuration.clear();
        for (CustomLootTable table : tables.values()) {
            configuration.set(table.id().getKey(), configuration);
        }
    }

    public CustomLootTable getTable(NamespacedKey key) {
        if (!key.getNamespace().equals("justlootit")) {
            return null;
        }
        return tables.get(key.getKey());
    }

    public ObjectCollection<CustomLootTable> getTables() {
        return tables.values();
    }

}
