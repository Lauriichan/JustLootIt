package me.lauriichan.spigot.justlootit.compatibility.betterinputs;

import org.bukkit.plugin.Plugin;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.CompatProvider;
import me.lauriichan.spigot.justlootit.input.InputProvider;
import me.lauriichan.spigot.justlootit.input.SimpleChatInputProvider;

public class BetterInputsCompatProvider extends CompatProvider {
    
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
