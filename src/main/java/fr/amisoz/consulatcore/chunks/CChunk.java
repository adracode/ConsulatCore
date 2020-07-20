package fr.amisoz.consulatcore.chunks;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class CChunk implements Comparable<CChunk> {
    
    private static final int SHIFT = 25 - 4; //Max coordonnées MC - Taille du chunk
    private static final int LIMIT_X = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int LIMIT_Z = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int CONVERT = (1 << SHIFT + 1) - 1; //1111111111111111111111
    
    public static long convert(int x, int z){
        return (((long)z + LIMIT_Z) << SHIFT + 1) | (x + LIMIT_X);
    }
    
    private long coords;
    private Map<Material, Integer> limits = new EnumMap<>(Material.class);
    
    protected void setCoords(int x, int z){
        if(x < -LIMIT_X || x > LIMIT_X || z < -LIMIT_Z || z > LIMIT_Z){
            throw new IllegalArgumentException("Les coordonnées d'un chunk ne peuvent dépasse les limites");
        }
        coords = CChunk.convert(x, z);
    }
    
    public long getCoordinates(){
        return coords;
    }
    
    public int getX(){
        return (int)((coords & CONVERT) - LIMIT_X);
    }
    
    public int getZ(){
        return (int)((coords >> SHIFT + 1) - LIMIT_Z);
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof CChunk)){
            return false;
        }
        CChunk cChunk = (CChunk)o;
        return coords == cChunk.coords;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    @Override
    public int compareTo(@NotNull CChunk o){
        return Long.compare(this.coords, o.coords);
    }
    
    @Override
    public String toString(){
        return "CChunk{" +
                "coords=" + coords +
                '}';
    }
    
}
