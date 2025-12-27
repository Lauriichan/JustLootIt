package me.lauriichan.spigot.justlootit.config.world;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;

import org.bukkit.NamespacedKey;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.util.ExplosionType;
import me.lauriichan.spigot.justlootit.util.TypeName;

public class WorldConfig implements IConfigExtension {
    
    private static final ExplosionType[] EXPLOSION_TYPES = ExplosionType.values();
    
    private static final String EXPLOSION_KEY = "destruction.explosion.%s_allowed";

    private final Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> blacklistedStructures = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> blacklistedLootTables = new Object2ObjectArrayMap<>();

    private final Object2ObjectArrayMap<String, Object2ObjectOpenHashMap<String, String>> containerRefreshGroupIds = new Object2ObjectArrayMap<>();

    private final ObjectOpenHashSet<String> blacklistedCompatibilities = new ObjectOpenHashSet<>();

    private final boolean trialChamberBuggedVersion;

    private volatile String worldRefreshGroupId = "";

    private volatile boolean blacklistVanillaContainers = false;
    private volatile boolean blacklistStaticContainers = false;
    private volatile boolean blacklistFrameContainers = false;
    
    private final Object2BooleanArrayMap<ExplosionType> explosionsAllowed = new Object2BooleanArrayMap<>();

    private volatile boolean modified = false;

    private volatile boolean whitelisted = false;

    public WorldConfig(boolean trialChamberBuggedVersion) {
        this.trialChamberBuggedVersion = trialChamberBuggedVersion;
        explosionsAllowed.defaultReturnValue(false);
        resetExplosionTypes();
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
        return this.modified;
    }

    private void setDirty() {
        this.modified = true;
    }

    /*
     * Propergate
     */

    @Override
    public void onPropergate(ISimpleLogger logger, Configuration configuration) throws Exception {
        propergateBlacklist(configuration, "blacklist.structures", "example_structure_id", "another_example_structure_id");
        propergateBlacklist(configuration, "blacklist.loottables", "example/loottable_id/with/category",
            "loottables/have/categories/example_loottable_name");
        configuration.set("whitelisted", whitelisted);
        configuration.set("blacklist.containers.vanilla", blacklistVanillaContainers);
        configuration.set("blacklist.containers.static", blacklistStaticContainers);
        configuration.set("blacklist.containers.frame", blacklistFrameContainers);
        configuration.set("blacklist.containers.compatibilities", Collections.emptyList());
        
        resetExplosionTypes();
        saveExplosionTypes(configuration);

        configuration.set("refresh.world_group", "");
        propergateRefreshGroups(configuration.getConfiguration("refresh.container_groups", true));
    }

    private void propergateBlacklist(Configuration configuration, String configKey, String... examples) {
        Configuration section = configuration.getConfiguration(configKey, true);
        ObjectArrayList<String> list = new ObjectArrayList<>();
        for (String example : examples) {
            list.add(example);
        }
        section.set("minecraft", list);
    }
    
    private void propergateRefreshGroups(Configuration configuration) {
        Configuration minecraftNamespace = configuration.getConfiguration("minecraft", true);
        minecraftNamespace.set("example/loottable_id/with/category", "");
        Configuration justlootitNamespace = configuration.getConfiguration(JustLootItConstant.PLUGIN_NAMESPACE, true);
        justlootitNamespace.set(JustLootItConstant.STATIC_CONTAINER_REFRESH_KEY, "");
        justlootitNamespace.set(JustLootItConstant.FRAME_CONTAINER_REFRESH_KEY_FORMAT.formatted("example_namespace", "example_item_id"), "");
        justlootitNamespace.set(JustLootItConstant.COMPATIBILITY_CONTAINER_REFRESH_KEY_FORMAT.formatted("example_namespace", "example_loot_table"), "");
    }
    
    private void resetExplosionTypes() {
        for (ExplosionType type : EXPLOSION_TYPES) {
            explosionsAllowed.put(type, false);
        }
    }

    /*
     * Load
     */

