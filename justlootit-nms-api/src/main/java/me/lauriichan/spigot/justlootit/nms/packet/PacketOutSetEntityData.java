package me.lauriichan.spigot.justlootit.nms.packet;

import me.lauriichan.spigot.justlootit.nms.model.IEntityData;

public abstract class PacketOutSetEntityData extends AbstractPacketOut {

    public interface IEntityDataPack {

        IEntityData get(int index);

        default IEntityData getById(int id) {
            for (int index = 0; index < size(); index++) {
                IEntityData data = get(index);
                if (data.getId() == id) {
                    return data;
                }
            }
            return null;
        }

        default int indexOf(IEntityData data) {
            if (data == null) {
                return -1;
            }
            return indexOf(data.getId());
        }

        default int indexOf(int id) {
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
