package me.waffles.additional.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.UUID;
import java.util.regex.Pattern;

public class MojangAPIUtils {
    private static final String USER_AGENT = "node-ao/2.0.3";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{32}");

    private MojangAPIUtils() {
    }

    public static String fetchUuid(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return null;
        }

        String response = AbyssAPIUtils.fetchPlayerData(
                "https://api.mojang.com/users/profiles/minecraft/" + username,
                USER_AGENT
        );
        if (response == null || response.isEmpty()) {
            return null;
        }

        try {
            JsonObject profile = new JsonParser().parse(response).getAsJsonObject();
            if (!profile.has("id") || profile.get("id").isJsonNull()) {
                return null;
            }

            String compactUuid = profile.get("id").getAsString().replace("-", "");
            if (!UUID_PATTERN.matcher(compactUuid).matches()) {
                return null;
            }

            return UUID.fromString(
                    compactUuid.substring(0, 8) + "-"
                            + compactUuid.substring(8, 12) + "-"
                            + compactUuid.substring(12, 16) + "-"
                            + compactUuid.substring(16, 20) + "-"
                            + compactUuid.substring(20)
            ).toString();
        } catch (Exception ignored) {
            return null;
        }
    }
}
