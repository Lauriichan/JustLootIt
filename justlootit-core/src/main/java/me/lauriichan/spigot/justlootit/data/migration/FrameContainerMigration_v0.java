package me.lauriichan.spigot.justlootit.data.migration;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.data.FrameContainer;

@Extension
public final class FrameContainerMigration_v0 extends ContainerDataMigration_v0<FrameContainer> {

    public FrameContainerMigration_v0() {
        super(FrameContainer.class);
    }

}
