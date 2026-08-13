package me.waffles.additional.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;

import java.util.UUID;

/**
 * Nick detection for stat lookups.
 *
 * Hypixel presents a nicked player to everyone else under a generated name and UUID, so
 * looking that UUID up finds no player on the API even though they are standing right
 * there in the game. "Present in the server player list but absent from the Hypixel API"
 * is therefore the signal for a nick, and it is exactly what separates a nick from a name
 * that genuinely has no Hypixel history.
 */
public final class NickUtils {

    private NickUtils() {
    }

    /**
     * Whether the client currently sees this UUID in the server player list.
     *
     * Must be called on the client thread. NetHandlerPlayClient's player map is mutated
     * there when PlayerListItem packets are handled, so reading it from a command's async
     * worker would be a data race - which is why the stat commands resolve this before
     * handing off to Multithreading.
     */
    public static boolean isInPlayerList(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        // Null on the main menu and between worlds.
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler == null) {
            return false;
        }

        return netHandler.getPlayerInfo(uuid) != null;
    }

    /**
     * Message for a player the API has no record of.
     *
     * @param inPlayerList whether the player is currently in the server player list, as
     *                     resolved on the client thread by {@link #isInPlayerList(UUID)}
     */
    public static String describeMissingPlayer(String username, boolean inPlayerList) {
        if (inPlayerList) {
            return "§b" + username + " §fis §cnicked§f.";
        }
        return username + " has no Hypixel stats.";
    }
}
