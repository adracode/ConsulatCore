package fr.amisoz.consulatcore.claims;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Claim {
    
    private long coords;
    private UUID owner;
    private String description;
    private Set<UUID> allowedPlayers = new HashSet<>();
    
    public Claim(int x, int z, UUID owner){
        this(x, z, owner, null);
    }
    
    public Claim(int x, int z, UUID owner, String description){
        this.coords = (long)z << 32 | x;
        this.owner = owner;
        this.description = description;
    }
    
    public int getX(){
        return (int)coords;
    }
    
    public int getZ(){
        return (int)(coords >> 32);
    }
    
    public UUID getOwner(){
        return owner;
    }
    
    public String getDescription(){
        return description;
    }
    
    public long getCoordinates(){
        return coords;
    }
    
    public boolean addPlayer(UUID uuid){
        return allowedPlayers.add(uuid);
    }
    
    public boolean removePlayer(UUID uuid){
        return allowedPlayers.remove(uuid);
    }
    
    public boolean isAllowed(UUID uuid){
        return uuid.equals(owner) || allowedPlayers.contains(uuid);
    }
    
}
