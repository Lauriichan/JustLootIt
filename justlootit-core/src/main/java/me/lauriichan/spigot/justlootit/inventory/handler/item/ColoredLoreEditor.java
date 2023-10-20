package me.lauriichan.spigot.justlootit.inventory.handler.item;

import java.util.List;

import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;

public final class ColoredLoreEditor extends ColoredStringEditor<ColoredLoreEditor> {
    
    public static ColoredLoreEditor of(final ItemEditor editor) {
        return new ColoredLoreEditor(editor);
    }

    public ColoredLoreEditor(final ItemEditor editor) {
        super(editor);
    }

    public List<String> asPlainList() {
        return content.asPlainList();
    }

    @Override
    public ItemEditor apply() {
        if (editor.hasItemMeta()) {
            editor.getItemMeta().setDisplayName(content.asColoredString());
        }
        return editor;
    }

}