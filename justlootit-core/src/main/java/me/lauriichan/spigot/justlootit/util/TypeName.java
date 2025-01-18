package me.lauriichan.spigot.justlootit.util;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigWrapper;
import me.lauriichan.minecraft.pluginbase.config.IMultiConfigExtension;
import me.lauriichan.minecraft.pluginbase.message.config.MessageConfig;
import me.lauriichan.spigot.justlootit.data.Container;

public class TypeName {

    public static String ofContainer(Container container) {
        String name = ClassUtil.getClassName(container.getClass()).toLowerCase();
        if (name.endsWith("container")) {
            return name.substring(0, name.length() - 9);
        }
        return name;
    }

    public static String ofConfig(final IConfigWrapper<?> wrapper) {
        Class<?> clazz = wrapper.configType();
        if (wrapper instanceof final IMultiConfigExtension<?, ?, ?> multi) {
            clazz = multi.type();
        }
        if (clazz.isAssignableFrom(MessageConfig.class)) {
            return "message";
        }
        final String name = ClassUtil.getClassName(clazz).toLowerCase();
        if (name.endsWith("config")) {
            return name.substring(0, name.length() - 6);
        }
        return name;
    }

    public static String ofConfig(final IConfigExtension extension) {
        Class<?> clazz = extension.getClass();
        if (extension instanceof final IMultiConfigExtension<?, ?, ?> multi) {
            clazz = multi.type();
        }
        if (clazz.isAssignableFrom(MessageConfig.class)) {
            return "message";
        }
        final String name = ClassUtil.getClassName(clazz).toLowerCase();
        if (name.endsWith("config")) {
            return name.substring(0, name.length() - 6);
        }
        return name;
    }

}
