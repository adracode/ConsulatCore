package fr.amisoz.consulatcore.claims;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Claim {
    
    public static final double BUY_CLAIM = 180;
    public static final double REFUND = BUY_CLAIM * 0.7;
    
    private static final int SHIFT = 25 - 4; //Max coordonnées MC - Taille du chunk
    private static final int LIMIT_X = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int LIMIT_Z = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int CONVERT = (1 << SHIFT + 1) - 1; //1111111111111111111111
    
    private long coords;
    private UUID owner;
    private String description;
    private Set<UUID> allowedPlayers = new HashSet<>();
    
    Claim(int x, int z, UUID owner, String description){
        setCoords(x, z);
        this.owner = owner;
        this.description = description;
    }
    
    private void setCoords(int x, int z){
        if(x < -LIMIT_X || x > LIMIT_X || z < -LIMIT_Z || z > LIMIT_Z){
            throw new IllegalArgumentException("Les coordonnées d'un chunk ne peuvent dépasse les limites");
        }
        coords = Claim.convert(x, z);
    }
    
    public int getX(){
        return (int)((coords & CONVERT) - LIMIT_X);
    }
    
    public int getZ(){
        return (int)((coords >> SHIFT + 1) - LIMIT_Z);
    }
    
    public UUID getOwner(){
        return owner;
    }
    
    public boolean isOwner(UUID uuid){
        return owner.equals(uuid);
    }
    
    public String getDescription(){
        return description;
    }
    
    public long getCoordinates(){
        return coords;
    }
    
    public void addPlayer(UUID uuid){
        allowedPlayers.add(uuid);
    }
    
    public void removePlayer(UUID uuid){
        allowedPlayers.remove(uuid);
    }
    
    public boolean isAllowed(SurvivalPlayer player){
        return isAllowed(player.getPlayer().getUniqueId()) || player.hasPower(Rank.MODPLUS);
    }
    
    public boolean isAllowed(UUID uuid){
        return isOwner(uuid) || allowedPlayers.contains(uuid);
    }
    
    public void setDescription(String description) throws SQLException{
        ClaimManager.getInstance().setDescriptionDatabase(this, description);
        this.description = description;
    }
    
    public Set<UUID> getAllowedPlayers(){
        return Collections.unmodifiableSet(allowedPlayers);
    }
    
    public String getOwnerName(){
        return Bukkit.getOfflinePlayer(this.getOwner()).getName();
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim)o;
        return coords == claim.coords;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    public boolean isOwner(ConsulatPlayer player){
        return isOwner(player.getUUID());
    }
    
    public static long convert(int x, int z){
        return (((long)z + LIMIT_Z) << SHIFT + 1) | (x + LIMIT_X);
    }
    
}
