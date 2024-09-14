package me.lauriichan.spigot.justlootit.util;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.config.IConfigExtension;
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
    
    public static String ofConfig(IConfigExtension extension) {
        if (extension instanceof MessageConfig) {
            return "message";
        }
        String name = ClassUtil.getClassName(extension.getClass()).toLowerCase();
        if (name.endsWith("config")) {
            return name.substring(0, name.length() - 6);
        }
        return name;
    }

}
