package me.lauriichan.spigot.justlootit.compatibility.provider;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Compatibility {
    
    String name();
    
    int minMajor();
    
    int maxMajor() default -1;
    
    int minMinor();
    
    int maxMinor() default -1;

}
