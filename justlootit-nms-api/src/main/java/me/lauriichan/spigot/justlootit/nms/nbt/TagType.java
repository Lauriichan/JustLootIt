package me.lauriichan.spigot.justlootit.nms.nbt;

@SuppressWarnings("rawtypes")
public final class TagType<T> {
    
    public static final TagType<Byte> BYTE = new TagType<>(1, Byte.class);
    public static final TagType<Short> SHORT = new TagType<>(2, Short.class);
    public static final TagType<Integer> INT = new TagType<>(3, Integer.class);
    public static final TagType<Long> LONG = new TagType<>(4, Long.class);
    public static final TagType<Float> FLOAT = new TagType<>(5, Float.class);
    public static final TagType<Double> DOUBLE = new TagType<>(6, Double.class);
    public static final TagType<byte[]> BYTE_ARRAY = new TagType<>(7, byte[].class);
    public static final TagType<String> STRING = new TagType<>(8, String.class);
    public static final TagType<IListTag> LIST = new TagType<>(9, IListTag.class);
    public static final TagType<ICompoundTag> COMPOUND = new TagType<>(10, ICompoundTag.class);
    public static final TagType<int[]> INT_ARRAY = new TagType<>(11, int[].class);
    public static final TagType<long[]> LONG_ARRAY = new TagType<>(12, long[].class);
    
    private static final TagType[] TYPES = new TagType[] { BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY };
    
    public static final TagType<?> getType(int id) {
        return id > TYPES.length || id < 1 ? null : TYPES[id - 1];
    }
    
    private final int tagId;
    private final Class<T> tagType;

    private final boolean numeric;
    
    private TagType(final int tagId, final Class<T> tagType) {
        this(tagId, tagType, false);
    }
    
    private TagType(final int tagId, final Class<T> tagType, final boolean numeric) {
        this.tagId = tagId;
        this.tagType = tagType;
        this.numeric = numeric;
    }
    
    public int tagId() {
        return tagId;
    }
    
    public boolean numeric() {
        return numeric;
    }
    
    public Class<T> tagType() {
        return tagType;
    }
    
    public boolean isType(Class<?> type) {
        return type != null && type.isAssignableFrom(tagType);
    }
    
    public boolean isType(TagType<?> type) {
        return this == type;
    }

}
