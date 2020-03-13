package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.commands.players.CommandFly;
import fr.amisoz.consulatcore.utils.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by KIZAFOX on 12/03/2020 for ConsulatCore
 */
public class FlyRunnable extends BukkitRunnable {

    private Player player;
    private static int duration = 300;

    public FlyRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if(!CommandFly.fly.contains(player)){
            CommandFly.fly.add(player);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(ChatColor.GREEN+"Tu viens d'activer ton fly !");
        }

        if(duration == 240 || duration == 180 || duration == 120 || duration == 60 || duration == 30 || duration == 10 ||duration == 5){
            if(duration >= 60){
                Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + getDuration()/60 + " minutes !", 1, 4, 2);
            }else {
                Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + getDuration() + " secondes !", 1, 4, 2);
            }
        }else if(duration == 3 || duration == 2 || duration == 1) {
            Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + getDuration() + " secondes !", 1, 4, 2);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
        }

        if(duration == 0){
            duration = 300;
            CommandFly.fly.remove(player);
            player.setAllowFlight(false);
            player.setFlying(false);
            CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
            player.sendMessage(ChatColor.RED+"Ton fly est terminé !");
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
            cancel();
        }
        duration--;
    }

    public static int getDuration() {
        return duration;
    }

    public static void setDuration(int duration) {
        FlyRunnable.duration = duration;
    }
}
