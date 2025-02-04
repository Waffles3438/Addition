package org.polyfrost.example.config;

import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Text;
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

    @Slider(
            name = "Jump ticks",
            min = 0, max = 10,
            step = 1
    )
    public static int jumpTicks = 3;

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

    @Text(
            name = "Hypixel API",
            secure = true, multiline = false
    )
    public static String api = "";

    public ModConfig() {
        super(new Mod(Addition.NAME, ModType.UTIL_QOL), Addition.MODID + ".json");
        initialize();
        addDependency("jumpTicks", "ndj");
    }
}

