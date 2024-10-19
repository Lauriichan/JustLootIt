package me.lauriichan.spigot.justlootit.command.helper;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.command.argument.LootTableArgument;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.ContainerType;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.StaticContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.EntityUtil;

public final class ContainerCreationHelper {

    @SuppressWarnings("rawtypes")
    private static final Predicate ALLOW_ALL_PREDICATE = any -> true;

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> allowAll() {
        return ALLOW_ALL_PREDICATE;
    }

    public static final class BlockContainerCreator {

        @FunctionalInterface
        public static interface CreatorFunction {
            void create(final JustLootItPlugin plugin, final Actor<?> actor, final Location location,
                final org.bukkit.block.Container block, final BiConsumer<Supplier<Container>, LongConsumer> creator);
        }

        private final Class<? extends Container> containerType;
        private final CreatorFunction function;
        private final Predicate<org.bukkit.block.Container> supportedTest;

        public BlockContainerCreator(Class<? extends Container> containerType, CreatorFunction function) {
            this(containerType, function, allowAll());
        }

        public BlockContainerCreator(Class<? extends Container> containerType, CreatorFunction function,
            Predicate<org.bukkit.block.Container> supportedTest) {
            this.containerType = containerType;
            this.function = function;
            this.supportedTest = supportedTest;
        }

        public Class<? extends Container> containerType() {
            return containerType;
        }

        public CreatorFunction function() {
            return function;
        }

        public Predicate<org.bukkit.block.Container> supportedTest() {
            return supportedTest;
        }

    }

    public static final class EntityContainerCreator {

        @FunctionalInterface
        public static interface CreatorFunction {
            void create(final JustLootItPlugin plugin, final Actor<?> actor, final Location location, final Entity entity,
                final BiConsumer<Supplier<Container>, LongConsumer> creator);
        }

        private final Class<? extends Container> containerType;
        private final CreatorFunction function;
        private final Predicate<Entity> supportedTest;

        public EntityContainerCreator(Class<? extends Container> containerType, CreatorFunction function) {
            this(containerType, function, allowAll());
        }

        public EntityContainerCreator(Class<? extends Container> containerType, CreatorFunction function, Predicate<Entity> supportedTest) {
            this.containerType = containerType;
            this.function = function;
            this.supportedTest = supportedTest;
        }

        public Class<? extends Container> containerType() {
            return containerType;
        }

        public CreatorFunction function() {
            return function;
        }

        public Predicate<Entity> supportedTest() {
            return supportedTest;
        }

    }

    private ContainerCreationHelper() {
        throw new UnsupportedOperationException();
    }

    public static final EntityContainerCreator ENTITY_FRAME = new EntityContainerCreator(FrameContainer.class,
        (plugin, actor, location, entity, creator) -> {
            // We check for ItemFrame here since we need the interface, but theoretically we shouldn't need to and could just cast.
            if (!(entity instanceof ItemFrame itemFrame)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_UNSUPPORTED_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                    Key.of("type", ContainerType.FRAME));
                return;
            }
            if (JustLootItAccess.hasIdentity(itemFrame.getPersistentDataContainer())) {
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
            creator.accept(() -> new FrameContainer(itemStack.clone()), id -> {
                JustLootItAccess.setIdentity(itemFrame.getPersistentDataContainer(), id);
                itemFrame.setItem(null);
            });
        }, EntityUtil::isItemFrame);

