package me.lauriichan.spigot.justlootit.nms;

public interface IPlayerNetwork {

    static final String DECODER = "jli:in";
    static final String ENCODER = "jli:out";

    boolean isActive();

    void setActive(boolean active);

}
