package me.lauriichan.spigot.justlootit.compatibility.support;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(METHOD)
public @interface SupportContinue {

    /**
     * The class which has the method
     */
    Class<?> type() default SupportHelper.class;

    /**
     * The name of the method to use for the continue check
     */
    String name() default "abortOnChange";

}
