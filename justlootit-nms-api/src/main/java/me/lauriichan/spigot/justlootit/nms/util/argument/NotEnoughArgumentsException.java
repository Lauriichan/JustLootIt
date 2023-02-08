package me.lauriichan.spigot.justlootit.nms.util.argument;

import java.util.Iterator;
import java.util.Map.Entry;

public class NotEnoughArgumentsException extends RuntimeException {

    private static final long serialVersionUID = 1746623008020755421L;

    public NotEnoughArgumentsException(ArgumentStack missing) {
        super(buildMessage(missing));
    }

    private static String buildMessage(ArgumentStack missing) {
        StringBuilder builder = new StringBuilder("Missing elements (").append(missing.size()).append("): ");
        Iterator<Entry<String, Class<?>>> iterator = missing.iterator();
        while (iterator.hasNext()) {
            Entry<String, Class<?>> entry = iterator.next();
            builder.append('"').append(entry.getKey()).append("\"(").append(entry.getValue().getTypeName()).append("), ");
        }
        return builder.substring(0, builder.length() - 2);
    }

}
