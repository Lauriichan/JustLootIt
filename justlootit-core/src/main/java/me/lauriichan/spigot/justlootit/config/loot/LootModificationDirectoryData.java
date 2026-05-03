package me.lauriichan.spigot.justlootit.config.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.loot.condition.AndCondition;
import me.lauriichan.spigot.justlootit.loot.condition.LootTableCondition;
import me.lauriichan.spigot.justlootit.loot.condition.WorldRegexCondition;
import me.lauriichan.spigot.justlootit.loot.filter.MaterialFilter;
import me.lauriichan.spigot.justlootit.loot.modifier.AddPotionEffectModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.ClearEnchantmentsModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.ClearPotionEffectsModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.CombinedModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.FilteredModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.RemovePotionEffectModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.SelectorModifier;
import me.lauriichan.spigot.justlootit.loot.modifier.SetAmountModifier;
import me.lauriichan.spigot.justlootit.loot.provider.SimpleItemProvider;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.util.WeightedList;

@Extension
public class LootModificationDirectoryData extends DirectoryDataExtension<IJson<?>> {

    private final Object2ObjectOpenHashMap<NamespacedKey, LootModification> modifications = new Object2ObjectOpenHashMap<>();

    private final IOManager ioManager;

    public LootModificationDirectoryData(JustLootItPlugin plugin) {
        this.ioManager = plugin.ioManager();
        populateExample(plugin);
    }

    private void populateExample(JustLootItPlugin plugin) {
        JsonObject object = new JsonObject();
        object.put("condition", JsonIO.serialize(ioManager, new AndCondition(ObjectList.of(new ILootCondition[] {
            // This condition is invalid as the predicate is not set but for serialization it is enough
            new WorldRegexCondition("world", null),
            new LootTableCondition(NamespacedKey.fromString("iris:justlootit/v1"))
        }))));
        object.put("modifier", JsonIO.serialize(ioManager, new CombinedModifier(ObjectList.of(new ILootModifier[] {
            new FilteredModifier(new MaterialFilter(Material.NETHER_STAR), new SetAmountModifier(1, 10)),
            new FilteredModifier(new MaterialFilter(Material.DIAMOND_SWORD), new ClearEnchantmentsModifier()),
            new FilteredModifier(new MaterialFilter(Material.POTION), new CombinedModifier(ObjectList.of(new ILootModifier[] {
                new SelectorModifier(WeightedList.<ILootModifier>builder().add(5, new ClearPotionEffectsModifier())
                    .add(1, new CombinedModifier(ObjectList.of(new ILootModifier[] {
                        new RemovePotionEffectModifier(PotionEffectType.ABSORPTION),
                        new RemovePotionEffectModifier(PotionEffectType.INSTANT_HEALTH),
                        new RemovePotionEffectModifier(PotionEffectType.BAD_OMEN)
                    }))).build()),
                new SelectorModifier(WeightedList.<ILootModifier>builder()
                    .add(2, new AddPotionEffectModifier(new PotionEffect(PotionEffectType.REGENERATION, 200, 2, true), false))
                    .add(0.75, new AddPotionEffectModifier(new PotionEffect(PotionEffectType.REGENERATION, 400, 3, true), false))
                    .add(0.05, new AddPotionEffectModifier(new PotionEffect(PotionEffectType.REGENERATION, 600, 4, true), false)).build())
            })))
        }))));
        object.put("pool_provider", JsonIO.serialize(ioManager, new SimpleItemProvider(Material.ACACIA_BOAT, 1)));
        FileData<IJson<?>> wrapper = new FileData<>(null, null);
        wrapper.value(object);
        wrapper.version(0);
        try {
            handler().save(wrapper, plugin.resource("data://loot/example_loot_modification.json"));
        } catch (Exception e) {
            plugin.logger().error("Failed to write loot table example", e);
        }
    }

    @Override
    public boolean searchSupportedDirectories() {
        return true;
    }

    @Override
    public String path() {
        return "data://loot/modifications";
    }

    @Override
    public IDataHandler<IJson<?>> handler() {
        return JsonDataHandler.forKey("loot_modification");
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
        ILootModifier modifier = object.has("modifier", JsonType.OBJECT)
            ? JsonIO.deserialize(ioManager, object.getAsObject("modifier"), ILootModifier.class)
            : null;
        ILootPoolProvider provider = object.has("pool_provider", JsonType.OBJECT)
            ? JsonIO.deserialize(ioManager, object.getAsObject("pool_provider"), ILootPoolProvider.class)
            : null;
        if (provider == null && modifier == null) {
            modifications.remove(id);
            logger.warning("No loot modifier or loot pool provider set for loot modification '{0}'", id);
            return;
        }
        modifications.put(id, new LootModification(id, condition, modifier, provider));
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
        object.put("pool_provider", JsonIO.serialize(ioManager, modification.provider()));
    }

    public void applyModifications(Container container, PlayerAdapter player, Location location, Inventory inventory,
        NamespacedKey lootTableKey, long seed) {
        for (LootModification modification : modifications.values()) {
            if (modification.isApplicable(container, player, location, lootTableKey)) {
                modification.apply(player.versionHandler(), inventory, seed);
            }
        }
    }

    public ObjectList<ItemStack> applyModifications(Container container, PlayerAdapter player, Location location, ItemStack itemStack,
        NamespacedKey lootTableKey, long seed) {
        ObjectArrayList<ItemStack> list = new ObjectArrayList<>();
        list.add(itemStack);
        for (LootModification modification : modifications.values()) {
            if (modification.isApplicable(container, player, location, lootTableKey)) {
                modification.apply(player.versionHandler(), list, seed);
            }
        }
        return list;
    }

}
