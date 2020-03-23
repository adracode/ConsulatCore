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

    public void flyInitialize(Player player) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM fly WHERE uuid=?");
        preparedStatement.setString(1, player.getUniqueId().toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
            corePlayer.canFly = resultSet.getBoolean("canFly");
            corePlayer.flyTime = resultSet.getLong("flyTime");
            corePlayer.lastTime = resultSet.getLong("lastTime");
        } else {
            PreparedStatement insertFly = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO fly(uuid, canFly, flyTime, lastTime) VALUES (?, ?, ?, ?)");
            insertFly.setString(1, player.getUniqueId().toString());
            insertFly.setBoolean(2, CoreManagerPlayers.getCorePlayer(player).canFly);
            insertFly.setLong(3, CoreManagerPlayers.getCorePlayer(player).flyTime);
            insertFly.setLong(4, CoreManagerPlayers.getCorePlayer(player).lastTime);
            insertFly.executeUpdate();
            insertFly.close();
        }
        resultSet.close();
        preparedStatement.close();
    }

    public void setLastTime(Player player, long lastTime) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET lastTime=? WHERE uuid=?");
        preparedStatement.setLong(1, lastTime);
        preparedStatement.setString(2, player.getUniqueId().toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void updateFlyTime(Player player, Long flyTime) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET flyTime = ?,canFly=1 WHERE uuid=?");
        preparedStatement.setLong(1, flyTime);
        preparedStatement.setString(2, player.getUniqueId().toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
