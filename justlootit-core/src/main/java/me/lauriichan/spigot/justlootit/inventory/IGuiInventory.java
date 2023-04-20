package me.lauriichan.spigot.justlootit.inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.inventory.item.ItemEditor;
import me.lauriichan.spigot.justlootit.util.attribute.IAttributable;

public interface IGuiInventory extends IAttributable {

    static int getRowSize(final InventoryType type) {
        switch (type) {
        case DISPENSER:
        case DROPPER:
            return 3;
        case HOPPER:
            return 5;
        case CHEST:
        case BARREL:
        case ENDER_CHEST:
            return 9;
        default:
            return 1;
        }
    }

    /**
     * Gets the bukkit inventory
     * 
     * @return the bukkit {@link Inventory}
     */
    Inventory getInventory();

    /**
     * Opens the inventory for the specified {@link HumanEntity}
     * 
     * @param entity the entity to open the inventory for
     */
    default void open(final HumanEntity entity) {
        entity.openInventory(getInventory());
    }

    /**
     * Gets the currently set handler for this inventory
     * 
     * @return the {@link IHandler} if a handler was already set otherwise
     *         {@code null}
     */
    IHandler getHandler();

    /**
     * Checks if this inventory has a handler
     * 
     * @return {@code true} if the inventory has a handler otherwise {@code false}
     */
    default boolean hasHandler() {
        return getHandler() != null;
    }

    /**
     * Sets the handler for this inventory
     * 
     * @param  handler the handler to set
     * 
     * @return         {@code true} if the handler was updated otherwise
     *                 {@code false}
     */
    boolean setHandler(IHandler handler);

    /**
     * Sets the title of the inventory
     * 
     * @param  title the title to set
     * 
     * @return       {@code true} if the inventory was changed otherwise
     *               {@code false}
     */
    boolean setTitle(String title);

    /**
     * Gets the title of the inventory
     * 
     * @return the title of the inventory or {@code null} if unsupported
     */
    String getTitle();

    /**
     * Sets the generic chest size of the inventory
     * 
     * @param  chestSize the chest size to set
     * 
     * @return           {@code true} if the inventory was changed otherwise
     *                   {@code false}
     */
    boolean setChestSize(ChestSize chestSize);

    /**
     * Gets the chest size of the inventory
     * 
     * @return the {@link ChestSize} or {@code null} if the inventory is not a
     *         generic chest-like container
     */
    ChestSize getChestSize();

    /**
     * Sets the type of the inventory
     * 
     * @param  type the inventory type to set
     * 
     * @return      {@code true} if the inventory was changed otherwise
     *              {@code false}
     */
    boolean setType(InventoryType type);

    /**
     * Gets the type of the inventory
     * 
     * @return the type of the inventory
     */
    InventoryType getType();

    /**
     * Triggers an handler update for this inventory
     */
    void update();

    /**
     * Gets the size of the rows of the inventory
     * 
     * @return the size of the rows
     */
    int getRowSize();

    /**
     * Gets the amount of columns that the inventory has
     * 
     * @return the amount of columns
     */
    int getColumnAmount();

    /**
     * Gets the slot amount of the inventory
     * 
     * @return the slot amount
     */
    int size();

    /**
     * Clears all items from all slots
     */
    void clear();

    /**
     * Gets the item of a slot
     * 
     * @param  index                     the slot index
     * 
     * @return                           the item at that slot or {@code null} if
     *                                   there is none
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    ItemStack get(int index);

    /**
     * Gets the item of a slot
     * 
     * @param  index                     the slot index
     * 
     * @return                           the item at that slot or {@code null} if
     *                                   there is none
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default ItemStack getItemStack(final int index) throws IndexOutOfBoundsException {
        return get(index);
    }

    /**
     * Gets the item of a slot
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * 
     * @return                           the item at that slot or {@code null} if
     *                                   there is none
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                   bounds
     */
    default ItemStack getItemStack(final int row, final int column) throws IndexOutOfBoundsException {
        return getItemStack(GridMath.checkSlot(row, column, getRowSize(), getColumnAmount()));
    }

