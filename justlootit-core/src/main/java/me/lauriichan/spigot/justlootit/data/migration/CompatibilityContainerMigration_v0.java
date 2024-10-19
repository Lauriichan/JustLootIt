package me.lauriichan.spigot.justlootit.data.migration;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;

@Extension
public final class CompatibilityContainerMigration_v0 extends ContainerDataMigration_v0<CompatibilityContainer> {

    public CompatibilityContainerMigration_v0() {
        super(CompatibilityContainer.class);
    }

}
