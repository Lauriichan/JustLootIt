package me.lauriichan.spigot.justlootit.compatibility.data.betterstructures;

import org.bukkit.Material;

import io.netty.buffer.ByteBuf;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.data.io.BufIO;

@Extension
public class BetterStructuresDataExtension extends CompatibilityDataExtension<IBetterStructuresData> {

    public BetterStructuresDataExtension() {
        super("BetterStructures", IBetterStructuresData.class);
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
            return new BetterStructuresDataV1(this, fileName_v1);
        }
        return null;
    }

    public IBetterStructuresData create(String fileName) {
        return new BetterStructuresDataV1(this, fileName);
    }

}
