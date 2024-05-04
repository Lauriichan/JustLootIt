package me.lauriichan.spigot.justlootit.compatibility.provider.betterinputs;

import org.bukkit.plugin.Plugin;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;
import me.lauriichan.spigot.justlootit.compatibility.provider.ICompatProvider;
import me.lauriichan.spigot.justlootit.input.InputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;

@Compatibility(name = "BetterInputs", minMajor = 0, minMinor = 3)
public final class BetterInputsCompatProvider implements ICompatProvider {
    
    private InputProvider provider;

    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        jli.inputProvider(provider = new BetterInputsInputProvider());
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        if (jli.inputProvider() == provider) {
            jli.inputProvider(SimpleChatInputProvider.CHAT);
        }
        clear();
    }
    
    private void clear() {
        provider = null;
    }

}
