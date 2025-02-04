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
        } catch (NullPointerException e) {
            UChat.chat("Player is nicked");
            return;
        }

        JsonObject profile, ach, bw;
        int star, fk, bb, w, ws;
        double fkdr, wlr, bblr;
        connection = newConnection("https://api.hypixel.net/player?key=" + ModConfig.api + "&uuid=" + uuid);
        profile = getStringAsJson(connection).getAsJsonObject("player");
        ach = profile.getAsJsonObject("achievements");
        bw = profile.getAsJsonObject("stats").getAsJsonObject("Bedwars");

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

        String color = "§7";
        String monthly = getString(profile, "monthlyRankColor");

        if(Player.equals("Technoblade")) color = "§d";
        else if (special.equals("YOUTUBER")) color = "§c";
        else if (special.equals("ADMIN")) color = "§c";
        else if (special.equals("GAME_MASTER")) color = "§2";
        else if (rank.equals("MVP_PLUS") && monthly.equals("AQUA"))  color = "§b";
        else if (rank.equals("MVP_PLUS") && monthly.equals("GOLD")) color = "§6";
        else if (rank.equals("MVP")) color = "§b";
        else if (rank.equals("VIP_PLUS")) color = "§a";
        else if (rank.equals("VIP")) color = "§a";

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

        UChat.chat("----------------------------------------------------");
        UChat.chat("[" + star + "✫] " + color + Player);
        UChat.chat("FKDR: " + fkdr);
        UChat.chat("Final kills: " + fk);
        UChat.chat("WLR: " + wlr);
        UChat.chat("Wins: " + w);
        UChat.chat("BBLR: " + bblr);
        UChat.chat("Beds: " + bb);
        UChat.chat("----------------------------------------------------");
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
}
