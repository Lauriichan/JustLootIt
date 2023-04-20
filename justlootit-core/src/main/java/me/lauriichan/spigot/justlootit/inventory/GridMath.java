package me.lauriichan.spigot.justlootit.inventory;

public final class GridMath {

    public static final int DEFAULT_ROW_SIZE = 9;

    private GridMath() {
        throw new UnsupportedOperationException();
    }

    public static int toSlot(int row, int column) {
        return toSlot(row, column, DEFAULT_ROW_SIZE);
    }

    public static int toSlot(int row, int column, int rowSize) {
        return row * rowSize + column;
    }

    public static int checkSlot(int row, int column, int rowSize, int columnAmount) throws IndexOutOfBoundsException {
        while (row >= rowSize) {
            column++;
            row -= rowSize;
        }
        if (column >= columnAmount) {
            throw new IndexOutOfBoundsException(columnAmount);
        }
        return row * rowSize + column;
    }

    public static int[] fromSlot(int slot) {
        return fromSlot(slot, DEFAULT_ROW_SIZE);
    }

    public static int[] fromSlot(int slot, int rowSize) {
        final int[] output = new int[2];
        output[1] = slot % rowSize;
        output[0] = (slot - output[1]) / rowSize;
        return output;
    }

}