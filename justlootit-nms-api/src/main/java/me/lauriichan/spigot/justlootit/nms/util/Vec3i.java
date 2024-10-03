package me.lauriichan.spigot.justlootit.nms.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class Vec3i {

    public static Vec3i unpackUnsignedByte(final byte value) {
        return new Vec3i(value & 0xF, 0, value >> 4 & 0xF);
    }

    public static Vec3i unpackByte(final byte value) {
        int x = (value & 0x7) * ((value >> 6 & 0x1) == 1 ? -1 : 1);
        int z = (value >> 3 & 0x7) * ((value >> 7 & 0x1) == 1 ? -1 : 1);
        return new Vec3i(x, 0, z);
    }

    public static Vec3i unpackShort(final short value) {
        int x = (value & 0xF) * ((value >> 8 & 0x1) == 1 ? -1 : 1);
        int y = (value >> 4 & 0xF) * ((value >> 9 & 0x1) == 1 ? -1 : 1);
        int z = (value >> 8 & 0xF) * ((value >> 10 & 0x1) == 1 ? -1 : 1);
        return new Vec3i(x, y, z);
    }

    private int x, y, z;

    public Vec3i(final Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public Vec3i(final Vector vector) {
        this.x = vector.getBlockX();
        this.y = vector.getBlockY();
        this.z = vector.getBlockZ();
    }

    public Vec3i(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    
    public Vec3i multiply(final int multiplier) {
        this.x *= multiplier;
        this.y *= multiplier;
        this.z *= multiplier;
        return this;
    }

    public Vec3i addX(final int x) {
        this.x += x;
        return this;
    }

    public Vec3i addY(final int y) {
        this.y += y;
        return this;
    }

    public Vec3i addZ(final int z) {
        this.z += z;
        return this;
    }

    public Vec3i add(final int x, final int y, final int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3i add(final Vec3i vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
        return this;
    }

    public Vec3i subtractX(final int x) {
        this.x -= x;
        return this;
    }

    public Vec3i subtractY(final int y) {
        this.y -= y;
        return this;
    }

    public Vec3i subtractZ(final int z) {
        this.z -= z;
        return this;
    }

    public Vec3i subtract(final int x, final int y, final int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3i subtract(final Vec3i vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;
        return this;
    }

    public Vec3i subtractOf(final Vector vector) {
        return subtract(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public Vec3i subtractOf(final Location location) {
        return subtract(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Vec3i addOf(final Vector vector) {
        return add(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public Vec3i addOf(final Location location) {
        return add(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Vector subtractOn(final Vector vector) {
        return vector.subtract(toVector());
    }

    public Location subtractOn(final Location location) {
        return location.subtract(x, y, z);
    }

    public Vector addOn(final Vector vector) {
        return vector.add(toVector());
    }

    public Location addOn(final Location location) {
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

    public Location toLocation(final World world) {
        return new Location(world, x, y, z);
    }

    public byte packUnsignedByte() {
        return packUnsignedByte(x, z);
    }

    public byte packByte() {
        return packByte(x, z);
    }

    public short packShort() {
        return packShort(x, y, z);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof final Location location) {
            return location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z;
        }
        if (obj instanceof final Vector vector) {
            return vector.getBlockX() == x && vector.getBlockY() == y && vector.getBlockZ() == z;
        }
        if (obj instanceof final Vec3i vec) {
            return vec.x == x && vec.y == y && vec.z == z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return 31 * result + z;
    }

    @Override
    public String toString() {
        return new StringBuilder("Vec[").append(x).append(", ").append(y).append(", ").append(z).append(']').toString();
    }

    public static byte packUnsignedByte(final int x, final int z) {
        byte pos = 0;
        pos += (byte) (Math.abs(x) & 0xF);
        pos += (byte) (Math.abs(z) & 0xF) << 4;
        return pos;
    }

    public static byte packByte(final int x, final int z) {
        byte pos = 0;
        pos += (byte) (x < 0 ? 0x40 : 0);
        pos += (byte) (Math.abs(x) & 0x7);
        pos += (byte) (z < 0 ? 0x80 : 0);
        pos += (byte) (Math.abs(z) & 0x7) << 3;
        return pos;
    }

    public static short packShort(final int x, final int y, final int z) {
        short pos = 0;
        pos += (short) (x < 0 ? 0x100 : 0);
        pos += (short) (Math.abs(x) & 0xF);
        pos += (short) (y < 0 ? 0x200 : 0);
        pos += (short) (Math.abs(y) & 0xF) << 4;
        pos += (short) (z < 0 ? 0x400 : 0);
        pos += (short) (Math.abs(z) & 0xF) << 8;
        return pos;
    }

}