    @Override
    public void onLoad(ISimpleLogger logger, Configuration configuration) throws Exception {
        this.modified = false;
        loadBlacklist(configuration, "blacklist.structures", blacklistedStructures);
        loadBlacklist(configuration, "blacklist.loottables", blacklistedLootTables);
        this.whitelisted = configuration.getBoolean("whitelisted", false);
        this.blacklistVanillaContainers = configuration.getBoolean("blacklist.containers.vanilla", false);
        this.blacklistStaticContainers = configuration.getBoolean("blacklist.containers.static", false);
        this.blacklistFrameContainers = configuration.getBoolean("blacklist.containers.frame", false);
        List<String> list = configuration.getList("blacklist.containers.compatibilities", String.class);
        blacklistedCompatibilities.clear();
        addAllLowerCase(blacklistedCompatibilities, list);

        if (trialChamberBuggedVersion && !isStructureBlacklisted("minecraft", "trial_chambers")) {
            setStructureBlacklisted(NamespacedKey.minecraft("trial_chambers"), true);
        }
        
        loadExplosionTypes(configuration);

        worldRefreshGroupId = configuration.get("refresh.world_group", String.class);
        if (worldRefreshGroupId != null && worldRefreshGroupId.isEmpty()) {
            worldRefreshGroupId = null;
        }
        loadRefreshGroups(configuration.getConfiguration("refresh.container_groups", false));
    }

    private void loadBlacklist(Configuration configuration, String configKey,
        Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> map) {
        Configuration section = configuration.getConfiguration(configKey);
        if (section == null) {
            return;
        }
        map.clear();
        for (String key : section.keySet()) {
            List<String> list = section.getList(key, String.class);
            if (list.isEmpty()) {
                continue;
            }
            String namespaceKey = key.toLowerCase();
            if (map.containsKey(namespaceKey)) {
                addAllLowerCase(map.get(namespaceKey), list);
                continue;
            }
            ObjectOpenHashSet<String> set = new ObjectOpenHashSet<>();
            map.put(namespaceKey, set);
            addAllLowerCase(set, list);
        }
    }
    
    private boolean loadRefreshGroups(Configuration configuration) {
        containerRefreshGroupIds.clear();
        if (configuration == null) {
            return true;
        }
        for (String namespace : configuration.keySet()) {
            Configuration namespaceSection = configuration.getConfiguration(namespace);
            if (namespaceSection == null) {
                continue;
            }
            Object2ObjectOpenHashMap<String, String> namespaceMap = new Object2ObjectOpenHashMap<>();
            for (String key : namespaceSection.keySet()) {
                String value = namespaceSection.get(key, String.class);
                if (value == null) {
                    continue;
                }
                namespaceMap.put(key.toLowerCase(), value.isBlank() ? null : value);
            }
            if (namespaceMap.isEmpty()) {
                continue;
            }
            containerRefreshGroupIds.put(namespace.toLowerCase(), namespaceMap);
        }
        return false;
    }
    
    private void loadExplosionTypes(Configuration configuration) {
        for (ExplosionType type : EXPLOSION_TYPES) {
            explosionsAllowed.put(type, configuration.getBoolean(EXPLOSION_KEY.formatted(type.configName()), false));
        }
    }

    /*
     * Save
     */

    @Override
    public void onSave(ISimpleLogger logger, Configuration configuration) throws Exception {
        this.modified = false;
        saveBlacklist(configuration, "blacklist.structures", blacklistedStructures);
        saveBlacklist(configuration, "blacklist.loottables", blacklistedLootTables);
        configuration.set("whitelisted", whitelisted);
        configuration.set("blacklist.containers.vanilla", blacklistVanillaContainers);
        configuration.set("blacklist.containers.static", blacklistStaticContainers);
        configuration.set("blacklist.containers.frame", blacklistFrameContainers);
        configuration.set("blacklist.containers.compatibilities", new ObjectArrayList<>(blacklistedCompatibilities));
        
        saveExplosionTypes(configuration);

        configuration.set("refresh.world_group", worldRefreshGroupId == null ? "" : worldRefreshGroupId);
        saveRefreshGroups(configuration.getConfiguration("refresh.container_groups", true));
    }

    private void saveBlacklist(Configuration configuration, String configKey,
        Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> map) {
        Configuration section = configuration.getConfiguration(configKey, true);
        section.clear();
        for (Entry<String, ObjectOpenHashSet<String>> entry : map.object2ObjectEntrySet()) {
            section.set(entry.getKey(), new ObjectArrayList<>(entry.getValue()));
        }
    }
    
    private void saveRefreshGroups(Configuration configuration) {
        configuration.clear();
        for (Entry<String, Object2ObjectOpenHashMap<String, String>> namespaceEntry : containerRefreshGroupIds.object2ObjectEntrySet()) {
            Configuration namespace = configuration.getConfiguration(namespaceEntry.getKey(), true);
            for (Entry<String, String> keyEntry : namespaceEntry.getValue().object2ObjectEntrySet()) {
                namespace.set(keyEntry.getKey(), keyEntry.getValue() == null ? "" : keyEntry.getValue());
            }
        }
    }
    
