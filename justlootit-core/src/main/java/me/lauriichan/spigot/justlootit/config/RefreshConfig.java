package me.lauriichan.spigot.justlootit.config;

import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.IConfigHandler;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.handler.JsonConfigHandler;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.util.DataHelper;

@Extension
public class RefreshConfig implements ISingleConfigExtension {

    private final Object2ObjectOpenHashMap<String, RefreshGroup> groups = new Object2ObjectOpenHashMap<>();
    private volatile boolean modified = false;

    @Override
    public IConfigHandler handler() {
        return JsonConfigHandler.JSON;
    }

    @Override
    public String path() {
        return "data://refresh_groups.json";
    }
    
    @Override
    public boolean isModified() {
        return modified;
    }
    
    public void setDirty() {
        modified = true;
    }
    
    @Override
    public void onPropergate(Configuration configuration) throws Exception {
        Configuration section = configuration.getConfiguration("example", true);
        section.set("__comment", "This an example group (which can also be used ingame), it defines a refresh interval of 3 hours.");
        section.set("unit", TimeUnit.HOURS.name());
        section.set("time", 3);
    }

    @Override
    public void onLoad(Configuration configuration) throws Exception {
        this.modified = false;
        ObjectArrayList<String> list = new ObjectArrayList<>();
        list.addAll(groups.keySet());
        for (String key : configuration.keySet()) {
            if (!configuration.isConfiguration(key)) {
                continue;
            }
            list.remove(key);
            Configuration groupConfig = configuration.getConfiguration(key);
            RefreshGroup group = groups.get(key);
            if (group == null) {
                groups.put(key, group = new RefreshGroup(key));
            }
            TimeUnit unit = groupConfig.getEnum("unit", TimeUnit.class, TimeUnit.MILLISECONDS);
            long time = groupConfig.getLong("time", 0);
            if (unit == TimeUnit.NANOSECONDS || unit == TimeUnit.MICROSECONDS) {
                time = DataHelper.unsupportedToMillis(time, unit);
                unit = TimeUnit.MILLISECONDS;
            }
            group.set(time, unit);
        }
        for (String entry : list) {
            groups.remove(entry);
        }
    }

    @Override
    public void onSave(Configuration configuration) throws Exception {
        this.modified = false;
        for (RefreshGroup group : groups.values()) {
            Configuration groupConfig = configuration.getConfiguration(group.id(), true);
            groupConfig.set("unit", group.unit().name());
            groupConfig.set("time", group.timeoutTime());
        }
    }
    
    public boolean deleteGroup(String id) {
        if (groups.remove(id) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public RefreshGroup getOrCreateGroup(String id) {
        RefreshGroup group = groups.get(id);
        if (group == null) {
            group = new RefreshGroup(id);
            groups.put(id, group);
            setDirty();
        }
        return group;
    }

    public RefreshGroup group(String id) {
        return groups.get(id);
    }

    public ObjectCollection<RefreshGroup> groups() {
        return groups.values();
    }

    public ObjectSet<String> ids() {
        return groups.keySet();
    }

}
