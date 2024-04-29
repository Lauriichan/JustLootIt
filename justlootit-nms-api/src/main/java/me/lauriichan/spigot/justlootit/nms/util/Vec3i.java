package me.lauriichan.spigot.justlootit.nms.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class Vec3i {

    private int x, y, z;

    public Vec3i(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public Vec3i(Vector vector) {
        this.x = vector.getBlockX();
        this.y = vector.getBlockY();
        this.z = vector.getBlockZ();
    }

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vec3i(byte packedByte) {
        this.x = packedByte & 0xf;
        this.y = 0;
        this.z = (packedByte >> 4) & 0xf;
    }
    
    public Vec3i(short packedShort) {
        this.x = packedShort & 0xf;
        this.y = (packedShort >> 4) & 0xf;
        this.z = (packedShort >> 8) & 0xf;
    }

    public Vec3i() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Vec3i addX(int x) {
        this.x += x;
        return this;
    }

    public Vec3i addY(int y) {
        this.y += y;
        return this;
    }

    public Vec3i addZ(int z) {
        this.z += z;
        return this;
    }

    public Vec3i add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3i add(Vec3i vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
        return this;
    }

    public Vec3i subtractX(int x) {
        this.x -= x;
        return this;
    }

    public Vec3i subtractY(int y) {
        this.y -= y;
        return this;
    }

    public Vec3i subtractZ(int z) {
        this.z -= z;
        return this;
    }

    public Vec3i subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3i subtract(Vec3i vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;
        return this;
    }
    
    public Vec3i subtractOf(Vector vector) {
        return subtract(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    
    public Vec3i subtractOf(Location location) {
        return subtract(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    public Vec3i addOf(Vector vector) {
        return add(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    
    public Vec3i addOf(Location location) {
        return add(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    public Vector subtractOn(Vector vector) {
        return vector.subtract(toVector());
    }
    
    public Location subtractOn(Location location) {
        return location.subtract(x, y, z);
    }
    
    public Vector addOn(Vector vector) {
        return vector.add(toVector());
    }
    
    public Location addOn(Location location) {
        return location.add(x, y, z);
    }

    public Vec3i copy() {
        return new Vec3i(x, y, z);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
    
    public Location toLocation() {
        return new Location(null, x, y, z);
    }
    
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }
    
    public byte packByte() {
        return packByte(x, z);
    }

    public short packShort() {
        return packShort(x, y, z);
    }

    public static byte packByte(int x, int z) {
        byte pos = 0;
        pos += (byte) (x & 0xf);
        pos += (byte) (z & 0xf) << 4;
        return pos;
    }

    public static short packShort(int x, int y, int z) {
        short pos = 0;
        pos += (short) (x & 0xf);
        pos += (short) (y & 0xf) << 4;
        pos += (short) (z & 0xf) << 8;
        return pos;
    }

}
