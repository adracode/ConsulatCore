package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.guis.shop.admin.AdminShopGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListeners implements Listener {
    
    public InventoryListeners(ConsulatCore consulatCore){
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer == null){
            return;
        }
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPick(EntityPickupItemEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player)event.getEntity();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer == null){
            return;
        }
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
            return;
        }
        if(survivalPlayer.getCurrentlyOpen() instanceof AdminShopGui){
            survivalPlayer.getCurrentlyOpen().onOpen(new GuiOpenEvent(survivalPlayer));
        }
    }
    
    @EventHandler
    public void onDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player)){
            return;
        }
        Player player = (Player)event.getWhoClicked();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(!(event.getPlayer() instanceof Player)){
            return;
        }
        Player player = (Player)event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer != null){
            survivalPlayer.setLookingInventory(false);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player) ||
                event.getClickedInventory() == null ||
                event.getCurrentItem() == null){
            return;
        }
        SurvivalPlayer moderator = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getWhoClicked().getUniqueId());
        if(!moderator.hasPower(Rank.MODO)){
            return;
        }
        if(moderator.isLookingInventory() || moderator.isInModeration()){
            event.setCancelled(true);
        }
    }
}
