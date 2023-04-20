package me.lauriichan.spigot.justlootit.inventory.item;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

public final class HeadProfileProvider {

    public static final UUID HEAD_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String URL_BASE = "http://textures.minecraft.net/texture/%s";

    static final HeadProfileProvider PROVIDER = new HeadProfileProvider();
    
    public static void dispose() {
        PROVIDER.profiles.clear();
    }
    
    private final ConcurrentHashMap<String, PlayerProfile> profiles = new ConcurrentHashMap<>();
    
    private HeadProfileProvider() {}

    private final PlayerProfile buildProfile(final String texture) {
        final PlayerProfile profile = Bukkit.createPlayerProfile(HEAD_ID, "Head");
        profile.getTextures().setSkin(buildUrl(texture));
        return profile;
    }

    private final URL buildUrl(final String texture) {
        try {
            return new URL(String.format(URL_BASE, texture));
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    public final void setTexture(final SkullMeta meta, final String texture) {
        meta.setOwnerProfile(profiles.computeIfAbsent(texture, this::buildProfile));
    }

    public final String getTexture(final SkullMeta meta) {
        final PlayerProfile profile = meta.getOwnerProfile();
        if (profile == null) {
            return null;
        }
        final URL url = profile.getTextures().getSkin();
        if (url == null) {
            return null;
        }
        final String[] parts = url.toString().split("/");
        return parts[parts.length - 1];
    }

}