package me.lauriichan.spigot.justlootit.nms.v1_21_R3.packet;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.model.EntityData1_21_R3;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.model.ItemEntityData1_21_R3;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataItem;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class PacketOutSetEntityData1_21_R3 extends PacketOutSetEntityData {

    private static final MethodHandle EntityData_map = JavaLookup.PLATFORM
        .unreflectGetter(ClassUtil.getField(SynchedEntityData.class, false, DataItem[].class));

    private static List<DataValue<?>> extractValues(final SynchedEntityData data) {
        DataItem<?>[] map;
        try {
            map = (DataItem<?>[]) EntityData_map.invoke(data);
        } catch (final Throwable e) {
            return Collections.emptyList();
        }
        final ArrayList<DataValue<?>> values = new ArrayList<>();
        for (DataItem<?> item : map) {
            values.add(item.value());
        }
        return values;
    }

    private final ClientboundSetEntityDataPacket packet;
    private final EntityDataPack data;

    public PacketOutSetEntityData1_21_R3(final ClientboundSetEntityDataPacket packet) {
        this.packet = packet;
        this.data = new EntityDataPack(packet.packedItems());
    }

    public PacketOutSetEntityData1_21_R3(final ArgumentMap map) {
        final CraftEntity entity = (CraftEntity) map.require("entity", Entity.class);
        map.throwIfMissing();
        this.packet = new ClientboundSetEntityDataPacket(entity.getEntityId(), extractValues(entity.getHandle().getEntityData()));
        this.data = new EntityDataPack(packet.packedItems());
    }

    @Override
    public Object asMinecraft() {
        return packet;
    }

    @Override
    public int getEntityId() {
        return packet.id();
    }

    @Override
    public IEntityDataPack getData() {
        return data;
    }

    private static class EntityDataPack implements IEntityDataPack {

        private final List<DataValue<?>> values;
        private final List<IEntityData> dataValues = new ArrayList<>();

        public EntityDataPack(final List<DataValue<?>> values) {
            this.values = values;
            for (final DataValue<?> value : values) {
                final EntityData1_21_R3 data = EntityData1_21_R3.create(value);
                if (data instanceof ItemEntityData1_21_R3) {
                    dataValues.add(new ItemData(this, (ItemEntityData1_21_R3) data));
                    continue;
                }
                dataValues.add(data);
            }
        }

        @Override
        public IEntityData get(final int index) {
            if (index >= values.size() || index < 0) {
                return null;
            }
            return dataValues.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        private static class ItemData implements IItemEntityData {

            private final EntityDataPack pack;
            private final ItemEntityData1_21_R3 data;

            public ItemData(final EntityDataPack pack, final ItemEntityData1_21_R3 data) {
                this.pack = pack;
                this.data = data;
            }

            @Override
            public int getId() {
                return data.getId();
            }

            @Override
            public ItemStack getItem() {
                return data.getItem();
            }

            @Override
            public void setItem(final ItemStack itemStack) {
                data.setItem(itemStack);
                if (data.isDirty()) {
                    pack.values.set(pack.indexOf(data.getId()), data.build());
                }
            }

        }

    }

}