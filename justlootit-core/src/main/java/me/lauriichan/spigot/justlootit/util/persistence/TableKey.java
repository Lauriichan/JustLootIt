package me.lauriichan.spigot.justlootit.util.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.inventory.handler.tables.LootTableViewerTabPage.LootTableType;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;

public record TableKey(LootTableType type, String namespace, String key) {

    public static final SimpleDataType<PersistentDataContainer, TableKey> KEY_TYPE = new SimpleDataType<>(PersistentDataContainer.class,
        TableKey.class) {

        @Override
        public PersistentDataContainer toPrimitive(TableKey complex, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(JustLootItKey.tableType(), PersistentDataType.STRING, complex.type().name());
            container.set(JustLootItKey.tableNamespace(), PersistentDataType.STRING, complex.namespace());
            container.set(JustLootItKey.tableKey(), PersistentDataType.STRING, complex.key());
            return container;
        }

        @Override
        public TableKey fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
            return new TableKey(LootTableType.valueOf(primitive.get(JustLootItKey.tableType(), PersistentDataType.STRING).toUpperCase()),
                primitive.get(JustLootItKey.tableNamespace(), PersistentDataType.STRING),
                primitive.get(JustLootItKey.tableKey(), PersistentDataType.STRING));
        }

    };

}
