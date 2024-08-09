package org.polyfrost.example.config;

import org.polyfrost.example.MultiCheat;
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
            name = "Show nametags on shift",
            size = OptionSize.DUAL
    )
    public static boolean nametagsOnShift = false;

    public ModConfig() {
        super(new Mod(MultiCheat.NAME, ModType.UTIL_QOL), MultiCheat.MODID + ".json");
        initialize();
    }
}

