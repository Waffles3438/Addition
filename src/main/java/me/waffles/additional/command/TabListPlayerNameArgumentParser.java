package me.waffles.additional.command;

import cc.polyfrost.oneconfig.utils.commands.arguments.ArgumentParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TabListPlayerNameArgumentParser extends ArgumentParser<String> {
    @Override
    public String parse(String value) {
        return value;
    }

    @Override
    public List<String> complete(String current, Parameter parameter) {
        Minecraft minecraft = Minecraft.getMinecraft();
        NetHandlerPlayClient netHandler = minecraft.getNetHandler();
        if (minecraft.theWorld == null || netHandler == null) {
            return Collections.emptyList();
        }

        String prefix = current == null ? "" : current.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (NetworkPlayerInfo playerInfo : netHandler.getPlayerInfoMap()) {
            GameProfile profile = playerInfo.getGameProfile();
            if (profile == null || profile.getName() == null) {
                continue;
            }

            String name = profile.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(name);
            }
        }
        return matches;
    }
}
