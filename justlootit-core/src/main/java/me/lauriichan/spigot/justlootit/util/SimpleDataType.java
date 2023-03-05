package me.lauriichan.spigot.justlootit.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public abstract class SimpleDataType<P, C> implements PersistentDataType<P, C> {

    public static final SimpleDataType<Byte, Boolean> BOOLEAN = new SimpleDataType<>(Byte.class, Boolean.class) {
        @Override
        public Byte toPrimitive(Boolean complex, PersistentDataAdapterContext context) {
            return complex ? (byte) 1 : (byte) 0;
        }

        @Override
        public Boolean fromPrimitive(Byte primitive, PersistentDataAdapterContext context) {
            return primitive == 1 ? true : false;
        }
    };

    private final Class<P> primitiveType;
    private final Class<C> complexType;

    public SimpleDataType(Class<P> primitiveType, Class<C> complexType) {
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
