package me.lauriichan.spigot.justlootit.storage;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.logger.util.StringUtil;

public abstract class StorageMigrator {

    private static record Migration(int targetVersion, ObjectList<StorageMigration<?>> migrations) {}

    protected final Object2ObjectArrayMap<Class<?>, Migration> migrations = new Object2ObjectArrayMap<>();
    protected final ISimpleLogger logger;
    
    public StorageMigrator(final ISimpleLogger logger) {
        this.logger = logger;
    }
    
    public final ISimpleLogger logger() {
        return logger;
    }

    public final int getTargetVersion(Class<?> type) {
        Migration migration = migrations.get(type);
        return migration == null ? 0 : migration.targetVersion();
    }

    public final boolean needsMigration(Class<?> type, int version) {
        Migration migration = migrations.get(type);
        return migration != null && version < migration.targetVersion();
    }

    public final Map.Entry<Integer, ByteBuf> migrate(Class<?> type, int version, ByteBuf buffer) {
        Migration migration = migrations.get(type);
        if (migration == null || version >= migration.targetVersion()) {
            return Map.entry(version, buffer);
        }
        ByteBuf nextBuffer = buffer;
        for (StorageMigration<?> migrationExt : migration.migrations()) {
            if (migrationExt.targetVersion() <= version) {
                continue;
            }
            logger.debug("Applying migration '{3}' (version {1} to {2}) for type '{0}'", type.getName(), version,
                migrationExt.targetVersion(), migrationExt.description());
            nextBuffer.resetReaderIndex();
            ByteBuf out = Unpooled.buffer(nextBuffer.readableBytes());
            try {
                migrationExt.migrate(this, nextBuffer, out);
                version = migrationExt.targetVersion();
                nextBuffer = out;
            } catch (Throwable throwable) {
                throw new StorageMigrationFailedException(
                    StringUtil.format("Failed to apply migration '{3}' (version {1} to {2}) for type '{0}'", new Object[] {
                        type.getName(),
                        version,
                        migrationExt.targetVersion(),
                        migrationExt.description()
                    }), throwable);
            }
        }
        nextBuffer.resetReaderIndex();
        return Map.entry(version, nextBuffer);
    }

}
