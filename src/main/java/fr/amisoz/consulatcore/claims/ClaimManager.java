package fr.amisoz.consulatcore.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.claim.ChunkLoader;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ClaimManager {
    
    private static ClaimManager instance;
    
    private Map<Long, Claim> claims = new HashMap<>();
    
    public ClaimManager(){
        if(instance != null){
            return;
        }
        instance = this;
        loadClaims();
    }
    
    private void loadClaims(){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
            try {
                PreparedStatement getClaims = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM claims;");
                ResultSet resultClaims = getClaims.executeQuery();
                while(resultClaims.next()){
                    addClaim(resultClaims.getInt("claim_x"),
                            resultClaims.getInt("claim_z"),
                            UUID.fromString(resultClaims.getString("player_uuid")),
                            resultClaims.getString("description"));
                }
                resultClaims.close();
                getClaims.close();
                PreparedStatement getAllowedPlayers = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM access;");
                ResultSet resultAllowedPlayers = getAllowedPlayers.executeQuery();
                while(resultAllowedPlayers.next()){
                    getClaim(resultAllowedPlayers.getInt("claim_x"),
                            resultAllowedPlayers.getInt("claim_z"))
                            .addPlayer(UUID.fromString(resultAllowedPlayers.getString("player_uuid")));
                }
                resultAllowedPlayers.close();
                getAllowedPlayers.close();
            } catch(SQLException e){
                Bukkit.getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation des claims");
                e.printStackTrace();
                Bukkit.shutdown();
            }
        });
    }
    
    public Claim addClaim(int x, int z, UUID owner, String description){
        Claim claim = new Claim(x, z, owner, description);
        this.claims.put(claim.getCoordinates(), claim);
        return claim;
    }
    
    public void claim(int x, int z, UUID uuid) throws SQLException{
        Claim claim = new Claim(x, z, uuid);
        claim(claim);
        claims.put(claim.getCoordinates(), claim);
        //TODO: Ajouter le claim au joueur
    }
    
    private void claim(Claim claim) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO claims (claim_x, claim_z, player_uuid) VALUES (?, ?, ?);");
        preparedStatement.setInt(1, claim.getX());
        preparedStatement.setInt(2, claim.getZ());
        preparedStatement.setString(3, claim.getOwner().toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public boolean isClaimed(Chunk chunk){
        return getClaim(chunk) != null;
    }
    
    public Claim getClaim(Player player){
        Objects.requireNonNull(player);
        return getClaim(player.getLocation().getChunk());
    }
    
    public Claim getClaim(Chunk chunk){
        return getClaim(chunk.getX(), chunk.getZ());
    }
    
    public Claim getClaim(int x, int z){
        return getClaim((long)z << 32 | x);
    }
    
    public Claim getClaim(long coords){
        return this.claims.get(coords);
    }
    
    public static ClaimManager getInstance(){
        return instance;
    }
}
