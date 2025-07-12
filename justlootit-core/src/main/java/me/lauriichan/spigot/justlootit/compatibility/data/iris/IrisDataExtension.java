package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.data.io.BufIO;

@Extension
public class IrisDataExtension extends CompatibilityDataExtension<IIrisData> {

    public IrisDataExtension() {
        super("Iris", IIrisData.class);
    }

    @Override
    public Material iconType() {
        return Material.ENDER_EYE;
    }

    @Override
    public void save(ByteBuf buffer, IIrisData data) {
        switch (data.version()) {
        case 0:
            IrisDataV1 v1 = (IrisDataV1) data;
            int vanillaOffset = 0;
            for (int i = 0; i < v1.keys().length; i++) {
                if (v1.keys()[i] instanceof IIrisTableKey.VanillaTableKey) {
                    vanillaOffset = i;
                    break;
                }
            }
            buffer.writeLong(v1.seed());
            buffer.writeByte(v1.keys().length);
            buffer.writeByte(vanillaOffset);
            for (IIrisTableKey key : v1.keys()) {
                BufIO.writeString(buffer, key.identifier());
            }
            return;
        }
    }

    @Override
    public IIrisData load(ByteBuf buffer, int version) {
        switch (version) {
        case 0:
            long seed_v1 = buffer.readLong();
            int size_v1 = buffer.readUnsignedByte();
            int vanillaOffset_v1 = buffer.readUnsignedByte();
            IIrisTableKey[] keys_v1 = new IIrisTableKey[size_v1];
            for (int i = 0; i < size_v1; i++) {
                String string = BufIO.readString(buffer);
                if (i >= vanillaOffset_v1) {
                    keys_v1[i] = new IIrisTableKey.VanillaTableKey(NamespacedKey.fromString(string));
                    continue;
                }
                keys_v1[i] = new IIrisTableKey.IrisTableKey(string);
            }
            return new IrisDataV1(this, keys_v1, seed_v1);
        }
        return null;
    }

    public IIrisData create(IIrisTableKey[] keys, long seed) {
        return new IrisDataV1(this, keys, seed);
    }

}