    public static final EntityContainerCreator ENTITY_VANILLA = new EntityContainerCreator(VanillaContainer.class,
        (plugin, actor, location, entity, creator) -> {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            if (JustLootItAccess.hasIdentity(dataContainer)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            plugin.inputProvider().getLongInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_SEED),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_SEED), (a1, seed) -> {
                    if (seed == null) {
                        return;
                    }
                    plugin.inputProvider().getStringInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_KEY),
                        actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_KEY),
                        (str) -> LootTableArgument.isLootTable(plugin.versionHelper(), str), (a2, rawLootTable) -> {
                            if (rawLootTable == null) {
                                return;
                            }
                            LootTable lootTable = LootTableArgument.parseLootTable(plugin.versionHelper(), rawLootTable);
                            plugin.scheduler().syncEntity(entity, () -> {
                                if (entity.isDead()) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_CHANGED_ENTITY,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                if (JustLootItAccess.hasIdentity(dataContainer)) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                creator.accept(() -> new VanillaContainer(lootTable, seed), id -> {
                                    JustLootItAccess.setIdentity(dataContainer, id);
                                    ((InventoryHolder) entity).getInventory().clear();
                                });
                            });
                        });
                });
        }, EntityUtil::isSupportedEntity);
    public static final BlockContainerCreator BLOCK_VANILLA = new BlockContainerCreator(VanillaContainer.class,
        (plugin, actor, location, block, creator) -> {
            PersistentDataContainer blockData = block.getPersistentDataContainer();
            if (JustLootItAccess.hasIdentity(blockData) || JustLootItAccess.hasAnyOffset(blockData)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            plugin.inputProvider().getLongInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_SEED),
                actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_SEED), (a1, seed) -> {
                    if (seed == null) {
                        return;
                    }
                    plugin.inputProvider().getStringInput(actor, actor.getTranslatedMessageAsString(Messages.INPUT_PROMPT_LOOTTABLE_KEY),
                        actor.getTranslatedMessageAsString(Messages.INPUT_RETRY_LOOTTABLE_KEY),
                        (str) -> LootTableArgument.isLootTable(plugin.versionHelper(), str), (a2, rawLootTable) -> {
                            if (rawLootTable == null) {
                                return;
                            }
                            LootTable lootTable = LootTableArgument.parseLootTable(plugin.versionHelper(), rawLootTable);
                            plugin.scheduler().syncRegional(location, () -> {
                                if (!(block.getBlock().getState() instanceof org.bukkit.block.Container blockContainer)) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_CHANGED_BLOCK,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                PersistentDataContainer dataContainer = blockContainer.getPersistentDataContainer();
                                if (JustLootItAccess.hasIdentity(dataContainer)) {
                                    actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK,
                                        Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()),
                                        Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                                    return;
                                }
                                creator.accept(() -> new VanillaContainer(lootTable, seed), id -> {
                                    BlockUtil.setContainerOffsetToNearbyChest(blockContainer);
                                    JustLootItAccess.setIdentity(dataContainer, id);
                                    blockContainer.update(false, false);
                                    blockContainer.getInventory().clear();
                                });
                            });
                        });
                });
        });

    public static final EntityContainerCreator ENTITY_STATIC = new EntityContainerCreator(StaticContainer.class,
        (plugin, actor, location, entity, creator) -> {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            if (JustLootItAccess.hasIdentity(dataContainer)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            InventoryHolder holder = (InventoryHolder) entity;
            creator.accept(() -> new StaticContainer(holder.getInventory()), id -> {
                JustLootItAccess.setIdentity(dataContainer, id);
                holder.getInventory().clear();
            });
        }, EntityUtil::isSupportedEntity);
    public static final BlockContainerCreator BLOCK_STATIC = new BlockContainerCreator(StaticContainer.class,
        (plugin, actor, location, block, creator) -> {
            PersistentDataContainer dataContainer = block.getPersistentDataContainer();
            if (JustLootItAccess.hasIdentity(dataContainer) || JustLootItAccess.hasAnyOffset(dataContainer)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_ALREADY_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
                return;
            }
            creator.accept(() -> new StaticContainer(block.getInventory()), id -> {
                BlockUtil.setContainerOffsetToNearbyChest(block);
                JustLootItAccess.setIdentity(dataContainer, id);
                block.update(false, false);
                block.getInventory().clear();
            });
        });
}
