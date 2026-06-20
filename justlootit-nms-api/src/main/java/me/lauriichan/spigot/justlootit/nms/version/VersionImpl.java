package me.lauriichan.spigot.justlootit.nms.version;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import me.lauriichan.spigot.justlootit.platform.PlatformType;

@Retention(SOURCE)
@Target(TYPE)
public @interface VersionImpl {

    /**
     * The name of this implementation
     */
    String name();

    /**
     * The versions supported by this implementation
     */
    String[] versions();

    /**
     * The platforms supported by this implementation
     */
    PlatformType[] platforms() default {
        PlatformType.SPIGOT,
        PlatformType.PAPER,
        PlatformType.FOLIA
    };

}
