package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageListener implements Listener {
    
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(player == null){
            return;
        }
        if(player.isInModeration()){
            event.setCancelled(true);
            return;
        }
        if(player.isCancelNextFallDamage() && event.getCause() == EntityDamageEvent.DamageCause.FALL){
            event.setCancelled(true);
            player.setCancelNextFallDamage(false);
            return;
        }
        EntityDamageEvent.DamageCause damageCause = event.getCause();
        if(damageCause == EntityDamageEvent.DamageCause.SUFFOCATION || damageCause == EntityDamageEvent.DamageCause.VOID){
            long cooldownTeleport = (System.currentTimeMillis() - player.getLastTeleport()) / 1000;
            if((cooldownTeleport > 2 && cooldownTeleport < 10) || (cooldownTeleport <= 2 && player.getPlayer().getHealth() <= 2)){
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10));
                player.getPlayer().teleport(ConsulatCore.getInstance().getSpawn());
                player.sendMessage(Text.TP_BECAUSE_STUCK);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        if(!(event.getDamager() instanceof Player)){
            return;
        }
        SurvivalPlayer damager = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getDamager().getUniqueId());
        if(damager == null){
            return;
        }
        if(damager.isInModeration()){
            event.setCancelled(true);
        }
    }
    
}
