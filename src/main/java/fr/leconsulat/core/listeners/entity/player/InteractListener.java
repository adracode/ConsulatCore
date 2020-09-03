package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class InteractListener implements Listener {
    
    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(event.getClickedBlock() != null){
            if(event.getClickedBlock().getType() == Material.SPAWNER){
                event.setCancelled(true);
            }
        }
        if(event.getItem() == null || event.getItem().getItemMeta() == null){
            return;
        }
        ItemStack item = event.getItem();
        ItemMeta itemMeta = item.getItemMeta();
        if(player.isInModeration()){
            if(itemMeta.getDisplayName().contains("Se téléporter aléatoirement")){
                List<Player> online = (List<Player>)Bukkit.getOnlinePlayers();
                if(online.size() == 1){
                    player.sendMessage(Text.YOUR_ALONE);
                    event.setCancelled(true);
                    return;
                }
                int random = ConsulatCore.getRandom().nextInt(online.size());
                Player resultPlayer = online.get(random);
                if(resultPlayer.getUniqueId().equals(player.getUUID())){
                    resultPlayer = random == 0 ? online.get(1) : online.get(random - 1);
                }
                player.getPlayer().teleportAsync(resultPlayer.getLocation());
                player.sendMessage(Text.YOU_TELEPORTED_TO(resultPlayer.getName()));
                event.setCancelled(true);
            }
            ConsulatCore core = ConsulatCore.getInstance();
            if(itemMeta.getDisplayName().contains("Changer son statut d'invisibilité")){
                if(player.isVanished()){
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(core, player.getPlayer()));
                    player.setVanished(false);
                    player.sendMessage(Text.NOW_VISIBLE);
                    for(PotionEffect effect : player.getPlayer().getActivePotionEffects()){
                        if(effect.getType().equals(PotionEffectType.INVISIBILITY))
                            player.getPlayer().removePotionEffect(effect.getType());
                    }
                } else {
                    player.setVanished(true);
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        if(onlinePlayer != player.getPlayer()){
                            ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                            if(!consulatPlayer.hasPower(Rank.MODO)){
                                onlinePlayer.hidePlayer(core, player.getPlayer());
                            }
                        }
                    });
                    player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                    player.sendMessage(Text.NOW_INVISIBLE);
                }
            }
        }
    }
    
    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event){
        if(!(event.getRightClicked() instanceof Player)){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        Player target = (Player)event.getRightClicked();
        if(!player.isInModeration()){
            return;
        }
        ItemStack itemStack = player.getPlayer().getInventory().getItemInMainHand();
        if(itemStack.getItemMeta() == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.getDisplayName().contains("Voir l'inventaire")){
            Inventory inventory = target.getInventory();
            player.getPlayer().openInventory(inventory);
        }
        if(itemMeta.getDisplayName().contains("Freeze") && event.getHand().equals(EquipmentSlot.HAND)){
            SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(target.getUniqueId());
            if(survivalPlayer.isFrozen()){
                target.sendMessage(Text.BEEN_UNFROZEN);
                player.sendMessage(Text.PLAYER_UNFREEZE);
            } else {
                target.sendMessage(Text.BEEN_FROZEN);
                player.sendMessage(Text.PLAYER_FREEZE);
            }
            survivalPlayer.setFrozen(!survivalPlayer.isFrozen());
        }
    }
    
}
