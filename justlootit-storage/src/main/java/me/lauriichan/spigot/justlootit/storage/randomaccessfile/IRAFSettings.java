package me.lauriichan.spigot.justlootit.storage.randomaccessfile;

public interface IRAFSettings {
    
    int valueIdBits();
    
    int valueIdMask();
    
    int valueIdAmount();
    
    long fileCacheTicks();
    
    int fileCacheMaxAmount();
    
    long fileCachePurgeStep();

}
