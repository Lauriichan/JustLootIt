package me.lauriichan.spigot.justlootit.nms;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.capability.Capable;
import me.lauriichan.spigot.justlootit.nms.packet.AbstractPacketOut;

public abstract class PlayerAdapter extends Capable<PlayerAdapter> {

    protected final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
    protected final UUID uniqueId;
    
    protected final long uniqueSeed;

    public PlayerAdapter(final UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.uniqueSeed = uniqueId.getLeastSignificantBits() * uniqueId.getMostSignificantBits();
    }

    public abstract VersionHandler versionHandler();

    public abstract Object asMinecraft();

    public String getName() {
        return asBukkit().getName();
    }

    public final UUID getUniqueId() {
        return uniqueId;
    }
    
    public final long getUniqueSeed() {
        return uniqueSeed;
    }

    public final void removeData(final String key) {
        this.data.remove(key);
    }

    public final boolean hasData(final String key) {
        return data.containsKey(key);
    }

    public final boolean hasData(final String key, final Class<?> type) {
        final Object value = data.get(key);
        return value != null && type.isAssignableFrom(value.getClass());
    }

    public final Object getData(final String key) {
        return data.get(key);
    }

    public final Object getDataOrFallback(final String key, final Object fallback) {
        final Object value = data.get(key);
        if (value == null) {
            return fallback;
        }
        return value;
    }

    public final <E> E getData(final String key, final Class<E> type) {
        final Object value = this.data.get(key);
        if (value == null || !type.isAssignableFrom(value.getClass())) {
            return null;
        }
        return type.cast(value);
    }

    public final <E> E getDataOrFallback(final String key, final E fallback, final Class<E> type) {
        final Object value = this.data.get(key);
        if (value == null || !type.isAssignableFrom(value.getClass())) {
            return fallback;
        }
        return type.cast(value);
    }

    public final void setData(final String key, final Object data) {
        this.data.put(key, data);
    }

    public final int createAnvilMenu(final String name) {
        return createAnvilMenu(name, new ItemStack(Material.PAPER));
    }

    public abstract int createAnvilMenu(String name, ItemStack placeholderItem);

    public abstract void reopenMenu();

    public abstract void closeMenu();

    public abstract LevelAdapter getLevel();

    public abstract IPlayerNetwork getNetwork();

    public abstract Player asBukkit();

    public abstract int getPermissionLevel();

    public abstract void acknowledgeBlockChangesUpTo(int sequence);

    public abstract void send(AbstractPacketOut... packets);

}
