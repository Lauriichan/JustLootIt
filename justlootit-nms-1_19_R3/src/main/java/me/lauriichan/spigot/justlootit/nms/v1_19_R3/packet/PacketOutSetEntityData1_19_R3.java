package me.lauriichan.spigot.justlootit.nms.v1_19_R3.packet;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.nms.v1_19_R3.model.EntityData1_19_R3;
import me.lauriichan.spigot.justlootit.nms.v1_19_R3.model.ItemEntityData1_19_R3;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;

public class PacketOutSetEntityData1_19_R3 extends PacketOutSetEntityData {

    private final ClientboundSetEntityDataPacket packet;
    private final EntityDataPack data;

    public PacketOutSetEntityData1_19_R3(final ClientboundSetEntityDataPacket packet) {
        this.packet = packet;
        this.data = new EntityDataPack(packet.packedItems());
    }

    public PacketOutSetEntityData1_19_R3(final ArgumentMap map) {
        int entityId;
        if (map.has("entity")) {
            entityId = map.require("entity", Entity.class).getEntityId();
        } else {
            entityId = map.require("entityId", int.class);
        }
        this.packet = new ClientboundSetEntityDataPacket(entityId, new ArrayList<>());
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

        public EntityDataPack(List<DataValue<?>> values) {
            this.values = values;
            for (DataValue<?> value : values) {
                EntityData1_19_R3 data = EntityData1_19_R3.create(value);
                if (data instanceof ItemEntityData1_19_R3) {
                    dataValues.add(new ItemData(this, (ItemEntityData1_19_R3) data));
                    continue;
                }
                dataValues.add(data);
            }
        }

        @Override
        public IEntityData get(int index) {
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
            private final ItemEntityData1_19_R3 data;

            public ItemData(final EntityDataPack pack, final ItemEntityData1_19_R3 data) {
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
            public void setItem(ItemStack itemStack) {
                data.setItem(itemStack);
                if (data.isDirty()) {
                    pack.values.set(pack.indexOf(data.getId()), data.build());
                }
            }

        }

    }

}