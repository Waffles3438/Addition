package org.polyfrost.example.config;

import org.polyfrost.example.Addition;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;

public class ModConfig extends Config {
    @Switch(
            name = "No jump delay",
            size = OptionSize.DUAL
    )
    public static boolean ndj = false;

    @Switch(
            name = "Show nametags on shift"
    )
    public static boolean nametagsOnShift = false;

    @Switch(
            name = "Show invisible player nametags"
    )
    public static boolean invisNametags = false;

    @Switch(
            name = "Extend nametag range"
    )
    public static boolean extendNametagRange = false;

    @Switch(
            name = "Show nametags behind walls"
    )
    public static boolean nametagsThroughWalls = false;

    public ModConfig() {
        super(new Mod(Addition.NAME, ModType.UTIL_QOL), Addition.MODID + ".json");
        initialize();
    }
}

