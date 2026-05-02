package me.lauriichan.spigot.justlootit.inventory.handler.tables;

import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.ClickType;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PageContext;
import me.lauriichan.minecraft.pluginbase.util.StringUtil;
import me.lauriichan.minecraft.pluginbase.util.math.InventoryMath;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.inventory.ItemHelper;
import me.lauriichan.spigot.justlootit.inventory.ItemHelper.PageType;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;
import me.lauriichan.spigot.justlootit.util.persistence.TableKey;

public abstract class LootTableViewerTabPage extends LootTableViewerPage {

    public static enum LootTableType {

        CUSTOM(CustomTableViewerPage.class),
        VANILLA(VanillaTableViewerPage.class),
        COMPATIBILITY(CompatibilityTableViewerPage.class),;

        private final Class<? extends LootTableViewerTabPage> page;
        private final String name;

        private LootTableType(Class<? extends LootTableViewerTabPage> page) {
            this.page = page;
            this.name = StringUtil.formatPascalCase(name());
        }

        public String typeName() {
            return name;
        }

        public Class<? extends LootTableViewerTabPage> page() {
            return page;
        }

    }

    private static final LootTableType[] TYPES = LootTableType.values();

    public static final String ATTR_CACHED = "ViewerCached";
    public static final String ATTR_PATH = "ViewerPath";
    public static final String ATTR_PAGE = "ViewerPage";
    public static final String ATTR_MAX_PAGE = "ViewerMaxPage";

    protected int currentPageIndex() {
        return 0;
    }

    @Override
    public void onPageUpdate(PageContext<LootTableViewerPage, PlayerAdapter> context, boolean changed) {
        IGuiInventory inventory = context.inventory();
        Actor<Player> actor = ActorCapability.actor(context.player());

        if (inventory
            .updater().chestSize(ChestSize.GRID_6x9).title(actor
                .getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_NAME, Key.of("type", tableType().typeName())))
            .apply()) {
            return;
        }

        inventory.fill(ItemEditor.of(Material.BLACK_STAINED_GLASS_PANE).setName("&r"));
        inventory.clearSection(1, 1, 1, 7);
        inventory.clearSection(2, 1, 2, 7);
        inventory.clearSection(3, 1, 3, 7);
        inventory.clearSection(4, 1, 4, 7);

        for (LootTableType type : TYPES) {
            boolean selected;
            LootTableViewerTabPage page = (selected = (type == tableType())) ? this : (LootTableViewerTabPage) context.getPage(type.page());
            inventory.set(1 + type.ordinal(),
                page.createIcon(selected)
                    .setName(actor.getTranslatedMessageAsString(selected ? UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_SELECTED_NAME
                        : UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_UNSELECTED_NAME, Key.of("name", type.typeName())))
                    .lore()
                    .set(actor
                        .getTranslatedMessageAsString(selected ? UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_SELECTED_LORE
                            : UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_TAB_UNSELECTED_LORE, Key.of("name", type.typeName()))
                        .split("\n"))
                    .apply());
        }

        CategorizedKeyMap keyMap = keyMap(context);
        String path = inventory.attr(ATTR_PATH, String.class);
        if (path != null && !path.isEmpty()) {
            keyMap = keyMap.get(path);
        }

        boolean hasParent = keyMap.parent() != null;
        if (hasParent && keyMap.isKey()) {
            keyMap = keyMap.parent();
        }
        if ((path = keyMap.path()).isEmpty()) {
            inventory.attrUnset(ATTR_PATH);
        } else {
            inventory.attrSet(ATTR_PATH, path);
        }

        int maxAmount = hasParent ? 27 : 28;
        int amount = keyMap.childrenCount();

        int maxPage = Math.max(Math.floorDiv(amount, maxAmount), 1);
        if (amount > maxAmount && amount % maxAmount != 0) {
            maxPage++;
        }
        inventory.attrSet(ATTR_MAX_PAGE, maxPage);
        int page = Math.min(inventory.attrOrDefault(ATTR_PAGE, Number.class, 1).intValue(), maxPage);
        inventory.attrSet(ATTR_PAGE, page);
        if (page > 1) {
            if (page != 2) {
                inventory.set(5, 1, ItemHelper.firstPage(actor));
            }
            inventory.set(5, 2, ItemHelper.pageCount(ItemHelper.previousPage(actor), page, maxPage, PageType.PREVIOUS));
        }
        if (page != maxPage) {
            if (page + 1 != maxPage) {
                inventory.set(5, 7, ItemHelper.pageCount(ItemHelper.lastPage(actor), page, maxPage, PageType.LAST));
            }
            inventory.set(5, 6, ItemHelper.pageCount(ItemHelper.nextPage(actor), page, maxPage, PageType.NEXT));
        }
        inventory.set(5, 4, ItemHelper.pageCount(ItemHelper.currentPage(actor, page, maxPage), page, maxPage, PageType.CURRENT));

