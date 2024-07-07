package me.lauriichan.spigot.justlootit.command;

import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.command.annotation.Description;
import me.lauriichan.laylib.command.annotation.Param;
import me.lauriichan.laylib.command.annotation.Permission;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPermission;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.PlayerGUICapability;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.command.argument.CoordinateArgument.Coord;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.ContainerType;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.inventory.handler.manage.ContainerPageHandler;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.BlockUtil;
import me.lauriichan.spigot.justlootit.util.CommandUtil;
import me.lauriichan.spigot.justlootit.util.DataHelper;
import me.lauriichan.spigot.justlootit.util.EntityUtil;
import me.lauriichan.spigot.justlootit.util.SimpleDataType;
import me.lauriichan.spigot.justlootit.util.TypeName;
import net.md_5.bungee.api.chat.HoverEvent;

@Extension
@Command(name = "container")
@Permission(JustLootItPermission.COMMAND_CONTAINER)
public class ContainerCommand implements ICommandExtension {

    @Action("manage")
    @Description("$#command.description.justlootit.container.manage.id")
    public void manage(final JustLootItPlugin plugin, final Actor<?> rawActor,
        @Argument(name = "id", index = 1, params = @Param(name = "minimum", longValue = 0, type = Param.TYPE_LONG)) Long id,
        @Argument(name = "world", optional = true, index = 2) World world) {
        Actor<Player> actor = rawActor.as(Player.class);
        if (!actor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT_SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        plugin.scheduler().async(() -> {
            Container container = accessContainer(plugin, actor, id, null, null, null, world);
            if (container == null) {
                return;
            }
            openManageInventory(plugin, actor, container, world);
        });
    }

    @Action("manage loc")
    @Action("manage location")
    @Description("$#command.description.justlootit.container.manage.location")
    public void manage(final JustLootItPlugin plugin, final Actor<?> rawActor,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        Actor<Player> actor = rawActor.as(Player.class);
        if (!actor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT_SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        plugin.scheduler().async(() -> {
            Container container = accessContainer(plugin, actor, null, x, y, z, world);
            if (container == null) {
                return;
            }
            openManageInventory(plugin, actor, container, world);
        });
    }

    private void openManageInventory(final JustLootItPlugin plugin, final Actor<Player> actor, final Container container, final World world) {
        plugin.scheduler().sync(() -> {
            plugin.versionHandler().getPlayer(actor.getHandle()).getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                final IGuiInventory inventory = guiCapability.gui();
                inventory.attrSet(ContainerPageHandler.ATTR_CONTAINER, container);
                inventory.attrSet(ContainerPageHandler.ATTR_WORLD, CommandUtil.getLocation(actor, null, null, null, world).getWorld());
                inventory.setHandler(plugin.pagedInventoryRegistry().get(ContainerPageHandler.class));
                inventory.open(actor.getHandle());
            });
        });
    }

