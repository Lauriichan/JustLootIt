package me.lauriichan.spigot.justlootit.command.helper;

import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.command.argument.LootTableArgument;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.ContainerType;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.EntityUtil;

public final class ContainerCreationHelper {

    public static final class BlockContainerCreator {

        @FunctionalInterface
        public static interface CreatorFunction {
            void create(final JustLootItPlugin plugin, final Actor<?> actor, final Location location,
                final org.bukkit.block.Container block, final Consumer<Function<Long, Container>> creator);
        }

        private final Class<? extends Container> containerType;
        private final CreatorFunction function;

        public BlockContainerCreator(Class<? extends Container> containerType, CreatorFunction function) {
            this.containerType = containerType;
            this.function = function;
        }

        public Class<? extends Container> containerType() {
            return containerType;
        }

        public CreatorFunction function() {
            return function;
        }

    }

    public static final class EntityContainerCreator {

        @FunctionalInterface
        public static interface CreatorFunction {
            void create(final JustLootItPlugin plugin, final Actor<?> actor, final Location location, final Entity entity,
                final Consumer<Function<Long, Container>> creator);
        }

        private final Class<? extends Container> containerType;
        private final CreatorFunction function;

        public EntityContainerCreator(Class<? extends Container> containerType, CreatorFunction function) {
            this.containerType = containerType;
            this.function = function;
        }

        public Class<? extends Container> containerType() {
            return containerType;
        }

        public CreatorFunction function() {
            return function;
        }

    }

    private ContainerCreationHelper() {
        throw new UnsupportedOperationException();
    }

    public static final EntityContainerCreator ENTITY_FRAME = new EntityContainerCreator(FrameContainer.class,
        (plugin, actor, location, entity, creator) -> {
            if (!(entity instanceof ItemFrame itemFrame)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_UNSUPPORTED_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                    Key.of("type", ContainerType.FRAME));
                return;
            }
            if (itemFrame.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            final ItemStack itemStack = itemFrame.getItem();
            if (itemStack == null || itemStack.getType().isAir()) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_FRAME_ITEM_REQUIRED, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            creator.accept((id) -> {
                itemFrame.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                itemFrame.setItem(null);
                return new FrameContainer(id, itemStack.clone());
            });
        });

    public static final EntityContainerCreator ENTITY_VANILLA = new EntityContainerCreator(VanillaContainer.class,
        (plugin, actor, location, entity, creator) -> {
            if (!EntityUtil.isSuppportedEntity(entity)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_UNSUPPORTED_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                    Key.of("type", ContainerType.VANILLA));
                return;
            }
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            plugin.inputProvider().getLongInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_SEED),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_SEED), (a1, seed) -> {
                    plugin.inputProvider().getStringInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_KEY),
                        actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_KEY),
                        (str) -> LootTableArgument.isLootTable(plugin.versionHelper(), str), (a2, rawLootTable) -> {
                            LootTable lootTable = LootTableArgument.parseLootTable(plugin.versionHelper(), rawLootTable);
                            plugin.scheduler().entity(entity, () -> {
                                if (entity.isDead()) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_CHANGED_ENTITY,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                creator.accept((id) -> {
                                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                                    ((InventoryHolder) entity).getInventory().clear();
                                    return new VanillaContainer(id, lootTable, seed);
                                });
                            });
                        });
                });
        });
    public static final BlockContainerCreator BLOCK_VANILLA = new BlockContainerCreator(VanillaContainer.class,
        (plugin, actor, location, block, creator) -> {
            if (block.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            plugin.inputProvider().getLongInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_SEED),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_SEED), (a1, seed) -> {
                    plugin.inputProvider().getStringInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_KEY),
                        actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_KEY),
                        (str) -> LootTableArgument.isLootTable(plugin.versionHelper(), str), (a2, rawLootTable) -> {
                            LootTable lootTable = LootTableArgument.parseLootTable(plugin.versionHelper(), rawLootTable);
                            plugin.scheduler().regional(location, () -> {
                                if (!(block.getBlock().getState() instanceof org.bukkit.block.Container blockContainer)) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_CHANGED_BLOCK,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                PersistentDataContainer dataContainer = blockContainer.getPersistentDataContainer();
                                if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                creator.accept((id) -> {
                                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                                    blockContainer.update(false, false);
                                    blockContainer.getInventory().clear();
                                    return new VanillaContainer(id, lootTable, seed);
                                });
                            });
                        });
                });
        });

    public static final EntityContainerCreator ENTITY_STATIC = new EntityContainerCreator(StaticContainer.class,
        (plugin, actor, location, entity, creator) -> {
            if (!EntityUtil.isSuppportedEntity(entity)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_UNSUPPORTED_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                    Key.of("type", ContainerType.STATIC));
                return;
            }
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            if (dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            creator.accept((id) -> {
                Inventory inventory = ((InventoryHolder) entity).getInventory();
                try {
                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                    return new StaticContainer(id, inventory);
                } finally {
                    inventory.clear();
                }
            });
        });
    public static final BlockContainerCreator BLOCK_STATIC = new BlockContainerCreator(StaticContainer.class,
        (plugin, actor, location, block, creator) -> {
            PersistentDataContainer dataContainer = block.getPersistentDataContainer();
            if (block.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            creator.accept((id) -> {
                Inventory inventory = block.getInventory();
                try {
                    dataContainer.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
                    return new StaticContainer(id, inventory);
                } finally {
                    block.update(false, false);
                    block.getInventory().clear();
                }
            });
        });

}
