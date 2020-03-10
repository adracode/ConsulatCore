package fr.amisoz.consulatcore.duel;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;

public class DuelListeners implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);


        if(corePlayer.isFighting && corePlayer.arena.getArenaState() == ArenaState.DUEL_ACCEPTED){
            Location from = event.getFrom();
            Location to = event.getTo();
            if(from.getX() != to.getX() || from.getZ() != to.getZ()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Le duel n'a pas encore commencé !");
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFighting){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas faire de commandes en duel.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFighting && corePlayer.arena.getArenaState() != ArenaState.AFTER_FIGHT){
            player.setHealth(0D);
            Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §c" + player.getName() + " s'est déconnecté !");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFighting){
            Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §c" + player.getName() + " a perdu le duel !");
            corePlayer.isFighting = false;
            Arena arena = corePlayer.arena;

            Player firstPlayer = arena.getFirstPlayer();
            Player secondPlayer = arena.getSecondPlayer();

            if(firstPlayer == player){
                arena.setVictoryPlayer(secondPlayer);
                arena.winLocation = arena.secondBefore;
            }else{
                arena.setVictoryPlayer(firstPlayer);
                arena.winLocation = arena.firstBefore;
            }

            corePlayer.isFighting = false;

            arena.setArenaState(ArenaState.AFTER_FIGHT);
            arena.setFirstPlayer(null);
            arena.setSecondPlayer(null);

            Random random = new Random();
            int result = random.nextInt(10);
            if(result == 2){
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setPlayerProfile(player.getPlayerProfile());
                skull.setItemMeta(skullMeta);
                arena.getVictoryPlayer().getWorld().dropItemNaturally(arena.getVictoryPlayer().getLocation(), skull);
            }

            PlayersManager.getConsulatPlayer(arena.getVictoryPlayer()).addMoney((double) (arena.bet*2));
            arena.getVictoryPlayer().sendMessage("§aTu as gagné " + arena.bet*2 + "€ !");
            Bukkit.getScheduler().runTaskLater(ConsulatCore.INSTANCE, () -> {
                arena.getVictoryPlayer().getNearbyEntities(10, 5, 10).forEach(entity -> {
                    if(entity instanceof Item){
                        entity.remove();
                    }
                });

                Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §cL'arène est à nouveau disponible.");
                arena.setArenaState(ArenaState.FREE);
                arena.setBusy(false);
                arena.getVictoryPlayer().teleport(arena.winLocation);
                CoreManagerPlayers.getCorePlayer(arena.getVictoryPlayer()).isFighting = false;

                // Retrait du /back qui téléporterait à nouveau dans l'arène
                CoreManagerPlayers.getCorePlayer(arena.getVictoryPlayer()).oldLocation = arena.winLocation;
            }, 20*20);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFighting){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas faire ceci en duel.");
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFighting){
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Tu ne peux pas faire ceci en duel.");
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        ItemStack item = event.getItem();

        if(corePlayer.isFighting){
            if(item.getType() == Material.CHORUS_FRUIT){
                player.sendMessage(ChatColor.RED + "Tu ne peux pas manger ceci en combat.");
                event.setCancelled(true);
            }
        }
    }
}
