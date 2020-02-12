package fr.amisoz.consulatcore.listeners.entity;


import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class MobListeners implements Listener {

    private static List<EntityType> mobsToEnable = new ArrayList<>();

    public MobListeners() {
        mobsToEnable.add(EntityType.VILLAGER);
        mobsToEnable.add(EntityType.HORSE);
        mobsToEnable.add(EntityType.IRON_GOLEM);
        mobsToEnable.add(EntityType.ENDER_DRAGON);
        mobsToEnable.add(EntityType.WITHER);
        mobsToEnable.add(EntityType.WITHER_SKELETON);
        mobsToEnable.add(EntityType.PILLAGER);
        mobsToEnable.add(EntityType.VINDICATOR);
        mobsToEnable.add(EntityType.RAVAGER);
        mobsToEnable.add(EntityType.WITCH);
        mobsToEnable.add(EntityType.EVOKER);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        EntityType entityType = event.getEntityType();
        if (entityType == EntityType.PHANTOM) event.setCancelled(true);

       /* if(!mobsToEnable.contains(entityType)) {
            event.getEntity().setAI(false);
        }*/
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        if (!mobsToEnable.contains(entity.getType())) {
            entity.setAI(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event){
        if (!(event.getRightClicked() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getRightClicked();
        if(!mobsToEnable.contains(entity.getType())){
            entity.setAI(true);
        }
    }


}
