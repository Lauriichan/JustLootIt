package me.lauriichan.spigot.justlootit.platform;

public enum PlatformType {
    
    SPIGOT,
    PAPER,
    FOLIA;

    public boolean isPaper() {
        return this != SPIGOT;
    }

}
