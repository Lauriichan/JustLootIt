package me.lauriichan.spigot.justlootit.inventory.item;

public final class ColoredNameEditor extends ColoredStringEditor<ColoredNameEditor> {

    public ColoredNameEditor(ItemEditor editor) {
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