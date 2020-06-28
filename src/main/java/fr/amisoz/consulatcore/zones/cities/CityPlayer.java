package fr.amisoz.consulatcore.zones.cities;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CityPlayer {
    
    @NotNull private UUID uuid;
    @NotNull private Set<String> permissions;
    @NotNull private CityRank rank;
    
    public CityPlayer(@NotNull UUID uuid, @NotNull CityRank rank){
        this(uuid, new HashSet<>(), rank);
    }
    
    CityPlayer(@NotNull UUID uuid, @NotNull Set<String> permissions, @NotNull CityRank rank){
        this.uuid = uuid;
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
    
    public UUID getUUID(){
        return uuid;
    }
}
