package fr.amisoz.consulatcore.players;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CoreManagerPlayers {

    private static Map<Player, CorePlayer> players = new HashMap<>();

    public static CorePlayer getCorePlayer(Player player){
        return players.get(player);
    }

    public static void initializePlayer(Player player, CorePlayer corePlayer){ players.put(player, corePlayer); }
}
