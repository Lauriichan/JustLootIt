package me.lauriichan.spigot.justlootit.nms.v1_20_R3.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;

public abstract class NbtIO1_20_R3<E, N extends Tag> extends IOHandler<E> {

    protected final TagType<N> tagType;
    protected final E[] emptyArray;

    @SuppressWarnings("unchecked")
    public NbtIO1_20_R3(final Class<E> type, final TagType<N> tagType) {
        super(type);
        this.tagType = tagType;
        this.emptyArray = (E[]) Array.newInstance(type, 0);
    }

    @Override
    public final E deserialize(final ByteBuf buffer) {
        final int size = buffer.readInt();
        if (size == 0) {
            return null;
        }
        final byte[] data = new byte[size];
        buffer.readBytes(data);
        N tag;
        try (DataInputStream stream = new DataInputStream(
            new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
            tag = tagType.load(stream, NbtAccounter.unlimitedHeap());
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            return null;
        }
        return fromNbt(tag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final E[] deserializeArray(final ByteBuf buffer) {
        final int size = buffer.readInt();
        if (size == 0) {
            return emptyArray;
        }
        final byte[] data = new byte[size];
        buffer.readBytes(data);
        ListTag list;
        try (DataInputStream stream = new DataInputStream(
            new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
            list = ListTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            return emptyArray;
        }
        final int listSize = list.size();
        if (listSize == 0) {
            return emptyArray;
        }
        final TagType<?> foundType = TagTypes.getType(list.getElementType());
        if (foundType != tagType) {
            return emptyArray; // Invalid type
        }
        final E[] array = (E[]) Array.newInstance(type, listSize);
        for (int index = 0; index < listSize; index++) {
            array[index] = fromNbt((N) list.get(index));
        }
        return array;
    }

    @Override
    public final void serialize(final ByteBuf buffer, final E value) {
        final N tag = asNbt(value);
        if (tag == null) {
            buffer.writeInt(0);
            return;
        }
        final FastByteArrayOutputStream output = new FastByteArrayOutputStream();
        try (DataOutputStream stream = new DataOutputStream(new FastBufferedOutputStream(new GZIPOutputStream(output)))) {
            tag.write(stream);
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(output.length);
        buffer.writeBytes(output.array, 0, output.length);
    }

    @Override
    public final void serializeArray(final ByteBuf buffer, final E[] array) {
        if (array.length == 0) {
            buffer.writeInt(0);
            return;
        }
        final ListTag listTag = new ListTag();
        for (int index = 0; index < array.length; index++) {
            listTag.add(asNbt(array[index]));
        }
        final FastByteArrayOutputStream output = new FastByteArrayOutputStream();
        try (DataOutputStream stream = new DataOutputStream(new FastBufferedOutputStream(new GZIPOutputStream(output)))) {
            listTag.write(stream);
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(output.length);
        buffer.writeBytes(output.array, 0, output.length);
    }

    public abstract N asNbt(E value);

    public abstract E fromNbt(N tag);

}