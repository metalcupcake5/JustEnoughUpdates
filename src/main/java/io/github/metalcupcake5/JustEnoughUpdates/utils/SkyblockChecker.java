package io.github.metalcupcake5.JustEnoughUpdates.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

/*
    adapted from https://github.com/Kraineff/Skyblocker/ because i cannot code
 */

public class SkyblockChecker {
    public static boolean inSkyblock = false;
    public static boolean inDwarvenMines = false;
    public static String location = "";
    public static String time = "";
    public static List<Commission> commissions = new ArrayList<>();

    public static void check() {
        List<String> sidebar = getSidebar();

        if (sidebar.isEmpty()) return;
        if (sidebar.get(sidebar.size() - 1).equals("www.hypixel.net")) {
            inSkyblock = sidebar.get(0).contains("SKYBLOCK");

            if(inSkyblock) {
                ArrayList<String> locationArray = new ArrayList<>(Arrays.asList(sidebar.get(4).split(" ")));
                locationArray.remove(0);
                locationArray.remove(0);
                location = String.join(" ", locationArray);
                inDwarvenMines = !SkyblockLocations.getLocationFromName(location).equals("Unknown");
                commissions = inDwarvenMines ? getCommissions(getTabList()) : new ArrayList<>();

                ArrayList<String> timeArray = new ArrayList<>(Arrays.asList(sidebar.get(3).split(" ")));
                if (!timeArray.isEmpty()) time = timeArray.get(1);
            }
        } else {
            inSkyblock = false;
            inDwarvenMines = false;
        }
    }

    private static List<String> getSidebar() {
        List<String> lines = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return lines;

        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) return lines;
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(1);
        if (sidebar == null) return lines;

        Collection<ScoreboardPlayerScore> scores = scoreboard.getAllPlayerScores(sidebar);
        List<ScoreboardPlayerScore> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (ScoreboardPlayerScore score : scores) {
            Team team = scoreboard.getPlayerTeam(score.getPlayerName());
            if (team == null) return lines;
            String text = team.getPrefix().getString() + team.getSuffix().getString();
            if (text.trim().length() > 0)
                lines.add(text);
        }

        lines.add(sidebar.getDisplayName().getString());
        Collections.reverse(lines);

        return lines;
    }

    private static List<PlayerListEntry> getTabList(){
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player.networkHandler.getPlayerList().stream().toList();
    }

    private static List<Commission> getCommissions(List<PlayerListEntry> players){
        MinecraftClient client = MinecraftClient.getInstance();
        List<Commission> comms = new ArrayList<>();
        for (PlayerListEntry player : players) {
            Text name = client.inGameHud.getPlayerListHud().getPlayerName(player);
            List<Text> list = name.getSiblings();
            if(!list.isEmpty() && list.size() > 2) {
                if(Commissions.isCommission(list.get(1).asString())){
                    String commission = list.get(1).asString().trim();
                    String completion = list.get(2).asString();
                    comms.add(new Commission(commission.substring(0, commission.length() - 1), completion, SkyblockLocations.getLocationFromName(location).equals("Dwarven Mines") ? 0 : 1));
                }
            }

        }
        return comms;
    }

}