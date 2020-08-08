package fr.amisoz.consulatcore.zones.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.chunks.CChunk;
import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.amisoz.consulatcore.events.ClaimChangeEvent;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.claimlist.ClaimsGui;
import fr.amisoz.consulatcore.guis.claims.ManageClaimGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.utils.ChestUtils;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.events.PlayerClickBlockEvent;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ClaimManager implements Listener {
    
    public static final Set<Material> PROTECTABLE = Collections.unmodifiableSet(EnumSet.of(
            Material.CHEST,
            Material.TRAPPED_CHEST));
    private static ClaimManager instance;
    
    static{
        new ClaimManager();
    }
    
    public static boolean isKey(ItemStack item){
        if(item == null || item.getType() != Material.TRIPWIRE_HOOK || !item.hasItemMeta()){
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().equals("§6[§7Clé§6]");
    }
    
    public static ItemStack getKey(){
        ItemStack item = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6[§7Clé§6]");
        item.setItemMeta(meta);
        return item;
    }
    
    private ChunkManager chunkManager = ChunkManager.getInstance();
    private final World world;
    
    private ClaimManager(){
        if(instance == null){
            instance = this;
        }
        new ManageClaimGui.Container();
        world = Bukkit.getWorld("world");
        if(world == null){
            ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Couldn't find base world");
            Bukkit.shutdown();
        }
        loadClaims();
    }
    
    private void loadClaims(){
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "Loading chunks...");
        long start = System.currentTimeMillis();
        ZoneManager zoneManager = ZoneManager.getInstance();
        int size = 0;
        try {
            PreparedStatement getClaims = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM claims;");
            ResultSet resultClaims = getClaims.executeQuery();
            while(resultClaims.next()){
                String stringUUID = resultClaims.getString("player_uuid");
                if(stringUUID == null){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Player UUID id null at claim x=" + resultClaims.getInt("claim_x") + ", z=" + resultClaims.getInt("claim_z") + " in claim table");
                    continue;
                }
                UUID uuid = UUID.fromString(stringUUID);
                Zone claimOwner = zoneManager.getZone(uuid);
                if(claimOwner == null){
                    zoneManager.addZone(claimOwner = new Zone(uuid, Bukkit.getOfflinePlayer(uuid).getName(), uuid));
                    claimOwner.loadNBT();
                }
                int x = resultClaims.getInt("claim_x"), z = resultClaims.getInt("claim_z");
                String description = resultClaims.getString("description");
                long coords = CChunk.convert(x, z);
                CChunk chunk = chunkManager.getChunk(world, coords);
                if(chunk instanceof Claim){
                    Claim claim = (Claim)chunk;
                    claim.setOwner(claimOwner);
                    claim.setDescription(description);
                } else {
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Database Claim (x=" + x + ", z=" + z + ") is not referenced as a Claim");
                    addClaim(x, z, claimOwner, description);
                }
                ++size;
            }
            resultClaims.close();
            getClaims.close();
            if(!new File(ConsulatAPI.getConsulatAPI().getDataFolder(), "chunks").exists()){
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
                        removeAccessesDatabase(resultAllowedPlayers.getInt("claim_x"), resultAllowedPlayers.getInt("claim_z"));
                        ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Claim is null at x=" + resultAllowedPlayers.getInt("claim_x") + ", z=" + resultAllowedPlayers.getInt("claim_z") + " in access table");
                        continue;
                    }
                    claim.addPlayer(UUID.fromString(uuid));
                }
                resultAllowedPlayers.close();
                getAllowedPlayers.close();
            }
        } catch(SQLException e){
            ConsulatAPI.getConsulatAPI().log(Level.SEVERE, "Erreur lors de l'initialisation des claims");
            e.printStackTrace();
            Bukkit.shutdown();
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, size + " Claims loaded in " + (System.currentTimeMillis() - start) + " ms");
    }
    
    private Claim addClaim(int x, int z, Zone owner, String description){
        Claim claim = new Claim(x, z, owner, description);
        chunkManager.addChunk(world, claim);
        return claim;
    }
    
    public void removeClaim(Zone claimOwner){
        for(Claim claim : new ArrayList<>(claimOwner.getZoneClaims())){
            removeClaim(claim);
        }
    }
    
    private void removeClaim(Claim claim){
        chunkManager.removeChunk(world, claim, true);
        Zone owner = claim.getOwner();
        owner.removeClaim(claim);
        if(owner instanceof City){
            IGui iClaimsGui = GuiManager.getInstance().getContainer("city").getGui(false, owner, CityGui.CLAIMS);
            if(iClaimsGui != null){
                ((ClaimsGui)iClaimsGui).removeItemClaim(claim);
            }
        }
        GuiManager.getInstance().getContainer("claim").removeGui(claim);
    }
    
    public Claim playerClaim(int x, int z, Zone zone){
        return claim(x, z, zone);
    }
    
    public Claim cityClaim(int x, int z, City city){
        Claim claim = getClaim(x, z);
        if(claim != null && claim.getOwner().getClass() == Zone.class){
            claim.setOwner(city);
            changeOwner(x, z, claim);
            return claim;
        }
        claim = claim(x, z, city);
        IGui iClaimsGui = GuiManager.getInstance().getContainer("city").getGui(false, city, CityGui.CLAIMS);
        if(iClaimsGui != null){
            ((ClaimsGui)iClaimsGui).addItemClaim(claim);
        }
        return claim;
    }
    
    private Claim claim(int x, int z, Zone owner){
        Claim claim = addClaim(x, z, owner, null);
        addClaimDatabase(x, z, owner);
        return claim;
    }
    
    public Claim unClaim(Claim claim){
        removeClaimDatabase(claim.getX(), claim.getZ());
        removeClaim(claim);
        return claim;
    }
    
    public boolean isClaimed(Chunk chunk){
        return getClaim(chunk) != null;
    }
    
    public @Nullable Claim getClaim(Block block){
        return getClaim(block.getChunk());
    }
    
    public @Nullable Claim getClaim(Chunk chunk){
        if(chunk.getWorld() != Bukkit.getWorlds().get(0)){
            return null;
        }
        return getClaim(chunk.getX(), chunk.getZ());
    }
    
    public @Nullable Claim getClaim(int x, int z){
        return getClaim(CChunk.convert(x, z));
    }
    
    public @Nullable Claim getClaim(long coords){
        CChunk chunk = chunkManager.getChunk(world, coords);
        return chunk instanceof Claim ? (Claim)chunk : null;
    }
    
    @EventHandler
    public void setChestPrivate(PlayerClickBlockEvent event){
        ItemStack itemInHand = event.getItemInHand();
        if(!isKey(itemInHand)){
            return;
        }
        if(!PROTECTABLE.contains(event.getBlock().getType())){
            return;
        }
        event.setCancelled(true);
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        Claim claim = getClaim(event.getBlock());
        if(claim == null || !player.belongsToCity() || !player.getCity().equals(claim.getOwner())){
            event.getPlayer().sendActionBar("§cCe claim n'appartiens pas à ta ville");
            return;
        }
        if(!claim.canInteract(player, ClaimPermission.OPEN_CONTAINER)){
            event.getPlayer().sendActionBar("§cTu ne peux pas mettre un coffre en privé ici");
            return;
        }
        if(ShopManager.getInstance().getPlayerShop(event.getBlock().getLocation()) != null){
            event.getPlayer().sendActionBar("§cTu ne peux pas mettre un shop en privé");
            return;
        }
        if(!claim.protectContainer(CoordinatesUtils.convertCoordinates(event.getBlock().getLocation()), event.getPlayer().getUniqueId())){
            event.getPlayer().sendActionBar("§cCe coffre est déjà privé");
            return;
        }
        Block chestClicked = event.getBlock();
        Block nextChest = ChestUtils.getNextChest(chestClicked);
        if(ChestUtils.isDoubleChest((Chest)chestClicked.getState())){
            if(nextChest != null){
                claim.protectContainer(CoordinatesUtils.convertCoordinates(nextChest.getLocation()), event.getPlayer().getUniqueId());
            }
        } else {
            if(nextChest != null && !nextChest.getLocation().equals(chestClicked.getLocation())){
                UUID nextChestOwner = claim.getProtectedContainer(CoordinatesUtils.convertCoordinates(nextChest.getLocation()));
                if(event.getPlayer().getUniqueId().equals(nextChestOwner)){
                    ChestUtils.setChestDouble(chestClicked, nextChest);
                }
            }
        }
        itemInHand.setAmount(itemInHand.getAmount() - 1);
        event.getPlayer().sendActionBar("§aCe coffre est maintenant privé");
    }
    
    public void setDescription(Claim claim, String description) throws SQLException{
        claim.setDescription(description);
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE claims SET description = ? WHERE claim_x = ? AND claim_z = ?;");
        preparedStatement.setString(1, description);
        preparedStatement.setInt(2, claim.getX());
        preparedStatement.setInt(3, claim.getZ());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    private void addClaimDatabase(final int x, final int z, final Zone owner){
        addClaimDatabase(x, z, owner, null);
    }
    
    private void addClaimDatabase(final int x, final int z, final Zone owner, final Runnable onError){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO claims (claim_x, claim_z, player_uuid) VALUES (?, ?, ?);");
                preparedStatement.setInt(1, x);
                preparedStatement.setInt(2, z);
                preparedStatement.setString(3, owner.getUniqueId().toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch(SQLException e){
                e.printStackTrace();
                if(onError != null){
                    onError.run();
                }
            }
        });
    }
    
    private void changeOwner(final int x, final int z, final Claim claim){
        changeOwner(x, z, claim, null);
    }
    
    private void changeOwner(final int x, final int z, final Claim claim, final Runnable onError){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE claims SET player_uuid = ? WHERE claim_x = ? AND claim_z = ?;");
                statement.setString(1, claim.getOwner().getUniqueId().toString());
                statement.setInt(2, x);
                statement.setInt(3, z);
                statement.executeUpdate();
            } catch(SQLException e){
                e.printStackTrace();
                if(onError != null){
                    onError.run();
                }
            }
        });
    }
    
    private void removeClaimDatabase(int x, int z){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM claims WHERE claim_x = ? AND claim_z = ?;");
                preparedStatement.setInt(1, x);
                preparedStatement.setInt(2, z);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
    }
    
    private void removeAccessesDatabase(int x, int z){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM access WHERE claim_x = ? AND claim_z = ?;");
                preparedStatement.setInt(1, x);
                preparedStatement.setInt(2, z);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
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
            Bukkit.getPluginManager().callEvent(new ClaimChangeEvent((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId()), getClaim(chunkFrom), getClaim(chunkTo)));
        }
    }
    
    @EventHandler
    public void chunkChangeEvent(ClaimChangeEvent event){
        SurvivalPlayer player = event.getPlayer();
        Claim claimFrom = event.getClaimFrom();
        Claim claimTo = event.getClaimTo();
        if(claimTo == null && claimFrom != null){
            player.sendMessage(claimFrom.getOwner().getLeaveMessage());
            return;
        }
        if(claimTo != null){
            if(claimFrom == null || !claimFrom.isOwner(claimTo.getOwner())){
                player.sendMessage(claimTo.getOwner().getEnterMessage());
            }
            String description = claimTo.getDescription();
            if(description != null)
                if(claimFrom != null && !description.equals(claimFrom.getDescription())){
                    player.sendMessage(Text.PREFIX + "§7" + description);
                } else if(claimFrom == null){
                    player.sendMessage(Text.PREFIX + "§7" + description);
                }
        }
    }
    
    public static ClaimManager getInstance(){
        return instance;
    }
}
