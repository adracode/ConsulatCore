package fr.amisoz.consulatcore.zones.cities;

import fr.amisoz.consulatcore.players.CityPermission;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CityRank implements Comparable<CityRank> {
    
    private final int id;
    @NotNull private String rankName;
    @NotNull private Set<CityPermission> defaultPermissions = new HashSet<>(1);
    @NotNull private ChatColor color;
    
    public CityRank(int id, @NotNull String rankName, @NotNull ChatColor color, CityPermission... defaultPermissions){
        this.id = id;
        this.rankName = rankName;
        this.color = color;
        this.defaultPermissions.addAll(Arrays.asList(defaultPermissions));
    }
    
    public boolean hasPermission(CityPermission permission){
        return defaultPermissions.contains(permission);
    }
    
    public boolean addPermission(CityPermission permission){
        return defaultPermissions.add(permission);
    }
    
    public boolean removePermission(CityPermission permission){
        return defaultPermissions.remove(permission);
    }
    
    @Override
    public int compareTo(@NotNull CityRank o){
        return Integer.compare(id, o.id);
    }
    
    @NotNull
    public String getRankName(){
        return rankName;
    }
    
    public void setRankName(@NotNull String rankName){
        this.rankName = rankName;
    }
    
    public int getId(){
        return id;
    }
    
    public Set<CityPermission> getDefaultPermissions(){
        return Collections.unmodifiableSet(defaultPermissions);
    }
    
    @NotNull
    public ChatColor getColor(){
        return color;
    }
}
