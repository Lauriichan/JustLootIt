package me.lauriichan.spigot.justlootit.storage;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class UpdateInfo<S extends Storable> {

    public static enum UpdateState {

        NONE,
        MODIFY,
        DELETE;

    }

    private static final UpdateInfo NONE = new UpdateInfo(UpdateState.NONE, null);
    private static final UpdateInfo MODIFY = new UpdateInfo(UpdateState.MODIFY, null);
    private static final UpdateInfo DELETE = new UpdateInfo(UpdateState.DELETE, null);

    public static final <T extends Storable> UpdateInfo<T> none() {
        return NONE;
    }

    public static final <T extends Storable> UpdateInfo<T> delete() {
        return DELETE;
    }
    
    public static final <T extends Storable> UpdateInfo<T> modify() {
        return MODIFY;
    }
    
    public static final <T extends Storable> UpdateInfo<T> modify(T storable) {
        return new UpdateInfo<>(UpdateState.MODIFY, storable);
    }

    private final UpdateState state;
    private final S storable;

    private UpdateInfo(UpdateState state, S storable) {
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