    /**
     * Gets the item of a slot as an editor
     * 
     * @param  index                     the slot index
     * 
     * @return                           the item editor of the item at that slot or
     *                                   {@code null} if there is none
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default ItemEditor getItemEditor(final int index) throws IndexOutOfBoundsException {
        return ItemEditor.ofNullable(getItemStack(index));
    }

    /**
     * Gets the item of a slot as an editor
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * 
     * @return                           the item editor of the item at that slot or
     *                                   {@code null} if there is none
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                   bounds
     */
    default ItemEditor getItemEditor(final int row, final int column) throws IndexOutOfBoundsException {
        return ItemEditor.ofNullable(getItemStack(row, column));
    }

    /**
     * Sets the item at a slot
     * 
     * @param  index                     the slot index
     * @param  itemStack                 the item to be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    void set(int index, ItemStack itemStack);

    /**
     * Sets the item at a slot
     * 
     * @param  index                     the slot index
     * @param  itemStack                 the item to be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default void setItemStack(final int index, final ItemStack itemStack) throws IndexOutOfBoundsException {
        set(index, itemStack);
    }

    /**
     * Sets the item at a slot
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * @param  itemStack                 the item to be set
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                   bounds
     */
    default void setItemStack(final int row, final int column, final ItemStack itemStack) throws IndexOutOfBoundsException {
        setItemStack(GridMath.checkSlot(row, column, getRowSize(), getColumnAmount()), itemStack);
    }

    /**
     * Sets the item from an editor at a slot
     * 
     * @param  index                     the slot index
     * @param  itemStack                 the item editor to retrieve the item from
     *                                   that should be set
     * 
     * @throws IndexOutOfBoundsException if slot index is out of bounds
     */
    default void setItemEditor(final int index, final ItemEditor editor) throws IndexOutOfBoundsException {
        if (editor == null) {
            setItemStack(index, null);
            return;
        }
        setItemStack(index, editor.asItemStack());
    }

    /**
     * Sets the item from an editor at a slot
     * 
     * @param  row                       the row index
     * @param  column                    the column index
     * @param  itemStack                 the item editor to retrieve the item from
     *                                   that should be set
     * 
     * @throws IndexOutOfBoundsException if converted slot index would be out of
     *                                   bounds
     */
    default void setItemEditor(final int row, final int column, final ItemEditor editor) throws IndexOutOfBoundsException {
        if (editor == null) {
            setItemStack(row, column, null);
            return;
        }
        setItemStack(row, column, editor.asItemStack());
    }

    /**
     * Finds all slots that are similar the the provided {@link ItemStack}
     *
     * @param  itemStack the item to find slots for
     * 
     * @return           the slots as map
     */
    default Map<Integer, ItemStack> findSimilarSlots(final ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyMap();
        }
        final HashMap<Integer, ItemStack> map = new HashMap<>();
        final int size = size();
        for (int index = 0; index < size; index++) {
            final ItemStack current = get(index);
            if (current == null || !current.isSimilar(itemStack)) {
                continue;
            }
            map.put(index, current);
        }
        return map;
    }

    /**
     * Finds all slots in that the provided {@link ItemStack} can fit in
     *
     * @param  itemStack the item to check for
     * 
     * @return           the possible slots as map
     */
    default Map<Integer, ItemStack> findPossibleSlots(final ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyMap();
        }
        final HashMap<Integer, ItemStack> map = new HashMap<>();
        final int size = size();
        for (int index = 0; index < size; index++) {
            final ItemStack current = get(index);
            if (current != null && !current.isSimilar(itemStack) && !current.getType().isAir()) {
                continue;
            }
            map.put(index, current);
        }
        return map;
    }

}
