package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
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
        if(hour != 22) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), ()->{
            try {
                giveToMecenes();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        for(ConsulatPlayer player : CPlayerManager.getInstance().getConsulatPlayers()){
            if(player.getRank().equals(Rank.MECENE)){
                ((SurvivalPlayer)player).addMoney(100D);
            }
        }
    }

    /*
    * Si un mécène est connecté, il recevra l'argent en jeu ET en BDD,
    * mais l'argent en jeu surpasse l'argent BDD donc il ne devrait pas
    * y avoir de problèmes
    */
    private void giveToMecenes() throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET money = money + 100 WHERE player_rank = 'Mécène'");
        preparedStatement.executeUpdate();
    }
}
