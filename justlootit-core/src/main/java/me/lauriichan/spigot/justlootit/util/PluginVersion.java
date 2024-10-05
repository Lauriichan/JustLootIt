package me.lauriichan.spigot.justlootit.util;

import me.lauriichan.spigot.justlootit.platform.util.SimpleVersion;

public final class PluginVersion implements Comparable<PluginVersion> {

    private final SimpleVersion version;
    private final String suffix;

    public PluginVersion(String version) {
        int lastIdx = version.lastIndexOf('.');
        String suffix = "";
        if (lastIdx != -1) {
            String lastPart = version.substring(lastIdx + 1);
            for (int i = 0; i < lastPart.length(); i++) {
                if (Character.isDigit(lastPart.charAt(i))) {
                    continue;
                }
                suffix = lastPart.substring(i);
                version = version.substring(0, lastIdx + i + 1);
                break;
            }
        }
        this.version = SimpleVersion.of(version);
        this.suffix = suffix;
    }

    public SimpleVersion version() {
        return version;
    }

    public String suffix() {
        return suffix;
    }

    @Override
    public int compareTo(PluginVersion other) {
        int comp = version.compareTo(other.version);
        if (comp != 0) {
            return comp;
        }
        if (suffix.isEmpty() || other.suffix.isEmpty()) {
            return -Boolean.compare(suffix.isEmpty(), other.suffix.isEmpty());
        }
        return suffix.compareTo(other.suffix);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(version.major()).append('.').append(version.minor()).append('.').append(version.patch())
            .append(suffix).toString();
    }

}
