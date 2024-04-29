package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.util.Objects;

import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;

import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlock;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ProtoBlock1_20_R2 extends ProtoBlock {

    private final BlockPos pos;

    private volatile CraftBlockData data;

    public ProtoBlock1_20_R2(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.data = CraftBlockData.fromData(state);
    }

    @Override
    public BlockData getData() {
        return data;
    }

    @Override
    public void setData(BlockData blockData) {
        this.data = (CraftBlockData) Objects.requireNonNull(blockData);
    }

    @Override
    public Vec3i getPos() {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos pos() {
        return pos;
    }

    public BlockState state() {
        return data.getState();
    }

}
