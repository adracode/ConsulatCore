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

    public void insertInFly(Player player){
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT uuid FROM fly WHERE uuid='" + player.getUniqueId().toString() + "'");
            ResultSet rs = sts.executeQuery();

            if(!rs.next()){
                sts.close();
                sts = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO fly(uuid, canFly, duration, flyTime, lastTime) VALUES (?, ?, ?, ?, ?)");
                sts.setString(1, player.getUniqueId().toString());
                sts.setBoolean(2, CoreManagerPlayers.getCorePlayer(player).canFly);
                sts.setLong(3, CoreManagerPlayers.getCorePlayer(player).flyDuration);
                sts.setLong(4, CoreManagerPlayers.getCorePlayer(player).flyTime);
                sts.setLong(5, CoreManagerPlayers.getCorePlayer(player).lastTime);
                sts.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveInformations(Player player){
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        boolean canFly = corePlayer.canFly;
        long duration = corePlayer.flyDuration;
        long flyTime = corePlayer.flyTime;
        long lastTime = corePlayer.lastTime;

        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET canFly=?, duration=?, flyTime=?, lastTime=? WHERE uuid=?");
            sts.setBoolean(1, canFly);
            sts.setLong(2, duration);
            sts.setLong(3, flyTime);
            sts.setLong(4, lastTime);
            sts.setString(5, player.getUniqueId().toString());
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setParams(String uuid, boolean fly, long duration, long flyTime, long lastTime) {
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET canFly=?, duration=?, flyTime=?, lastTime=? WHERE uuid=?");
            sts.setBoolean(1, fly);
            sts.setLong(2, duration);
            sts.setLong(3, flyTime);
            sts.setLong(4, lastTime);
            sts.setString(5, uuid);
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canFly(Player player){
        boolean canFly = false;
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
        return canFly;
    }

    public long getDuration(Player player) {
        long duration = 0;
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT duration FROM fly WHERE uuid=?");
            sts.setString(1, player.getUniqueId().toString());
            ResultSet rs = sts.executeQuery();

            if(rs.next()){
                duration = rs.getLong("duration");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return duration;
    }

    public void setDuration(Player player, long duration) {
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET duration=? WHERE uuid=?");
            sts.setLong(1, duration);
            sts.setString(2, player.getUniqueId().toString());
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getLastTime(Player player) {
        long lastTime = 0;
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT lastTime FROM fly WHERE uuid=?");
            sts.setString(1, player.getUniqueId().toString());
            ResultSet rs = sts.executeQuery();

            if(rs.next()){
                lastTime = rs.getLong("lastTime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastTime;
    }

    public void setLastTime(Player player, long lastTime) {
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET lastTime=? WHERE uuid=?");
            sts.setLong(1, lastTime);
            sts.setString(2, player.getUniqueId().toString());
            sts.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getFlyTime(Player player) {
        long flyTime = 0;
        try {
            PreparedStatement sts = ConsulatAPI.getDatabase().prepareStatement("SELECT flyTime FROM fly WHERE uuid=?");
            sts.setString(1, player.getUniqueId().toString());
            ResultSet rs = sts.executeQuery();

            if(rs.next()){
                flyTime = rs.getLong("flyTime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flyTime;
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
}
