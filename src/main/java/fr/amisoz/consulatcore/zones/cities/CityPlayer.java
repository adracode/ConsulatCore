package fr.amisoz.consulatcore.zones.cities;

import fr.amisoz.consulatcore.players.CityPermission;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CityPlayer {
    
    @NotNull private UUID uuid;
    @NotNull private Set<CityPermission> permissions;
    @NotNull private CityRank rank;
    
    public CityPlayer(@NotNull UUID uuid, @NotNull CityRank rank){
        this(uuid, new HashSet<>(), rank);
    }
    
    CityPlayer(@NotNull UUID uuid, @NotNull Set<CityPermission> permissions, @NotNull CityRank rank){
        this.uuid = uuid;
        this.permissions = permissions;
        this.rank = rank;
    }
    
    public boolean addPermission(CityPermission... permission){
        return permissions.addAll(Arrays.asList(permission));
    }
    
    public boolean removePermission(CityPermission... permission){
        return permissions.removeAll(Arrays.asList(permission));
    }
    
    public boolean hasPermission(CityPermission permission){
        return permissions.contains(permission);
    }
    
    public @NotNull CityRank getRank(){
        return rank;
    }
    
    public void setRank(@NotNull CityRank rank){
        this.rank = rank;
    }
    
    public @NotNull Set<CityPermission> getPermissions(){
        return Collections.unmodifiableSet(permissions);
    }
    
    public UUID getUUID(){
        return uuid;
    }
    
    public void clearPermissions(){
        permissions.clear();
    }
}
