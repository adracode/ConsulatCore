package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceListener implements Listener {

    @EventHandler
    public void onExp(PlayerExpChangeEvent event){
        int amount = event.getAmount();
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(amount > 0 && player.hasPower(Rank.MECENE)){
            double result = amount * 1.1;
            player.getPlayer().giveExp((int) result);
        }
    }

}
