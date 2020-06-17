package fr.amisoz.consulatcore.zones.cities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CityPlayer {

    private short accessClaims = 0;
    @NotNull private Set<String> permissions;
    @Nullable private CityRank rank;

    public CityPlayer(@Nullable CityRank rank){
        this(new HashSet<>(), rank);
    }

    public CityPlayer(@NotNull Set<String> permissions, @Nullable CityRank rank){
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

    @Nullable
    public CityRank getRank(){
        return rank;
    }

    @NotNull
    public Set<String> getPermissions(){
        return Collections.unmodifiableSet(permissions);
    }
    
    public short getAccessClaims(){
        return accessClaims;
    }
    
    public void addAccessClaims(int amount){
        accessClaims += amount;
    }
    
}
