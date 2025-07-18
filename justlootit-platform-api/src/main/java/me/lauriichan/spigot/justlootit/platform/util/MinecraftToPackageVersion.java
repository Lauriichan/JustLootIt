package me.lauriichan.spigot.justlootit.platform.util;

import static me.lauriichan.spigot.justlootit.platform.util.SimpleVersion.*;

public enum MinecraftToPackageVersion {

    v1_20_R2(of(1, 20, 2), of(1, 20, 3)),
    v1_20_R3(of(1, 20, 4)),
    v1_20_R4(of(1, 20, 5), of(1, 20, 6)),
    v1_21_R1(of(1, 21, 0), of(1, 21, 2)),
    v1_21_R2(of(1, 21, 3)),
    v1_21_R3(of(1, 21, 4)),
    v1_21_R4(of(1, 21, 5)),
    v1_21_R5(of(1, 21, 6), of(1, 21, 8));

    private static final MinecraftToPackageVersion[] values = MinecraftToPackageVersion.values();

    private final SimpleVersion min, max;

    private MinecraftToPackageVersion(SimpleVersion version) {
        this.min = version;
        this.max = null;
    }

    private MinecraftToPackageVersion(SimpleVersion min, SimpleVersion max) {
        this.min = min;
        this.max = max;
    }

    public String packageVersion() {
        return name();
    }

    public boolean isVersion(SimpleVersion version) {
        if (max == null) {
            return version.isSame(min);
        }
        return version.isBetween(min, max);
    }

    public static String getPackageVersion(SimpleVersion version) {
        for (MinecraftToPackageVersion value : values) {
            if (value.isVersion(version)) {
                return value.packageVersion();
            }
        }
        return null;
    }

}
