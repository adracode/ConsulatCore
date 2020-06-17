package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
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
    
    /*
    [11:15:13] [Server thread/ERROR]: Could not pass event EntityDamageEvent to ConsulatCore v2.0.0-RELEASE
    java.lang.NullPointerException: null
	at fr.amisoz.consulatcore.listeners.entity.player.DamageListener.onDamage(DamageListener.java:23) ~[?:?]
	at com.destroystokyo.paper.event.executor.asm.generated.GeneratedEventExecutor232.execute(Unknown Source) ~[?:?]
	at org.bukkit.plugin.EventExecutor.lambda$create$1(EventExecutor.java:69) ~[patched_1.14.4.jar:git-Paper-243]
	at co.aikar.timings.TimedEventExecutor.execute(TimedEventExecutor.java:80) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.plugin.RegisteredListener.callEvent(RegisteredListener.java:70) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.plugin.SimplePluginManager.callEvent(SimplePluginManager.java:545) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory.callEvent(CraftEventFactory.java:231) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory.callEntityDamageEvent(CraftEventFactory.java:990) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory.handleEntityDamageEvent(CraftEventFactory.java:972) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory.handleEntityDamageEvent(CraftEventFactory.java:833) ~[patched_1.14.4.jar:git-Paper-243]
	at org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory.handleLivingEntityDamageEvent(CraftEventFactory.java:1022) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityLiving.damageEntity0(EntityLiving.java:1686) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityHuman.damageEntity0(EntityHuman.java:863) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityLiving.damageEntity(EntityLiving.java:1095) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityHuman.damageEntity(EntityHuman.java:786) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityPlayer.damageEntity(EntityPlayer.java:757) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.MobEffectList.tick(MobEffectList.java:44) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.MobEffect.b(SourceFile:135) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.MobEffect.tick(SourceFile:122) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityLiving.tickPotionEffects(EntityLiving.java:654) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityLiving.entityBaseTick(EntityLiving.java:337) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.Entity.tick(Entity.java:370) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityLiving.tick(EntityLiving.java:2286) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityHuman.tick(EntityHuman.java:165) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.EntityPlayer.playerTick(EntityPlayer.java:426) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.PlayerList.disconnect(PlayerList.java:412) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.PlayerConnection.a(PlayerConnection.java:1485) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.NetworkManager.handleDisconnection(NetworkManager.java:356) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.ServerConnection.c(ServerConnection.java:163) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.MinecraftServer.b(MinecraftServer.java:1255) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.DedicatedServer.b(DedicatedServer.java:417) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.MinecraftServer.a(MinecraftServer.java:1098) ~[patched_1.14.4.jar:git-Paper-243]
	at net.minecraft.server.v1_14_R1.MinecraftServer.run(MinecraftServer.java:925) ~[patched_1.14.4.jar:git-Paper-243]
	at java.lang.Thread.run(Thread.java:748) [?:1.8.0_242]
	*/
    
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getEntity().getUniqueId());
        if(player.isInModeration()){
            event.setCancelled(true);
        }
        EntityDamageEvent.DamageCause damageCause = event.getCause();
        if(damageCause == EntityDamageEvent.DamageCause.SUFFOCATION || damageCause == EntityDamageEvent.DamageCause.VOID){
            long cooldownTeleport = (System.currentTimeMillis() - player.getLastTeleport()) / 1000;
            if((cooldownTeleport > 2 && cooldownTeleport < 10) || (cooldownTeleport <= 2 && player.getPlayer().getHealth() <= 2)){
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 10));
                player.getPlayer().teleport(ConsulatCore.getInstance().getSpawn());
                player.sendMessage(Text.PREFIX + "§aTu as été téléporté au spawn pour cause de suffocation.");
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
