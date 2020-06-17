package fr.amisoz.consulatcore.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ModerationDatabase {
    
    private Connection connection;
    
    public ModerationDatabase(ConsulatCore consulatCore){
        this.connection = consulatCore.getDatabaseConnection();
    }
    
    public void addSanction(UUID uuid, String name, Player moderator, String sanctionType, String reason, Long expireMillis, Long applicationMillis) throws SQLException{
        PreparedStatement request = connection.prepareStatement("INSERT INTO antecedents(playeruuid, playername, modname, moduuid, sanction, reason, expire, applicated, cancelled, active) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        request.setString(1, uuid.toString());
        request.setString(2, name);
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
    
    public void setMute(Player player) throws SQLException{
        PreparedStatement request = connection.prepareStatement("SELECT * FROM antecedents WHERE playeruuid = ? AND sanction = 'MUTE' AND active = '1'");
        request.setString(1, player.getUniqueId().toString());
        ResultSet resultSet = request.executeQuery();
        if(resultSet.next()){
            long expireMute = resultSet.getLong("expire");
            if(System.currentTimeMillis() >= expireMute){
                PreparedStatement unmuteRequest = connection.prepareStatement("UPDATE antecedents SET active = '0' WHERE sanction = 'MUTE' AND playeruuid = ?");
                unmuteRequest.setString(1, player.getUniqueId().toString());
                unmuteRequest.executeUpdate();
                
            } else {
                SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
                survivalPlayer.setMuted(true);
                survivalPlayer.setMuteReason(resultSet.getString("reason"));
                survivalPlayer.setMuteExpireMillis(expireMute);
            }
        }
    }
    
    public void unmute(String playerName){
        try {
            PreparedStatement unmuteRequest = connection.prepareStatement("UPDATE antecedents SET active = '0', cancelled = '1' WHERE sanction = 'MUTE' AND playername = ? AND active = '1'");
            unmuteRequest.setString(1, playerName);
            unmuteRequest.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void unban(String playerName){
        try {
            PreparedStatement unbanRequest = connection.prepareStatement("UPDATE antecedents SET active = '0', cancelled = '1' WHERE sanction = 'BAN' AND playername = ? AND active = '1'");
            unbanRequest.setString(1, playerName);
            unbanRequest.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
}
