package me.lauriichan.maven.justlootit.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(TYPE)
public @interface Event {

    public static final boolean DEFAULT_IS_ABSTRACT = false;
    public static final boolean DEFAULT_CANCELLABLE = false;

    /**
     * Abstract
     */
    boolean isAbstract() default DEFAULT_IS_ABSTRACT;

    /**
     * Cancellable
     */
    boolean cancellable() default DEFAULT_CANCELLABLE;

}
