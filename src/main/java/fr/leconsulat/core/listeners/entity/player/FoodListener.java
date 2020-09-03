package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodListener implements Listener {
    
    @EventHandler
    public void onFood(FoodLevelChangeEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(player == null){
            return;
        }
        if(player.isInModeration()){
            event.setCancelled(true);
        }
    }
}
