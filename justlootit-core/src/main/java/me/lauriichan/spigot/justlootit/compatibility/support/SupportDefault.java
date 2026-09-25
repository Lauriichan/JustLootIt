package me.lauriichan.spigot.justlootit.compatibility.support;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(METHOD)
public @interface SupportDefault {

    /**
     * The class which has the field
     */
    Class<?> type() default SupportHelper.class;

    /**
     * The name of the field to use as default value
     */
    String name() default "DEFAULT_TRUE";

}
