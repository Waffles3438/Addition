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

@Command(value = "bw")
public class StatsCommand {

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

        JsonObject profile, ach, bw;
        int star, fk, bb, w;
        double fkdr, wlr, bblr;
        connection = newConnection("https://api.hypixel.net/player?key=" + ModConfig.api + "&uuid=" + uuid);
        if (connection.isEmpty()) {
            UChat.chat("Player has never logged on Hypixel");
            return;
        }
        if (connection.equals("{\"success\":true,\"player\":null}")) {
            // player is nicked
            UChat.chat("Player has never logged on Hypixel");
            return;
        }
        try {
            profile = getStringAsJson(connection).getAsJsonObject("player");
            bw = profile.getAsJsonObject("stats").getAsJsonObject("Bedwars");
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

        star = getValue(ach, "bedwars_level");

        fk = getValue(bw, "final_kills_bedwars");

        bb = getValue(bw, "beds_broken_bedwars");

        w = getValue(bw, "wins_bedwars");

        fkdr = (double) getValue(bw, "final_kills_bedwars") / (double) getValue(bw, "final_deaths_bedwars");
        fkdr = (double) Math.round(fkdr * 100) / 100;

        wlr = (double) getValue(bw, "wins_bedwars") / (double) getValue(bw, "losses_bedwars");
        wlr = (double) Math.round(wlr * 100) / 100;

        bblr = (double) getValue(bw, "beds_broken_bedwars") / (double) getValue(bw, "beds_lost_bedwars");
        bblr = (double) Math.round(bblr * 100) / 100;

        UChat.chat("§9----------------------------------------------------");
        UChat.chat(getFormattedRank(star) + " " + Player);
        UChat.chat("FKDR: " + fkdr);
        UChat.chat("Final kills: " + fk);
        UChat.chat("WLR: " + wlr);
        UChat.chat("Wins: " + w);
        UChat.chat("BBLR: " + bblr);
        UChat.chat("Beds: " + bb);
        UChat.chat("§9----------------------------------------------------");
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

    public enum Rank {
        STONE1("§7[_✫]"),
        STONE("§7[__✫]"),
        IRON("§f[___✫]"),
        GOLD("§6[___✫]"),
        DIAMOND("§b[___✫]"),
        EMERALD("§2[___✫]"),
        SAPPHIRE("§3[___✫]"),
        RUBY("§4[___✫]"),
        CRYSTAL("§d[___✫]"),
        OPAL("§9[___✫]"),
        AMETHYST("§5[___✫]"),
        RAINBOW("§c[§6_§e_§a_§b_§d✫§5]"),
        IRON_PRIME("§7[§f____§7✪§7]"),
        GOLD_PRIME("§7[§e____§6✪§7]"),
        DIAMOND_PRIME("§7[§b____§3✪§7]"),
        EMERALD_PRIME("§7[§a____§2✪§7]"),
        SAPPHIRE_PRIME("§7[§3____§9✪§7]"),
        RUBY_PRIME("§7[§c____§4✪§7]"),
        CRYSTAL_PRIME("§7[§d____§5✪§7]"),
        OPAL_PRIME("§7[§9____§1✪§7]"),
        AMETHYST_PRIME("§7[§5____§8✪§7]"),
        MIRROR("§8[§7_§f__§7_§8✪]"),
        LIGHT("§f[_§e__§6_§l⚝§6]"),
        DAWN("§6[_§f__§b_§3§l⚝§3]"),
        DUSK("§5[_§d__§6_§e§l⚝§e]"),
        AIR("§b[_§f__§7_§l⚝§8]"),
        WIND("§f[_§a__§2_§l⚝§2]"),
        NEBULA("§4[_§c__§d_§l⚝§d]"),
        THUNDER("§e[_§f__§8_§l⚝§8]"),
        EARTH("§a[_§2__§6_§l⚝§e]"),
        WATER("§b[_§3__§9_§l⚝§1]"),
        FIRE("§e[_§6__§c_§l⚝§4]");

        private final String format;

        Rank(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }
    }

    public String getFormattedRank(int star) {
        if(star > 3000) {
            return "§f[" + star + "✫]";
        }
        Rank rank = getRankForNumber(star);
        String starString = String.valueOf(star);
        StringBuilder txt = new StringBuilder(rank.getFormat());
        int starCounter = 0;
        for(int i = 0; i < txt.length(); i++){
            if(rank.getFormat().charAt(i) == '_'){
                txt.deleteCharAt(i);
                txt.insert(i, starString.charAt(starCounter));
                starCounter++;
            }
        }
        return txt.toString();
    }

    private Rank getRankForNumber(int number) {
        if (number < 10) return Rank.STONE1;
        else if (number < 100) return Rank.STONE;
        else if (number < 200) return Rank.IRON;
        else if (number < 300) return Rank.GOLD;
        else if (number < 400) return Rank.DIAMOND;
        else if (number < 500) return Rank.EMERALD;
        else if (number < 600) return Rank.SAPPHIRE;
        else if (number < 700) return Rank.RUBY;
        else if (number < 800) return Rank.CRYSTAL;
        else if (number < 900) return Rank.OPAL;
        else if (number < 1000) return Rank.AMETHYST;
        else if (number < 1100) return Rank.RAINBOW;
        else if (number < 1200) return Rank.IRON_PRIME;
        else if (number < 1300) return Rank.GOLD_PRIME;
        else if (number < 1400) return Rank.DIAMOND_PRIME;
        else if (number < 1500) return Rank.EMERALD_PRIME;
        else if (number < 1600) return Rank.SAPPHIRE_PRIME;
        else if (number < 1700) return Rank.RUBY_PRIME;
        else if (number < 1800) return Rank.CRYSTAL_PRIME;
        else if (number < 1900) return Rank.OPAL_PRIME;
        else if (number < 2000) return Rank.AMETHYST_PRIME;
        else if (number < 2100) return Rank.MIRROR;
        else if (number < 2200) return Rank.LIGHT;
        else if (number < 2300) return Rank.DAWN;
        else if (number < 2400) return Rank.DUSK;
        else if (number < 2500) return Rank.AIR;
        else if (number < 2600) return Rank.WIND;
        else if (number < 2700) return Rank.NEBULA;
        else if (number < 2800) return Rank.THUNDER;
        else if (number < 2900) return Rank.EARTH;
        else if (number < 3000) return Rank.WATER;
        else if (number < 3100) return Rank.FIRE;
        return Rank.RAINBOW;
    }
}
