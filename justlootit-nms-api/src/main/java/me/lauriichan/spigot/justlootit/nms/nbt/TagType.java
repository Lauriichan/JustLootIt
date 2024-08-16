package me.lauriichan.spigot.justlootit.nms.nbt;

@SuppressWarnings("rawtypes")
public final class TagType<T> {
    
    public static final int ID_BYTE = 1;
    public static final int ID_SHORT = 2;
    public static final int ID_INT = 3;
    public static final int ID_LONG = 4;
    public static final int ID_FLOAT = 5;
    public static final int ID_DOUBLE = 6;
    public static final int ID_BYTE_ARRAY = 7;
    public static final int ID_STRING = 8;
    public static final int ID_LIST = 9;
    public static final int ID_COMPOUND = 10;
    public static final int ID_INT_ARRAY = 11;
    public static final int ID_LONG_ARRAY = 12;
    
    public static final TagType<Byte> BYTE = new TagType<>(ID_BYTE, "byte", Byte.class, true);
    public static final TagType<Short> SHORT = new TagType<>(ID_SHORT, "short", Short.class, true);
    public static final TagType<Integer> INT = new TagType<>(ID_INT, "integer", Integer.class, true);
    public static final TagType<Long> LONG = new TagType<>(ID_LONG, "long", Long.class, true);
    public static final TagType<Float> FLOAT = new TagType<>(ID_FLOAT, "float", Float.class, true);
    public static final TagType<Double> DOUBLE = new TagType<>(ID_DOUBLE, "double", Double.class, true);
    public static final TagType<byte[]> BYTE_ARRAY = new TagType<>(ID_BYTE_ARRAY, "byte_array", byte[].class);
    public static final TagType<String> STRING = new TagType<>(ID_STRING, "string", String.class);
    public static final TagType<IListTag> LIST = new TagType<>(ID_LIST, "list", IListTag.class);
    public static final TagType<ICompoundTag> COMPOUND = new TagType<>(ID_COMPOUND, "compound", ICompoundTag.class);
    public static final TagType<int[]> INT_ARRAY = new TagType<>(ID_INT_ARRAY, "int_array", int[].class);
    public static final TagType<long[]> LONG_ARRAY = new TagType<>(ID_LONG_ARRAY, "long_array", long[].class);
    
    private static final TagType[] TYPES = new TagType[] { BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY };
    
    public static final TagType<?> getType(int id) {
        return id > TYPES.length || id < 1 ? null : TYPES[id - 1];
    }
    
    public static final TagType<?> getType(String name) {
        for (TagType<?> type : TYPES) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
    
    private final int tagId;
    private final Class<T> tagType;
    
    private final String name;

    private final boolean numeric;
    
    private TagType(final int tagId, final String name, final Class<T> tagType) {
        this(tagId, name, tagType, false);
    }
    
    private TagType(final int tagId, final String name, final Class<T> tagType, final boolean numeric) {
        this.tagId = tagId;
        this.tagType = tagType;
        this.name = name;
        this.numeric = numeric;
    }
    
    public int tagId() {
        return tagId;
    }
    
    public String name() {
        return name;
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
    
    @Override
    public String toString() {
        return new StringBuilder().append("TagType[id=").append(tagId).append(",name=").append(name).append(",numeric=").append(numeric)
            .append("]").toString();
    }

}
