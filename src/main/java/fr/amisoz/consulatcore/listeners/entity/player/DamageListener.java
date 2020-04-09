package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
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
        if(!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isModerate()){
            event.setCancelled(true);
        }

        long cooldownTeleport = (System.currentTimeMillis() - corePlayer.lastTeleport) / 1000;

        if(cooldownTeleport > 2 && cooldownTeleport < 20){
            EntityDamageEvent.DamageCause damageCause = event.getCause();
            if(damageCause.equals(EntityDamageEvent.DamageCause.SUFFOCATION) || player.getLocation().getY() < 5){
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10));
                player.teleport(ConsulatCore.spawnLocation);
            }
        }

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        if(!(event.getDamager() instanceof Player)) return;

        CorePlayer damager = CoreManagerPlayers.getCorePlayer((Player) event.getDamager());

        if(damager.isModerate()){
            event.setCancelled(true);
        }
    }

}
