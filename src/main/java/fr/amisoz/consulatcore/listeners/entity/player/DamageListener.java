package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
        if(player.isInModeration()){
            event.setCancelled(true);
        }
        long cooldownTeleport = (System.currentTimeMillis() - player.getLastTeleport()) / 1000;
        if(cooldownTeleport > 2 && cooldownTeleport < 20){
            EntityDamageEvent.DamageCause damageCause = event.getCause();
            if(damageCause.equals(EntityDamageEvent.DamageCause.SUFFOCATION) || player.getPlayer().getLocation().getY() < 5){
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10));
                player.getPlayer().teleport(ConsulatCore.getInstance().getSpawn());
            }
        }
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        if(!(event.getDamager() instanceof Player)){
            return;
        }
        SurvivalPlayer damager = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getDamager().getUniqueId());
        if(damager.isInModeration()){
            event.setCancelled(true);
        }
    }
    
}
