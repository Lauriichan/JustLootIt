package me.lauriichan.maven.justlootit.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(FIELD)
public @interface EventField {
    
    public static final boolean DEFAULT_CONSTRUCTOR = true;
    public static final boolean DEFAULT_GETTER = true;
    public static final boolean DEFAULT_SETTER = false;
    public static final boolean DEFAULT_ALLOW_NULL = false;
    
    boolean constructor() default DEFAULT_CONSTRUCTOR;
    
    boolean getter() default DEFAULT_GETTER;
    
    boolean setter() default DEFAULT_SETTER;
    
    boolean allowNull() default DEFAULT_ALLOW_NULL;

}