        int currentIdx = 0;
        if (hasParent) {
            inventory.set(1, 1,
                ItemHelper.goBack(actor).lore()
                    .set(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_BACK_LORE,
                        Key.of("name", keyMap.parent().name()), Key.of("path", keyMap.path())).split("\n"))
                    .apply());
            currentIdx++;
        }

        ObjectList<CategorizedKeyMap> maps = keyMap.childrenList();
        maps.sort(Comparator.naturalOrder());
        ObjectIterator<CategorizedKeyMap> mapIter = maps.iterator();
        int offset = (page - 1) * maxAmount;
        for (int i = 0; i < offset; i++) {
            mapIter.next();
        }
        // To offset by 1 if there is a back button
        maxAmount += currentIdx;
        for (; mapIter.hasNext() && currentIdx < maxAmount; currentIdx++) {
            CategorizedKeyMap map = mapIter.next();
            ItemEditor editor;
            if (!map.isKey()) {
                editor = ItemEditor.of(Material.CHEST)
                    .setName(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_CATEGORY_NAME,
                        Key.of("name", map.name()), Key.of("path", map.path())))
                    .lore().set(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_CATEGORY_LORE,
                        Key.of("name", map.name()), Key.of("path", map.path())).split("\n"))
                    .apply();
            } else {
                editor = ItemEditor.of(Material.MAP)
                    .setName(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_TABLE_NAME,
                        Key.of("name", map.name()), Key.of("path", map.path())))
                    .lore().set(actor.getTranslatedMessageAsString(UIInventoryNames.LOOTTABLE_VIEWER_PAGE_BROWSER_ENTRY_TABLE_LORE,
                        Key.of("name", map.name()), Key.of("path", map.path())).split("\n"))
                    .apply();
            }
            int row = 1 + Math.floorDiv(currentIdx, 7), column = 1 + currentIdx % 7;
            inventory.set(row, column, editor.applyItemMeta(
                meta -> meta.getPersistentDataContainer().set(JustLootItKey.identity(), PersistentDataType.STRING, map.path())));
        }
    }

    private CategorizedKeyMap keyMap(PageContext<LootTableViewerPage, PlayerAdapter> context) {
        CategorizedKeyMap keyMap = context.inventory().attr(ATTR_CACHED, CategorizedKeyMap.class);
        if (keyMap == null) {
            keyMap = new CategorizedKeyMap();
            provideLootTableKeys(context.player().asBukkit().getWorld(), keyMap);
            context.inventory().attrSet(ATTR_CACHED, keyMap);
        }
        return keyMap;
    }

    @Override
    public boolean onClickPickup(PageContext<LootTableViewerPage, PlayerAdapter> context, ItemStack item, int slot, int amount,
        boolean cursor, ClickType type) {
        IGuiInventory inventory = context.inventory();
        if (slot > 0 && slot < 8) {
            int index = slot - 1;
            if (index > TYPES.length) {
                return true;
            }
            LootTableType tableType = TYPES[index];
            if (tableType == tableType()) {
                return true;
            }
            inventory.attrUnset(ATTR_CACHED);
            inventory.attrUnset(ATTR_PATH);
            context.openPage(tableType.page());
            return true;
        }
        if (slot == InventoryMath.toSlot(5, 1, 9)) {
            inventory.attrSet(ATTR_PAGE, 1);
            inventory.update();
            return true;
        }
        if (slot == InventoryMath.toSlot(5, 2, 9)) {
            inventory.attrSet(ATTR_PAGE, inventory.attr(ATTR_PAGE, Number.class).intValue() - 1);
            inventory.update();
            return true;
        }
        if (slot == InventoryMath.toSlot(5, 6, 9)) {
            inventory.attrSet(ATTR_PAGE, inventory.attr(ATTR_PAGE, Number.class).intValue() + 1);
            inventory.update();
            return true;
        }
        if (slot == InventoryMath.toSlot(5, 7, 9)) {
            inventory.attrSet(ATTR_PAGE, inventory.attr(ATTR_MAX_PAGE, Number.class).intValue());
            inventory.update();
            return true;
        }
        Material itemType = item.getType();
        if (itemType == Material.PLAYER_HEAD && slot == InventoryMath.toSlot(1, 1, 9)) {
            goBack(context);
            inventory.update();
            return true;
        }
        if (itemType == Material.CHEST) {
            String path = item.getItemMeta().getPersistentDataContainer().get(JustLootItKey.identity(), PersistentDataType.STRING);
            inventory.attrSet(ATTR_PATH, path);
            inventory.update();
            return true;
        }
        if (itemType == Material.MAP) {
            String[] namespaceAndKey = item.getItemMeta().getPersistentDataContainer()
                .get(JustLootItKey.identity(), PersistentDataType.STRING).split("/", 2);
            inventory.attrSet(ChooseItemTableViewerPage.ATTR_TABLE_KEY, new TableKey(tableType(), namespaceAndKey[0], namespaceAndKey[1]));
            context.openPage(ChooseItemTableViewerPage.class);
            return true;
        }
        return true;
    }

    private void goBack(PageContext<LootTableViewerPage, PlayerAdapter> context) {
        IGuiInventory inventory = context.inventory();
        CategorizedKeyMap keyMap = keyMap(context);
        String path = inventory.attr(ATTR_PATH, String.class);
        if (path != null && !path.isEmpty()) {
            keyMap = keyMap.get(path).parent();
        }
        if ((path = keyMap.path()).isEmpty()) {
            inventory.attrUnset(ATTR_PATH);
        } else {
            inventory.attrSet(ATTR_PATH, path);
        }
    }

    @Override
    public boolean onInventoryClose(PageContext<LootTableViewerPage, PlayerAdapter> context) {
        if (!context.inventory().attrHas(ATTR_PATH, String.class)) {
            return false;
        }
        goBack(context);
        return true;
    }

    protected abstract void provideLootTableKeys(World world, CategorizedKeyMap keyMap);

    protected abstract LootTableType tableType();

    protected abstract ItemEditor createIcon(boolean selected);

}
