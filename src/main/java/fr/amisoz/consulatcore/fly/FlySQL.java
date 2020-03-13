package fr.amisoz.consulatcore.fly;

import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.ConsulatAPI;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by KIZAFOX on 13/03/2020 for ConsulatCore
 */
public class FlySQL {

    public void createTable(){
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("CREATE TABLE IF NOT EXISTS fly(" +
                    "`#` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(255), " +
                    "canFly BOOL, " +
                    "duration LONG)");
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertInFly(Player player){
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT uuid FROM fly WHERE uuid='" + player.getUniqueId().toString() + "'");
            ResultSet rs = sts.executeQuery();

            if(!rs.next()){
                sts.close();
                sts = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO fly(uuid, canFly, duration) VALUES (?, ?, ?)");
                sts.setString(1, player.getUniqueId().toString());
                sts.setBoolean(2, CoreManagerPlayers.getCorePlayer(player).canFly);
                sts.setLong(3, CoreManagerPlayers.getCorePlayer(player).flyDuration);
                sts.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveInformations(Player player){
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        boolean canFly = corePlayer.canFly;
        int duration = corePlayer.flyDuration;

        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET canFly=?, duration=? WHERE uuid=?");
            sts.setBoolean(1, canFly);
            sts.setLong(2, duration);
            sts.setString(3, player.getUniqueId().toString());
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParams(String uuid, boolean fly, int duration) {
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET canFly=?, duration=? WHERE uuid=?");
            sts.setBoolean(1, fly);
            sts.setLong(2, duration);
            sts.setString(3, uuid);
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canFly(Player player){
        boolean canFly = CoreManagerPlayers.getCorePlayer(player).canFly;
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT canFly FROM fly WHERE uuid=?");
            sts.setString(1, player.getUniqueId().toString());
            ResultSet rs = sts.executeQuery();

            if(rs.next()){
                canFly = rs.getBoolean("canFly");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return !canFly;
    }

    public int getDuration(Player player) {
        int duration = 0;
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT duration FROM fly WHERE uuid=?");
            sts.setString(1, player.getUniqueId().toString());
            ResultSet rs = sts.executeQuery();

            if(rs.next()){
                duration = rs.getInt("duration");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return duration;
    }

    public void updateDuration(Player player) {
        try {
            PreparedStatement rs = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET duration = duration - ? WHERE uuid=?");
            rs.setDouble(1, 1);
            rs.setString(2, player.getUniqueId().toString());
            rs.executeUpdate();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setDuration(Player player, int duration) {
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET duration=? WHERE uuid=?");
            sts.setInt(1, duration);
            sts.setString(3, player.getUniqueId().toString());
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
