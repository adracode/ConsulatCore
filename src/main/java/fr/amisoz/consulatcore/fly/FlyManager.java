package fr.amisoz.consulatcore.fly;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.utils.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
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
                if(timeLeft % 60 == 0 && timeLeft > 0){
                    Title.send(player.getPlayer(), ChatColor.GOLD + "[Fly]", ChatColor.BLUE + "Tu as encore ton fly pendant " + minutes + " minute" + ((minutes > 1) ? ("s") : ("")), 1, 4, 2);
                }
                if(timeLeft == 30 || timeLeft == 10 || timeLeft == 5 || timeLeft == 3 || timeLeft == 2 || timeLeft == 1){
                    Title.send(player.getPlayer(), ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + seconds + " seconde" + ((seconds > 1) ? ("s") : ("")), 1, 4, 2);
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
                }
                if(timeLeft <= 0){
                    try {
                        player.disableFly();
                    } catch(SQLException e){
                        player.sendMessage(Text.FLY + "§cUne erreur interne est survenue.");
                    }
                    player.sendMessage(Text.FLY + "Ton fly est terminé !");
                }
                player.decrementTimeLeft();
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
