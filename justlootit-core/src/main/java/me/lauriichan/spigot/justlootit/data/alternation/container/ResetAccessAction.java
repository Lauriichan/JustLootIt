package me.lauriichan.spigot.justlootit.data.alternation.container;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.data.alternation.AlternationAction;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo.UpdateState;

public final class ResetAccessAction extends AlternationAction<Container> {

    public static final ResetAccessAction RESET = new ResetAccessAction();
    
    private ResetAccessAction() {
        super(Container.class);
    }

    @Override
    protected UpdateState updateEntry(ISimpleLogger logger, Stored<?> stored, Container value, boolean possiblyModified) {
        value.resetAllAccesses();
        return UpdateState.MODIFY;
    }

}
