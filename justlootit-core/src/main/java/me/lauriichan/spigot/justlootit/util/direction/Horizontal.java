package me.lauriichan.spigot.justlootit.util.direction;

public enum Horizontal {

    NORTH,
    NORTH_NORTH_EAST(0),
    NORTH_EAST(0),
    EAST_NORTH_EAST(4),
    EAST,
    EAST_SOUTH_EAST(4),
    SOUTH_EAST(4),
    SOUTH_SOUTH_EAST(8),
    SOUTH,
    SOUTH_SOUTH_WEST(8),
    SOUTH_WEST(8),
    WEST_SOUTH_WEST(12),
    WEST,
    WEST_NORTH_WEST(12),
    NORTH_WEST(12),
    NORTH_NORTH_WEST(0);

    private static final Horizontal[] VALUES = values();

    private final int normalization;

    Horizontal() {
        this.normalization = ordinal();
    }

    Horizontal(final int normalization) {
        this.normalization = normalization;
    }

    public Horizontal normalize() {
        return VALUES[normalization];
    }

}
