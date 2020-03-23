package fr.amisoz.consulatcore.fly;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlyManager {

    public static List<Player> infiniteFly = new ArrayList<>();
    public static Map<Player, Long> flyMap = new HashMap<>();
    public static String flyPrefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "Fly" + ChatColor.GRAY + "] " + ChatColor.YELLOW;


}
