package fr.amisoz.consulatcore.duel;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class DuelManager {

    public static List<Arena> arenas = new ArrayList<>();

    public DuelManager(){
        Arena firstArena = new Arena(new Location(Bukkit.getWorlds().get(0), 56, 70, -261), new Location(Bukkit.getWorlds().get(0), 57, 70, -270), new Location(Bukkit.getWorlds().get(0), 57, 80, -265));
        registerArena(firstArena);
    }

    private void registerArena(Arena arena){
        arenas.add(arena);
    }

}
