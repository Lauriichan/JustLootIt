package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.block.data.BlockData;

import me.lauriichan.spigot.justlootit.nms.util.Vec3i;

public abstract class ProtoBlock {

    public abstract BlockData getData();

    public abstract void setData(BlockData blockData);

    public abstract Vec3i getPos();

}
