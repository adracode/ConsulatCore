package fr.amisoz.consulatcore.listeners.entity.player;

import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceListener implements Listener {

    @EventHandler
    public void onExp(PlayerExpChangeEvent event){
        int amount = event.getAmount();
        Player player = event.getPlayer();
        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
        RankEnum playerRank = consulatPlayer.getRank();
        if(amount > 0 && playerRank.getRankPower() >= RankEnum.MECENE.getRankPower()){
            double result = amount * 1.1;
            player.giveExp((int) result);
        }
    }

}
