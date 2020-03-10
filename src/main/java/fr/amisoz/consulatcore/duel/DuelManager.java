package fr.amisoz.consulatcore.duel;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuelManager {

    public static List<Arena> arenas = new ArrayList<>();
    public static HashMap<String, Arena> askedDuels = new HashMap<>();
    public static List<String> removeAnnounces = new ArrayList<>();

    public DuelManager(){
        Arena firstArena = new Arena(new Location(Bukkit.getWorlds().get(0), 505, 64, -678, -25, 0), new Location(Bukkit.getWorlds().get(0), 515, 64, -649, 150, 0), new Location(Bukkit.getWorlds().get(0), 511, 76, -663, -90, 0));
        registerArena(firstArena);
    }

    private void registerArena(Arena arena){
        arenas.add(arena);
    }

}
