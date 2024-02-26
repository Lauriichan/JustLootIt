package me.lauriichan.spigot.justlootit;

import me.lauriichan.spigot.justlootit.compatibility.betterinputs.BetterInputsInputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;
import me.lauriichan.spigot.justlootit.util.CompatDependency;

public final class JustLootItCompatibilities {

    public static final CompatDependency BETTER_INPUTS;

    static {
        BETTER_INPUTS = new CompatDependency("BetterInputs", 0, 3, (jli, plugin) -> jli.inputProvider(new BetterInputsInputProvider()),
            (jli, plugin) -> jli.inputProvider(SimpleChatInputProvider.CHAT));
    }

}
