package me.lauriichan.spigot.justlootit.inventory.handler.item;

import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;

public final class ColoredNameEditor extends ColoredStringEditor<ColoredNameEditor> {
    
    public static ColoredNameEditor of(final ItemEditor editor) {
        return new ColoredNameEditor(editor);
    }

    public ColoredNameEditor(final ItemEditor editor) {
        super(editor);
    }

    public String asPlainString() {
        return content.asPlainString();
    }

    @Override
    public ItemEditor apply() {
        if (editor.hasItemMeta()) {
            editor.getItemMeta().setDisplayName(content.asColoredString());
        }
        return editor;
    }

}