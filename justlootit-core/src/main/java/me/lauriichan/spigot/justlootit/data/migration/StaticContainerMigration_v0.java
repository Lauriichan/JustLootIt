package me.lauriichan.spigot.justlootit.data.migration;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.data.StaticContainer;

@Extension
public final class StaticContainerMigration_v0 extends ContainerDataMigration_v0<StaticContainer> {

    public StaticContainerMigration_v0() {
        super(StaticContainer.class);
    }

}
