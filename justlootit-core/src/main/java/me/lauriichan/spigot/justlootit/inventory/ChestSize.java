package me.lauriichan.spigot.justlootit.inventory;

public enum ChestSize {
    
    GRID_1x9(1),
    GRID_2x9(2),
    GRID_3x9(3),
    GRID_4x9(4),
    GRID_5x9(5),
    GRID_6x9(6);

    private final int inventorySize;

    private ChestSize(final int gridSize) {
        this.inventorySize = gridSize * 9;
    }

    public int inventorySize() {
        return inventorySize;
    }

}