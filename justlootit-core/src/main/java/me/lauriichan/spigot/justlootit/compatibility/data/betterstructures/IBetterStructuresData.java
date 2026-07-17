package me.lauriichan.spigot.justlootit.compatibility.data.betterstructures;

import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;

public interface IBetterStructuresData extends ICompatibilityData {

    String fileName();

    @Override
    default String refreshContainerId() {
        return fileName();
    }

}
