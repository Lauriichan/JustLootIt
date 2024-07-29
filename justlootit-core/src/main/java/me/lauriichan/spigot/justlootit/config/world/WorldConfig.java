package me.lauriichan.spigot.justlootit.config.world;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.NamespacedKey;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;

public class WorldConfig implements IConfigExtension {
    
    private final Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> blacklistedStructures = new Object2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> blacklistedLootTables = new Object2ObjectArrayMap<>();

    private final ObjectOpenHashSet<String> blacklistedCompatibilities = new ObjectOpenHashSet<>();
    
    private volatile boolean blacklistVanillaContainers = false;
    private volatile boolean blacklistStaticContainers = false;
    private volatile boolean blacklistFrameContainers = false;
    
    private volatile boolean modified = false;
    
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
    
    @Override
    public void onPropergate(Configuration configuration) throws Exception {
        propergate(configuration, "blacklist.structures", "example_structure_id", "another_example_structure_id");
        propergate(configuration, "blacklist.loottables", "example/loottable_id/with/category", "loottables/have/categories/example_loottable_name");
        configuration.set("blacklist.containers.vanilla", blacklistVanillaContainers);
        configuration.set("blacklist.containers.static", blacklistStaticContainers);
        configuration.set("blacklist.containers.frame", blacklistFrameContainers);
        configuration.set("blacklist.containers.compatibilities", Collections.emptyList());
    }
    
    @Override
    public void onLoad(Configuration configuration) throws Exception {
        this.modified = false;
        load(configuration, "blacklist.structures", blacklistedStructures);
        load(configuration, "blacklist.loottables", blacklistedLootTables);
        this.blacklistVanillaContainers = configuration.getBoolean("blacklist.containers.vanilla", false);
        this.blacklistStaticContainers = configuration.getBoolean("blacklist.containers.static", false);
        this.blacklistFrameContainers = configuration.getBoolean("blacklist.containers.frame", false);
        List<String> list = configuration.getList("blacklist.containers.compatibilities", String.class);
        blacklistedCompatibilities.clear();
        addAll(blacklistedCompatibilities, list);
    }
    
    @Override
    public void onSave(Configuration configuration) throws Exception {
        this.modified = false;
        save(configuration, "blacklist.structures", blacklistedStructures);
        save(configuration, "blacklist.loottables", blacklistedLootTables);
        configuration.set("blacklist.containers.vanilla", blacklistVanillaContainers);
        configuration.set("blacklist.containers.static", blacklistStaticContainers);
        configuration.set("blacklist.containers.frame", blacklistFrameContainers);
        configuration.set("blacklist.containers.compatibilities", new ObjectArrayList<>(blacklistedCompatibilities));
    }
    
    /*
     * Access
     */
    
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
        return blacklistedStructures.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(key -> NamespacedKey.fromString(entry.getKey() + ':' + key))).collect(ObjectArrayList.toList());
    }
    
    public ObjectList<NamespacedKey> getBlacklistedLootTables() {
        return blacklistedLootTables.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(key -> NamespacedKey.fromString(entry.getKey() + ':' + key))).collect(ObjectArrayList.toList());
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
    
    private void addAll(ObjectCollection<String> output, Collection<String> input) {
        for (String in : input) {
            output.add(in.toLowerCase());
        }
    }
    
    private void propergate(Configuration configuration, String configKey, String... examples) {
        Configuration section = configuration.getConfiguration(configKey, true);
        ObjectArrayList<String> list = new ObjectArrayList<>();
        for (String example : examples) {
            list.add(example);
        }
        section.set("minecraft", list);
    }
    
    private void load(Configuration configuration, String configKey, Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> map) {
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
                addAll(map.get(namespaceKey), list);
                continue;
            }
            ObjectOpenHashSet<String> set = new ObjectOpenHashSet<>();
            map.put(namespaceKey, set);
            addAll(set, list);
        }
    }
    
    private void save(Configuration configuration, String configKey, Object2ObjectArrayMap<String, ObjectOpenHashSet<String>> map) {
        Configuration section = configuration.getConfiguration(configKey, true);
        section.clear();
        for (Entry<String, ObjectOpenHashSet<String>> entry : map.entrySet()) {
            section.set(entry.getKey(), new ObjectArrayList<>(entry.getValue()));
        }
    }

}
