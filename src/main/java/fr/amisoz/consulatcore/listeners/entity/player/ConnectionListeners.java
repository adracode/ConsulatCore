package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
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

        RankEnum playerRank;
        try {
            playerRank = PlayersManager.getConsulatPlayer(player).getRank();

            CoreManagerPlayers.initializePlayer(player, new CorePlayer());
            consulatCore.getModerationDatabase().setMute(player);

            if(playerRank.getRankPower() < RankEnum.MODO.getRankPower()) {
                ModerationUtils.vanishedPlayers.forEach(moderator -> player.hidePlayer(consulatCore, moderator));
            }

            if(playerRank.getRankPower() >= RankEnum.MODO.getRankPower()){
                event.setJoinMessage(null);
            }else{
                event.setJoinMessage(ChatColor.GRAY + "(" + ChatColor.GREEN + "+" + ChatColor.GRAY + ")" + playerRank.getRankColor() + " [" + playerRank.getRankName() + "] " + player.getName());
            }

            saveConnection(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.kickPlayer(ChatColor.RED + "Erreur lors de la récupération de vos données.\n" + e.getMessage());
        }

        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        corePlayer.lastMove = System.currentTimeMillis();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        RankEnum playerRank = PlayersManager.getConsulatPlayer(player).getRank();

        if (corePlayer.isModerate()) {
            player.getInventory().clear();
        }

        if (playerRank.getRankPower() >= RankEnum.MODO.getRankPower()) {
            event.setQuitMessage("");
        }else{
            event.setQuitMessage(ChatColor.GRAY + "(" + ChatColor.RED + "-" + ChatColor.GRAY + ")" + playerRank.getRankColor() + " [" + playerRank.getRankName() + "] " + player.getName());
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
}
