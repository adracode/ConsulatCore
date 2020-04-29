package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;

public class ModerateCommand extends ConsulatCommand {
    
    public ModerateCommand(){
        super("staff", "/staff", 0, Rank.MODO);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        Player bukkitPlayer = sender.getPlayer();
        player.setInModeration(!player.isInModeration());
        if(!player.isInModeration()){
            sender.sendMessage(Text.MODERATION_PREFIX + "§cTu n'es plus en mode modérateur.");
            ModerationUtils.moderatePlayers.remove(bukkitPlayer);
            ModerationUtils.vanishedPlayers.remove(bukkitPlayer);
            for(PotionEffect effect : bukkitPlayer.getActivePotionEffects()){
                if(effect.getType().equals(PotionEffectType.NIGHT_VISION) || effect.getType().equals(PotionEffectType.INVISIBILITY)){
                    bukkitPlayer.removePotionEffect(effect.getType());
                }
            }
            Bukkit.getOnlinePlayers().forEach(onlinePlayers -> onlinePlayers.showPlayer(ConsulatCore.getInstance(), bukkitPlayer));
            bukkitPlayer.getInventory().setContents(player.getStockedInventory());
            if(bukkitPlayer.getGameMode() == GameMode.SURVIVAL){
                bukkitPlayer.setAllowFlight(false);
                bukkitPlayer.setFlying(false);
            }
        } else {
            sender.sendMessage(Text.MODERATION_PREFIX + "§aTu es désormais en mode modérateur.");
            ModerationUtils.moderatePlayers.add(bukkitPlayer);
            ModerationUtils.vanishedPlayers.add(bukkitPlayer);
            player.setStockedInventory(bukkitPlayer.getInventory().getContents());
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if(onlinePlayer != bukkitPlayer){
                    ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                    if(!consulatPlayer.hasPower(Rank.MODO)){
                        onlinePlayer.hidePlayer(ConsulatCore.getInstance(), bukkitPlayer);
                    }
                }
            });
            bukkitPlayer.getInventory().clear();
            bukkitPlayer.getInventory().setHelmet(null);
            bukkitPlayer.getInventory().setChestplate(null);
            bukkitPlayer.getInventory().setLeggings(null);
            bukkitPlayer.getInventory().setBoots(null);
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2, false, false));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false));
            // Tp aléatoire, vanish/devanish, see inv du joueur sur lequel on clique
            ItemStack randomTeleport = new ItemStack(Material.ENDER_EYE);
            ItemMeta randomMeta = randomTeleport.getItemMeta();
            assert randomMeta != null;
            randomMeta.setDisplayName("§6Se téléporter aléatoirement");
            randomTeleport.setItemMeta(randomMeta);
            
            ItemStack vanish = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta vanishMeta = vanish.getItemMeta();
            assert vanishMeta != null;
            vanishMeta.setDisplayName("§cChanger son statut d'invisibilité");
            vanish.setItemMeta(vanishMeta);
            
            ItemStack invsee = new ItemStack(Material.PAPER);
            ItemMeta invseeMeta = invsee.getItemMeta();
            assert invseeMeta != null;
            invseeMeta.setDisplayName("§aVoir l'inventaire");
            invsee.setItemMeta(invseeMeta);
            
            ItemStack freeze = new ItemStack(Material.PACKED_ICE);
            ItemMeta freezeMeta = freeze.getItemMeta();
            assert freezeMeta != null;
            invseeMeta.setDisplayName("§bFreeze");
            freeze.setItemMeta(invseeMeta);
            
            bukkitPlayer.getInventory().setItem(3, randomTeleport);
            bukkitPlayer.getInventory().setItem(4, vanish);
            bukkitPlayer.getInventory().setItem(5, invsee);
            bukkitPlayer.getInventory().setItem(6, freeze);
            if(player.isFlying()){
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        player.disableFly();
                    } catch(SQLException e){
                        e.printStackTrace();
                    }
                });
            } else {
                bukkitPlayer.setAllowFlight(true);
                bukkitPlayer.setFlying(true);
            }
        }
    }
}
