package fr.amisoz.consulatcore.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.events.SurvivalPlayerLoadedEvent;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.events.ConsulatPlayerLeaveEvent;
import fr.leconsulat.api.events.ConsulatPlayerLoadedEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class SPlayerManager implements Listener {
    
    private static SPlayerManager instance;
    
    private DateFormat dateFormat;
    
    public SPlayerManager(){
        if(instance != null){
            return;
        }
        instance = this;
        dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT, new Locale("FR", "fr"));
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        if(!player.hasPlayedBefore()){
            player.teleport(ConsulatCore.getInstance().getSpawn());
            player.getInventory().addItem(new ItemStack(Material.BREAD, 32));
        }
    }
    
    @EventHandler
    public void onPlayerLoaded(ConsulatPlayerLoadedEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        ConsulatCore core = ConsulatCore.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                fetchPlayer(player);
                core.getModerationDatabase().setMute(player.getPlayer());
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    Bukkit.getServer().getPluginManager().callEvent(new SurvivalPlayerLoadedEvent(player));
                });
                saveConnection(player);
            } catch(SQLException e){
                e.printStackTrace();
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    player.getPlayer().kickPlayer("§cErreur lors de la récupération de vos données.\n" + e.getMessage());
                });
            }
        });
    }
    
    @EventHandler
    public void onSurvivalPlayerLoaded(SurvivalPlayerLoadedEvent event){
        SurvivalPlayer player = event.getPlayer();
        Rank playerRank = player.getRank();
        if(!player.hasPower(Rank.MODO)){
            ModerationUtils.vanishedPlayers.forEach(moderator -> player.getPlayer().hidePlayer(ConsulatCore.getInstance(), moderator));
            if(player.hasCustomRank() && player.getCustomRank() != null && !player.getCustomRank().isEmpty()){
                Bukkit.broadcastMessage("§7(§a+§7) " + ChatColor.translateAlternateColorCodes('&', player.getCustomRank()) + player.getName());
            } else {
                Bukkit.broadcastMessage("§7(§a+§7)" + playerRank.getRankColor() + " [" + playerRank.getRankName() + "] " + player.getName());
            }
        }
        for(Claim claim : ClaimManager.getInstance().getClaims(player.getUUID())){
            player.addClaim(claim);
        }
    }
    
    //TODO: Liste modo
    @EventHandler
    public void onLeave(ConsulatPlayerLeaveEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        if(player.isFrozen()){
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                ConsulatPlayer consulatOnline = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                if(consulatOnline != null && consulatOnline.hasPower(Rank.MODO)){
                    onlinePlayer.sendMessage(Text.MODERATION_PREFIX + ChatColor.GOLD + player.getPlayer().getName() + ChatColor.RED + " s'est déconnecté en étant freeze.");
                }
            });
        }
        if(player.isInModeration()){
            player.getPlayer().getInventory().clear();
            player.getPlayer().getInventory().setContents(player.getStockedInventory());
        }
        if(!player.hasPower(Rank.MODO)){
            if(player.hasCustomRank() && player.getCustomRank() != null && !player.getCustomRank().isEmpty()){
                Bukkit.broadcastMessage("§7(§c-§7) " + ChatColor.translateAlternateColorCodes('&', player.getCustomRank()) + player.getPlayer().getName());
            } else {
                Rank playerRank = player.getRank();
                Bukkit.broadcastMessage("§7(§c-§7)" + playerRank.getRankColor() + " [" + playerRank.getRankName() + "] " + player.getPlayer().getName());
            }
        }
        if(player.isFlying()){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    player.disableFly();
                } catch(SQLException e){
                    player.getPlayer().sendMessage("§cErreur lors de la sauvegarde du fly.");
                    e.printStackTrace();
                }
            });
        }
    }
    
    public void fetchPlayer(SurvivalPlayer player) throws SQLException{
        Map<String, Location> homes = getHomes(player.getName(), true);
        Fly fly = getFly(player.getUUID());
        List<Shop> shops = ShopManager.getInstance().getShops(player.getUUID());
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement(
                "SELECT * FROM players WHERE player_uuid = ?;");
        preparedStatement.setString(1, player.getUUID().toString());
        ResultSet result = preparedStatement.executeQuery();
        if(result.next()){
            player.initialize(
                    result.getDouble("money"),
                    result.getInt("moreHomes"),
                    result.getInt("shops"),
                    homes,
                    result.getBoolean("canUp"),
                    fly,
                    shops
            );
        } else {
            player.initialize(
                    400D,
                    0,
                    0,
                    null,
                    false,
                    null,
                    null
            );
        }
        result.close();
        preparedStatement.close();
    }
    
    private void saveConnection(ConsulatPlayer player) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO connections(player_name, player_id, player_ip, connection_date) VALUES(?, ?, ?, ?)");
        preparedStatement.setString(1, player.getName());
        preparedStatement.setInt(2, player.getId());
        preparedStatement.setString(3, Objects.requireNonNull(player.getPlayer().getAddress()).getAddress().getHostAddress());
        preparedStatement.setString(4, dateFormat.format(new Date()));
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public Map<String, Location> getHomes(String player, boolean getLocations) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM homes INNER JOIN players ON players.id = homes.idplayer WHERE players.player_name = ?");
        preparedStatement.setString(1, player);
        ResultSet resultSet = preparedStatement.executeQuery();
        Map<String, Location> result = new HashMap<>();
        while(resultSet.next()){
            result.put(resultSet.getString("home_name").toLowerCase(),
                    getLocations ?
                            new Location(Bukkit.getWorlds().get(0),
                                    resultSet.getDouble("x"),
                                    resultSet.getDouble("y"),
                                    resultSet.getDouble("z"),
                                    resultSet.getFloat("pitch"),
                                    resultSet.getFloat("yaw")
                            ) :
                            null);
        }
        resultSet.close();
        preparedStatement.close();
        return result;
    }
    
    public void addHome(SurvivalPlayer player, String homeName, Location location) throws SQLException{
        PreparedStatement preparedStatement =
                player.getHome(homeName) == null ?
                        ConsulatAPI.getDatabase().prepareStatement("INSERT INTO homes(x, y, z, pitch, yaw, idplayer, home_name) VALUES(?, ?, ?, ?, ?, ?, ?)") :
                        ConsulatAPI.getDatabase().prepareStatement("UPDATE homes SET x = ?, y = ?, z = ?, pitch = ?, yaw = ? WHERE idplayer = ? AND home_name = ?");
        preparedStatement.setDouble(1, location.getX());
        preparedStatement.setDouble(2, location.getY());
        preparedStatement.setDouble(3, location.getZ());
        preparedStatement.setFloat(4, location.getPitch());
        preparedStatement.setFloat(5, location.getYaw());
        preparedStatement.setInt(6, player.getId());
        preparedStatement.setString(7, homeName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public boolean removeHome(String name, String home) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE homes FROM homes INNER JOIN players ON players.id = homes.idplayer WHERE player_name = ? AND home_name = ?");
        preparedStatement.setString(1, name);
        preparedStatement.setString(1, home);
        int result = preparedStatement.executeUpdate();
        preparedStatement.close();
        return result != 0;
    }
    
    public boolean removeHome(UUID uuid, String home) throws SQLException{
        ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayer(uuid);
        PreparedStatement preparedStatement;
        if(player != null){
            preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM homes WHERE idplayer = ? AND home_name = ?");
            preparedStatement.setInt(1, player.getId());
            preparedStatement.setString(2, home);
        } else {
            preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE homes FROM homes INNER JOIN players ON players.id = homes.idplayer WHERE player_uuid = ? AND home_name = ?");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, home);
        }
        int result = preparedStatement.executeUpdate();
        preparedStatement.close();
        return result != 0;
    }
    
    public void addMoney(UUID uuid, double amount) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET money = money + ? WHERE player_uuid = ?");
        preparedStatement.setDouble(1, amount);
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public Optional<SurvivalOffline> fetchOffline(String playerName) throws SQLException{
        PreparedStatement request = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM players WHERE player_name = ?");
        request.setString(1, playerName);
        ResultSet resultSet = request.executeQuery();
        SurvivalOffline offline;
        if(resultSet.next()){
            offline = new SurvivalOffline(
                    resultSet.getInt("id"),
                    UUID.fromString(resultSet.getString("player_uuid")),
                    resultSet.getString("player_name"),
                    Rank.byName(resultSet.getString("player_rank")),
                    resultSet.getString("registered"),
                    resultSet.getDouble("money"));
        } else {
            return Optional.empty();
        }
        resultSet.close();
        request.close();
        PreparedStatement lastConnection = ConsulatAPI.getDatabase().prepareStatement(
                "SELECT connection_date FROM connections WHERE player_id = ? ORDER BY id DESC LIMIT 1");
        lastConnection.setInt(1, offline.getId());
        ResultSet resultLastConnection = lastConnection.executeQuery();
        if(resultLastConnection.next()){
            offline.setLastConnection(resultLastConnection.getString("connection_date"));
        }
        resultLastConnection.close();
        lastConnection.close();
        return Optional.of(offline);
    }
    
    
    public void setPerkUp(UUID uuid, boolean perkTop) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET canUp = ? WHERE player_uuid = ?");
        preparedStatement.setBoolean(1, perkTop);
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public void incrementLimitHome(UUID uuid) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET moreHomes = moreHomes + 1 WHERE player_uuid = ?");
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public Fly getFly(UUID uuid) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM fly WHERE uuid = ?");
        preparedStatement.setString(1, uuid.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        Fly fly;
        if(resultSet.next()){
            fly = new Fly(
                    (int)resultSet.getLong("flyTime"),
                    resultSet.getLong("lastTime"),
                    (int)resultSet.getLong("timeLeft"));
        } else {
            PreparedStatement insertFly = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO fly(uuid, canFly, flyTime, lastTime, timeLeft) VALUES (?, 0, 0, 0, 0)");
            insertFly.setString(1, uuid.toString());
            insertFly.executeUpdate();
            insertFly.close();
            fly = new Fly(0, 0, 0);
        }
        resultSet.close();
        preparedStatement.close();
        return fly.getFlyTime() == 0 ? null : fly;
    }
    
    public void setFly(UUID uuid, Fly fly) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET flyTime = ?, lastTime = ?, timeLeft = ? WHERE uuid = ?");
        preparedStatement.setLong(1, fly.getFlyTime());
        preparedStatement.setLong(2, fly.getReset());
        preparedStatement.setLong(3, fly.getTimeLeft());
        preparedStatement.setString(4, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public static SPlayerManager getInstance(){
        return instance;
    }
    
    public boolean hasAccount(String playerName) throws SQLException{
        PreparedStatement request = ConsulatAPI.getDatabase().prepareStatement("SELECT id FROM players WHERE player_name = ?");
        request.setString(1, playerName);
        ResultSet resultSet = request.executeQuery();
        boolean present = resultSet.next();
        resultSet.close();
        request.close();
        return present;
    }
    
}
