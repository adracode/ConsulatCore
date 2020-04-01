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
            corePlayer.timeLeft = resultSet.getLong("timeLeft");
        } else {
            PreparedStatement insertFly = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO fly(uuid, canFly, flyTime, lastTime, timeLeft) VALUES (?, 0, 0, 0, 0)");
            insertFly.setString(1, player.getUniqueId().toString());
            insertFly.executeUpdate();
            insertFly.close();
        }
        resultSet.close();
        preparedStatement.close();
    }

    public void saveFly(Player player, long lastTime, long timeLeft) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET lastTime=?, timeLeft = ? WHERE uuid=?");
        preparedStatement.setLong(1, lastTime);
        preparedStatement.setLong(2, timeLeft);
        preparedStatement.setString(3, player.getUniqueId().toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void updateFlyTime(Player player, Long flyTime) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE fly SET flyTime = ?,canFly=1,timeLeft=? WHERE uuid=?");
        preparedStatement.setLong(1, flyTime);
        preparedStatement.setLong(2, flyTime);
        preparedStatement.setString(3, player.getUniqueId().toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
