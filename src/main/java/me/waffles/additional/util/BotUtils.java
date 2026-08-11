package me.waffles.additional.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class BotUtils {

    // Read from the render thread on every nametag, but cleared from Forge's
    // disconnect event, which fires on the Netty thread. A plain HashMap being
    // cleared underneath a concurrent get() can corrupt the table, so this has to
    // be a concurrent map rather than just "usually fine".
    private static final Map<UUID, Boolean> botCache = new ConcurrentHashMap<>();

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9_]");

    public static boolean isBot(Entity entity) {
        if (!(entity instanceof EntityPlayer)) return true;
        EntityPlayer player = (EntityPlayer) entity;
        UUID uuid = player.getUniqueID();

        Boolean cached = botCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        if (uuid.version() == 2) {
            botCache.put(uuid, true);
            return true;
        }

        // Null between leaving a world and joining the next, and on the main menu.
        // RenderWorldLastEvent and the Render mixins can both still fire in that
        // window, so this cannot be dereferenced blind.
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) {
            return true; // not cached - we simply cannot tell yet
        }

        NetworkPlayerInfo info = netHandler.getPlayerInfo(uuid);
        if (info == null) {
            return true; // not cached — tab entry may just not have arrived yet
        }

        // Use the tab-list profile's name, not player.getName() — the entity's
        // own GameProfile can be permanently null-named if SpawnPlayer raced
        // ahead of the PlayerListItem packet at spawn time.
        String name = info.getGameProfile().getName();
        boolean result = name == null || NON_ALPHANUMERIC.matcher(name).find();

        botCache.put(uuid, result);
        return result;
    }

    public static void clearCache() {
        botCache.clear();
    }
}
