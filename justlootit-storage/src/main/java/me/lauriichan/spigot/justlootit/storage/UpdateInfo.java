package me.lauriichan.spigot.justlootit.storage;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class UpdateInfo<T> {

    public enum UpdateState {

        NONE,
        MODIFY,
        DELETE;

    }

    private static final UpdateInfo NONE = new UpdateInfo(UpdateState.NONE, null);
    private static final UpdateInfo MODIFY = new UpdateInfo(UpdateState.MODIFY, null);
    private static final UpdateInfo DELETE = new UpdateInfo(UpdateState.DELETE, null);

    public static <T> UpdateInfo<T> none() {
        return NONE;
    }

    public static <T> UpdateInfo<T> delete() {
        return DELETE;
    }

    public static <T> UpdateInfo<T> modify() {
        return MODIFY;
    }

    public static <T> UpdateInfo<T> modify(final Stored<T> stored) {
        return new UpdateInfo<>(UpdateState.MODIFY, stored);
    }

    private final UpdateState state;
    private final Stored<T> stored;

    private UpdateInfo(final UpdateState state, final Stored<T> stored) {
        this.state = state;
        this.stored = stored;
    }

    public UpdateState state() {
        return state;
    }

    public Stored<T> stored() {
        return stored;
    }

}
