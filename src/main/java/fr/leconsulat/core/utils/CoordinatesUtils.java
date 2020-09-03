package fr.leconsulat.core.utils;

import org.bukkit.Location;

public class CoordinatesUtils {
    
    private static final int SHIFT = 25; //Max coordonnées MC
    private static final int SHIFT_Y = 8; //Max y MC
    private static final int LIMIT_X = 1 << SHIFT; //33 554 432 > 30 000 000
    private static final int LIMIT_Y = 1 << SHIFT_Y; //256 > 255
    private static final int LIMIT_Z = 1 << SHIFT; //33 554 432 > 30 000 000
    private static final long CONVERT_Y = ((long)1 << SHIFT + SHIFT_Y + 1) - 1;
    private static final int CONVERT_X = (1 << SHIFT + 1) - 1;
    
    public static long convertCoordinates(Location location){
        return convertCoordinates(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    public static long convertCoordinates(int x, int y, int z){
        if(x < -LIMIT_X || x > LIMIT_X || y < 0 || y > LIMIT_Y || z < -LIMIT_Z || z > LIMIT_Z){
            throw new IllegalArgumentException("Les coordonnées d'un shop ne peuvent dépasse les limites");
        }
        return (((long)z + LIMIT_Z) << SHIFT + 1 + SHIFT_Y + 1) | ((long)y << SHIFT + 1) | (x + LIMIT_X);
    }
    
    public static int getX(long coords){
        return (int)((coords & CONVERT_X) - LIMIT_X);
    }
    
    public static int getY(long coords){
        return (int)((coords & CONVERT_Y) >> SHIFT + 1);
    }
    
    public static int getZ(long coords){
        return (int)((coords >> SHIFT + 1 + SHIFT_Y + 1) - LIMIT_Z);
    }
}
