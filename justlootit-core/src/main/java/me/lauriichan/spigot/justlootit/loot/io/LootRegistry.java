package me.lauriichan.spigot.justlootit.loot.io;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public final class LootRegistry {

    public static final LootRegistry REGISTRY = new LootRegistry();

    private LootRegistry() {
        if (REGISTRY != null) {
            throw new UnsupportedOperationException();
        }
    }

    private Enchantment enchUnbreaking;
    private Attribute attrLuck;

    public Enchantment enchUnbreaking() {
        if (enchUnbreaking != null) {
            return enchUnbreaking;
        }
        return enchUnbreaking = enchantment().get(NamespacedKey.minecraft("unbreaking"));
    }

    public Attribute attrLuck() {
        if (attrLuck != null) {
            return attrLuck;
        }
        return attrLuck = attribute().get(NamespacedKey.minecraft("luck"));
    }

    public Registry<Material> material() {
        return Registry.MATERIAL;
    }

    public Registry<Attribute> attribute() {
        return Registry.ATTRIBUTE;
    }

    public Registry<Enchantment> enchantment() {
        return Registry.ENCHANTMENT;
    }

    public Registry<PotionEffectType> effect() {
        return Registry.EFFECT;
    }

}
