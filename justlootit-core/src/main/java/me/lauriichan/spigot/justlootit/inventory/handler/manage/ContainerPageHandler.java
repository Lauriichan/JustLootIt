package me.lauriichan.spigot.justlootit.inventory.handler.manage;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.inventory.AbstractPageHandler;

@Extension
public class ContainerPageHandler extends AbstractPageHandler<ContainerPage> {
    
    public static final String ATTR_CONTAINER = "Container";
    public static final String ATTR_WORLD = "World";

    public ContainerPageHandler(JustLootItPlugin plugin) {
        super(plugin, ContainerPage.class);
    }
    
}
