package fr.leconsulat.core.fly;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.events.ClaimChangeEvent;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class FlyManager implements Listener {
    
    private static FlyManager instance;
    
    private Set<SurvivalPlayer> flyingPlayers = new HashSet<>();
    
    public FlyManager(){
        if(instance != null){
            return;
        }
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, ConsulatAPI.getConsulatAPI());
        Bukkit.getScheduler().runTaskTimer(ConsulatCore.getInstance(), () -> {
            for(SurvivalPlayer player : flyingPlayers){
                long timeLeft = player.getFlyTimeLeft();
                long minutes = ((timeLeft / 60) % 60);
                long seconds = timeLeft % 60;
                player.sendActionBar("§6[§7Fly§6] " + (timeLeft > 30 ? "§a" : "§c") + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                if(timeLeft < 15){
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
                }
                if(timeLeft <= 0){
                    player.disableFly();
                    player.sendMessage(Text.FLY_IS_FINISHED);
                } else {
                    player.decrementTimeLeft();
                }
            }
        }, 0L, 20L);
    }
    
    @EventHandler
    public void onChunkChangeEvent(ClaimChangeEvent event){
        SurvivalPlayer player = event.getPlayer();
        if(player.isFlying()){
            if(!player.canFlyHere(event.getClaimTo())){
                player.disableFly();
                player.sendMessage(Text.FLY_OUTSIDE_CLAIM);
            }
        }
    }
    
    public void addFlyingPlayer(SurvivalPlayer player){
        if(player.hasInfiniteFly()){
            return;
        }
        flyingPlayers.add(player);
    }
    
    public void removeFlyingPlayer(SurvivalPlayer player){
        flyingPlayers.remove(player);
    }
    
    public static FlyManager getInstance(){
        return instance;
    }
}
