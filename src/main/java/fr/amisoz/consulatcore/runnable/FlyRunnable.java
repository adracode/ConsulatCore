package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.utils.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by KIZAFOX on 18/03/2020 for ConsulatCore
 */
public class FlyRunnable implements Runnable{

    public static Map<Player, Long> flyMap = new HashMap<>();
    private static long duration;

    @Override
    public void run() {
        for(Map.Entry<Player, Long> fly : flyMap.entrySet()){
            Player player = fly.getKey();

            duration = ConsulatCore.INSTANCE.getFlySQL().getDuration(player);

            if(ConsulatCore.INSTANCE.getFlySQL().getFlyTime(player) == 300) {
                if(duration == 240 || duration == 180 || duration == 120 || duration == 60 || duration == 30 || duration == 10 || duration == 5){
                    Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + (duration >= 60 ? ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + " minutes !" : ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !"), 1, 4, 2);
                }
            }else if(ConsulatCore.INSTANCE.getFlySQL().getFlyTime(player) == 1500){
                if(duration == 1200 || duration == 900 || duration == 600 || duration == 300 || duration == 30 || duration == 10 || duration == 5){
                    System.out.println("Duration: " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + "s");
                    System.out.println("Duration: " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + "min");

                    Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + (duration >= 60 ? ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + " minutes !" : ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !"), 1, 4, 2);
                }
            }

            if(duration == 3 || duration == 2 || duration == 1) {
                Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !", 1, 4, 2);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
            }

            if(duration == 0){
                ConsulatCore.INSTANCE.getFlySQL().setDuration(player, CoreManagerPlayers.getCorePlayer(player).flyDuration+1);
                CoreManagerPlayers.getCorePlayer(player).lastTime = System.currentTimeMillis();
                ConsulatCore.INSTANCE.getFlySQL().setLastTime(player, flyMap.get(player));
                flyMap.remove(player);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
                player.sendMessage(ChatColor.RED+"Ton fly est terminé !");
            }
            if(!player.isOnline()){
                ConsulatCore.INSTANCE.getFlySQL().setDuration(player, CoreManagerPlayers.getCorePlayer(player).flyDuration+1);
                CoreManagerPlayers.getCorePlayer(player).lastTime = System.currentTimeMillis();
                ConsulatCore.INSTANCE.getFlySQL().setLastTime(player, flyMap.get(player));
                flyMap.remove(player);
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            ConsulatCore.INSTANCE.getFlySQL().updateDuration(player);
            System.out.println(duration+"s");
        }
    }

    public static long getDuration() {
        return duration;
    }
}
