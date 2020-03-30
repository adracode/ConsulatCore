package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ConnectionListeners implements Listener {

    private ConsulatCore consulatCore;

    public ConnectionListeners(ConsulatCore consulatCore) {
        this.consulatCore = consulatCore;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(!player.hasPlayedBefore()){
            player.teleport(ConsulatCore.spawnLocation);
            player.getInventory().addItem(new ItemStack(Material.BREAD, 32));
        }

        try {

            CoreManagerPlayers.initializePlayer(player, new CorePlayer());
            consulatCore.getModerationDatabase().setMute(player);
            ConsulatPlayer consulatPlayer =  PlayersManager.getConsulatPlayer(player);
            RankEnum playerRank = consulatPlayer.getRank();

            getHomes(player);

            if(playerRank.getRankPower() < RankEnum.MODO.getRankPower()) {
                ModerationUtils.vanishedPlayers.forEach(moderator -> player.hidePlayer(consulatCore, moderator));
            }

            if(playerRank.getRankPower() >= RankEnum.MODO.getRankPower()){
                event.setJoinMessage(null);
            }else {
                if(consulatPlayer.isPerso() && consulatPlayer.getPersoPrefix() != null && !consulatPlayer.getPersoPrefix().equalsIgnoreCase("")){
                    event.setJoinMessage(ChatColor.GRAY + "(" + ChatColor.GREEN + "+" + ChatColor.GRAY + ") " + ChatColor.translateAlternateColorCodes('&', consulatPlayer.getPersoPrefix()) + player.getName());
                } else {
                    event.setJoinMessage(ChatColor.GRAY + "(" + ChatColor.GREEN + "+" + ChatColor.GRAY + ")" + playerRank.getRankColor() + " [" + playerRank.getRankName() + "] " + player.getName());
                }
            }

            saveConnection(player);

            setPerks(player);
        } catch (SQLException e) {
            e.printStackTrace();
            player.kickPlayer(ChatColor.RED + "Erreur lors de la récupération de vos données.\n" + e.getMessage());
        }

        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        corePlayer.lastMove = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
            try {
                ConsulatCore.INSTANCE.getFlySQL().flyInitialize(player);
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Erreur lors du chargement du fly.");
            }
        });

    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
        if(consulatPlayer == null) return;
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        RankEnum playerRank = consulatPlayer.getRank();


        if(corePlayer.isFreezed){
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                ConsulatPlayer consulatOnline = PlayersManager.getConsulatPlayer(onlinePlayer);
                if(consulatOnline != null && consulatOnline.getRank().getRankPower() >= RankEnum.MODO.getRankPower()){
                    onlinePlayer.sendMessage(ModerationUtils.MODERATION_PREFIX + ChatColor.GOLD + player.getName() + ChatColor.RED + " s'est déconnecté en étant freeze.");
                }
            });
        }

        if (corePlayer.isModerate()) {
            player.getInventory().clear();
            player.getInventory().setContents(corePlayer.stockedInventory);
        }

        if (playerRank.getRankPower() >= RankEnum.MODO.getRankPower()) {
            event.setQuitMessage("");
        }else{
            if(consulatPlayer.isPerso() && consulatPlayer.getPersoPrefix() != null && !consulatPlayer.getPersoPrefix().equalsIgnoreCase("")){
                event.setQuitMessage(ChatColor.GRAY + "(" + ChatColor.RED + "-" + ChatColor.GRAY + ") " + ChatColor.translateAlternateColorCodes('&', consulatPlayer.getPersoPrefix()) + player.getName());
            }else{
                event.setQuitMessage(ChatColor.GRAY + "(" + ChatColor.RED + "-" + ChatColor.GRAY + ")" + playerRank.getRankColor() + " [" + playerRank.getRankName() + "] " + player.getName());
            }
        }

        if(FlyManager.flyMap.containsKey(player)){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {

                    long startFly = FlyManager.flyMap.get(player);
                    long timeLeft = corePlayer.timeLeft - (System.currentTimeMillis() - startFly) / 1000;

                    ConsulatCore.INSTANCE.getFlySQL().saveFly(player, System.currentTimeMillis(), timeLeft);
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Erreur lors de la sauvegarde du fly.");
                }
            });
        }
    }

    private void saveConnection(Player player) throws SQLException {
        PreparedStatement preparedStatement = consulatCore.getDatabaseConnection().prepareStatement("INSERT INTO connections(player_name, player_id, player_ip, connection_date) VALUES(?, ?, ?, ?)");
        preparedStatement.setString(1, player.getName());
        preparedStatement.setInt(2, PlayersManager.getConsulatPlayer(player).getIdPlayer());
        preparedStatement.setString(3, Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress());
        DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT, new Locale("FR", "fr"));
        preparedStatement.setString(4, shortDateFormat.format(new Date()));

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void setPerks(Player player) throws SQLException {
        PreparedStatement preparedStatement = consulatCore.getDatabaseConnection().prepareStatement("SELECT canUp FROM players WHERE player_uuid = ?");
        preparedStatement.setString(1, player.getUniqueId().toString());

        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()){
            CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
            corePlayer.canUp = resultSet.getBoolean("canUp");
        }

        resultSet.close();
        preparedStatement.close();
    }

    private void getHomes(Player player) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM homes INNER JOIN players ON players.id = homes.idplayer WHERE players.player_name = ?");
        preparedStatement.setString(1, player.getName());

        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()) {
            Location homeLocation = new Location(Bukkit.getWorlds().get(0), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));
            CoreManagerPlayers.getCorePlayer(player).homes.put(resultSet.getString("home_name"), homeLocation);
        }

        resultSet.close();
        preparedStatement.close();
    }
}
