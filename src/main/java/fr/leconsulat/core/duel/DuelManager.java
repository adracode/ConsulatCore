package fr.leconsulat.core.duel;

import fr.leconsulat.core.ConsulatCore;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuelManager {
    
    public static List<Arena> arenas = new ArrayList<>();
    public static HashMap<String, Arena> askedDuels = new HashMap<>();
    public static List<String> removeAnnounces = new ArrayList<>();
    
    public DuelManager(){
        Arena firstArena = new Arena(new Location(ConsulatCore.getInstance().getOverworld(), 505, 64, -678, -25, 0), new Location(ConsulatCore.getInstance().getOverworld(), 515, 64, -649, 150, 0), new Location(ConsulatCore.getInstance().getOverworld(), 511, 76, -663, -90, 0));
        registerArena(firstArena);
    }
    
    private void registerArena(Arena arena){
        arenas.add(arena);
    }
    
}
