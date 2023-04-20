package me.lauriichan.spigot.justlootit.util.direction;

public enum Vertical {

    UP,
    UP_MID(2),
    MID,
    MID_DOWN(2),
    DOWN;

    private static final Vertical[] VALUES = values();

    private final int normalization;

    Vertical() {
        this.normalization = ordinal();
    }

    Vertical(final int normalization) {
        this.normalization = normalization;
    }

    public Vertical normalize() {
        return VALUES[normalization];
    }

}
