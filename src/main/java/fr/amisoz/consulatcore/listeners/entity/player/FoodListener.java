package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodListener implements Listener {

    @EventHandler
    public void onFood(FoodLevelChangeEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        if(corePlayer.isModerate()){
            event.setCancelled(true);
        }
    }
}
