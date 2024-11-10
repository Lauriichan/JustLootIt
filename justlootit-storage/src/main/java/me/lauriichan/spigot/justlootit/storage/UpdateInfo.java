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

    public static <T> UpdateInfo<T> modify(T value) {
        return new UpdateInfo(UpdateState.MODIFY, value);
    }

    private final UpdateState state;
    private final T value;

    private UpdateInfo(final UpdateState state, final T value) {
        this.state = state;
        this.value = value;
    }

    public UpdateState state() {
        return state;
    }
    
    public T value() {
        return value;
    }

}
