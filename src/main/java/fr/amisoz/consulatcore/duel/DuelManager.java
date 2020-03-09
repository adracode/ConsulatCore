package fr.amisoz.consulatcore.duel;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelManager {

    public static List<Arena> arenas = new ArrayList<>();
    public static Map<Player, Arena> askedDuels = new HashMap<>();

    public DuelManager(){
        Arena firstArena = new Arena(new Location(Bukkit.getWorlds().get(0), 505, 64, -678, -25, 0), new Location(Bukkit.getWorlds().get(0), 515, 64, -649, 150, 0), new Location(Bukkit.getWorlds().get(0), 498, 70, -663));
        registerArena(firstArena);
    }

    private void registerArena(Arena arena){
        arenas.add(arena);
    }

}
