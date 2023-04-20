package me.lauriichan.spigot.justlootit.inventory.item;

import java.util.List;

public final class ColoredLoreEditor extends ColoredStringEditor<ColoredLoreEditor> {

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