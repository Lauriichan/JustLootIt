package me.lauriichan.spigot.justlootit;

import me.lauriichan.spigot.justlootit.compatibility.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.betterinputs.BetterInputsCompatProvider;

public final class JustLootItCompatibilities {

    public static final CompatDependency BETTER_INPUTS;
    
    private JustLootItCompatibilities() {
        throw new UnsupportedOperationException();
    }

    static {
        BETTER_INPUTS = new CompatDependency("BetterInputs", 0, 3, new BetterInputsCompatProvider());
    }

    public static void loadClass() {
        BETTER_INPUTS.getClass();
    }
    
}
