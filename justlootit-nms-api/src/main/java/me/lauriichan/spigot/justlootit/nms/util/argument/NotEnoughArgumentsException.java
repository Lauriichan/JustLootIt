package me.lauriichan.spigot.justlootit.nms.util.argument;

import java.util.Iterator;
import java.util.Map.Entry;

public class NotEnoughArgumentsException extends RuntimeException {

    private static final long serialVersionUID = 1746623008020755421L;

    public NotEnoughArgumentsException(final ArgumentStack missing) {
        super(buildMessage(missing));
    }

    private static String buildMessage(final ArgumentStack missing) {
        final StringBuilder builder = new StringBuilder("Missing elements (").append(missing.size()).append("): ");
        final Iterator<Entry<String, Class<?>>> iterator = missing.iterator();
        while (iterator.hasNext()) {
            final Entry<String, Class<?>> entry = iterator.next();
            builder.append('"').append(entry.getKey()).append("\"(").append(entry.getValue().getTypeName()).append("), ");
        }
        return builder.substring(0, builder.length() - 2);
    }

}
