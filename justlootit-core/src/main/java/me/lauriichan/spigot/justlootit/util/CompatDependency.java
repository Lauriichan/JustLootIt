package me.lauriichan.spigot.justlootit.util;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.reflection.StackTracker;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

public record CompatDependency(String name, int minMajor, int maxMajor, int minMinor, int maxMinor, DependencyExecutable enable,
    DependencyExecutable disable) {

    @FunctionalInterface
    public static interface DependencyExecutable {

        void execute(JustLootItPlugin justlootit, Plugin plugin);

    }

    private static final Object2ObjectArrayMap<String, ObjectArrayList<CompatDependency>> DEPENDENCIES = new Object2ObjectArrayMap<>();

    public static void updateAll(JustLootItPlugin jliPlugin) {
        Class<?> caller = StackTracker.getCallerClass().orElse(null);
        if (caller == null || JustLootItPlugin.class.getClassLoader() != caller.getClassLoader()) {
            throw new UnsupportedOperationException("Only JustLootIt is allowed to update JustLootIt dependencies");
        }
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        for (Plugin plugin : plugins) {
            if (jliPlugin == plugin || !plugin.isEnabled()) {
                continue;
            }
            handleUpdate(jliPlugin, plugin, true);
        }
    }

    public static void handleUpdate(JustLootItPlugin justlootit, Plugin plugin, boolean enabled) {
        Class<?> caller = StackTracker.getCallerClass().orElse(null);
        if (caller == null || JustLootItPlugin.class.getClassLoader() != caller.getClassLoader()) {
            throw new UnsupportedOperationException("Only JustLootIt is allowed to update JustLootIt dependencies");
        }
        ObjectArrayList<CompatDependency> dependencies = DEPENDENCIES.get(plugin.getName());
        if (dependencies == null) {
            return;
        }
        for (CompatDependency dependency : dependencies) {
            if (dependency.isSupported(plugin)) {
                justlootit.logger().info("{0} compatibility for {1}.", enabled ? "Enabling" : "Disabling", dependency.name());
                try {
                    (enabled ? dependency.enable() : dependency.disable()).execute(justlootit, plugin);
                } catch (Throwable exception) {
                    justlootit.logger().error("Failed to {0} compatibility for {1}.", exception, enabled ? "enable" : "disable",
                        dependency.name());
                }
                break;
            }
        }
    }

    public CompatDependency(String name, int minMajor, int maxMajor, int minMinor, int maxMinor, DependencyExecutable enable,
        DependencyExecutable disable) {
        this.name = name;
        this.minMajor = minMajor;
        this.maxMajor = maxMajor;
        this.minMinor = minMinor;
        this.maxMinor = maxMinor;
        this.enable = Objects.requireNonNull(enable);
        this.disable = Objects.requireNonNull(disable);
        Class<?> caller = StackTracker.getCallerClass().orElse(null);
        if (caller == null || JustLootItPlugin.class.getClassLoader() != caller.getClassLoader()) {
            throw new UnsupportedOperationException("Only JustLootIt is allowed to create JustLootIt dependencies");
        }
        ObjectArrayList<CompatDependency> dependencies = DEPENDENCIES.get(name);
        if (dependencies == null) {
            dependencies = new ObjectArrayList<>();
            DEPENDENCIES.put(name, dependencies);
        }
        dependencies.add(this);
    }

    public CompatDependency(String name, int major, int minor, DependencyExecutable enable, DependencyExecutable disable) {
        this(name, major, -1, minor, -1, enable, disable);
    }

    private boolean isSupported(Plugin plugin) {
        String version = plugin.getDescription().getVersion();
        if (!version.contains(".")) {
            return false;
        }
        String[] ver = version.split("\\.", 3);
        try {
            int major = Integer.parseInt(ver[0]);
            int minor = Integer.parseInt(ver[1]);
            if (major > minMajor) {
                return maxMajor == -1 || major <= maxMajor;
            }
            if (major == minMajor && minor >= minMinor) {
                return maxMinor == -1 || minor <= maxMinor;
            }
            return false;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}