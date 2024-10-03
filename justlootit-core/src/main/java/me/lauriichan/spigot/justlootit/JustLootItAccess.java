package me.lauriichan.spigot.justlootit;

import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;
import me.lauriichan.spigot.justlootit.util.persistence.BreakData;

public final class JustLootItAccess {

    private JustLootItAccess() {
        throw new UnsupportedOperationException();
    }
    
    /*
     * Has
     */

    public static boolean hasIdentity(PersistentDataContainer container) {
        return container.has(JustLootItKey.identity(), SimpleDataType.LONG);
    }

    public static boolean hasAnyOffset(PersistentDataContainer container) {
        return hasOffset(container) || hasOffsetV1(container) || hasLegacyOffset(container);
    }

    public static boolean hasOffset(PersistentDataContainer container) {
        return container.has(JustLootItKey.chestOffset(), SimpleDataType.OFFSET_VECTOR);
    }

    public static boolean hasOffsetV1(PersistentDataContainer container) {
        return container.has(JustLootItKey.chestOffsetV1(), SimpleDataType.OFFSET_VECTOR_V1);
    }

    public static boolean hasLegacyOffset(PersistentDataContainer container) {
        return container.has(JustLootItKey.legacyChestOffset(), SimpleDataType.LEGACY_OFFSET_VECTOR);
    }

    public static boolean hasBreakData(PersistentDataContainer container) {
        return container.has(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE);
    }
    
    /*
     * Get
     */

    public static long getIdentity(PersistentDataContainer container) {
        return container.get(JustLootItKey.identity(), SimpleDataType.LONG);
    }

    public static Vec3i getAnyOffset(PersistentDataContainer container) {
        Vec3i offset = getOffset(container);
        if (offset != null) {
            return offset;
        }
        offset = getOffsetV1(container);
        if (offset != null) {
            return offset;
        }
        return getLegacyOffset(container);
    }

    public static Vec3i getOffset(PersistentDataContainer container) {
        if (container.has(JustLootItKey.chestOffset(), SimpleDataType.OFFSET_VECTOR)) {
            return container.get(JustLootItKey.chestOffset(), SimpleDataType.OFFSET_VECTOR);
        }
        return null;
    }

    public static Vec3i getOffsetV1(PersistentDataContainer container) {
        if (container.has(JustLootItKey.chestOffsetV1(), SimpleDataType.OFFSET_VECTOR_V1)) {
            return container.get(JustLootItKey.chestOffsetV1(), SimpleDataType.OFFSET_VECTOR_V1);
        }
        return null;
    }

    public static Vec3i getLegacyOffset(PersistentDataContainer container) {
        if (container.has(JustLootItKey.legacyChestOffset(), SimpleDataType.LEGACY_OFFSET_VECTOR)) {
            return container.get(JustLootItKey.legacyChestOffset(), SimpleDataType.LEGACY_OFFSET_VECTOR);
        }
        return null;
    }

    public static BreakData getBreakData(PersistentDataContainer container) {
        if (container.has(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE)) {
            return container.get(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE);
        }
        return null;
    }

    /*
     * Placeholder
     */

    public static Key getIdentityKey(String key, PersistentDataContainer container, String fallback) {
        if (hasIdentity(container)) {
            return Key.of(key, getIdentity(container));
        }
        return Key.of(key, fallback);
    }

    public static Key getOffsetKey(String key, PersistentDataContainer container, String fallback) {
        Vec3i vec = getOffset(container);
        if (vec == null) {
            return Key.of(key, fallback);
        }
        return Key.of(key, vec);
    }

    public static Key getOffsetV1Key(String key, PersistentDataContainer container, String fallback) {
        Vec3i vec = getOffsetV1(container);
        if (vec == null) {
            return Key.of(key, fallback);
        }
        return Key.of(key, vec);
    }

    public static Key getLegacyOffsetKey(String key, PersistentDataContainer container, String fallback) {
        Vec3i vec = getLegacyOffset(container);
        if (vec == null) {
            return Key.of(key, fallback);
        }
        return Key.of(key, vec);
    }

    public static Key getBreakDataKey(String key, PersistentDataContainer container, String fallback) {
        BreakData data = getBreakData(container);
        if (data == null) {
            return Key.of(key, fallback);
        }
        return Key.of(key, data);
    }
    
    /*
     * Remove
     */

    public static void removeIdentity(PersistentDataContainer container) {
        container.remove(JustLootItKey.identity());
    }

    public static void removeOffset(PersistentDataContainer container) {
        container.remove(JustLootItKey.chestOffset());
    }

    public static void removeOffsetV1(PersistentDataContainer container) {
        container.remove(JustLootItKey.chestOffsetV1());
    }

    public static void removeLegacyOffset(PersistentDataContainer container) {
        container.remove(JustLootItKey.legacyChestOffset());
    }

    public static void removeBreakData(PersistentDataContainer container) {
        container.remove(JustLootItKey.breakData());
    }
    
    /*
     * Set
     */

    public static void setIdentity(PersistentDataContainer container, long id) {
        container.set(JustLootItKey.identity(), SimpleDataType.LONG, id);
    }

    public static void setOffset(PersistentDataContainer container, Vec3i offset) {
        container.set(JustLootItKey.chestOffset(), SimpleDataType.OFFSET_VECTOR, offset);
    }

    public static void setBreakData(PersistentDataContainer container, BreakData breakData) {
        container.set(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE, breakData);
    }

}
