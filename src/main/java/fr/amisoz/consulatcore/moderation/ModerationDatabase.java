package fr.amisoz.consulatcore.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class ModerationDatabase {

    private Connection connection;

    public ModerationDatabase(ConsulatCore consulatCore) {
        this.connection = consulatCore.getDatabaseConnection();
    }

    public void addSanction(String playerUUID, String playerName, Player moderator, String sanctionType, String reason, Long expireMillis, Long applicationMillis) throws SQLException {
        PreparedStatement request = connection.prepareStatement("INSERT INTO antecedents(playeruuid, playername, modname, moduuid, sanction, reason, expire, applicated, cancelled, active) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        request.setString(1, playerUUID);
        request.setString(2, playerName);
        request.setString(3, moderator.getName());
        request.setString(4, moderator.getUniqueId().toString());
        request.setString(5, sanctionType);
        request.setString(6, reason);
        request.setLong(7, expireMillis);
        request.setLong(8, applicationMillis);
        request.setBoolean(9, false);
        request.setBoolean(10, true);
        request.executeUpdate();
    }

    public void setMute(Player player) throws SQLException {
        PreparedStatement request = connection.prepareStatement("SELECT * FROM antecedents WHERE playeruuid = ? AND sanction = 'MUTE' AND active = '1'");
        request.setString(1, player.getUniqueId().toString());
        ResultSet resultSet = request.executeQuery();
        if(resultSet.next()){
            long expireMute = resultSet.getLong("expire");
            if(System.currentTimeMillis() >= expireMute){
                PreparedStatement unban = connection.prepareStatement("UPDATE antecedents SET active = '0' WHERE sanction = 'MUTE' AND playeruuid = ?");
                unban.setString(1, player.getUniqueId().toString());
                unban.executeUpdate();

            }else{
                CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
                corePlayer.isMuted = true;
                corePlayer.muteReason = resultSet.getString("reason");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(expireMute);
                calendar.add(Calendar.HOUR_OF_DAY, 2);
                corePlayer.muteExpireMillis = calendar.getTimeInMillis();
            }
        }
    }

    public void unmute(String playerName){
        try {
            PreparedStatement unbanRequest = connection.prepareStatement("UPDATE antecedents SET active = '0', cancelled = '1' WHERE sanction = 'MUTE' AND playername = ? AND active = '1'");
            unbanRequest.setString(1, playerName);
            unbanRequest.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unban(String playerName){
        try {
            PreparedStatement unbanRequest = connection.prepareStatement("UPDATE antecedents SET active = '0', cancelled = '1' WHERE sanction = 'BAN' AND playername = ? AND active = '1'");
            unbanRequest.setString(1, playerName);
            unbanRequest.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
