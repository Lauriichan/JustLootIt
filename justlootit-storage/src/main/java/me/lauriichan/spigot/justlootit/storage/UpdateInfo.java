package me.lauriichan.spigot.justlootit.storage;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class UpdateInfo<S extends Storable> {

    public enum UpdateState {

        NONE,
        MODIFY,
        DELETE;

    }

    private static final UpdateInfo NONE = new UpdateInfo(UpdateState.NONE, null);
    private static final UpdateInfo MODIFY = new UpdateInfo(UpdateState.MODIFY, null);
    private static final UpdateInfo DELETE = new UpdateInfo(UpdateState.DELETE, null);

    public static <T extends Storable> UpdateInfo<T> none() {
        return NONE;
    }

    public static <T extends Storable> UpdateInfo<T> delete() {
        return DELETE;
    }

    public static <T extends Storable> UpdateInfo<T> modify() {
        return MODIFY;
    }

    public static <T extends Storable> UpdateInfo<T> modify(final T storable) {
        return new UpdateInfo<>(UpdateState.MODIFY, storable);
    }

    private final UpdateState state;
    private final S storable;

    private UpdateInfo(final UpdateState state, final S storable) {
        this.state = state;
        this.storable = storable;
    }

    public UpdateState state() {
        return state;
    }

    public S storable() {
        return storable;
    }

}
