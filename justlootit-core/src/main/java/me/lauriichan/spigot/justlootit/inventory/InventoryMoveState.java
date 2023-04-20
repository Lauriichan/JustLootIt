package me.lauriichan.spigot.justlootit.inventory;

public enum InventoryMoveState {

    SOURCE_INITIATOR(true, true),
    SOURCE_DESTINATION(false, true),
    DESTINATION(false, false),
    INITIATOR(true, false);

    private final boolean initiator, source;

    InventoryMoveState(final boolean initiator, final boolean source) {
        this.initiator = initiator;
        this.source = source;
    }

    public boolean isInitiator() {
        return initiator;
    }

    public boolean isSource() {
        return source;
    }

    public static InventoryMoveState of(final boolean initiator, final boolean source) {
        if (initiator) {
            if (source) {
                return InventoryMoveState.SOURCE_INITIATOR;
            }
            return InventoryMoveState.INITIATOR;
        }
        if (source) {
            return InventoryMoveState.SOURCE_DESTINATION;
        }
        return InventoryMoveState.DESTINATION;
    }

}
