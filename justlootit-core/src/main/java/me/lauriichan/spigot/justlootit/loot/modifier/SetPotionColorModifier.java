package me.lauriichan.spigot.justlootit.loot.modifier;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import me.lauriichan.minecraft.pluginbase.util.color.ColorParser;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record SetPotionColorModifier(Color color) implements ILootModifier {
    
    public SetPotionColorModifier(String colorString) {
        this(Color.fromARGB(ColorParser.parse(colorString).getRGB()));
    }
    
    public String colorString() {
        return ColorParser.asString(new java.awt.Color(color.asRGB()));
    }

    @Override
    public void modify(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (metaRef.isEmpty() || !(metaRef.get() instanceof PotionMeta potionMeta)) {
            return;
        }
        potionMeta.setColor(color);
    }

}
