package me.lauriichan.spigot.justlootit.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public abstract class SimpleDataType<P, C> implements PersistentDataType<P, C> {

    public static final SimpleDataType<Byte, Boolean> BOOLEAN = new SimpleDataType<>(Byte.class, Boolean.class) {
        @Override
        public Byte toPrimitive(final Boolean complex, final PersistentDataAdapterContext context) {
            return complex ? (byte) 1 : (byte) 0;
        }

        @Override
        public Boolean fromPrimitive(final Byte primitive, final PersistentDataAdapterContext context) {
            return (primitive == 1) == true;
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
