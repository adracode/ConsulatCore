package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.events.blocks.PlayerInteractSignEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
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
    
    private ConsulatCore consulatCore;
    
    public InteractListener(ConsulatCore consulatCore){
        this.consulatCore = consulatCore;
    }
    
    @EventHandler
    public void onClickSign(PlayerInteractSignEvent event){
        Player player = event.getPlayer();
        Sign sign = (Sign)event.getBlock().getState();
        String[] lines = sign.getLines();
        if(lines[0].equals("§9[Téléportation]")){
            try {
                int x = Integer.parseInt(lines[1]);
                int y = Integer.parseInt(lines[2]);
                int z = Integer.parseInt(lines[3]);
                Location result = new Location(ConsulatCore.getInstance().getOverworld(), x, y, z);
                player.teleportAsync(result);
                player.sendMessage("§aTu as été téléporté à la zone.");
            } catch(NumberFormatException e){
                player.sendMessage("§cErreur de coordonnées");
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
            if(itemMeta.getDisplayName().contains("Changer son statut d'invisibilité")){
                if(player.isVanished()){
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(consulatCore, player.getPlayer()));
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
                                onlinePlayer.hidePlayer(consulatCore, player.getPlayer());
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
