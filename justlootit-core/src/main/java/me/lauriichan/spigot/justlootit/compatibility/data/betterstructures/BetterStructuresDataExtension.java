package me.lauriichan.spigot.justlootit.compatibility.data.betterstructures;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures.IBetterStructuresProvider;
import me.lauriichan.spigot.justlootit.data.io.BufIO;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

@Extension
public class BetterStructuresDataExtension extends CompatibilityDataExtension<IBetterStructuresData> {

    private static final NamespacedKey GENERIC = NamespacedKey.fromString("betterstructures:justlootit/generic");

    public BetterStructuresDataExtension() {
        super("BetterStructures", IBetterStructuresData.class);
    }

    public IBetterStructuresData create(String fileName) {
        return new BetterStructuresDataV1(this, createDataId(fileName), fileName);
    }

    private NamespacedKey createDataId(String name) {
        try {
            return NamespacedKey.fromString("betterstructures:" + name);
        } catch (IllegalArgumentException iae) {
            return GENERIC;
        }
    }

    @Override
    public Material iconType() {
        return Material.DEEPSLATE_BRICKS;
    }

    @Override
    public void save(ByteBuf buffer, IBetterStructuresData data) {
        switch (data.version()) {
        case 0:
            BetterStructuresDataV1 v1 = (BetterStructuresDataV1) data;
            BufIO.writeString(buffer, v1.fileName());
            return;
        }
    }

    @Override
    public IBetterStructuresData load(ByteBuf buffer, int version) {
        switch (version) {
        case 0:
            String fileName_v1 = BufIO.readString(buffer);
            return new BetterStructuresDataV1(this, NamespacedKey.fromString("betterstructures:" + fileName_v1), fileName_v1);
        }
        return null;
    }

    @Override
    public boolean provideLootTableKeys(World world, CategorizedKeyMap keyMap) {
        IBetterStructuresProvider provider = CompatDependency.getActiveProvider(id(), IBetterStructuresProvider.class);
        if (provider == null) {
            return false;
        }
        provider.access().provideLootTableKeys(keyMap);
        return true;
    }

    @Override
    public ICompatibilityData createData(String key, long seed) {
        return create(key);
    }

}
