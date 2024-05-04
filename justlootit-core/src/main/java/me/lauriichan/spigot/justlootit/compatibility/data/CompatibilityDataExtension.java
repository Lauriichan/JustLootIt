package me.lauriichan.spigot.justlootit.compatibility.data;

import org.bukkit.Material;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;

@ExtensionPoint
public abstract class CompatibilityDataExtension<I extends ICompatibilityData> implements IExtension {

    private static final Object2ObjectArrayMap<String, CompatibilityDataExtension<?>> EXTENSIONS = new Object2ObjectArrayMap<>();

    public static CompatibilityDataExtension<?> get(String id) {
        return EXTENSIONS.get(id);
    }

    public static <E extends CompatibilityDataExtension<?>> E get(String id, Class<E> type) {
        CompatibilityDataExtension<?> extension = EXTENSIONS.get(id);
        if (extension == null || !type.isAssignableFrom(extension.getClass())) {
            return null;
        }
        return type.cast(extension);
    }
    
    private final String id;
    private final Class<I> type;

    public CompatibilityDataExtension(String id, Class<I> type) {
        this.id = id;
        this.type = type;
        if (EXTENSIONS.containsKey(id)) {
            throw new IllegalStateException("Id already known: " + id);
        }
        EXTENSIONS.put(id, this);
    }

    public final String id() {
        return id;
    }

    public final Class<I> type() {
        return type;
    }
    
    public final boolean isActive() {
        return CompatDependency.isActive(id());
    }

    public abstract Material iconType();

    public void modifyIcon(ItemEditor editor) {}

    public final boolean hasUpgrade(ICompatibilityData data) {
        if (type.isAssignableFrom(data.getClass())) {
            return hasUpgradeImpl(type.cast(data));
        }
        return false;
    }

    public final I upgrade(ICompatibilityData data) {
        if (type.isAssignableFrom(data.getClass())) {
            return upgradeImpl(type.cast(data));
        }
        return null;
    }

    public final void saveSafe(ByteBuf buffer, ICompatibilityData data) {
        if (type.isAssignableFrom(data.getClass())) {
            save(buffer, type.cast(data));
        }
    }

    public abstract void save(ByteBuf buffer, I data);

    public abstract I load(ByteBuf buffer, int version);

    protected boolean hasUpgradeImpl(I data) {
        return false;
    }

    protected I upgradeImpl(I data) {
        return data;
    }

}
