package me.waffles.additional.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;

import java.util.UUID;

/**
 * Nick detection for stat lookups.
 *
 * Hypixel presents a nicked player to everyone else under a generated name and UUID, so
 * looking that UUID up finds no player on the API even though they are standing right
 * there in the game. That is the signal, but "no Hypixel record" on its own is not enough
 * to call a nick, because a real account that simply never played Hypixel also has no
 * record. RedMister is one: a valid account, zero Hypixel stats, not nicked.
 *
 * So a nick needs all three of:
 * <ul>
 *   <li>a UUID that could belong to a real account - Hypixel issues version 2 UUIDs to its
 *       fake entities and lobby NPCs, and those are not nicked players;</li>
 *   <li>presence in the server player list, meaning they are on Hypixel right now, so a
 *       name the API does not know cannot be their real one;</li>
 *   <li>no Hypixel player record at all, which the caller establishes.</li>
 * </ul>
 *
 * Looking up a real-but-unplayed account by name satisfies the first and third but not the
 * second, so it keeps the plain "no Hypixel stats" message.
 */
public final class NickUtils {

    private NickUtils() {
    }

    /**
     * Whether this UUID could belong to a real Minecraft account.
     *
     * Hypixel hands out version 2 UUIDs to fake entities and lobby NPCs. Those show up in
     * the world without a Hypixel record, so without this check they would be reported as
     * nicked players.
     */
    public static boolean isRealAccountUuid(UUID uuid) {
        return uuid != null && uuid.version() != 2;
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
     * Whether a player the API has no record of should be reported as nicked.
     *
     * Client thread only, see {@link #isInPlayerList(UUID)}.
     */
    public static boolean looksNicked(UUID uuid) {
        return isRealAccountUuid(uuid) && isInPlayerList(uuid);
    }

    /**
     * Message for a player the API has no record of.
     *
     * @param nicked as resolved on the client thread by {@link #looksNicked(UUID)}
     */
    public static String describeMissingPlayer(String username, boolean nicked) {
        if (nicked) {
            return "§b" + username + " §fis §cnicked§f.";
        }
        return username + " has no Hypixel stats.";
    }
}
