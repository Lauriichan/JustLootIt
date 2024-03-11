package me.lauriichan.spigot.justlootit.inventory;

import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;

public final class ItemHelper {

    public static enum PageType {
        NEXT,
        CURRENT,
        PREVIOUS,
        LAST;
    }

    private ItemHelper() {
        throw new UnsupportedOperationException();
    }

    public static ItemEditor nextPage(Actor<Player> actor) {
        return ItemEditor.ofHead(Textures.GEODE_ARROW_FORWARD_SINGLE)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_PAGE_NEXT)).apply();
    }

    public static ItemEditor previousPage(Actor<Player> actor) {
        return ItemEditor.ofHead(Textures.GEODE_ARROW_BACKWARD_SINGLE)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_PAGE_PREVIOUS)).apply();
    }

    public static ItemEditor lastPage(Actor<Player> actor) {
        return ItemEditor.ofHead(Textures.GEODE_ARROW_FORWARD_DOUBLE)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_PAGE_LAST)).apply();
    }

    public static ItemEditor firstPage(Actor<Player> actor) {
        return ItemEditor.ofHead(Textures.GEODE_ARROW_BACKWARD_DOUBLE)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_PAGE_FIRST)).apply();
    }

    public static ItemEditor currentPage(Actor<Player> actor, int current, int max) {
        return ItemEditor.ofHead(Textures.GEODE_OCTOTHORPE)
            .setName(
                actor.getTranslatedMessageAsString(UIInventoryNames.GENERAL_PAGE_CURRENT, Key.of("current", current), Key.of("max", max)))
            .apply();
    }

    public static ItemEditor pageCount(ItemEditor editor, int current, int max, PageType type) {
        int amount = select(current, max, type);
        if (amount > 64 || amount < 1) {
            amount = 1;
        }
        return editor.setAmount(amount, true);
    }
    
    private static int select(int current, int max, PageType type) {
        switch(type) {
        case NEXT:
            return current + 1;
        case PREVIOUS:
            return current - 1;
        case LAST:
            return max;
        case CURRENT:
            return current;
        default:
            return 1;
        }
    }

}
