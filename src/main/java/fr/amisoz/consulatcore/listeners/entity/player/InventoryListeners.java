package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class InventoryListeners implements Listener {
    
    public InventoryListeners(ConsulatCore consulatCore){
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPick(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player)) {
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
    
    //A refaire
    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player)event.getWhoClicked();
        
        if(event.getClickedInventory() == null) return;
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        SurvivalPlayer moderator = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(!moderator.hasPower(Rank.MODO)){
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        InventoryView inventoryView = event.getView();
        String inventoryName = inventoryView.getTitle();
        if(moderator.isLookingInventory()){
            event.setCancelled(true);
            return;
        }
        String targetName = moderator.getSanctionTarget();
        if(targetName == null){
            return;
        }
        UUID uuidTarget = CPlayerManager.getInstance().getPlayerUUID(targetName);
        if(uuidTarget == null){
            return;
        }
        ItemMeta itemMeta = clickedItem.getItemMeta();
        if(!(inventoryName.contains("Sanction")) && !(inventoryName.contains("Mute")) && !(inventoryName.contains("Bannir"))){
            if(moderator.isInModeration()){
                event.setCancelled(true);
            }
            return;
        }
        if(CPlayerManager.getInstance().getPlayerUUID(targetName) == null){
            player.sendMessage("§cLe joueur ne s'est jamais connecté au serveur.");
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetName);
        Long currentTime = System.currentTimeMillis();
    }
    
    
}
