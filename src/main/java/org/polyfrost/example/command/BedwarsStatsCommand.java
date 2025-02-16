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
public class BedwarsStatsCommand {

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

        int star, fk, bb, w, l, fd, bl, ws;
        double fkdr, wlr, bblr;

        star = getValue(ach, "bedwars_level");
        fk = getValue(bw, "final_kills_bedwars");
        bb = getValue(bw, "beds_broken_bedwars");
        w = getValue(bw, "wins_bedwars");
        fd = getValue(bw, "final_deaths_bedwars");
        l = getValue(bw, "losses_bedwars");
        bl = getValue(bw, "beds_lost_bedwars");
        ws = getValue(bw, "winstreak");

        if (fd != 0) fkdr = (double) fk / (double) fd;
        else fkdr = fk;
        fkdr = (double) Math.round(fkdr * 100) / 100;

        if (l != 0) wlr = (double) w / (double) l;
        else wlr = w;
        wlr = (double) Math.round(wlr * 100) / 100;

        if (bl != 0) bblr = (double) bb / (double) bl;
        else bblr = bb;
        bblr = (double) Math.round(bblr * 100) / 100;

        UChat.chat("§9------------------------------------------");
        UChat.chat(getFormattedRank(star) + " " + Player);
        UChat.chat("FKDR: " + fkdr);
        UChat.chat("Final kills: " + fk);
        UChat.chat("WLR: " + wlr);
        UChat.chat("Wins: " + w);
        UChat.chat("BBLR: " + bblr);
        UChat.chat("Beds: " + bb);
        if(ws != -1) UChat.chat("Winstreak: " + ws);
        UChat.chat("§9------------------------------------------");
    }

    private int getValue(JsonObject type, String member) {
        try {
            return type.get(member).getAsInt();
        } catch (NullPointerException er) {
            if (member.equals("winstreak")) return -1;
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
        FIRE("§e[_§6__§c_§l⚝§4]"),
        THREEONE("§9[_§3__§60✥§e]"),
        THREETWO("§c[§4_§7__§4_§c✥]"),
        THREETHREE("§9[__§d_§6_✥§d]"),
        THREEFOUR("§2[_§d__§5_✥§2]"),
        THREEFIVE("§c[_§4__§2_§a✥]"),
        THREESIX("§a[__§b_§9_✥§1]"),
        THREESEVEN("§4[_§c__§b_§3✥]"),
        THREEEIGHT("§1[_§9_§5__§d✥§1]"),
        THREENINE("§c[_§a__§3_§9✥]"),
        FOURZERO("§5[_§c__§6_✥§e]"),
        FOURONE("§e[_§6_§c_§d_✥§5]"),
        FOURTWO("§1[§9_§3_§b_§f_§7✥]"),
        FOURTHREE("§0[§5_§8__§5_✥§0]"),
        FOURFOUR("§2[_§a_§e_§6_§5✥§d]"),
        FOURFIVE("§f[_§b__§3_✥]"),
        FOURSIX("§3[§b_§e__§6_§d✥§5]"),
        FOURSEVEN("§f[§4_§c__§9_§1✥§9]"),
        FOUREIGHT("§5[_§c_§6_§e_§b✥§3]"),
        FOURNINE("§2[§a_§f__§a_✥§2]"),
        FIVEZERO("§4[_§5_§9__§1✥§0]");

        private final String format;

        Rank(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }
    }

    public String getFormattedRank(int star) {
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
        else if (number < 3200) return Rank.THREEONE;
        else if (number < 3300) return Rank.THREETWO;
        else if (number < 3400) return Rank.THREETHREE;
        else if (number < 3500) return Rank.THREEFOUR;
        else if (number < 3600) return Rank.THREEFIVE;
        else if (number < 3700) return Rank.THREESIX;
        else if (number < 3800) return Rank.THREESEVEN;
        else if (number < 3900) return Rank.THREEEIGHT;
        else if (number < 4000) return Rank.THREENINE;
        else if (number < 4100) return Rank.FOURZERO;
        else if (number < 4200) return Rank.FOURONE;
        else if (number < 4300) return Rank.FOURTWO;
        else if (number < 4400) return Rank.FOURTHREE;
        else if (number < 4500) return Rank.FOURFOUR;
        else if (number < 4600) return Rank.FOURFIVE;
        else if (number < 4700) return Rank.FOURSIX;
        else if (number < 4800) return Rank.FOURSEVEN;
        else if (number < 4900) return Rank.FOUREIGHT;
        else if (number < 5000) return Rank.FOURNINE;
        else if (number < 5100) return Rank.FIVEZERO;
        return Rank.RAINBOW;
    }
}
