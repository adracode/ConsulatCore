package fr.amisoz.consulatcore.zones.cities;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CityPlayer {
    
    @NotNull private Set<String> permissions;
    @NotNull private CityRank rank;
    
    public CityPlayer(@NotNull CityRank rank){
        this(new HashSet<>(), rank);
    }
    
    CityPlayer(@NotNull Set<String> permissions, @NotNull CityRank rank){
        this.permissions = permissions;
        this.rank = rank;
    }
    
    public boolean addPermission(String... permission){
        return permissions.addAll(Arrays.asList(permission));
    }
    
    public boolean removePermission(String... permission){
        return permissions.removeAll(Arrays.asList(permission));
    }
    
    public boolean hasPermission(String permission){
        return permissions.contains(permission);
    }
    
    public @NotNull CityRank getRank(){
        return rank;
    }
    
    public void setRank(@NotNull CityRank rank){
        this.rank = rank;
    }
    
    public @NotNull Set<String> getPermissions(){
        return Collections.unmodifiableSet(permissions);
    }
    
}
