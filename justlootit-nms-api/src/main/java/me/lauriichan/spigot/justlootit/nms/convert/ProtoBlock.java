package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.block.data.BlockData;
import org.joml.Vector3i;

public abstract class ProtoBlock {

    public abstract BlockData getData();

    public abstract void setData(BlockData blockData);

    public abstract Vector3i getPos();

}
