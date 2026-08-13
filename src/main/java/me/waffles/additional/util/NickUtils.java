package me.waffles.additional.util;

import java.util.UUID;

/**
 * Nick detection for stat lookups.
 *
 * Hypixel presents a nicked player to everyone else under a generated name, and the API has
 * no player behind it - abyssoverlay answers with {@code "player": null}. So once a name has
 * resolved to a valid UUID and the request itself succeeded, "Hypixel has no record of this
 * account" is the nick signal.
 *
 * Note this is a property of the account the lookup landed on, not of the lookup being
 * performed in game: RedMister is a real account that returns {@code "player": null} on
 * abyssoverlay, and is reported as nicked on that basis.
 */
public final class NickUtils {

    private NickUtils() {
    }

    /**
     * Whether this is a well formed UUID, i.e. the name actually resolved to an account
     * rather than to something we can draw no conclusion from.
     */
    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }

        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Message for a player Hypixel has no record of.
     *
     * Shared by both stat commands so the wording cannot drift between them.
     */
    public static String describeMissingPlayer(String username, String uuid) {
        if (isValidUuid(uuid)) {
            return "§b" + username + " §fis §cnicked§f.";
        }
        return username + " has no Hypixel stats.";
    }
}