    private void saveExplosionTypes(Configuration configuration) {
        for (ExplosionType type : EXPLOSION_TYPES) {
            configuration.set(EXPLOSION_KEY.formatted(type.configName()), explosionsAllowed.getBoolean(type));
        }
    }
    
    /*
     * Explosions
     */
    
    public void setExplosionAllowed(ExplosionType type, boolean allowed) {
        Objects.requireNonNull(type);
        if (explosionsAllowed.getBoolean(type) == allowed) {
            return;
        }
        setDirty();
        explosionsAllowed.put(type, allowed);
    }

    public boolean isExplosionAllowed(ExplosionType type) {
        return explosionsAllowed.getBoolean(type);
    }
    
    /*
     * Access refresh groups
     */

    public String getWorldRefreshGroupId() {
        return worldRefreshGroupId;
    }

    public void setWorldRefreshGroupId(String groupId) {
        this.worldRefreshGroupId = groupId != null && groupId.isBlank() ? null : groupId;
    }

    public String getLootTableRefreshGroupId(NamespacedKey key) {
        Object2ObjectOpenHashMap<String, String> map = containerRefreshGroupIds.get(key.getNamespace());
        if (map == null) {
            return null;
        }
        return map.get(key.getKey());
    }

    public String getLootTableRefreshGroupId(String namespace, String key) {
        Object2ObjectOpenHashMap<String, String> map = containerRefreshGroupIds.get(namespace.toLowerCase());
        if (map == null) {
            return null;
        }
        return map.get(key.toLowerCase());
    }

    public void setLootTableRefreshGroupId(NamespacedKey key, String groupId) {
        Object2ObjectOpenHashMap<String, String> map = containerRefreshGroupIds.get(key.getNamespace());
        if (groupId == null || groupId.isBlank()) {
            if (map == null) {
                return;
            }
            if (map.remove(key) != null) {
                setDirty();
                if (map.isEmpty()) {
                    containerRefreshGroupIds.remove(key.getNamespace());
                }
            }
            return;
        }
        if (map == null) {
            map = new Object2ObjectOpenHashMap<>();
            containerRefreshGroupIds.put(key.getNamespace(), map);
        }
        String prev = map.put(key.getKey(), groupId);
        if (!groupId.equals(prev)) {
            setDirty();
        }
    }

    public void setLootTableRefreshGroupId(String namespace, String key, String groupId) {
        Object2ObjectOpenHashMap<String, String> map = containerRefreshGroupIds.get(namespace = namespace.toLowerCase());
        if (groupId == null || groupId.isBlank()) {
            if (map == null) {
                return;
            }
            if (map.remove(key) != null) {
                setDirty();
                if (map.isEmpty()) {
                    containerRefreshGroupIds.remove(namespace);
                }
            }
            return;
        }
        if (map == null) {
            map = new Object2ObjectOpenHashMap<>();
            containerRefreshGroupIds.put(namespace, map);
        }
        String prev = map.put(key.toLowerCase(), groupId);
        if (!groupId.equals(prev)) {
            setDirty();
        }
    }

    public void clearLootTableRefreshGroupIds() {
        if (containerRefreshGroupIds.isEmpty()) {
            return;
        }
        containerRefreshGroupIds.clear();
        setDirty();
    }

    /*
     * Access Blacklist
     */

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public boolean isStructureBlacklisted(NamespacedKey key) {
        ObjectOpenHashSet<String> set = blacklistedStructures.get(key.getNamespace());
        return set != null && set.contains(key.getKey());
    }

    public boolean isStructureBlacklisted(String namespace, String key) {
        ObjectOpenHashSet<String> set = blacklistedStructures.get(namespace.toLowerCase());
        return set != null && set.contains(key.toLowerCase());
    }

    public boolean isLootTableBlacklisted(NamespacedKey key) {
        ObjectOpenHashSet<String> set = blacklistedLootTables.get(key.getNamespace());
        return set != null && set.contains(key.getKey());
    }

    public boolean isLootTableBlacklisted(String namespace, String key) {
        ObjectOpenHashSet<String> set = blacklistedLootTables.get(namespace.toLowerCase());
        return set != null && set.contains(key.toLowerCase());
    }

