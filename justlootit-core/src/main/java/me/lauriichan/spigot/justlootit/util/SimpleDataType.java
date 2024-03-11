package me.lauriichan.spigot.justlootit.util;

import java.util.UUID;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public abstract class SimpleDataType<P, C> implements PersistentDataType<P, C> {

    public static final SimpleDataType<Byte, Boolean> BOOLEAN = new SimpleDataType<>(Byte.class, Boolean.class) {
        @Override
        public Byte toPrimitive(final Boolean complex, final PersistentDataAdapterContext context) {
            return complex ? (byte) 1 : (byte) 0;
        }

        @Override
        public Boolean fromPrimitive(final Byte primitive, final PersistentDataAdapterContext context) {
            return primitive == 1;
        }
    };
    public static final SimpleDataType<int[], Vector> OFFSET_VECTOR = new SimpleDataType<>(int[].class, Vector.class) {
        @Override
        public int[] toPrimitive(Vector complex, PersistentDataAdapterContext context) {
            return new int[] {
                complex.getBlockX(),
                complex.getBlockZ()
            };
        }

        @Override
        public Vector fromPrimitive(int[] primitive, PersistentDataAdapterContext context) {
            return new Vector(primitive[0], 0, primitive[1]);
        }
    };
    public static final SimpleDataType<long[], UUID> UUID = new SimpleDataType<>(long[].class, UUID.class) {
        @Override
        public long[] toPrimitive(UUID complex, PersistentDataAdapterContext context) {
            return new long[] {
                complex.getMostSignificantBits(),
                complex.getLeastSignificantBits()
            };
        }

        @Override
        public UUID fromPrimitive(long[] primitive, PersistentDataAdapterContext context) {
            return new UUID(primitive[0], primitive[1]);
        }
    };

    private final Class<P> primitiveType;
    private final Class<C> complexType;

    public SimpleDataType(final Class<P> primitiveType, final Class<C> complexType) {
        this.primitiveType = primitiveType;
        this.complexType = complexType;
    }

    @Override
    public final Class<P> getPrimitiveType() {
        return primitiveType;
    }

    @Override
    public final Class<C> getComplexType() {
        return complexType;
    }

}
