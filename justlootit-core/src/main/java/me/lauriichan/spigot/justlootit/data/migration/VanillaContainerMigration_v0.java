package me.lauriichan.spigot.justlootit.data.migration;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;

@Extension
public final class VanillaContainerMigration_v0 extends ContainerDataMigration_v0<VanillaContainer> {

    public VanillaContainerMigration_v0() {
        super(VanillaContainer.class);
    }

}
