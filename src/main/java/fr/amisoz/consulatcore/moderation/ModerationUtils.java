package fr.amisoz.consulatcore.moderation;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ModerationUtils {

    public static final String MODERATION_PREFIX = ChatColor.DARK_GREEN + "(Staff)" + ChatColor.GRAY + "[" + ChatColor.GOLD + "Modération" + ChatColor.GRAY +"] ";
    public static final String ANNOUNCE_PREFIX = ChatColor.GRAY + "§l[" + ChatColor.GOLD + "Modération" + ChatColor.GRAY +"§l]§r";

    public static final String BROADCAST_PREFIX = ChatColor.RED + "§l[ANNONCE] ";
    public static List<Player> moderatePlayers = new ArrayList<>();
    public static List<Player> vanishedPlayers = new ArrayList<>();

}
