package fr.amisoz.consulatcore.fly;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.HashSet;
import java.util.Set;

public class FlyManager {
    
    private static FlyManager instance;
    
    private Set<SurvivalPlayer> flyingPlayers = new HashSet<>();
    
    public FlyManager(){
        if(instance != null){
            return;
        }
        instance = this;
        Bukkit.getScheduler().runTaskTimer(ConsulatCore.getInstance(), () -> {
            for(SurvivalPlayer player : flyingPlayers){
                long timeLeft = player.getFlyTimeLeft();
                long minutes = ((timeLeft / 60) % 60);
                long seconds = timeLeft % 60;
                player.sendActionBar("§6[§7Fly§6] " + (timeLeft > 30 ? "§a" : "§c") + minutes + ":" + seconds);
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
