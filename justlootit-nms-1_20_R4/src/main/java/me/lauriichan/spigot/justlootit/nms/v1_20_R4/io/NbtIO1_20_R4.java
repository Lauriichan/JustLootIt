package me.lauriichan.spigot.justlootit.nms.v1_20_R4.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.mojang.datafixers.DataFixer;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import me.lauriichan.spigot.justlootit.nms.io.IOHandler;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.util.datafix.DataFixers;

public abstract class NbtIO1_20_R4<E, N extends Tag> extends IOHandler<E> {
    
    public static final String VERSION_ID = "jli:version";
    public static final String DATA_ID = "jli:data";
    
    private static final int MIN_VERSION = 3578;
    private static final int SERVER_VERSION = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    private static final DataFixer FIXER = DataFixers.getDataFixer();

    protected final TagType<N> tagType;
    protected final Result<E[]> emptyArray;
    protected final Result<E[]> emptyArrayDirty;

    @SuppressWarnings("unchecked")
    public NbtIO1_20_R4(final Class<E> type, final TagType<N> tagType) {
        super(type);
        this.tagType = tagType;
        this.emptyArray = new Result<>((E[]) Array.newInstance(type, 0), false);
        this.emptyArrayDirty = new Result<>(emptyArray.value(), true);
    }
    
    @SuppressWarnings("unchecked")
    private Result<N> readTag(DataInputStream stream) throws IOException {
        CompoundTag compound = CompoundTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
        if (!compound.contains(VERSION_ID, 99)) {
            // This is a data tag and not a version tag
            // We should make sure this is the correct version now :/
            return new Result<>(upgradeNbt(FIXER, (N) compound, MIN_VERSION, SERVER_VERSION), true);
        }
        int tagVersion = compound.getInt(VERSION_ID);
        N tag = (N) compound.get(DATA_ID);
        if (tagVersion < SERVER_VERSION) {
            return new Result<>(upgradeNbt(FIXER, tag, tagVersion, SERVER_VERSION), true);
        } else if (tagVersion > SERVER_VERSION) {
            throw new IllegalStateException("Found unsupported data version on nbt data '" + tagVersion + "', did you downgrade your server?");
        }
        return new Result<>(tag, false);
    }
    
    @SuppressWarnings("unchecked")
    private Result<ListTag> readListTag(DataInputStream stream, boolean readListDirectly) throws IOException {
        if (readListDirectly) {
            ListTag listTag;
            try {
                listTag = ListTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
            } catch(ReportedNbtException exp) {
                return new Result<>(null, false);
            }
            if (listTag.isEmpty()) {
                return null;
            }
            TagType<?> elementTagType = TagTypes.getType(listTag.getElementType());
            if (!elementTagType.equals(tagType)) {
                throw new IllegalStateException("Invalid tag type on list, expected '" + tagType.getPrettyName() + "' but found '" + elementTagType.getPrettyName() + "'");
            }
            for (int i = 0; i < listTag.size(); i++) {
                listTag.set(i, upgradeNbt(FIXER, (N) listTag.get(i), MIN_VERSION, SERVER_VERSION));
            }
            return new Result<>(listTag, true);
        }
        CompoundTag compound;
        try {
            compound = CompoundTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
        } catch(ReportedNbtException | EOFException exp) {
            return new Result<>(null, false);
        }
        if (!compound.contains(VERSION_ID, 99)) {
            return new Result<>(null, false);
        }
        int tagVersion = compound.getInt(VERSION_ID);
        ListTag listTag = (ListTag) compound.get(DATA_ID);
        if (listTag.isEmpty()) {
            return null;
        }
        TagType<?> elementTagType = TagTypes.getType(listTag.getElementType());
        if (!elementTagType.equals(tagType)) {
            throw new IllegalStateException("Invalid tag type on list, expected '" + tagType.getPrettyName() + "' but found '" + elementTagType.getPrettyName() + "'");
        }
        if (tagVersion < SERVER_VERSION) {
            for (int i = 0; i < listTag.size(); i++) {
                listTag.set(i, upgradeNbt(FIXER, (N) listTag.get(i), tagVersion, SERVER_VERSION));
            }
            return new Result<>(listTag, true);
        } else if (tagVersion > SERVER_VERSION) {
            throw new IllegalStateException("Found unsupported data version on nbt data '" + tagVersion + "', did you downgrade your server?");
        }
        return new Result<>(listTag, false);
    }
    
    private void writeTag(DataOutputStream stream, Tag tag) throws IOException {
        CompoundTag compound = new CompoundTag();
        compound.putInt(VERSION_ID, SERVER_VERSION);
        compound.put(DATA_ID, tag);
        compound.write(stream);
    }

    @Override
    public final Result<E> deserialize(final ByteBuf buffer) {
        final int size = buffer.readInt();
        if (size == 0) {
            return null;
        }
        final byte[] data = new byte[size];
        buffer.readBytes(data);
        Result<N> result;
        try (DataInputStream stream = new DataInputStream(
            new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
            result = readTag(stream);
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            return null;
        }
        return new Result<>(fromNbt(result.value()), result.dirty());
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Result<E[]> deserializeArray(final ByteBuf buffer) {
        final int size = buffer.readInt();
        if (size == 0) {
            return emptyArray;
        }
        final byte[] data = new byte[size];
        buffer.readBytes(data);
        Result<ListTag> result;
        try {
            try (DataInputStream stream = new DataInputStream(
                new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
                result = readListTag(stream, false);
            }
            if (result.value() == null) {
                try (DataInputStream stream = new DataInputStream(
                    new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
                    result = readListTag(stream, true);
                }
            }
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            return emptyArray;
        }
        if (result == null) {
            return emptyArrayDirty;
        }
        ListTag list = result.value();
        final int listSize = list.size();
        if (listSize == 0) {
            return emptyArrayDirty;
        }
        final E[] array = (E[]) Array.newInstance(type, listSize);
        for (int index = 0; index < listSize; index++) {
            array[index] = fromNbt((N) list.get(index));
        }
        return new Result<>(array, result.dirty());
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
            writeTag(stream, tag);
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
            writeTag(stream, listTag);
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
    
    public abstract N upgradeNbt(DataFixer fixer, N tag, int tagVersion, int serverVersion);

}