package me.lauriichan.spigot.justlootit.compatibility.support;

public final class SupportHelper {

    public static final boolean DEFAULT_TRUE = true;
    public static final boolean DEFAULT_FALSE = false;

    private SupportHelper() {
        throw new UnsupportedOperationException();
    }

    public static boolean abortOnChange(boolean current, boolean newState) {
        return current == newState;
    }

}
