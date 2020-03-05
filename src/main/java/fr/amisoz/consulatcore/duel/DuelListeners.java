package fr.amisoz.consulatcore.duel;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

        if(corePlayer.isFighting){
            player.setHealth(0D);
            Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §4" + player.getName() + " s'est déconnecté !");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFighting){
            Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §4" + player.getName() + " a perdu le duel !");

            Arena arena = corePlayer.arena;

            Player firstPlayer = arena.getFirstPlayer();
            Player secondPlayer = arena.getSecondPlayer();

            if(firstPlayer == player){
                arena.setVictoryPlayer(secondPlayer);
            }else{
                arena.setVictoryPlayer(firstPlayer);
            }

            corePlayer.isFighting = false;
            CorePlayer victoryCore = CoreManagerPlayers.getCorePlayer(arena.getVictoryPlayer());
            victoryCore.isFighting = false;

            arena.setArenaState(ArenaState.AFTER_FIGHT);
            arena.setFirstPlayer(null);
            arena.setSecondPlayer(null);

            PlayersManager.getConsulatPlayer(arena.getVictoryPlayer()).addMoney((double) (arena.bet*2));
            arena.getVictoryPlayer().sendMessage("§aTu as gagné " + arena.bet*2 + "€ !");
            Bukkit.getScheduler().runTaskLater(ConsulatCore.INSTANCE, () -> {
                Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §4L'arène est à nouveau disponible.");
                arena.setArenaState(ArenaState.FREE);
                arena.setBusy(false);
            }, 20*60);
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
}
