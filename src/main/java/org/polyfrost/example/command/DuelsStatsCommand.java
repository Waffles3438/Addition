package org.polyfrost.example.command;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.NetworkUtils;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Greedy;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.polyfrost.example.config.ModConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Command(value = "d")
public class DuelsStatsCommand {

    @Main
    private void main(@Greedy String player) {
        if(player.isEmpty()) {
            UChat.chat("Enter a username");
            return;
        }

        Multithreading.runAsync(() -> {
            printStats(player);
        });
    }

    private void printStats(String player){

        String uuid, connection, Player;

        try {
            uuid = NetworkUtils.getJsonElement("https://api.mojang.com/users/profiles/minecraft/" + player).getAsJsonObject().get("id").getAsString();
            Player = NetworkUtils.getJsonElement("https://api.mojang.com/users/profiles/minecraft/" + player).getAsJsonObject().get("name").getAsString();
        } catch (Exception e) {
            UChat.chat("Invalid player");
            return;
        }

        JsonObject profile, ach, d;
        connection = newConnection("https://api.hypixel.net/player?key=" + ModConfig.api + "&uuid=" + uuid);
        if (connection.isEmpty()) {
            UChat.chat("Invalid API key");
            return;
        }
        if (connection.equals("{\"success\":true,\"player\":null}")) {
            // player is nicked
            UChat.chat("Player has never logged on Hypixel");
            return;
        }
        try {
            profile = getStringAsJson(connection).getAsJsonObject("player");
            d = profile.getAsJsonObject("stats").getAsJsonObject("Duels");
            ach = profile.getAsJsonObject("achievements");
        } catch (NullPointerException er) {
            // never played bedwars or joined lobby
            UChat.chat("Player has never played bedwars");
            return;
        }

        String rank;
        if(getString(profile, "newPackageRank") != null) {
            rank = getString(profile, "newPackageRank");
        } else {
            rank = "non";
        }

        String special = "nothing";
        if(getString(profile, "rank") != null){
            special = getString(profile, "rank");
        }

        String monthly = getString(profile, "monthlyRankColor");

        if(Player.equals("Technoblade")) {
            Player = "§d[PIG§b+++§d] " + Player;
        } else if (special.equals("YOUTUBER")) {
            Player = "§c[§fYOUTUBE§c] " + Player;
        } else if (special.equals("ADMIN")) {
            if(getString(profile, "prefix").equals("§c[OWNER]")) {
                Player = "§c[OWNER] " + Player;
            } else {
                Player = "§c[ADMIN] " + Player;
            }
        } else if (special.equals("GAME_MASTER")) {
            Player = "§2[GM] " + Player;
        } else if (rank.equals("MVP_PLUS") && monthly.equals("AQUA"))  {
            String plusColor = getString(profile, "rankPlusColor");
            String color = "§c";
            switch (plusColor) {
                case "RED":
                    color = "§c";
                    break;
                case "GOLD":
                    color = "§6";
                    break;
                case "GREEN":
                    color = "§a";
                    break;
                case "YELLOW":
                    color = "§e";
                    break;
                case "LIGHT_PURPLE":
                    color = "§d";
                    break;
                case "WHITE":
                    color = "§f";
                    break;
                case "BLUE":
                    color = "§9";
                    break;
                case "DARK_GREEN":
                    color = "§2";
                    break;
                case "DARK_RED":
                    color = "§4";
                    break;
                case "DARK_AQUA":
                    color = "§3";
                    break;
                case "DARK_PURPLE":
                    color = "§5";
                    break;
                case "GRAY":
                    color = "§7";
                    break;
                case "BLACK":
                    color = "§0";
                    break;
                case "DARK_BLUE":
                    color = "§1";
                    break;
            }
            if(getString(profile, "monthlyPackageRank") != null && getString(profile, "monthlyPackageRank").equals("SUPERSTAR")) {
                Player = "§b[MVP" + color + "++" + "§b] " + Player;
            } else {
                Player = "§b[MVP" + color + "+" + "§b] " + Player;
            }
        } else if (rank.equals("MVP_PLUS") && monthly.equals("GOLD")) {
            String plusColor = getString(profile, "rankPlusColor");
            String color = "§c";
            switch (plusColor) {
                case "RED":
                    color = "§c";
                    break;
                case "GOLD":
                    color = "§6";
                    break;
                case "GREEN":
                    color = "§a";
                    break;
                case "YELLOW":
                    color = "§e";
                    break;
                case "LIGHT_PURPLE":
                    color = "§d";
                    break;
                case "WHITE":
                    color = "§f";
                    break;
                case "BLUE":
                    color = "§9";
                    break;
                case "DARK_GREEN":
                    color = "§2";
                    break;
                case "DARK_RED":
                    color = "§4";
                    break;
                case "DARK_AQUA":
                    color = "§3";
                    break;
                case "DARK_PURPLE":
                    color = "§5";
                    break;
                case "GRAY":
                    color = "§7";
                    break;
                case "BLACK":
                    color = "§0";
                    break;
                case "DARK_BLUE":
                    color = "§1";
                    break;
            }
            Player = "§6[MVP" + color + "++" + "§6] " + Player;
        } else if (rank.equals("MVP")) {
            Player = "§b[MVP] " + Player;
        } else if (rank.equals("VIP_PLUS")) {
            Player = "§a[VIP§6+§a] " + Player;
        } else if (rank.equals("VIP")) {
            Player = "§a[VIP] " + Player;
        } else {
            Player = "§7" + Player;
        }

        int wins, kills, exp;
        double wlr, kdr;
        String level;

        exp = getValue(profile, "networkExp");

        level = levelColor(String.valueOf((double) Math.round(getExactLevel(exp) * 100) / 100));

        wins = getValue(d, "wins");
        kills = getValue(d, "kills");

        wlr = (double) wins / (double) getValue(d, "losses");
        wlr = (double) Math.round(wlr * 100) / 100;

        kdr = (double) kills / (double) getValue(d, "deaths");
        kdr = (double) Math.round(kdr * 100) / 100;

        UChat.chat("§9------------------------------------------");
        UChat.chat(Player);
        UChat.chat("Level: " + level);
        UChat.chat("WLR: " + wlr);
        UChat.chat("Wins: " + wins);
        UChat.chat("KDR: " + kdr);
        UChat.chat("Kills: " + kills);
        UChat.chat("§9------------------------------------------");
    }

