package me.lauriichan.spigot.justlootit.nms.v1_19_R2.io;

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

public abstract class NbtIO1_19_R2<E, N extends Tag> extends IOHandler<E> {
    
    protected final TagType<N> tagType;
    protected final E[] emptyArray;

    @SuppressWarnings("unchecked")
    public NbtIO1_19_R2(Class<E> type, TagType<N> tagType) {
        super(type);
        this.tagType = tagType;
        this.emptyArray = (E[]) Array.newInstance(type, 0);
    }

    @Override
    public final E deserialize(ByteBuf buffer) {
        int size = buffer.readInt();
        if(size == 0) {
            return null;
        }
        byte[] data = new byte[size];
        buffer.readBytes(data);
        N tag;
        try (DataInputStream stream = new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
            tag = tagType.load(stream, 0, NbtAccounter.UNLIMITED);
        } catch (IOException e) {
            // Theoretically shouldn't happen
            return null;
        }
        return fromNbt(tag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final E[] deserializeArray(ByteBuf buffer) {
        int size = buffer.readInt();
        if(size == 0) {
            return emptyArray;
        }
        byte[] data = new byte[size];
        buffer.readBytes(data);
        ListTag list;
        try (DataInputStream stream = new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
            list = ListTag.TYPE.load(stream, 0, NbtAccounter.UNLIMITED);
        } catch (IOException e) {
            // Theoretically shouldn't happen
            return emptyArray;
        }
        int listSize = list.size();
        if(listSize == 0) {
            return emptyArray;
        }
        TagType<?> foundType = TagTypes.getType(list.getElementType());
        if(foundType != tagType) {
            return emptyArray; // Invalid type
        }
        E[] array = (E[]) Array.newInstance(type, listSize);
        for(int index = 0; index < listSize; index++) {
            array[index] = fromNbt((N) list.get(index));
        }
        return array;
    }

    @Override
    public final void serialize(ByteBuf buffer, E value) {
        N tag = asNbt(value);
        if(tag == null) {
            buffer.writeInt(0);
            return;
        }
        FastByteArrayOutputStream output = new FastByteArrayOutputStream();
        try (DataOutputStream stream = new DataOutputStream(new FastBufferedOutputStream(new GZIPOutputStream(output)))) {
            tag.write(stream);
        } catch(IOException e) {
            // Theoretically shouldn't happen
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(output.length);
        buffer.writeBytes(output.array, 0, output.length);
    }

    @Override
    public final void serializeArray(ByteBuf buffer, E[] array) {
        if(array.length == 0) {
            buffer.writeInt(0);
            return;
        }
        ListTag listTag = new ListTag();
        for(int index = 0; index < array.length; index++) {
            listTag.add(asNbt(array[index]));
        }
        FastByteArrayOutputStream output = new FastByteArrayOutputStream();
        try (DataOutputStream stream = new DataOutputStream(new FastBufferedOutputStream(new GZIPOutputStream(output)))) {
            listTag.write(stream);
        } catch(IOException e) {
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
