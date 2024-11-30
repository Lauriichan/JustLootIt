package me.lauriichan.spigot.justlootit.data.alternation.container.vanilla;

import java.util.Objects;

import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.data.VanillaContainer;
import me.lauriichan.spigot.justlootit.data.alternation.AlternationAction;
import me.lauriichan.spigot.justlootit.storage.Stored;
import me.lauriichan.spigot.justlootit.storage.UpdateInfo.UpdateState;

public final class UpdateLootTableAction extends AlternationAction<VanillaContainer> {

    private final NamespacedKey find;
    private final NamespacedKey replace;
    
    public UpdateLootTableAction(NamespacedKey find, LootTable replace) {
        super(VanillaContainer.class);
        this.find = Objects.requireNonNull(find);
        this.replace = Objects.requireNonNull(replace).getKey();
    }

    @Override
    protected UpdateState updateEntry(ISimpleLogger logger, Stored<?> stored, VanillaContainer value, boolean possiblyModified) {
        if (!value.getLootTableKey().equals(find)) {
            return UpdateState.NONE;
        }
        value.setLootTableKey(replace);
        return UpdateState.MODIFY;
    }

}
