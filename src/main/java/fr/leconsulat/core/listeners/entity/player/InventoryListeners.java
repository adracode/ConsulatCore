package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.guis.shop.admin.AdminShopGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class InventoryListeners implements Listener {
    
    @EventHandler
    public void onPick(EntityPickupItemEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(player == null){
            return;
        }
        if(player.getCurrentlyOpen() instanceof AdminShopGui){
            player.getCurrentlyOpen().refresh(player);
        }
    }
}