    private Container accessContainer(final JustLootItPlugin plugin, final Actor<Player> actor, Long id, Coord x, Coord y, Coord z,
        World world) {
        if (id == null) {
            Location location = CommandUtil.getLocation(actor, x, y, z, world);
            if (location == null) {
                return null;
            }
            id = plugin.scheduler().syncRegional(location, () -> {
                Block block = location.getBlock();
                if (block.isEmpty()) {
                    return retrieveEntityContainer(plugin, location.getWorld(), actor, location.getBlockX(), location.getBlockY(),
                        location.getBlockZ());
                }
                if (!(block.getState() instanceof org.bukkit.block.Container stateContainer)) {
                    return retrieveEntityContainer(plugin, location.getWorld(), actor, location.getBlockX(), location.getBlockY(),
                        location.getBlockZ());
                }
                return retrieveBlockContainer(plugin, block, stateContainer, location.getWorld(), actor);
            }).join();
            if (id == null) {
                return null;
            }
        }
        if (world == null) {
            world = actor.getHandle().getWorld();
        }
        StorageCapability capability = plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).orElse(null);
        if (capability == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName()));
            return null;
        }
        Container container = (Container) capability.storage().read(id.longValue());
        if (container == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ID, Key.of("id", id));
            return null;
        }
        return container;
    }

    private Long retrieveEntityContainer(JustLootItPlugin plugin, World world, Actor<?> actor, int x, int y, int z) {
        Collection<Entity> entities = world.getNearbyEntities(new Location(world, x + 0.5d, y + 0.5d, z + 0.5d), 1.5d, 1.5d, 1.5d);
        if (entities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return null;
        }
        List<Entity> validEntities = entities.stream()
            .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
        if (validEntities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return null;
        }
        double distance = Double.MAX_VALUE;
        Location origin = new Location(world, x + 0.5d, y + 0.5d, z + 0.5d);
        Entity closest = null;
        for (Entity entity : validEntities) {
            Location current = entity.getLocation();
            double dist = origin.distanceSquared(current);
            if (dist < distance) {
                closest = entity;
                distance = dist;
            }
        }
        Location loc = closest.getLocation();
        PersistentDataContainer dataContainer = closest.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
            return null;
        }
        return dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
    }

    private Long retrieveBlockContainer(JustLootItPlugin plugin, Block block, org.bukkit.block.Container stateContainer, World world,
        Actor<?> actor) {
        Location location = block.getLocation();
        PersistentDataContainer dataContainer = stateContainer.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
            && dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            if (!(stateContainer.getBlockData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return null;
            }
            Vec3i vector = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            block = location.getWorld().getBlockAt(vector.addOn(location));
            if (block.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return null;
            }
            if (!(block.getState() instanceof org.bukkit.block.Container stateContainer0)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return null;
            }
            if (!stateContainer0.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return null;
            }
            stateContainer = stateContainer0;
            dataContainer = stateContainer.getPersistentDataContainer();
        }
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
            return null;
        }
        return dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
    }

    @Action("link entity")
    public void linkEntity(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "id", index = 0, params = @Param(name = "minimum", longValue = 0, type = Param.TYPE_LONG)) Long id,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        runLink(plugin, actor, id, x, y, z, world, true);
    }

    @Action("link")
    public void link(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "id", index = 0, params = @Param(name = "minimum", longValue = 0, type = Param.TYPE_LONG)) Long id,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        runLink(plugin, actor, id, x, y, z, world, false);
    }

    private void runLink(final JustLootItPlugin plugin, final Actor<?> actor, final Long id, final Coord x, final Coord y, final Coord z,
        final World world, final boolean useEntity) {
        final Location loc = CommandUtil.getLocation(actor, x, y, z, world);
        StorageCapability capability = plugin.versionHandler().getLevel(loc.getWorld()).getCapability(StorageCapability.class).orElse(null);
        if (capability == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", loc.getWorld().getName()));
            return;
        }
        Container container = (Container) capability.storage().read(id.longValue());
        if (container == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ID, Key.of("id", id));
            return;
        }
        ContainerType type = container.type();
        if (type.blockCreator() == null && type.entityCreator() == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_NOT_LINKABLE, Key.of("type", type));
            return;
        }
        plugin.scheduler().syncRegional(loc, () -> {
            if (type.blockCreator() == null || (useEntity && type.entityCreator() != null)) {
                linkEntityContainer(plugin, actor, type, container.id(), loc);
                return;
            }
            linkBlockContainer(plugin, actor, type, container.id(), loc);
        });
    }

    private void linkEntityContainer(final JustLootItPlugin plugin, final Actor<?> actor, final ContainerType type, final long id,
        final Location location) {
        final Location centerLocation = new Location(location.getWorld(), location.getBlockX() + 0.5d, location.getBlockY() + 0.5d,
            location.getBlockZ() + 0.5d);
        Collection<Entity> entities = location.getWorld().getNearbyEntities(centerLocation, 1.5d, 1.5d, 1.5d);
        if (entities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_NOT_FOUND_ENTITY, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
            return;
        }
        List<Entity> validEntities = entities.stream()
            .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
        if (validEntities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_NOT_FOUND_ENTITY, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
            return;
        }
        double distance = Double.MAX_VALUE;
        Entity closest = null;
        for (Entity entity : validEntities) {
            Location current = entity.getLocation();
            double dist = centerLocation.distanceSquared(current);
            if (dist < distance) {
                closest = entity;
                distance = dist;
            }
        }
        if (!type.entityCreator().supportedTest().test(closest)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_UNSUPPORTED_ENTITY, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                Key.of("type", type));
            return;
        }
        PersistentDataContainer container = closest.getPersistentDataContainer();
        if (DataHelper.hasIdentity(container)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_ALREADY_CONTAINER_ENTITY, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
            return;
        }
        container.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
        actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_SUCCESS_ENTITY, Key.of("x", location.getBlockX()),
            Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()),
            Key.of("type", type), Key.of("id", id));
    }

    private void linkBlockContainer(final JustLootItPlugin plugin, final Actor<?> actor, final ContainerType type, final long id,
        final Location location) {
        Block block = location.getBlock();
        if (block.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_NOT_FOUND_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
            return;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Container stateContainer) || (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(stateContainer.getInventory().getType()))) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_NOT_FOUND_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
            return;
        }
        if (!type.blockCreator().supportedTest().test(stateContainer)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_UNSUPPORTED_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                Key.of("type", type));
            return;
        }
        PersistentDataContainer container = stateContainer.getPersistentDataContainer();
        if (DataHelper.hasIdentityOrOffset(container)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_ALREADY_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
            return;
        }
        BlockUtil.setContainerOffsetToNearbyChest(stateContainer);
        container.set(JustLootItKey.identity(), PersistentDataType.LONG, id);
        stateContainer.update(false, false);
        actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_LINK_SUCCESS_BLOCK, Key.of("x", location.getBlockX()),
            Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()),
            Key.of("type", type), Key.of("id", id));
    }

    @Action("create entity")
    @Description("$#command.description.justlootit.container.create.entity")
    public void createEntity(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "type", index = 0, params = @Param(name = "type", classValue = ContainerType.class, type = Param.TYPE_CLASS)) final ContainerType type,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        runCreate(plugin, actor, type, x, y, z, world, true);
    }

    @Action("create")
    @Description("$#command.description.justlootit.container.create.any")
    public void create(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "type", index = 0, params = @Param(name = "type", classValue = ContainerType.class, type = Param.TYPE_CLASS)) final ContainerType type,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) final Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) final Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) final Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        runCreate(plugin, actor, type, x, y, z, world, false);
    }

    private void runCreate(final JustLootItPlugin plugin, final Actor<?> actor, final ContainerType type, final Coord x, final Coord y,
        final Coord z, World world, final boolean useEntity) {
        if (type.blockCreator() == null && type.entityCreator() == null) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_NOT_CREATABLE, Key.of("type", type));
            return;
        }
        final Location loc = CommandUtil.getLocation(actor, x, y, z, world);
        plugin.scheduler().syncRegional(loc, () -> {
            if (type.blockCreator() == null || (useEntity && type.entityCreator() != null)) {
                createEntityContainer(plugin, actor, type, loc);
                return;
            }
            createBlockContainer(plugin, actor, type, loc);
        });
    }

    private void createEntityContainer(final JustLootItPlugin plugin, final Actor<?> actor, final ContainerType type,
        final Location location) {
        final Location centerLocation = new Location(location.getWorld(), location.getBlockX() + 0.5d, location.getBlockY() + 0.5d,
            location.getBlockZ() + 0.5d);
        Collection<Entity> entities = location.getWorld().getNearbyEntities(centerLocation, 1.5d, 1.5d, 1.5d);
        if (entities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_NOT_FOUND_ENTITY, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
            return;
        }
        List<Entity> validEntities = entities.stream()
            .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
        if (validEntities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_NOT_FOUND_ENTITY, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()));
            return;
        }
        double distance = Double.MAX_VALUE;
        Entity closest = null;
        for (Entity entity : validEntities) {
            Location current = entity.getLocation();
            double dist = centerLocation.distanceSquared(current);
            if (dist < distance) {
                closest = entity;
                distance = dist;
            }
        }
        type.entityCreator().function().create(plugin, actor, closest.getLocation(), closest, (creator) -> {
            plugin.versionHandler().getLevel(location.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                IStorage<Storable> storage = capability.storage();
                long id = storage.newId();
                storage.write(creator.apply(id));
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_SUCCESS_ENTITY, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()),
                    Key.of("type", type), Key.of("id", id));
            }, () -> actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL,
                Key.of("level", location.getWorld().getName())));
        });
    }

    private void createBlockContainer(final JustLootItPlugin plugin, final Actor<?> actor, final ContainerType type,
        final Location location) {
        Block block = location.getBlock();
        if (block.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_NOT_FOUND_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()));
            return;
        }
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Container stateContainer) || (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(stateContainer.getInventory().getType()))) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_UNSUPPORTED_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld()),
                Key.of("type", type));
            return;
        }
        type.blockCreator().function().create(plugin, actor, location, stateContainer, (creator) -> {
            plugin.versionHandler().getLevel(location.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
                IStorage<Storable> storage = capability.storage();
                long id = storage.newId();
                storage.write(creator.apply(id));
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_CREATE_SUCCESS_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", location.getWorld().getName()),
                    Key.of("type", type), Key.of("id", id));
            }, () -> actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL,
                Key.of("level", location.getWorld().getName())));
        });
    }

    @Action("group remove")
    @Description("$#command.description.justlootit.container.group.remove")
    public void groupRemove(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "x", optional = true, index = 0, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) Coord x,
        @Argument(name = "y", optional = true, index = 1, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) Coord y,
        @Argument(name = "z", optional = true, index = 2, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) Coord z,
        @Argument(name = "world", optional = true, index = 3) World world) {
        runGroup(plugin, actor, null, x, y, z, world);
    }

    @Action("group")
    @Description("$#command.description.justlootit.container.group.set")
    public void group(final JustLootItPlugin plugin, final Actor<?> actor, @Argument(name = "group", index = 0) final RefreshGroup group,
        @Argument(name = "x", optional = true, index = 1, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) Coord x,
        @Argument(name = "y", optional = true, index = 2, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) Coord y,
        @Argument(name = "z", optional = true, index = 3, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) Coord z,
        @Argument(name = "world", optional = true, index = 4) World world) {
        runGroup(plugin, actor, group, x, y, z, world);
    }

    private void runGroup(final JustLootItPlugin plugin, final Actor<?> actor, final RefreshGroup group, final Coord x, final Coord y,
        final Coord z, final World world) {
        final Location loc = CommandUtil.getLocation(actor, x, y, z, world);
        plugin.scheduler().syncRegional(loc, () -> {
            Block block = loc.getBlock();
            if (block.isEmpty()) {
                doGroupForEntityContainer(plugin, loc.getWorld(), actor, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), group);
                return;
            }
            if (!(block.getState() instanceof org.bukkit.block.Container stateContainer)) {
                doGroupForEntityContainer(plugin, loc.getWorld(), actor, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), group);
                return;
            }
            doGroupForBlockContainer(plugin, block, stateContainer, loc.getWorld(), actor, loc.getBlockX(), loc.getBlockY(),
                loc.getBlockZ(), group);
        });
    }

    private void doGroupForEntityContainer(JustLootItPlugin plugin, World world, Actor<?> actor, int x, int y, int z, RefreshGroup group) {
        Collection<Entity> entities = world.getNearbyEntities(new Location(world, x + 0.5d, y + 0.5d, z + 0.5d), 1.5d, 1.5d, 1.5d);
        if (entities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return;
        }
        List<Entity> validEntities = entities.stream()
            .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
        if (validEntities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return;
        }
        double distance = Double.MAX_VALUE;
        Location origin = new Location(world, x + 0.5d, y + 0.5d, z + 0.5d);
        Entity closest = null;
        for (Entity entity : validEntities) {
            Location current = entity.getLocation();
            double dist = origin.distanceSquared(current);
            if (dist < distance) {
                closest = entity;
                distance = dist;
            }
        }
        Location loc = closest.getLocation();
        PersistentDataContainer dataContainer = closest.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
            return;
        }
        long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container container = (Container) capability.storage().read(id);
            if (container == null) {
                dataContainer.remove(JustLootItKey.identity());
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                    Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
                return;
            }
            if (group == null) {
                container.setGroupId(null);
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_GROUP_REMOVED_ENTITY, Key.of("x", loc.getX()),
                    Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
            } else {
                container.setGroupId(group.id());
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_GROUP_SET_ENTITY, Key.of("x", loc.getX()), Key.of("y", loc.getY()),
                    Key.of("z", loc.getZ()), Key.of("world", world.getName()), Key.of("group", group.id()));
            }
        }, () -> actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName())));
    }

    private void doGroupForBlockContainer(JustLootItPlugin plugin, Block block, org.bukkit.block.Container stateContainer, World world,
        Actor<?> actor, int x, int y, int z, RefreshGroup group) {
        PersistentDataContainer dataContainer = stateContainer.getPersistentDataContainer();
        Location location = block.getLocation();
        org.bukkit.block.Container otherContainer = stateContainer;
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
            && dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            if (!(stateContainer.getBlockData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            Vec3i vector = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            block = world.getBlockAt(vector.addOn(location));
            if (block.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            if (!(block.getState() instanceof org.bukkit.block.Container stateContainer0)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            if (!stateContainer0.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            stateContainer = stateContainer0;
            dataContainer = stateContainer.getPersistentDataContainer();
        }
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", world.getName()));
            return;
        }
        long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        final org.bukkit.block.Container finalContainer = stateContainer;
        plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container container = (Container) capability.storage().read(id);
            if (container == null) {
                PersistentDataContainer finalDataContainer = finalContainer.getPersistentDataContainer();
                if (finalContainer != otherContainer) {
                    otherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                    otherContainer.update(false, false);
                } else if (finalDataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                    final Block finalBlock = world
                        .getBlockAt(finalDataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR).addOn(location));
                    final BlockState finalState = finalBlock.getState();
                    if (finalState instanceof org.bukkit.block.Container finalOtherContainer) {
                        finalOtherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                        finalOtherContainer.update(false, false);
                    }
                }
                finalDataContainer.remove(JustLootItKey.chestData());
                finalDataContainer.remove(JustLootItKey.identity());
                finalContainer.update(false, false);
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", world.getName()));
                return;
            }
            if (group == null) {
                container.setGroupId(null);
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_GROUP_REMOVED_BLOCK, Key.of("x", location.getX()),
                    Key.of("y", location.getY()), Key.of("z", location.getZ()), Key.of("world", world.getName()));
            } else {
                container.setGroupId(group.id());
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_GROUP_SET_BLOCK, Key.of("x", location.getX()),
                    Key.of("y", location.getY()), Key.of("z", location.getZ()), Key.of("world", world.getName()),
                    Key.of("group", group.id()));
            }
        }, () -> actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName())));
    }

    @Action("info")
    @Description("$#command.description.justlootit.container.info")
    public void info(final JustLootItPlugin plugin, final Actor<?> actor,
        @Argument(name = "x", optional = true, index = 0, params = @Param(name = "axis", stringValue = "x", type = Param.TYPE_STRING)) Coord x,
        @Argument(name = "y", optional = true, index = 1, params = @Param(name = "axis", stringValue = "y", type = Param.TYPE_STRING)) Coord y,
        @Argument(name = "z", optional = true, index = 2, params = @Param(name = "axis", stringValue = "z", type = Param.TYPE_STRING)) Coord z,
        @Argument(name = "world", optional = true, index = 3) World world) {
        final Location loc = CommandUtil.getLocation(actor, x, y, z, world);
        plugin.scheduler().syncRegional(loc, () -> {
            Block block = loc.getBlock();
            if (block.isEmpty()) {
                doInfoForEntityContainer(plugin, loc.getWorld(), actor, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                return;
            }
            if (!(block.getState() instanceof org.bukkit.block.Container stateContainer)) {
                doInfoForEntityContainer(plugin, loc.getWorld(), actor, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                return;
            }
            doInfoForBlockContainer(plugin, block, stateContainer, loc.getWorld(), actor, loc.getBlockX(), loc.getBlockY(),
                loc.getBlockZ());
        });
    }

    private void doInfoForEntityContainer(JustLootItPlugin plugin, World world, Actor<?> actor, int x, int y, int z) {
        Collection<Entity> entities = world.getNearbyEntities(new Location(world, x + 0.5d, y + 0.5d, z + 0.5d), 1.5d, 1.5d, 1.5d);
        if (entities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return;
        }
        List<Entity> validEntities = entities.stream()
            .filter(entity -> EntityUtil.isSupportedEntity(entity) || EntityUtil.isItemFrame(entity)).toList();
        if (validEntities.isEmpty()) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y), Key.of("z", z),
                Key.of("world", world.getName()));
            return;
        }
        double distance = Double.MAX_VALUE;
        Location origin = new Location(world, x + 0.5d, y + 0.5d, z + 0.5d);
        Entity closest = null;
        for (Entity entity : validEntities) {
            Location current = entity.getLocation();
            double dist = origin.distanceSquared(current);
            if (dist < distance) {
                closest = entity;
                distance = dist;
            }
        }
        Location loc = closest.getLocation();
        PersistentDataContainer dataContainer = closest.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
            return;
        }
        long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container container = (Container) capability.storage().read(id);
            if (container == null) {
                dataContainer.remove(JustLootItKey.identity());
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_ENTITY, Key.of("x", loc.getX()),
                    Key.of("y", loc.getY()), Key.of("z", loc.getZ()), Key.of("world", world.getName()));
                return;
            }
            String refreshGroup = container.getGroupId();
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_ANY_ENTITY, Key.of("id", container.id()),
                Key.of("refreshGroup", refreshGroup == null || refreshGroup.isEmpty() ? "None" : refreshGroup),
                Key.of("type", TypeName.ofContainer(container)), Key.of("x", loc.getX()), Key.of("y", loc.getY()), Key.of("z", loc.getZ()),
                Key.of("world", world.getName()));
            sendAdditionalContainerInfo(plugin, actor, container);
        }, () -> actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName())));
    }

    private void doInfoForBlockContainer(JustLootItPlugin plugin, Block block, org.bukkit.block.Container stateContainer, World world,
        Actor<?> actor, int x, int y, int z) {
        PersistentDataContainer dataContainer = stateContainer.getPersistentDataContainer();
        Location location = block.getLocation();
        org.bukkit.block.Container otherContainer = stateContainer;
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)
            && dataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
            if (!(stateContainer.getBlockData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            Vec3i vector = dataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR);
            block = world.getBlockAt(vector.addOn(location));
            if (block.isEmpty()) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            if (!(block.getState() instanceof org.bukkit.block.Container stateContainer0)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            if (!stateContainer0.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG)) {
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", x), Key.of("y", y),
                    Key.of("z", z), Key.of("world", world.getName()));
                dataContainer.remove(JustLootItKey.chestData());
                stateContainer.update(false, false);
                return;
            }
            stateContainer = stateContainer0;
            dataContainer = stateContainer.getPersistentDataContainer();
        }
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", world.getName()));
            return;
        }
        long id = dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG);
        final org.bukkit.block.Container finalContainer = stateContainer;
        plugin.versionHandler().getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container container = (Container) capability.storage().read(id);
            if (container == null) {
                PersistentDataContainer finalDataContainer = finalContainer.getPersistentDataContainer();
                if (finalContainer != otherContainer) {
                    otherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                    otherContainer.update(false, false);
                } else if (finalDataContainer.has(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR)) {
                    final Block finalBlock = world
                        .getBlockAt(finalDataContainer.get(JustLootItKey.chestData(), SimpleDataType.OFFSET_VECTOR).addOn(location));
                    final BlockState finalState = finalBlock.getState();
                    if (finalState instanceof org.bukkit.block.Container finalOtherContainer) {
                        finalOtherContainer.getPersistentDataContainer().remove(JustLootItKey.chestData());
                        finalOtherContainer.update(false, false);
                    }
                }
                finalDataContainer.remove(JustLootItKey.chestData());
                finalDataContainer.remove(JustLootItKey.identity());
                finalContainer.update(false, false);
                actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_ALL_NO_CONTAINER_BLOCK, Key.of("x", location.getBlockX()),
                    Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()), Key.of("world", world.getName()));
                return;
            }
            String refreshGroup = container.getGroupId();
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_ANY_BLOCK, Key.of("id", container.id()),
                Key.of("refreshGroup", refreshGroup == null ? "None" : refreshGroup), Key.of("type", TypeName.ofContainer(container)),
                Key.of("x", location.getBlockX()), Key.of("y", location.getBlockY()), Key.of("z", location.getBlockZ()),
                Key.of("world", world.getName()));
            sendAdditionalContainerInfo(plugin, actor, container);
        }, () -> actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ERROR_STORAGE_ACCESS_LEVEL, Key.of("level", world.getName())));
    }

    private void sendAdditionalContainerInfo(JustLootItPlugin plugin, Actor<?> actor, Container container) {
        if (container instanceof VanillaContainer vanilla) {
            actor.sendTranslatedMessage(Messages.COMMAND_CONTAINER_INFO_CONTAINER_VANILLA, Key.of("seed", vanilla.getSeed()),
                Key.of("lootTable", vanilla.getLootTableKey()));
        } else if (container instanceof FrameContainer frame) {
            ComponentBuilder.create()
                .appendContent(actor.getTranslatedMessageAsString(Messages.COMMAND_CONTAINER_INFO_CONTAINER_FRAME,
                    Key.of("itemName", ItemEditor.of(frame.getItem()).getItemName())))
                .hover(new HoverEvent(HoverEvent.Action.SHOW_ITEM, plugin.versionHelper().createItemHover(frame.getItem()))).finish()
                .send(actor);
        } else if (container instanceof CompatibilityContainer compat) {
            ComponentBuilder<?, ?> root = ComponentBuilder.create();
            root.appendContent(actor.getTranslatedMessageAsString(Messages.COMMAND_CONTAINER_INFO_CONTAINER_COMPATIBILITY_HEADER,
                Key.of("plugin", compat.getCompatibilityData().extension().id()))).finish();
            ComponentBuilder<?, ?> compatData = ComponentBuilder.create();
            compat.getCompatibilityData()
                .addInfoData(key -> compatData
                    .appendContent(actor.getTranslatedMessageAsString(Messages.COMMAND_CONTAINER_INFO_CONTAINER_COMPATIBILITY_FORMAT,
                        Key.of("key", key.getKey()), Key.of("value", key.getValue()))).appendChar('\n')
                    .finish());
            root.send(actor);
            if (compatData.isEmpty()) {
                compatData.appendContent(Messages.COMMAND_CONTAINER_INFO_CONTAINER_COMPATIBILITY_NO_DATA, actor).finish();
            }
            compatData.send(actor);
        }
    }

}
