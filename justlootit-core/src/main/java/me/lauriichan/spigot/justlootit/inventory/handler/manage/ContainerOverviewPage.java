package me.lauriichan.spigot.justlootit.inventory.handler.manage;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.ClickType;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.item.ColoredLoreEditor;
import me.lauriichan.minecraft.pluginbase.inventory.item.ItemEditor;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PageContext;
import me.lauriichan.minecraft.pluginbase.util.math.InventoryMath;
import me.lauriichan.spigot.justlootit.capability.ActorCapability;
import me.lauriichan.spigot.justlootit.data.*;
import me.lauriichan.spigot.justlootit.inventory.Textures;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.storage.Stored;

@Extension
public final class ContainerOverviewPage extends ContainerPage {

    // TODO: Make frame item editable

    // TODO: Make vanilla lootable changable

    @Override
    public boolean defaultPage() {
        return true;
    }

    @Override
    public void onPageUpdate(PageContext<ContainerPage, PlayerAdapter> context, boolean changed) {
        Actor<Player> actor = ActorCapability.actor(context.player());
        IGuiInventory inventory = context.inventory();
        @SuppressWarnings("unchecked")
        Stored<Container> storedContainer = inventory.attr(ContainerPageHandler.ATTR_CONTAINER, Stored.class).cast();
        if (inventory
            .updater().chestSize(ChestSize.GRID_4x9).title(actor
                .getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_NAME, Key.of("id", storedContainer.id())))
            .apply()) {
            return;
        }
        Container container = storedContainer.value();
        inventory.fill(ItemEditor.of(Material.BLACK_STAINED_GLASS_PANE).setName("&r"));
        inventory.set(0, 4, container.createIcon());
        switch (container.type()) {
        case FRAME:
            onFrameContainer((FrameContainer) container, inventory, actor);
            break;
        case STATIC:
            onStaticContainer((StaticContainer) container, inventory, actor);
            break;
        case VANILLA:
            onVanillaContainer((VanillaContainer) container, inventory, actor);
            break;
        case COMPATIBILITY:
            onCompatibilityContainer((CompatibilityContainer) container, inventory, actor);
            break;
        default:
            break;
        }
        inventory.set(2, 4, ItemEditor.ofHead(Textures.GEODE_EXLAMATION_MARK)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_ACCESS_NAME,
                Key.of("amount", container.accessAmount())))
            .lore().set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_ACCESS_LORE).split("\n"))
            .apply());
        String refreshGroupId = container.getGroupId();
        inventory.set(2, 7,
            ItemEditor.ofHead(Textures.GEODE_OCTOTHORPE)
                .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_REFRESH_GROUP_NAME)).lore()
                .set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_REFRESH_GROUP_LORE,
                    Key.of("id", refreshGroupId == null || refreshGroupId.isEmpty() ? "None" : refreshGroupId)).split("\n"))
                .apply());
    }

    private void onFrameContainer(FrameContainer container, IGuiInventory inventory, Actor<Player> actor) {
        inventory.set(1, 1, ItemEditor.ofHead(Textures.GEODE_QUESTION_MARK)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_FRAME_NAME)));
        inventory.set(2, 1, container.getItem().clone());
    }

    private void onStaticContainer(StaticContainer container, IGuiInventory inventory, Actor<Player> actor) {
        inventory.set(2, 1,
            ItemEditor.ofHead(Textures.GEODE_SETTINGS)
                .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_STATIC_NAME,
                    Key.of("size", container.amount())))
                .lore()
                .set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_STATIC_LORE)
                    .split("\n"))
                .apply());
    }

    private void onVanillaContainer(VanillaContainer container, IGuiInventory inventory, Actor<Player> actor) {
        inventory.set(2, 1, ItemEditor.ofHead(Textures.GEODE_BLANK)
            .setName(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_NAME))
            .lore().set(actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_VANILLA_LORE,
                Key.of("seed", container.getSeed()), Key.of("lootTable", container.getLootTableKey())).split("\n"))
            .apply());
    }

    private void onCompatibilityContainer(CompatibilityContainer container, IGuiInventory inventory, Actor<Player> actor) {
        ColoredLoreEditor loreEditor = ItemEditor.ofHead(Textures.GEODE_BLANK)
            .setName(
                actor.getTranslatedMessageAsString(UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_NAME))
            .lore();
        loreEditor.clear();
        Object2ObjectArrayMap<String, ObjectArrayList<Object>> values = new Object2ObjectArrayMap<>();
        container.getCompatibilityData().addInfoData(key -> {
            ObjectArrayList<Object> list = values.get(key.getKey());
            if (list == null) {
                list = new ObjectArrayList<>();
                values.put(key.getKey(), list);
            }
            list.add(key.getValue());
        });
        if (!values.isEmpty()) {
            values.object2ObjectEntrySet().forEach(entry -> {
                if (entry.getValue().size() == 1) {
                    loreEditor.add(actor.getTranslatedMessageAsString(
                        UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_FORMAT,
                        Key.of("key", entry.getKey()), Key.of("value", entry.getValue().get(0))).split("\n"));
                    return;
                }
                loreEditor.add(actor.getTranslatedMessageAsString(
                    UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_MULTI_FORMAT_HEADER,
                    Key.of("key", entry.getKey())).split("\n"));
                for (Object value : entry.getValue()) {
                    loreEditor.add(actor.getTranslatedMessageAsString(
                        UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_MULTI_FORMAT_ENTRY,
                        Key.of("value", value)).split("\n"));
                }
            });
        } else {
            loreEditor
                .add(actor
                    .getTranslatedMessageAsString(
                        UIInventoryNames.CONTAINER_MANAGE_PAGE_OVERVIEW_ITEM_CONTAINER_INFO_COMPATIBILITY_LORE_NO_DATA_AVAILABLE)
                    .split("\n"));
        }
        inventory.set(2, 1, loreEditor.apply());
    }

    @Override
    public boolean onClickPickup(PageContext<ContainerPage, PlayerAdapter> context, ItemStack item, int slot, int amount, boolean cursor,
        ClickType type) {
        @SuppressWarnings("unchecked")
        Stored<Container> container = context.inventory().attr(ContainerPageHandler.ATTR_CONTAINER, Stored.class);
        if (slot == InventoryMath.toSlot(2, 1, 9)) {
            ContainerType containerType = container.value().type();
            switch (containerType) {
            case STATIC:
                context.openPage(ContainerStaticInventoryPage.class);
                break;
            default:
                break;
            }
            return true;
        }
        if (slot == InventoryMath.toSlot(2, 4, 9)) {
            context.openPage(ContainerAccessListPage.class);
            return true;
        }
        return true;
    }

}
