package me.lauriichan.spigot.justlootit.data.io;

import org.bukkit.NamespacedKey;

import io.netty.buffer.ByteBuf;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import me.lauriichan.spigot.justlootit.util.BufIO;

public final class NamespacedKeyIO extends IOHandler<NamespacedKey> {

    public static final NamespacedKeyIO NAMESPACED_KEY = new NamespacedKeyIO();
    
    private NamespacedKeyIO() {
        super(NamespacedKey.class);
    }

    @Override
    public void serialize(ByteBuf buffer, NamespacedKey value) {
        BufIO.writeString(buffer, value.getNamespace());
        BufIO.writeString(buffer, value.getKey());
    }

    @Override
    public NamespacedKey deserialize(ByteBuf buffer) {
        String namespace = BufIO.readString(buffer);
        String key = BufIO.readString(buffer);
        // Use fromString to prevent deprecation
        return NamespacedKey.fromString(namespace + ':' + key);
    }

}
