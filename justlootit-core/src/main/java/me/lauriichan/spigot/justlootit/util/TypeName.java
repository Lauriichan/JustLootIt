package me.lauriichan.spigot.justlootit.util;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
import me.lauriichan.minecraft.pluginbase.config.IConfigWrapper;
import me.lauriichan.minecraft.pluginbase.config.MultiConfigWrapper;
import me.lauriichan.minecraft.pluginbase.data.IDataExtension;
import me.lauriichan.minecraft.pluginbase.data.IDataWrapper;
import me.lauriichan.minecraft.pluginbase.data.MultiDataWrapper;
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

    public static String ofData(final IDataWrapper<?, ?> wrapper) {
        Class<?> clazz = wrapper.dataType();
        if (wrapper instanceof final MultiDataWrapper<?, ?, ?, ?, ?> multi) {
            clazz = multi.extension().type();
        }
        String name = ClassUtil.getClassName(clazz).toLowerCase();
        if (name.endsWith("data")) {
            name = name.substring(0, name.length() - 4);
        }
        if (name.endsWith("directory")) {
            name = name.substring(0, name.length() - 9);
        }
        return name;
    }

    public static String ofData(final IDataExtension<?> extension) {
        Class<?> clazz = extension.getClass();
        if (clazz.isAssignableFrom(MessageConfig.class)) {
            return "message";
        }
        final String name = ClassUtil.getClassName(clazz).toLowerCase();
        if (name.endsWith("config")) {
            return name.substring(0, name.length() - 6);
        }
        return name;
    }

    public static String ofConfig(final IConfigWrapper<?> wrapper) {
        Class<?> clazz = wrapper.configType();
        if (wrapper instanceof final MultiConfigWrapper<?, ?, ?, ?> multi) {
            clazz = multi.extension().type();
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
