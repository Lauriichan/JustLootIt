package me.lauriichan.spigot.justlootit.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.io.JsonParser;
import me.lauriichan.laylib.json.io.JsonSyntaxException;
import me.lauriichan.minecraft.pluginbase.command.argument.UUIDArgument;

public class MojangServer {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";

    private MojangServer() {
        throw new UnsupportedOperationException();
    }

    public static CompletableFuture<UUID> getUniqueId(String playerName) {
        final URL url;
        try {
            url = new URL(UUID_URL.formatted(playerName));
        } catch (MalformedURLException exp) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            IJson<?> json = null;
            try {
                json = JsonParser.fromStream(url.openStream());
            } catch (IOException | JsonSyntaxException | IllegalStateException exp) {
                return null;
            }
            if (json == null || !json.isObject()) {
                return null;
            }
            return readUUID(json.asJsonObject(), "id");
        });
    }

    public static CompletableFuture<String> getName(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        final URL url;
        try {
            url = new URL(UUID_URL.formatted(playerId.toString().replace("-", "")));
        } catch (MalformedURLException exp) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            IJson<?> json = null;
            try {
                json = JsonParser.fromStream(url.openStream());
            } catch (IOException | JsonSyntaxException | IllegalStateException exp) {
                return null;
            }
            if (json == null || !json.isObject()) {
                return null;
            }
            return readString(json.asJsonObject(), "name");
        });
    }

    private static String readString(JsonObject object, String name) {
        IJson<?> element = object.get(name);
        if (element == null || !element.isString()) {
            return null;
        }
        return element.asJsonString().value();
    }

    private static UUID readUUID(JsonObject object, String name) {
        String value = readString(object, name);
        if (value == null) {
            return null;
        }
        return UUIDArgument.uuidFromString(value);
    }

}
