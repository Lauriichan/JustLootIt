package me.lauriichan.spigot.justlootit.compatibility.provider;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.reflection.StackTracker;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

public record CompatDependency(String name, int minMajor, int maxMajor, int minMinor, int maxMinor, ICompatProvider provider) {

    private static final Object2ObjectArrayMap<String, ObjectArrayList<CompatDependency>> DEPENDENCIES = new Object2ObjectArrayMap<>();
    private static final Object2ObjectArrayMap<String, CompatDependency> ACTIVE_DEPENDENCY = new Object2ObjectArrayMap<>();

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

                try {
                    if (enabled) {
                        if (ACTIVE_DEPENDENCY.containsKey(dependency.name())) {
                            break;
                        }
                        justlootit.logger().info("Enabling compatibility for {0}.", dependency.name());
                        dependency.provider().onEnable(justlootit, plugin);
                        ACTIVE_DEPENDENCY.put(dependency.name(), dependency);
                        break;
                    }
                    if (!ACTIVE_DEPENDENCY.containsKey(dependency.name())) {
                        break;
                    }
                    justlootit.logger().info("Disabling compatibility for {0}.", dependency.name());
                    dependency.provider().onDisable(justlootit, plugin);
                    ACTIVE_DEPENDENCY.remove(dependency.name());
                } catch (Throwable exception) {
                    justlootit.logger().error("Failed to {0} compatibility for {1}.", exception, enabled ? "enable" : "disable",
                        dependency.name());
                }
                break;
            }
        }
    }

    public static CompatDependency getActive(String name) {
        return ACTIVE_DEPENDENCY.get(name);
    }

    public static <P extends ICompatProvider> P getActiveProvider(String name, Class<P> providerType) {
        CompatDependency dependency = ACTIVE_DEPENDENCY.get(name);
        if (dependency == null) {
            return null;
        }
        ICompatProvider provider = dependency.provider();
        if (providerType.isAssignableFrom(provider.getClass())) {
            return providerType.cast(provider);
        }
        return null;
    }

    public static boolean isActive(String name) {
        return ACTIVE_DEPENDENCY.containsKey(name);
    }

    public static CompatDependency[] get(String name) {
        ObjectArrayList<CompatDependency> list = DEPENDENCIES.get(name);
        if (list == null) {
            return new CompatDependency[0];
        }
        return list.toArray(CompatDependency[]::new);
    }

    public static boolean has(String name) {
        return DEPENDENCIES.containsKey(name);
    }

    public CompatDependency(String name, int major, int minor, ICompatProvider provider) {
        this(name, major, -1, minor, -1, provider);
    }

    public CompatDependency(String name, int minMajor, int maxMajor, int minMinor, int maxMinor, ICompatProvider provider) {
        this.name = name;
        this.minMajor = minMajor;
        this.maxMajor = maxMajor;
        this.minMinor = minMinor;
        this.maxMinor = maxMinor;
        this.provider = Objects.requireNonNull(provider);
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