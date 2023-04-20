package me.lauriichan.spigot.justlootit.nms.packet;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;

public abstract class PacketOutSetEntityData extends AbstractPacketOut {

    public interface IEntityDataPack {

        IEntityData get(int index);

        default IEntityData getById(final int id) {
            for (int index = 0; index < size(); index++) {
                final IEntityData data = get(index);
                if (data.getId() == id) {
                    return data;
                }
            }
            return null;
        }

        default int indexOf(final IEntityData data) {
            if (data == null) {
                return -1;
            }
            return indexOf(data.getId());
        }

        default int indexOf(final int id) {
            for (int index = 0; index < size(); index++) {
                if (get(index).getId() == id) {
                    return index;
                }
            }
            return -1;
        }

        int size();

    }

    public abstract int getEntityId();

    public abstract IEntityDataPack getData();

}
