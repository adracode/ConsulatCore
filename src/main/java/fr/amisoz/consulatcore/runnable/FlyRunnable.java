package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.players.CommandFly;
import fr.amisoz.consulatcore.fly.FlyDurations;
import fr.amisoz.consulatcore.utils.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by KIZAFOX on 12/03/2020 for ConsulatCore
 */
public class FlyRunnable implements Runnable {

    public static Map<Player, Long> fly = new HashMap<>();

    @Override
    public void run() {
        for(Map.Entry<Player, Long> flys : fly.entrySet()){
            Player player = flys.getKey();
            if(fly.containsKey(player)){
                if(!CommandFly.fly5.contains(player)){
                    CommandFly.fly5.add(player);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage(ChatColor.GREEN+"Tu viens d'activer ton fly !");
                }

                int duration = ConsulatCore.INSTANCE.getFlySQL().getDuration(player);

                if(duration == 240 || duration == 180 || duration == 120 || duration == 60 || duration == 30 || duration == 10 || duration == 5){
                    System.out.println("Duration: " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + "s");
                    System.out.println("Duration: " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + "min");

                    Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + (duration >= 60 ? ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + " minutes !" : ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !"), 1, 4, 2);
                }else if(duration == 3 || duration == 2 || duration == 1) {
                    Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !", 1, 4, 2);
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
                }

                if(duration == 0){
                    ConsulatCore.INSTANCE.getFlySQL().setDuration(player, FlyDurations.Fly5.getDuration()+1);
                    CommandFly.fly5.remove(player);
                    CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
                    fly.remove(player);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
                    player.sendMessage(ChatColor.RED+"Ton fly est terminé !");
                }
                if(!player.isOnline()){
                    ConsulatCore.INSTANCE.getFlySQL().setDuration(player, FlyDurations.Fly5.getDuration()+1);
                    CommandFly.fly5.remove(player);
                    CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
                    fly.remove(player);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
                ConsulatCore.INSTANCE.getFlySQL().updateDuration(player);
            }
        }
    }
}
