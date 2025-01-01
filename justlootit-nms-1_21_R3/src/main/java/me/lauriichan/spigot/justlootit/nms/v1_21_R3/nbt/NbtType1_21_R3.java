package me.lauriichan.spigot.justlootit.nms.v1_21_R3.nbt;

import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import net.minecraft.nbt.*;

public final class NbtType1_21_R3<P, C extends Tag> {

    private static final Int2ObjectMap<NbtType1_21_R3<?, ?>> TYPES;

    static {
        Int2ObjectArrayMap<NbtType1_21_R3<?, ?>> map = new Int2ObjectArrayMap<>(12);
        create(map, TagType.BYTE, ByteTag.class, ByteTag::getAsByte, ByteTag::valueOf);
        create(map, TagType.SHORT, ShortTag.class, ShortTag::getAsShort, ShortTag::valueOf);
        create(map, TagType.INT, IntTag.class, IntTag::getAsInt, IntTag::valueOf);
        create(map, TagType.LONG, LongTag.class, LongTag::getAsLong, LongTag::valueOf);
        create(map, TagType.FLOAT, FloatTag.class, FloatTag::getAsFloat, FloatTag::valueOf);
        create(map, TagType.DOUBLE, DoubleTag.class, DoubleTag::getAsDouble, DoubleTag::valueOf);
        create(map, TagType.BYTE_ARRAY, ByteArrayTag.class, ByteArrayTag::getAsByteArray, ByteArrayTag::new);
        create(map, TagType.STRING, StringTag.class, StringTag::getAsString, StringTag::valueOf);
        create(map, TagType.LIST, ListTag.class, tag -> new ListTag1_21_R3<>(tag, TagType.getType(tag.getId())), tag -> ((ListTag1_21_R3<?>) tag).handle());
        create(map, TagType.COMPOUND, CompoundTag.class, CompoundTag1_21_R3::new, tag -> ((CompoundTag1_21_R3) tag).handle());
        create(map, TagType.INT_ARRAY, IntArrayTag.class, IntArrayTag::getAsIntArray, IntArrayTag::new);
        create(map, TagType.LONG_ARRAY, LongArrayTag.class, LongArrayTag::getAsLongArray, LongArrayTag::new);
        TYPES = Int2ObjectMaps.unmodifiable(map);
    }

    private static <P, C extends Tag> void create(Int2ObjectMap<NbtType1_21_R3<?, ?>> map, TagType<P> type, Class<C> complexType,
        Function<C, P> complexToPrimitive, Function<P, C> primitiveToComplex) {
        map.put(type.tagId(), new NbtType1_21_R3<>(type, complexType, complexToPrimitive, primitiveToComplex));
    }

    @SuppressWarnings("unchecked")
    public static <T> NbtType1_21_R3<T, ?> get(TagType<T> type) {
        return (NbtType1_21_R3<T, ?>) TYPES.get(type.tagId());
    }

    private final TagType<P> type;

    private final Class<C> complexType;

    private final Function<C, P> complexToPrimitive;
    private final Function<P, C> primitiveToComplex;

    private NbtType1_21_R3(TagType<P> type, Class<C> complexType, Function<C, P> complexToPrimitive, Function<P, C> primitiveToComplex) {
        this.type = type;
        this.complexType = complexType;
        this.complexToPrimitive = complexToPrimitive;
        this.primitiveToComplex = primitiveToComplex;
    }

    public TagType<P> type() {
        return type;
    }

    public Class<C> complexType() {
        return complexType;
    }

    public P tryToPrimitive(Tag complex) {
        if (!complexType.isInstance(complex)) {
            throw new IllegalArgumentException("Invalid tag id: " + complex.getId());
        }
        return toPrimitive(complexType.cast(complex));
    }

    public P toPrimitive(C complex) {
        return complexToPrimitive.apply(complex);
    }

    public C fromPrimitive(P primitive) {
        return primitiveToComplex.apply(primitive);
    }

}
