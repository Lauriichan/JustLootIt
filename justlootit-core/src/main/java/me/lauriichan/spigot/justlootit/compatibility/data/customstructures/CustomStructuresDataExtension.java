package me.lauriichan.spigot.justlootit.compatibility.data.customstructures;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.customstructures.ICustomStructuresProvider;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

@Extension
public class CustomStructuresDataExtension extends CompatibilityDataExtension<ICustomStructuresData> {
    
    private static final NamespacedKey GENERIC = NamespacedKey.fromString("customstructures:justlootit/generic");

    public CustomStructuresDataExtension() {
        super("CustomStructures", ICustomStructuresData.class);
    }

    public ICustomStructuresData create(String tableName, long seed) {
        return new CustomStructuresDataV2(this, createDataId(tableName), tableName, seed);
    }
    
    private NamespacedKey createDataId(String name) {
        try {
            return NamespacedKey.fromString("customstructures:" + name);
        } catch(IllegalArgumentException iae) {
            return GENERIC;
        }
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
        case 1:
            String tableName_v2 = BufIO.readString(buffer);
            long seed_v2 = buffer.readLong();
            return new CustomStructuresDataV2(this, createDataId(tableName_v2), tableName_v2,
                seed_v2);
        }
        return null;
    }
    
    @Override
    public boolean provideLootTableKeys(World world, CategorizedKeyMap keyMap) {
        ICustomStructuresProvider provider = CompatDependency.getActiveProvider(id(), ICustomStructuresProvider.class);
        if (provider == null) {
            return false;
        }
        provider.access().provideLootTableKeys(keyMap);
        return true;
    }
    
    @Override
    public ICompatibilityData createData(String key, long seed) {
        return create(key, seed);
    }

}
