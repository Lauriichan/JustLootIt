package me.lauriichan.spigot.justlootit.util;

import java.util.UUID;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.spigot.justlootit.nms.util.Vec3i;

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
    public static final SimpleDataType<Byte, Vec3i> LEGACY_OFFSET_VECTOR = new SimpleDataType<>(Byte.class, Vec3i.class) {
        @Override
        public Byte toPrimitive(Vec3i complex, PersistentDataAdapterContext context) {
            throw new UnsupportedOperationException("Write is no longer supported");
        }

        @Override
        public Vec3i fromPrimitive(Byte primitive, PersistentDataAdapterContext context) {
            if (primitive == -16) {
                return new Vec3i(0, 0, -1);
            }
            if (primitive == 16) {
                return new Vec3i(0, 0, 1);
            }
            if (primitive == 1) {
                return new Vec3i(1, 0, 0);
            }
            if (primitive == 15) {
                return new Vec3i(-1, 0, 0);
            }
            throw new UnsupportedOperationException("Unsupported value: " + primitive);
        }
    };
    public static final SimpleDataType<Byte, Vec3i> OFFSET_VECTOR = new SimpleDataType<>(Byte.class, Vec3i.class) {
        @Override
        public Byte toPrimitive(Vec3i complex, PersistentDataAdapterContext context) {
            return complex.packByte();
        }

        @Override
        public Vec3i fromPrimitive(Byte primitive, PersistentDataAdapterContext context) {
            return Vec3i.unpackByte(primitive);
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
