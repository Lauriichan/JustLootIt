package me.lauriichan.spigot.justlootit.data;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import io.netty.buffer.ByteBuf;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.message.Messages;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.StorageAdapter;

public class CompatibilityContainer extends Container implements IInventoryContainer {

    public static final StorageAdapter<CompatibilityContainer> ADAPTER = new BaseAdapter<>(CompatibilityContainer.class, 17) {

        @Override
        protected void serializeSpecial(CompatibilityContainer storable, ByteBuf buffer) {
            ICompatibilityData data = storable.getCompatibilityData();
            CompatibilityDataExtension<?> extension = data.extension();
            BufIO.writeString(buffer, extension.id());
            buffer.writeInt(data.version());
            extension.saveSafe(buffer, data);
        }

        @Override
        protected CompatibilityContainer deserializeSpecial(long id, ContainerData data, ByteBuf buffer) {
            String compatId = BufIO.readString(buffer);
            int version = buffer.readInt();
            CompatibilityDataExtension<?> extension = CompatibilityDataExtension.get(compatId);
            ICompatibilityData compatData = extension.load(buffer, version);
            while (extension.hasUpgrade(compatData)) {
                compatData = extension.upgrade(compatData);
            }
            return new CompatibilityContainer(id, compatData);
        }

    };

    private ICompatibilityData compatibilityData;

    public CompatibilityContainer(final long id, final ICompatibilityData compatibilityData) {
        super(id);
        setCompatibilityData(compatibilityData);
    }

    public CompatibilityContainer(final long id, ContainerData data, final ICompatibilityData compatibilityData) {
        super(id, data);
        setCompatibilityData(compatibilityData);
    }

    public ICompatibilityData getCompatibilityData() {
        return compatibilityData;
    }

    public void setCompatibilityData(ICompatibilityData compatibilityData) {
        this.compatibilityData = Objects.requireNonNull(compatibilityData);
    }

    @Override
    public ItemEditor createIcon() {
        CompatibilityDataExtension<?> extension = compatibilityData.extension();
        ItemEditor editor = ItemEditor.of(extension.iconType()).setName("&9Compatibility (&b%s)".formatted(extension.id()));
        extension.modifyIcon(editor);
        return editor;
    }

    @Override
    public void fill(final PlayerAdapter player, final InventoryHolder holder, final Location location, final Inventory inventory) {
        if (!compatibilityData.extension().isActive()) {
            ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_COMPATIBILITY_NOT_ACTIVE,
                Key.of("plugin", compatibilityData.extension().id()));
            return;
        }
        try {
            if (holder instanceof Entity entity) {
                if (!compatibilityData.canFill(entity, location)) {
                    ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_COMPATIBILITY_FILL_NOT_AVAILABLE,
                        Key.of("plugin", compatibilityData.extension().id()));
                    return;
                }
                if (!compatibilityData.fill(player, entity, location, inventory)) {
                    ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_COMPATIBILITY_FILL_FAILED,
                        Key.of("plugin", compatibilityData.extension().id()));
                    return;
                }
            } else {
                BlockState state = (BlockState) holder;
                if (!compatibilityData.canFill(state, location)) {
                    ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_COMPATIBILITY_FILL_NOT_AVAILABLE,
                        Key.of("plugin", compatibilityData.extension().id()));
                    return;
                }
                if (!compatibilityData.fill(player, state, location, inventory)) {
                    ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_COMPATIBILITY_FILL_FAILED,
                        Key.of("plugin", compatibilityData.extension().id()));
                    return;
                }
            }
        } catch (RuntimeException exp) {
            ActorCapability.actor(player).sendTranslatedMessage(Messages.CONTAINER_COMPATIBILITY_FILL_FAILED,
                Key.of("plugin", compatibilityData.extension().id()));
            player.versionHandler().logger().error("Failed to fill compatibility container for plugin '{0}' with data version {1}.", exp,
                compatibilityData.extension().id(), compatibilityData.version());
        }
    }

}