    public void setStructureBlacklisted(NamespacedKey key, boolean blacklisted) {
        ObjectOpenHashSet<String> set = blacklistedStructures.get(key.getNamespace());
        if (!blacklisted) {
            if (set == null || set.isEmpty()) {
                return;
            }
            if (set.remove(key.getKey())) {
                setDirty();
                if (set.isEmpty()) {
                    blacklistedStructures.remove(key.getNamespace());
                }
            }
            return;
        }
        if (set == null) {
            set = new ObjectOpenHashSet<>();
            blacklistedStructures.put(key.getNamespace(), set);
        }
        if (set.add(key.getKey())) {
            setDirty();
        }
    }

    public void setLootTableBlacklisted(NamespacedKey key, boolean blacklisted) {
        ObjectOpenHashSet<String> set = blacklistedLootTables.get(key.getNamespace());
        if (!blacklisted) {
            if (set == null || set.isEmpty()) {
                return;
            }
            if (set.remove(key.getKey())) {
                setDirty();
                if (set.isEmpty()) {
                    blacklistedLootTables.remove(key.getNamespace());
                }
            }
            return;
        }
        if (set == null) {
            set = new ObjectOpenHashSet<>();
            blacklistedLootTables.put(key.getNamespace(), set);
        }
        if (set.add(key.getKey())) {
            setDirty();
        }
    }

    public void clearBlacklistedStructures() {
        if (blacklistedStructures.isEmpty()) {
            return;
        }
        blacklistedStructures.clear();
        setDirty();
    }

    public void clearBlacklistedStructures(String namespace) {
        if (blacklistedStructures.remove(namespace.toLowerCase()) != null) {
            setDirty();
        }
    }

    public void clearBlacklistedLootTables() {
        if (blacklistedLootTables.isEmpty()) {
            return;
        }
        blacklistedLootTables.clear();
        setDirty();
    }

    public void clearBlacklistedLootTables(String namespace) {
        if (blacklistedLootTables.remove(namespace.toLowerCase()) != null) {
            setDirty();
        }
    }

    public ObjectList<NamespacedKey> getBlacklistedStructures() {
        return blacklistedStructures.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(key -> NamespacedKey.fromString(entry.getKey() + ':' + key)))
            .collect(ObjectArrayList.toList());
    }

    public ObjectList<NamespacedKey> getBlacklistedLootTables() {
        return blacklistedLootTables.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(key -> NamespacedKey.fromString(entry.getKey() + ':' + key)))
            .collect(ObjectArrayList.toList());
    }

    public boolean isCompatibilityContainerBlacklisted(String plugin) {
        return blacklistedCompatibilities.contains(plugin);
    }

    public void setCompatibilityContainerBlacklisted(String plugin, boolean blacklisted) {
        if (!blacklisted) {
            if (blacklistedCompatibilities.isEmpty()) {
                return;
            }
            if (blacklistedCompatibilities.remove(plugin)) {
                setDirty();
            }
            return;
        }
        if (blacklistedCompatibilities.add(plugin)) {
            setDirty();
        }
    }

    public void clearBlacklistedCompatibilityContainers() {
        if (blacklistedCompatibilities.isEmpty()) {
            return;
        }
        blacklistedCompatibilities.clear();
        setDirty();
    }

    public ObjectSet<String> getBlacklistedCompatibilityContainers() {
        return ObjectSets.unmodifiable(blacklistedCompatibilities);
    }

    public boolean areVanillaContainersBlacklisted() {
        return blacklistVanillaContainers;
    }

    public void setVanillaContainersBlacklisted(boolean blacklisted) {
        if (this.blacklistVanillaContainers == blacklisted) {
            return;
        }
        this.blacklistVanillaContainers = blacklisted;
        setDirty();
    }

    public boolean areStaticContainersBlacklisted() {
        return blacklistStaticContainers;
    }

    public void setStaticContainersBlacklisted(boolean blacklisted) {
        if (this.blacklistStaticContainers == blacklisted) {
            return;
        }
        this.blacklistStaticContainers = blacklisted;
        setDirty();
    }

    public boolean areFrameContainersBlacklisted() {
        return blacklistFrameContainers;
    }

    public void setFrameContainersBlacklisted(boolean blacklisted) {
        if (this.blacklistFrameContainers == blacklisted) {
            return;
        }
        this.blacklistFrameContainers = blacklisted;
        setDirty();
    }

    /*
     * Helper
     */

    private void addAllLowerCase(ObjectCollection<String> output, Collection<String> input) {
        for (String in : input) {
            output.add(in.toLowerCase());
        }
    }

}
