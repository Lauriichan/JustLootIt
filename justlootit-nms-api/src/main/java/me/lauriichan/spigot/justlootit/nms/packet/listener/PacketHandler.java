package me.lauriichan.spigot.justlootit.nms.packet.listener;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
public @interface PacketHandler {

    /**
     * This value describes if the packet handler should receive already cancelled
     * packets or not
     * 
     * @return if the handler receives cancelled packets
     */
    boolean value() default false;

}
