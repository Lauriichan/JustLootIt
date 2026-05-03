package me.lauriichan.spigot.justlootit.inventory.handler.tables;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.ClickType;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PageContext;
import me.lauriichan.minecraft.pluginbase.util.StringUtil;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItConstant.MaterialInventory;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.inventory.ItemHelper;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.util.RegistryUtil;
import me.lauriichan.spigot.justlootit.util.SoundUtil;
import me.lauriichan.spigot.justlootit.util.persistence.TableKey;
import me.lauriichan.spigot.justlootit.util.registry.EntityRegistry;

@Extension
public class ChooseItemTableViewerPage extends LootTableViewerPage {

    public static final String ATTR_TABLE_KEY = "ViewerTableKey";

    @Override
    public void onPageUpdate(PageContext<LootTableViewerPage, PlayerAdapter> context, boolean changed) {
        IGuiInventory inventory = context.inventory();
        Actor<Player> actor = ActorCapability.actor(context.player());

        if (inventory.updater().chestSize(ChestSize.GRID_6x9)
            .title(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_CHOOSER_NAME)).apply()) {
            return;
        }

        inventory.fill(ItemEditor.of(Material.BLACK_STAINED_GLASS_PANE).setName("&r"));
        inventory.clearSection(1, 1, 1, 7);
        inventory.clearSection(2, 1, 2, 7);
        inventory.clearSection(3, 1, 3, 7);
        inventory.clearSection(4, 1, 4, 7);

        String path = inventory.attr(LootTableViewerTabPage.ATTR_PATH, String.class);
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        inventory.set(1, 1,
            ItemHelper.goBack(actor).lore().set(actor
                .getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_CHOOSER_BACK_LORE, Key.of("path", path)).split("\n"))
                .apply());

        ObjectList<MaterialInventory> items = JustLootItConstant.SUPPORTED_CONTAINER_ITEMS;
        int i = 1;
        for (; i <= items.size(); i++) {
            int row = 1 + Math.floorDiv(i, 7), column = 1 + i % 7;
            MaterialInventory item = items.get(i - 1);
            inventory.set(row, column, ItemEditor.of(item.material()));
        }
        int offset = i;
        ObjectArrayList<EntityType> entityTypes = new ObjectArrayList<>();
        entityTypes.addAll(EntityRegistry.CHEST_BOAT.values());
        entityTypes.addAll(EntityRegistry.MINECART_CHEST.values());
        if (JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()) {
            entityTypes.addAll(EntityRegistry.MINECART_HOPPER.values());
        }
        for (i = 0; i < entityTypes.size(); i++) {
            int row = 1 + Math.floorDiv(i + offset, 7), column = 1 + (i + offset) % 7;
            Material material = Registry.MATERIAL.get(RegistryUtil.getKey(entityTypes.get(i)));
            if (material == null) {
                entityTypes.remove(i--);
                continue;
            }
            inventory.set(row, column, ItemEditor.of(material));
        }
    }

    @Override
    public boolean onClickPickup(PageContext<LootTableViewerPage, PlayerAdapter> context, ItemStack item, int slot, int amount,
        boolean cursor, ClickType type) {
        IGuiInventory inventory = context.inventory();
        Actor<Player> actor = ActorCapability.actor(context.player());

        TableKey key = inventory.attr(ATTR_TABLE_KEY, TableKey.class);

        Material itemType = item.getType();
        if (itemType == Material.PLAYER_HEAD) {
            context.openPage(key.type().page());
            return true;
        }
        if (itemType == Material.BLACK_STAINED_GLASS_PANE || itemType.isAir()) {
            return true;
        }
        Key[] keys = new Key[] {
            Key.of("container.type", StringUtil.formatPascalCase(key.type().name())),
            Key.of("key", key.namespace() + ":" + key.key())
        };
        ItemStack containerItem = ItemEditor.of(itemType)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_CHOOSER_CONTAINER_ITEM_NAME, keys)).lore()
            .set(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_CHOOSER_CONTAINER_ITEM_LORE, keys).split("\n"))
            .apply().applyItemMeta(meta -> meta.getPersistentDataContainer().set(JustLootItKey.identity(), TableKey.KEY_TYPE, key))
            .asItemStack();
        if (!actor.getHandle().getInventory().addItem(containerItem).isEmpty()) {
            SoundUtil.playErrorSound(actor.getHandle());
            return true;
        }
        context.openPage(key.type().page());
        return true;
    }

    @Override
    public void onPageClose(PageContext<LootTableViewerPage, PlayerAdapter> context) {
        context.inventory().attrUnset(ATTR_TABLE_KEY);
    }

}
