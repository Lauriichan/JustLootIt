package me.lauriichan.spigot.justlootit.data.alternation;

import java.util.Objects;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo.UpdateState;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;
import me.lauriichan.spigot.justlootit.storage.util.executor.ProtoExecutor;

public abstract class AlternationAction<T> {

    private final Class<T> type;

    protected AlternationAction(Class<T> type) {
        this.type = type;
    }

    private boolean isApplicableEntry(UpdateState state, Stored<?> stored, boolean possiblyModified) {
        if (!type.isAssignableFrom(stored.adapter().type())) {
            return false;
        }
        return isApplicable(state, stored, possiblyModified);
    }

    protected boolean isApplicable(UpdateState state, Stored<?> stored, boolean possiblyModified) {
        return state != UpdateState.DELETE;
    }

    private UpdateState tryUpdateEntry(ISimpleLogger logger, Stored<?> stored, boolean possiblyModified) {
        return updateEntry(logger, stored, stored.valueAs(type), possiblyModified);
    }

    protected abstract UpdateState updateEntry(ISimpleLogger logger, Stored<?> stored, T value, boolean possiblyModified);

    public static CounterProgress apply(ProtoExecutor<?> executor, IStorage storage, AlternationAction<?>... actions) {
        System.out.println("Applying");
        return storage.updateEach(stored -> {
            UpdateState state = UpdateState.NONE;
            boolean possiblyModified = false;
            UpdateState tmp;
            for (AlternationAction<?> action : actions) {
                if (!action.isApplicableEntry(state, stored, possiblyModified)) {
                    continue;
                }
                tmp = Objects.requireNonNull(action.tryUpdateEntry(storage.logger(), stored, possiblyModified),
                    "Update state has to be valid for action '" + action.getClass().getSimpleName() + "'");
                if (state != UpdateState.NONE && tmp == UpdateState.NONE) {
                    possiblyModified = true;
                }
                state = tmp;
            }
            switch (state) {
            case DELETE:
                System.out.println("Delete");
                return UpdateInfo.delete();
            case MODIFY:
                System.out.println("Modify");
                return UpdateInfo.modify();
            case NONE:
            default:
                System.out.println("None");
                // We set 'default' to NONE as null is not an option here
                return UpdateInfo.none();
            }
        }, executor);
    }

}
