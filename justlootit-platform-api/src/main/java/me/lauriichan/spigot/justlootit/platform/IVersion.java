package me.lauriichan.spigot.justlootit.platform;

public interface IVersion {
    
    String packageVersion();
    
    default String coreVersion() {
        return packageVersion().substring(1);
    }
    
    String craftClassPath(String path);

}
