package me.lauriichan.spigot.justlootit.compatibility.data.customstructures;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.data.io.BufIO;

@Extension
public class CustomStructuresDataExtension extends CompatibilityDataExtension<ICustomStructuresData> {
    
    private static final NamespacedKey GENERIC = NamespacedKey.fromString("customstructures:justlootit/generic");

    public CustomStructuresDataExtension() {
        super("CustomStructures", ICustomStructuresData.class);
    }

    @Override
    public Material iconType() {
        return Material.STONE_BRICKS;
    }

    @Override
    public void save(ByteBuf buffer, ICustomStructuresData data) {
        switch (data.version()) {
        case 0:
            CustomStructuresDataV1 v1 = (CustomStructuresDataV1) data;
            BufIO.writeString(buffer, v1.structureName());
            buffer.writeLong(v1.seed());
            return;
        }
    }

    @Override
    public ICustomStructuresData load(ByteBuf buffer, int version) {
        switch (version) {
        case 0:
            String structureName_v1 = BufIO.readString(buffer);
            long seed_v1 = buffer.readLong();
            return new CustomStructuresDataV1(this, createDataId(structureName_v1), structureName_v1,
                seed_v1);
        }
        return null;
    }

    public ICustomStructuresData create(String structureName, long seed) {
        return new CustomStructuresDataV1(this, createDataId(structureName), structureName, seed);
    }
    
    private NamespacedKey createDataId(String name) {
        try {
            return NamespacedKey.fromString("customstructures:" + name);
        } catch(IllegalArgumentException iae) {
            return GENERIC;
        }
    }

}
