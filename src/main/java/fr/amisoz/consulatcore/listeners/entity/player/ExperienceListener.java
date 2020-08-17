package fr.amisoz.consulatcore.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExperienceListener implements Listener {
    
    @EventHandler
    public void onExp(PlayerExpChangeEvent event){
        ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player == null){
            return;
        }
        int amount = event.getAmount();
        if(amount > 0 && player.hasPower(Rank.MECENE)){
            event.setAmount((int)(amount * 1.1));
        }
    }
    
}
