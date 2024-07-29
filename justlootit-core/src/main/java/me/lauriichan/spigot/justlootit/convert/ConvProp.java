package me.lauriichan.spigot.justlootit.convert;

public final class ConvProp {
    
    private ConvProp() {
        throw new UnsupportedOperationException();
    }
    
    public static final String DO_LOOTIN_CONVERSION = "DoLootinConversion";
    public static final String LOOTIN_DISABLE_STATIC_CONTAINER = "LootinDisableStaticContainerConversion";
    
    public static final String DO_VANILLA_CONVERSION = "DoVanillaConversion";
    public static final String VANILLA_ALLOW_STATIC_CONTAINER = "VanillaAllowStaticContainerConversion";
    public static final String VANILLA_ALLOW_ITEM_FRAME = "VanillaAllowItemFrameConversion";
    public static final String VANILLA_ALLOW_ONLY_ELYTRA_FRAME = "VanillaAllowOnlyElytraFrameConversion";
    
    public static final String BLACKLISTED_WORLDS = "BlacklistedWorlds";

}
