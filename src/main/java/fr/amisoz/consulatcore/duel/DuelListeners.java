package fr.amisoz.consulatcore.duel;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Random;

public class DuelListeners implements Listener {
    
    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event){
        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();
        
        if(!(projectileSource instanceof Player)) return;
        Player player = (Player)projectileSource;
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        if(survivalPlayer.isFighting() && survivalPlayer.getArena().getArenaState() == ArenaState.DUEL_ACCEPTED){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Le duel n'a pas encore commencé !");
        }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        
        if(survivalPlayer.isFighting() && survivalPlayer.getArena().getArenaState() == ArenaState.DUEL_ACCEPTED){
            Location from = event.getFrom();
            Location to = event.getTo();
            if(from.getX() != to.getX() || from.getZ() != to.getZ()){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Le duel n'a pas encore commencé !");
            }
        }
    }
    
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        if(survivalPlayer.isFighting()){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas faire de commandes en duel.");
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        if(survivalPlayer.isFighting() && survivalPlayer.getArena().getArenaState() != ArenaState.AFTER_FIGHT){
            player.setHealth(0D);
            Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §c" + player.getName() + " s'est déconnecté !");
        }
    }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        if(survivalPlayer.isFighting()){
            Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §c" + player.getName() + " a perdu le duel !");
            survivalPlayer.setFighting(false);
            Arena arena = survivalPlayer.getArena();
            
            Player firstPlayer = arena.getFirstPlayer();
            Player secondPlayer = arena.getSecondPlayer();
            
            if(firstPlayer == player){
                arena.setVictoryPlayer(secondPlayer);
                arena.winLocation = arena.secondBefore;
            } else {
                arena.setVictoryPlayer(firstPlayer);
                arena.winLocation = arena.firstBefore;
            }
            
            survivalPlayer.setFighting(false);
            
            arena.setArenaState(ArenaState.AFTER_FIGHT);
            arena.setFirstPlayer(null);
            arena.setSecondPlayer(null);
            
            Random random = new Random();
            int result = random.nextInt(10);
            if(result == 2){
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta)skull.getItemMeta();
                skullMeta.setOwningPlayer(player);
                skull.setItemMeta(skullMeta);
                arena.getVictoryPlayer().getWorld().dropItemNaturally(arena.getVictoryPlayer().getLocation(), skull);
            }
            ((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(arena.getVictoryPlayer().getUniqueId())).addMoney(arena.bet * 2);
            arena.getVictoryPlayer().sendMessage("§aTu as gagné " + arena.bet * 2 + "€ !");
            Bukkit.getScheduler().runTaskLater(ConsulatCore.getInstance(), () -> {
                for(Entity entity : arena.getVictoryPlayer().getNearbyEntities(10, 5, 10)){
                    if(entity instanceof Item){
                        entity.remove();
                    }
                }
                Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §cL'arène est à nouveau disponible.");
                arena.setArenaState(ArenaState.FREE);
                arena.setBusy(false);
                arena.getVictoryPlayer().teleportAsync(arena.winLocation);
                ((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(arena.getVictoryPlayer().getUniqueId())).setFighting(false);
                // Retrait du /back qui téléporterait à nouveau dans l'arène
                ((SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(arena.getVictoryPlayer().getUniqueId())).setOldLocation(arena.winLocation);
            }, 20 * 20);
        }
    }
    
    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        if(survivalPlayer.isFighting()){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas faire ceci en duel.");
        }
    }
    
    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        if(survivalPlayer.isFighting()){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas faire ceci en duel.");
        }
    }
    
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        ItemStack item = event.getItem();
        
        if(survivalPlayer.isFighting()){
            if(item.getType() == Material.CHORUS_FRUIT){
                player.sendMessage(ChatColor.RED + "Tu ne peux pas manger ceci en combat.");
                event.setCancelled(true);
            }
        }
    }
}
