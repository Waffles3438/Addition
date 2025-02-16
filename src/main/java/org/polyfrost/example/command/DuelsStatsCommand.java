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
import java.util.NavigableMap;
import java.util.TreeMap;

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
            UChat.chat("Player has never played Duels");
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
        String MVPPlusPlusCheck = getString(profile, "monthlyPackageRank");

        if(Player.equals("Technoblade")) {
            Player = "§d[PIG§b+++§d] " + Player;
        } else if (special.equals("YOUTUBER")) {
            Player = "§c[§fYOUTUBE§c] " + Player;
        } else if (special.equals("ADMIN")) {
            String admin = getString(profile, "prefix");
            if(admin != null && admin.equals("§c[OWNER]")) {
                Player = "§c[OWNER] " + Player;
            } else {
                Player = "§c[ADMIN] " + Player;
            }
        } else if (special.equals("GAME_MASTER")) {
            Player = "§2[GM] " + Player;
        } else if (monthly != null && MVPPlusPlusCheck != null && rank.equals("MVP_PLUS") && monthly.equals("GOLD") && MVPPlusPlusCheck.equals("SUPERSTAR")) { // Gold MVP++ check
            String plusColor = getString(profile, "rankPlusColor");
            String color = "§c";
            if (plusColor != null) {
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
            }
            Player = "§6[MVP" + color + "++" + "§6] " + Player;
        } else if (rank.equals("MVP_PLUS"))  {
            String plusColor = getString(profile, "rankPlusColor");
            String color = "§c";
            if (plusColor != null) {
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
            }
            if(monthly != null && MVPPlusPlusCheck != null && monthly.equals("AQUA") && MVPPlusPlusCheck.equals("SUPERSTAR")) {
                Player = "§b[MVP" + color + "++" + "§b] " + Player;
            } else {
                Player = "§b[MVP" + color + "+" + "§b] " + Player;
            }
        } else if (rank.equals("MVP")) {
            Player = "§b[MVP] " + Player;
        } else if (rank.equals("VIP_PLUS")) {
            Player = "§a[VIP§6+§a] " + Player;
        } else if (rank.equals("VIP")) {
            Player = "§a[VIP] " + Player;
        } else {
            Player = "§7" + Player;
        }

        int wins, kills, exp, losses, deaths, cws, bws;
        double wlr, kdr;
        String level;

        exp = getValue(profile, "networkExp");
        level = levelColor(String.valueOf((double) Math.round(getExactLevel(exp) * 100) / 100));

        cws = getValue(d, "current_winstreak");
        bws = getValue(d, "best_overall_winstreak");
        wins = getValue(d, "wins");
        kills = getValue(d, "kills");
        deaths = getValue(d, "deaths");
        losses = getValue(d, "losses");

        if (losses != 0) wlr = (double) wins / (double) losses;
        else wlr = wins;
        wlr = (double) Math.round(wlr * 100) / 100;

        if (deaths != 0) kdr = (double) kills / (double) deaths;
        else kdr = kills;
        kdr = (double) Math.round(kdr * 100) / 100;

        UChat.chat("§9------------------------------------------");
        UChat.chat(getPlayerDivision(wins) + Player);
        UChat.chat("Level: " + level);
        UChat.chat("WLR: " + wlr);
        UChat.chat("Wins: " + wins);
        UChat.chat("KDR: " + kdr);
        UChat.chat("Kills: " + kills);
        if(cws != -1) {
            UChat.chat("Current Winstreak: " + cws);
            UChat.chat("Best Winstreak: " + bws);
        }
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
            if (member.equals("current_winstreak") || member.equals("best_overall_winstreak")) return -1;
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

    private static final NavigableMap<Integer, String> DIVISIONS = new TreeMap<>();

    static {
        DIVISIONS.put(100, "§8Rookie");
        DIVISIONS.put(120, "§8Rookie II");
        DIVISIONS.put(140, "§8Rookie III");
        DIVISIONS.put(160, "§8Rookie IV");
        DIVISIONS.put(180, "§8Rookie V");
        DIVISIONS.put(200, "§fIron");
        DIVISIONS.put(260, "§fIron II");
        DIVISIONS.put(320, "§fIron III");
        DIVISIONS.put(380, "§fIron IV");
        DIVISIONS.put(440, "§fIron V");
        DIVISIONS.put(500, "§6Gold");
        DIVISIONS.put(600, "§6Gold II");
        DIVISIONS.put(700, "§6Gold III");
        DIVISIONS.put(800, "§6Gold IV");
        DIVISIONS.put(900, "§6Gold V");
        DIVISIONS.put(1000, "§3Diamond");
        DIVISIONS.put(1200, "§3Diamond II");
        DIVISIONS.put(1400, "§3Diamond III");
        DIVISIONS.put(1600, "§3Diamond IV");
        DIVISIONS.put(1800, "§3Diamond V");
        DIVISIONS.put(2000, "§2Master");
        DIVISIONS.put(2400, "§2Master II");
        DIVISIONS.put(2800, "§2Master III");
        DIVISIONS.put(3200, "§2Master IV");
        DIVISIONS.put(3600, "§2Master V");
        DIVISIONS.put(4000, "§4§lLegend");
        DIVISIONS.put(5200, "§4§lLegend II");
        DIVISIONS.put(6400, "§4§lLegend III");
        DIVISIONS.put(7600, "§4§lLegend IV");
        DIVISIONS.put(8800, "§4§lLegend V");
        DIVISIONS.put(10000, "§e§lGrandmaster");
        DIVISIONS.put(12000, "§e§lGrandmaster II");
        DIVISIONS.put(14000, "§e§lGrandmaster III");
        DIVISIONS.put(16000, "§e§lGrandmaster IV");
        DIVISIONS.put(18000, "§e§lGrandmaster V");
        DIVISIONS.put(20000, "§5§lGodlike");
        DIVISIONS.put(26000, "§5§lGodlike II");
        DIVISIONS.put(32000, "§5§lGodlike III");
        DIVISIONS.put(38000, "§5§lGodlike IV");
        DIVISIONS.put(44000, "§5§lGodlike V");
        DIVISIONS.put(50000, "§b§lCelestial");
        DIVISIONS.put(60000, "§b§lCelestial II");
        DIVISIONS.put(70000, "§b§lCelestial III");
        DIVISIONS.put(80000, "§b§lCelestial IV");
        DIVISIONS.put(90000, "§b§lCelestial V");
        DIVISIONS.put(100000, "§d§lDivine");
        DIVISIONS.put(120000, "§d§lDivine II");
        DIVISIONS.put(140000, "§d§lDivine III");
        DIVISIONS.put(160000, "§d§lDivine IV");
        DIVISIONS.put(180000, "§d§lDivine V");
        DIVISIONS.put(200000, "§c§lAscended");
        DIVISIONS.put(220000, "§c§lAscended II");
        DIVISIONS.put(240000, "§c§lAscended III");
        DIVISIONS.put(260000, "§c§lAscended IV");
        DIVISIONS.put(280000, "§c§lAscended V");
        DIVISIONS.put(300000, "§c§lAscended VI");
        DIVISIONS.put(320000, "§c§lAscended VII");
        DIVISIONS.put(340000, "§c§lAscended VIII");
        DIVISIONS.put(360000, "§c§lAscended IX");
        DIVISIONS.put(380000, "§c§lAscended X");
        DIVISIONS.put(400000, "§c§lAscended XI");
        DIVISIONS.put(420000, "§c§lAscended XII");
        DIVISIONS.put(440000, "§c§lAscended XIII");
        DIVISIONS.put(460000, "§c§lAscended XIV");
        DIVISIONS.put(480000, "§c§lAscended XV");
        DIVISIONS.put(500000, "§c§lAscended XVI");
        DIVISIONS.put(520000, "§c§lAscended XVII");
        DIVISIONS.put(540000, "§c§lAscended XVIII");
        DIVISIONS.put(560000, "§c§lAscended XIX");
        DIVISIONS.put(580000, "§c§lAscended XX");
        DIVISIONS.put(600000, "§c§lAscended XXI");
        DIVISIONS.put(620000, "§c§lAscended XXII");
        DIVISIONS.put(640000, "§c§lAscended XXIII");
        DIVISIONS.put(660000, "§c§lAscended XXIV");
        DIVISIONS.put(680000, "§c§lAscended XXV");
        DIVISIONS.put(700000, "§c§lAscended XXVI");
        DIVISIONS.put(720000, "§c§lAscended XXVII");
        DIVISIONS.put(740000, "§c§lAscended XXVIII");
        DIVISIONS.put(760000, "§c§lAscended XXIX");
        DIVISIONS.put(780000, "§c§lAscended XXX");
        DIVISIONS.put(800000, "§c§lAscended XXXI");
        DIVISIONS.put(820000, "§c§lAscended XXXII");
        DIVISIONS.put(840000, "§c§lAscended XXXIII");
        DIVISIONS.put(860000, "§c§lAscended XXXIV");
        DIVISIONS.put(880000, "§c§lAscended XXXV");
        DIVISIONS.put(900000, "§c§lAscended XXXVI");
        DIVISIONS.put(920000, "§c§lAscended XXXVII");
        DIVISIONS.put(940000, "§c§lAscended XXXVIII");
        DIVISIONS.put(960000, "§c§lAscended XXXIX");
        DIVISIONS.put(980000, "§c§lAscended XL");
        DIVISIONS.put(1000000, "§c§lAscended XLI");
        DIVISIONS.put(1020000, "§c§lAscended XLII");
        DIVISIONS.put(1040000, "§c§lAscended XLIII");
        DIVISIONS.put(1060000, "§c§lAscended XLIV");
        DIVISIONS.put(1080000, "§c§lAscended XLV");
        DIVISIONS.put(1100000, "§c§lAscended XLVI");
        DIVISIONS.put(1120000, "§c§lAscended XLVII");
        DIVISIONS.put(1140000, "§c§lAscended XLVIII");
        DIVISIONS.put(1160000, "§c§lAscended XLIX");
        DIVISIONS.put(1180000, "§c§lAscended L");
    }

    public static String getPlayerDivision(int wins) {
        if (wins < 100) {
            return "";
        }
        return DIVISIONS.floorEntry(wins).getValue() + " ";
    }
}
