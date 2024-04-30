package me.lauriichan.spigot.justlootit.platform.util;

public class SimpleVersion implements Comparable<SimpleVersion> {

    public static SimpleVersion of(String version) {
        if (version.isBlank()) {
            return new SimpleVersion(0, 0, 0);
        }
        String[] parts = version.split("\\.");
        try {
            if (parts.length == 1) {
                return new SimpleVersion(Integer.parseInt(parts[0]), 0, 0);
            }
            if (parts.length == 2) {
                return new SimpleVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 0);
            }
            return new SimpleVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch(NumberFormatException nfe) {
            return null;
        }
    }

    public static SimpleVersion of(int major, int minor, int patch) {
        return new SimpleVersion(major, minor, patch);
    }

    protected final int major, minor, patch;

    protected SimpleVersion(int major, int minor, int patch) {
        this.major = Math.max(major, 0);
        this.minor = Math.max(minor, 0);
        this.patch = Math.max(patch, 0);
    }
    
    public final int major() {
        return major;
    }
    
    public final int minor() {
        return minor;
    }
    
    public final int patch() {
        return patch;
    }
    
    public boolean isSame(SimpleVersion other) {
        return compareTo(other) == 0;
    }
    
    public boolean isBetween(SimpleVersion min, SimpleVersion max) {
        return !(compareTo(min) < 0 || compareTo(max) > 0);
    }

    @Override
    public int compareTo(SimpleVersion o) {
        int diff = Integer.compare(major, o.major);
        if (diff != 0) {
            return diff;
        }
        diff = Integer.compare(minor, o.minor);
        if (diff != 0) {
            return diff;
        }
        return Integer.compare(patch, o.patch);
    }
    
    

}
