package fr.amisoz.consulatcore.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.events.ChunkChangeEvent;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ClaimManager implements Listener {
    
    private static ClaimManager instance;
    
    private Map<Long, Claim> claims = new HashMap<>();
    private Map<UUID, Set<Claim>> claimsByUUID = new HashMap<>();
    
    public ClaimManager(){
        if(instance != null){
            return;
        }
        instance = this;
        loadClaims();
    }
    
    private void loadClaims(){
        long start = System.currentTimeMillis();
        try {
            PreparedStatement getClaims = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM claims;");
            ResultSet resultClaims = getClaims.executeQuery();
            while(resultClaims.next()){
                String uuid = resultClaims.getString("player_uuid");
                if(uuid == null){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Player UUID id null at claim x=" + resultClaims.getInt("claim_x") + ", z=" + resultClaims.getInt("claim_z") + " in claim table");
                    continue;
                }
                addClaim(resultClaims.getInt("claim_x"),
                        resultClaims.getInt("claim_z"),
                        UUID.fromString(uuid),
                        resultClaims.getString("description"));
            }
            resultClaims.close();
            getClaims.close();
            PreparedStatement getAllowedPlayers = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM access;");
            ResultSet resultAllowedPlayers = getAllowedPlayers.executeQuery();
            while(resultAllowedPlayers.next()){
                String uuid = resultAllowedPlayers.getString("player_uuid");
                if(uuid == null){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Player UUID id null at claim x=" + resultAllowedPlayers.getInt("claim_x") + ", z=" + resultClaims.getInt("claim_z") + " in access table");
                    continue;
                }
                Claim claim = getClaim(resultAllowedPlayers.getInt("claim_x"),
                        resultAllowedPlayers.getInt("claim_z"));
                if(claim == null){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Claim is null at x=" + resultAllowedPlayers.getInt("claim_x") + ", z=" + resultAllowedPlayers.getInt("claim_z") + " in access table");
                    continue;
                }
                claim.addPlayer(UUID.fromString(uuid));
            }
            resultAllowedPlayers.close();
            getAllowedPlayers.close();
        } catch(SQLException e){
            ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Erreur lors de l'initialisation des claims");
            e.printStackTrace();
            Bukkit.shutdown();
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "Chunks loaded in " + (System.currentTimeMillis() - start) + " ms");
    }
    
    private Claim addClaim(int x, int z, UUID owner, String description){
        Claim claim = new Claim(x, z, owner, description);
        this.claims.put(claim.getCoordinates(), claim);
        this.claimsByUUID.computeIfAbsent(owner, k -> new HashSet<>()).add(claim);
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(owner);
        if(player != null){
            player.addClaim(claim);
        }
        return claim;
    }
    
    private void removeClaim(Claim claim){
        this.claims.remove(claim.getCoordinates());
        this.claimsByUUID.get(claim.getOwner()).remove(claim);
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(claim.getOwner());
        if(player != null){
            player.removeClaim(claim);
        }
    }
    
    public Claim claim(int x, int z, UUID uuid) throws SQLException{
        addClaimDatabase(x, z, uuid);
        return addClaim(x, z, uuid, null);
    }
    
    public void unClaim(int x, int z) throws SQLException{
        Claim claim = getClaim(x, z);
        removeClaimDatabase(claim.getX(), claim.getZ());
        removeAccessesDatabase(claim.getX(), claim.getZ());
        removeClaim(claim);
    }
    
    public boolean hasClaim(UUID owner){
        return claimsByUUID.containsKey(owner);
    }
    
    public Set<Claim> getClaims(UUID uuid){
        Set<Claim> claims = claimsByUUID.get(uuid);
        if(claims == null){
            return null;
        }
        return Collections.unmodifiableSet(claims);
    }
    
    public boolean isClaimed(Chunk chunk){
        return getClaim(chunk) != null;
    }
    
    public Claim getClaim(Chunk chunk){
        if(chunk.getWorld() != Bukkit.getWorlds().get(0)){
            return null;
        }
        return getClaim(chunk.getX(), chunk.getZ());
    }
    
    public Claim getClaim(int x, int z){
        return getClaim(Claim.convert(x, z));
    }
    
    public Claim getClaim(long coords){
        return this.claims.get(coords);
    }
    
    public void giveAccess(Claim claim, UUID uuid) throws SQLException{
        addAccessDatabase(claim, uuid);
        claim.addPlayer(uuid);
    }
    
    public void giveAccesses(UUID owner, UUID target) throws SQLException{
        Set<Claim> claims = getClaims(owner);
        if(claims == null){
            return;
        }
        addAccessesDatabase(owner, target);
        for(Claim claim : claims){
            claim.addPlayer(target);
        }
    }
    
    public void removeAccess(Claim claim, UUID uuid) throws SQLException{
        removeAccessDatabase(claim, uuid);
        claim.removePlayer(uuid);
    }
    
    public void removeAccesses(UUID owner, UUID target) throws SQLException{
        Set<Claim> claims = getClaims(owner);
        if(claims == null){
            return;
        }
        removeAccessesDatabase(owner, target);
        for(Claim claim : claims){
            claim.removePlayer(target);
        }
    }
    
    public void setDescriptionDatabase(Claim claim, String description) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE claims SET description = ? WHERE claim_x = ? AND claim_z = ?;");
        preparedStatement.setString(1, description);
        preparedStatement.setInt(2, claim.getX());
        preparedStatement.setInt(3, claim.getZ());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    private void addClaimDatabase(int x, int z, UUID uuid) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO claims (claim_x, claim_z, player_uuid) VALUES (?, ?, ?);");
        preparedStatement.setInt(1, x);
        preparedStatement.setInt(2, z);
        preparedStatement.setString(3, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    private void removeClaimDatabase(int x, int z) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM claims WHERE claim_x = ? AND claim_z = ?;");
        preparedStatement.setInt(1, x);
        preparedStatement.setInt(2, z);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    private void addAccessDatabase(Claim claim, UUID uuid) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO access (claim_x, claim_z, player_uuid) VALUES (?, ?, ?)");
        preparedStatement.setInt(1, claim.getX());
        preparedStatement.setInt(2, claim.getZ());
        preparedStatement.setString(3, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    private void addAccessesDatabase(UUID owner, UUID target) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO access (claim_x, claim_z, player_uuid) VALUES (?, ?, ?)");
        for(Claim claim : getClaims(owner)){
            preparedStatement.setInt(1, claim.getX());
            preparedStatement.setInt(2, claim.getZ());
            preparedStatement.setString(3, target.toString());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }
    
    private void removeAccessDatabase(Claim claim, UUID uuid) throws SQLException{
        PreparedStatement idStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM access WHERE claim_x = ? AND claim_z = ? AND player_uuid = ?");
        idStatement.setInt(1, claim.getX());
        idStatement.setInt(2, claim.getZ());
        idStatement.setString(3, uuid.toString());
        idStatement.executeUpdate();
    }
    
    private void removeAccessesDatabase(int x, int z) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM access WHERE claim_x = ? AND claim_z = ?;");
        preparedStatement.setInt(1, x);
        preparedStatement.setInt(2, z);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    private void removeAccessesDatabase(UUID owner, UUID target) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM access WHERE claim_x = ? AND claim_z = ? AND player_uuid = ?");
        for(Claim claim : getClaims(owner)){
            preparedStatement.setInt(1, claim.getX());
            preparedStatement.setInt(2, claim.getZ());
            preparedStatement.setString(3, target.toString());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }
    
    @EventHandler
    public void enterLeaveZone(PlayerMoveEvent event){
        Player player = event.getPlayer();
        if(player.getWorld() != Bukkit.getWorlds().get(0)){
            return;
        }
        Chunk chunkFrom = event.getFrom().getChunk();
        Chunk chunkTo = event.getTo().getChunk();
        if(chunkFrom != chunkTo){
            Bukkit.getPluginManager().callEvent(new ChunkChangeEvent(player, chunkFrom, chunkTo));
        }
    }
    
    @EventHandler
    public void chunkChangeEvent(ChunkChangeEvent event){
        Player player = event.getPlayer();
        Claim claimFrom = getClaim(event.getChunkFrom());
        Claim claimTo = getClaim(event.getChunkTo());
        if(claimTo == null && claimFrom != null){
            player.sendMessage(Text.PREFIX + "§cTu sors de la zone de §l" + claimFrom.getOwnerName() + ".");
            return;
        }
        if(claimTo != null){
            if(claimFrom == null || !claimFrom.isOwner(claimTo.getOwner())){
                player.sendMessage(Text.PREFIX + "§cTu entres dans la zone de §l" + claimTo.getOwnerName() + ".");
            }
            String description = claimTo.getDescription();
            if(description != null){
                player.sendMessage(Text.PREFIX + "§7" + description);
            }
        }
    }
    
    public static ClaimManager getInstance(){
        return instance;
    }
}