    private String levelColor(String level) {
        double lvl = Double.parseDouble(level);
        if(lvl < 35) return "§c" + level;
        else if(lvl < 45) return "§6" + level;
        else if(lvl < 55) return "§a" + level;
        else if(lvl < 65) return "§d" + level;
        else if(lvl < 75) return "§f" + level;
        else if(lvl < 85) return "§9" + level;
        else if(lvl < 95) return "§2" + level;
        else if(lvl < 150) return "§4" + level;
        else if(lvl < 200) return "§5" + level;
        else return "§0" + level;
    }

    private int getValue(JsonObject type, String member) {
        try {
            return type.get(member).getAsInt();
        } catch (NullPointerException er) {
            return 0;
        }
    }

    private String getString(JsonObject type, String member) {
        try {
            return type.get(member).getAsString();
        } catch (NullPointerException er) {
            return null;
        }
    }

    private JsonObject getStringAsJson(String text) {
        return new JsonParser().parse(text).getAsJsonObject();
    }

    private String newConnection(String link) {
        URL url;
        String result = "";
        HttpURLConnection con = null;
        try {
            url = new URL(link);
            con = (HttpURLConnection) url.openConnection();
            result = getContents(con);
        } catch (IOException e) { }
        finally {
            if (con != null) con.disconnect();
        }
        return result;
    }

    private String getContents(HttpURLConnection con) {
        if (con != null) {
            // since BufferedReader is defined within try catch, close is called regardless of completion
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String input;
                StringBuilder sb = new StringBuilder();
                while ((input = br.readLine()) != null) {
                    sb.append(input);
                }
                return sb.toString();
            } catch (IOException e) { }
        }
        return "";
    }

    private double BASE = 10_000;
    private double GROWTH = 2_500;
    private double HALF_GROWTH = 0.5 * GROWTH;
    private double REVERSE_PQ_PREFIX = -(BASE - 0.5 * GROWTH) / GROWTH;
    private double REVERSE_CONST = REVERSE_PQ_PREFIX * REVERSE_PQ_PREFIX;
    private double GROWTH_DIVIDES_2 = 2 / GROWTH;

    private double getLevel(double exp) {
        return exp < 0 ? 1 : Math.floor(1 + REVERSE_PQ_PREFIX + Math.sqrt(REVERSE_CONST + GROWTH_DIVIDES_2 * exp));
    }

    private double getExactLevel(double exp) {
        return getLevel(exp) + getPercentageToNextLevel(exp);
    }

    private double getTotalExpToFullLevel(double level) {
        return (HALF_GROWTH * (level - 2) + BASE) * (level - 1);
    }

    private double getTotalExpToLevel(double level) {
        double lv = Math.floor(level), x0 = getTotalExpToFullLevel(lv);
        if (level == lv) return x0;
        return (getTotalExpToFullLevel(lv + 1) - x0) * (level % 1) + x0;
    }

    private double getPercentageToNextLevel(double exp) {
        double lv = getLevel(exp), x0 = getTotalExpToLevel(lv);
        return (exp - x0) / (getTotalExpToLevel(lv + 1) - x0);
    }
}
