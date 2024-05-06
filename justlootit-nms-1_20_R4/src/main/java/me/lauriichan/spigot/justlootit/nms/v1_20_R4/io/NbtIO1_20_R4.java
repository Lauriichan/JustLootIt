package me.lauriichan.spigot.justlootit.nms.v1_20_R4.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import net.minecraft.util.datafix.DataFixers;

public abstract class NbtIO1_20_R4<E, N extends Tag> extends IOHandler<E> {
    
    public static final String VERSION_ID = "jli:version";
    public static final String DATA_ID = "jli:data";
    
    private static final int MIN_VERSION = 3578;
    private static final int SERVER_VERSION = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    private static final DataFixer FIXER = DataFixers.getDataFixer();

    protected final TagType<N> tagType;
    protected final E[] emptyArray;

    @SuppressWarnings("unchecked")
    public NbtIO1_20_R4(final Class<E> type, final TagType<N> tagType) {
        super(type);
        this.tagType = tagType;
        this.emptyArray = (E[]) Array.newInstance(type, 0);
    }
    
    @SuppressWarnings("unchecked")
    private N readTag(DataInputStream stream) throws IOException {
        CompoundTag compound = CompoundTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
        if (!compound.contains(VERSION_ID, 99)) {
            // This is a data tag and not a version tag
            // We should make sure this is the correct version now :/
            return upgradeNbt(FIXER, (N) compound, MIN_VERSION, SERVER_VERSION);
        }
        int tagVersion = compound.getInt(VERSION_ID);
        N tag = (N) compound.get(DATA_ID);
        if (tagVersion < SERVER_VERSION) {
            tag = upgradeNbt(FIXER, tag, tagVersion, SERVER_VERSION);
        } else if (tagVersion > SERVER_VERSION) {
            throw new IllegalStateException("Found unsupported data version on nbt data '" + tagVersion + "', did you downgrade your server?");
        }
        return tag;
    }
    
    @SuppressWarnings("unchecked")
    private ListTag readListTag(DataInputStream stream, boolean readListDirectly) throws IOException, ReportedNbtException {
        if (readListDirectly) {
            ListTag listTag = ListTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
            if (listTag.isEmpty()) {
                return listTag;
            }
            if (!listTag.getType().equals(tagType)) {
                throw new IllegalStateException("Invalid tag type on list, expected '" + tagType.getPrettyName() + "' but found '" + listTag.getType().getPrettyName() + "'");
            }
            for (int i = 0; i < listTag.size(); i++) {
                listTag.set(i, upgradeNbt(FIXER, (N) listTag.get(i), MIN_VERSION, SERVER_VERSION));
            }
            return listTag;
        }
        CompoundTag compound = CompoundTag.TYPE.load(stream, NbtAccounter.unlimitedHeap());
        int tagVersion = compound.getInt(VERSION_ID);
        ListTag listTag = (ListTag) compound.get(DATA_ID);
        if (!listTag.getType().equals(tagType)) {
            throw new IllegalStateException("Invalid tag type on list, expected '" + tagType.getPrettyName() + "' but found '" + listTag.getType().getPrettyName() + "'");
        }
        if (tagVersion < SERVER_VERSION) {
            for (int i = 0; i < listTag.size(); i++) {
                listTag.set(i, upgradeNbt(FIXER, (N) listTag.get(i), tagVersion, SERVER_VERSION));
            }
        } else if (tagVersion > SERVER_VERSION) {
            throw new IllegalStateException("Found unsupported data version on nbt data '" + tagVersion + "', did you downgrade your server?");
        }
        return listTag;
    }
    
    private void writeTag(DataOutputStream stream, Tag tag) throws IOException {
        CompoundTag compound = new CompoundTag();
        compound.putInt(VERSION_ID, SERVER_VERSION);
        compound.put(DATA_ID, tag);
        compound.write(stream);
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
            tag = readTag(stream);
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
        try {
            try (DataInputStream stream = new DataInputStream(
                new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
                list = readListTag(stream, false);
            } catch(ReportedNbtException exp) {
                try (DataInputStream stream = new DataInputStream(
                new FastBufferedInputStream(new GZIPInputStream(new FastByteArrayInputStream(data))))) {
                    list = readListTag(stream, true);
                }
            }
        } catch (final IOException e) {
            // Theoretically shouldn't happen
            return emptyArray;
        }
        final int listSize = list.size();
        if (listSize == 0) {
            return emptyArray;
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