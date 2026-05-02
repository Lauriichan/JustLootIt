package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {
        throw new UnsupportedOperationException();
    }

    public static void playErrorSound(Player player) {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.MASTER, 1, 0);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, SoundCategory.MASTER, 0.6f, 0);
    }

}
