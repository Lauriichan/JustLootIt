package me.lauriichan.spigot.justlootit.data.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

public final class BufIO {

    private BufIO() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void writeString(final ByteBuf buffer, final String string) {
        writeString(buffer, string, StandardCharsets.UTF_8);
    }

    public static void writeString(final ByteBuf buffer, final String string, final Charset charset) {
        if (string == null) {
            buffer.writeInt(-1);
            return;
        }
        buffer.writeInt(string.length());
        buffer.writeCharSequence(string, charset);
    }

    public static String readString(final ByteBuf buffer) {
        return readString(buffer, StandardCharsets.UTF_8);
    }

    public static String readString(final ByteBuf buffer, final Charset charset) {
        final int length = buffer.readInt();
        if (length == -1) {
            return null;
        }
        return buffer.readCharSequence(length, charset).toString();
    }

}
