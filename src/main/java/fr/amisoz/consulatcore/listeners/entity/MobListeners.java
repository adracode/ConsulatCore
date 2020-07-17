package fr.amisoz.consulatcore.listeners.entity;


import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class MobListeners implements Listener {
    
    private Set<EntityType> mobsToEnable = new HashSet<>();
    private Set<EntityType> canNaturallySpawn;
    private Set<CreatureSpawnEvent.SpawnReason> spawnReasonAllowed;
    
    public MobListeners(){
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
        canNaturallySpawn = EnumSet.of(
                EntityType.COW,
                EntityType.PIG,
                EntityType.SHEEP,
                EntityType.CHICKEN,
                EntityType.SHULKER,
                EntityType.ARMOR_STAND,
                EntityType.VILLAGER
        );
        spawnReasonAllowed = EnumSet.of(
                CreatureSpawnEvent.SpawnReason.BREEDING,
                CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
                CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
                CreatureSpawnEvent.SpawnReason.BUILD_WITHER,
                CreatureSpawnEvent.SpawnReason.CURED,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                CreatureSpawnEvent.SpawnReason.DEFAULT,
                CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
                CreatureSpawnEvent.SpawnReason.EGG,
                CreatureSpawnEvent.SpawnReason.ENDER_PEARL,
                CreatureSpawnEvent.SpawnReason.EXPLOSION,
                CreatureSpawnEvent.SpawnReason.INFECTION,
                CreatureSpawnEvent.SpawnReason.LIGHTNING,
                CreatureSpawnEvent.SpawnReason.PATROL,
                CreatureSpawnEvent.SpawnReason.RAID,
                CreatureSpawnEvent.SpawnReason.SHEARED,
                CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY,
                CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK,
                CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
                CreatureSpawnEvent.SpawnReason.TRAP,
                CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE
        );
    }
    
    @EventHandler
    public void onSpawn(CreatureSpawnEvent event){
        EntityType entityType = event.getEntityType();
        CreatureSpawnEvent.SpawnReason spawnReason = event.getSpawnReason();
        if(!spawnReasonAllowed.contains(spawnReason) && !canNaturallySpawn.contains(entityType)){
            event.setCancelled(true);
            return;
        }
        if(spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER){
            return;
        }
        if(!mobsToEnable.contains(entityType)){
            event.getEntity().setAI(false);
        }
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof LivingEntity)){
            return;
        }
        LivingEntity entity = (LivingEntity)event.getEntity();
        if(!mobsToEnable.contains(entity.getType())){
            entity.setAI(true);
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event){
        if(!(event.getRightClicked() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity)event.getRightClicked();
        if(!mobsToEnable.contains(entity.getType())){
            entity.setAI(true);
        }
    }
}
