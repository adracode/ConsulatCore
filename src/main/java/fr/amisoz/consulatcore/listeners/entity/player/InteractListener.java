package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    
    @SuppressWarnings("unchecked")
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(event.getClickedBlock() != null){
            if(event.getClickedBlock().getType() == Material.SPAWNER){
                event.setCancelled(true);
            }
            if(event.getClickedBlock().getType() == Material.OAK_WALL_SIGN){
                Sign sign = (Sign)event.getClickedBlock().getState();
                String[] lines = sign.getLines();
                if(lines[0].equals("§9[Téléportation]")){
                    try {
                        int x = Integer.parseInt(lines[1]);
                        int y = Integer.parseInt(lines[2]);
                        int z = Integer.parseInt(lines[3]);
                        Location result = new Location(Bukkit.getWorlds().get(0), x, y, z);
                        player.getPlayer().teleport(result);
                        player.sendMessage("§aTu as été téléporté à la zone.");
                    } catch(NumberFormatException e){
                        player.sendMessage("§cErreur de coordonnées");
                    }
                }
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
                    player.sendMessage(ChatColor.RED + "Tu es seul.... désolé");
                    event.setCancelled(true);
                    return;
                }
                int random = ConsulatCore.getRandom().nextInt(online.size());
                Player resultedPlayer = online.get(random);
                if(resultedPlayer.getUniqueId().equals(player.getUUID())){
                    resultedPlayer = random == 0 ? online.get(1) : online.get(random - 1);
                }
                player.getPlayer().teleport(resultedPlayer);
                player.sendMessage(ChatColor.GREEN + "Tu as été téléporté à : " + resultedPlayer.getName());
                event.setCancelled(true);
            }
            if(itemMeta.getDisplayName().contains("Changer son statut d'invisibilité")){
                if(ModerationUtils.vanishedPlayers.contains(player.getPlayer())){
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(consulatCore, player.getPlayer()));
                    ModerationUtils.vanishedPlayers.remove(player.getPlayer());
                    player.sendMessage("§aTu es désormais visible.");
                    for(PotionEffect effect : player.getPlayer().getActivePotionEffects()){
                        if(effect.getType().equals(PotionEffectType.INVISIBILITY))
                            player.getPlayer().removePotionEffect(effect.getType());
                    }
                } else {
                    ModerationUtils.vanishedPlayers.add(player.getPlayer());
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        if(onlinePlayer != player.getPlayer()){
                            ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                            if(!consulatPlayer.hasPower(Rank.MODO)){
                                onlinePlayer.hidePlayer(consulatCore, player.getPlayer());
                            }
                        }
                    });
                    player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false));
                    player.sendMessage("§cTu es désormais invisible.");
                }
            }
        }
    }
    
    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event){
        if(!(event.getRightClicked() instanceof Player)){
            return;
        }
        Player player = event.getPlayer();
        Player target = (Player)event.getRightClicked();
        if(!ModerationUtils.moderatePlayers.contains(player)){
            return;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(itemStack.getItemMeta() == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.getDisplayName().contains("Voir l'inventaire")){
            Inventory inventory = target.getInventory();
            player.openInventory(inventory);
        }
        if(itemMeta.getDisplayName().contains("Freeze") && event.getHand().equals(EquipmentSlot.HAND)){
            SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(target.getUniqueId());
            if(survivalPlayer.isFrozen()){
                target.sendMessage(Text.ANNOUNCE_PREFIX + " Tu as été un-freeze.");
                player.sendMessage(Text.ANNOUNCE_PREFIX + " Joueur un-freeze");
            } else {
                target.sendMessage(Text.ANNOUNCE_PREFIX + " Tu as été freeze par un modérateur.");
                player.sendMessage(Text.ANNOUNCE_PREFIX + " Joueur freeze");
            }
            survivalPlayer.setFrozen(!survivalPlayer.isFrozen());
        }
    }
    
}
