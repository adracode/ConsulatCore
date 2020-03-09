package fr.amisoz.consulatcore.runnable;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class MeceneRunnable implements Runnable {

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour != 22) return;

        try {
            giveToMecenes();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
            if(consulatPlayer.getRank().equals(RankEnum.MECENE)){
                consulatPlayer.addMoney(100D);
            }
        });
    }

    private void giveToMecenes() throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET money = money + 100 WHERE player_rank = 'Mécène'");
        preparedStatement.executeUpdate();
    }
}
