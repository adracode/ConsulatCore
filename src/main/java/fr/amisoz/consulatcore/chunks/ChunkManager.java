package fr.amisoz.consulatcore.chunks;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.ConsulatAPI;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class ChunkManager {
    
    private static final ChunkManager instance = new ChunkManager();
    
    private final Map<Long, CChunk> claims = new HashMap<>();
    private Map<Material, Integer> limits = new EnumMap<>(Material.class);
    
    private ChunkManager(){
        FileConfiguration config = ConsulatCore.getInstance().getConfig();
        for(String limit : config.getStringList("block-limits")){
            int separator = limit.indexOf(':');
            if(separator == -1){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Separator ':' not found in limit config (" + limit + ")");
                continue;
            }
            Material material;
            try {
                material = Material.valueOf(limit.substring(0, separator));
            } catch(IllegalArgumentException | NullPointerException e){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Invalid block in limit config (" + limit + ")");
                continue;
            }
            int definedLimit;
            try {
                definedLimit = Integer.parseInt(limit.substring(separator + 1));
            } catch(NumberFormatException e){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Invalid number in limit config (" + limit + ")");
                continue;
            }
            limits.put(material, definedLimit);
        }
    }
    
    public CChunk getChunk(long coords){
        return claims.get(coords);
    }
    
    public void addChunk(CChunk chunk){
        claims.put(chunk.getCoordinates(), chunk);
    }
    
    public boolean removeChunk(CChunk chunk){
        return claims.remove(chunk.getCoordinates()) != null;
    }
    
    public int getMaxLimit(Material material){
        Integer max = limits.get(material);
        return max == null ? -1 : max;
    }
    
    public static ChunkManager getInstance(){
        return instance;
    }
    
    public Collection<CChunk> getChunks(){
        return Collections.unmodifiableCollection(claims.values());
    }
}
