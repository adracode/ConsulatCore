package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.players.CommandFly;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
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
    private static int duration;

    public FlyRunnable(Player player, int duration) {
        this.player = player;
        FlyRunnable.duration = duration;
    }

    @Override
    public void run() {
        if(!CommandFly.fly.contains(player)){
            CommandFly.fly.add(player);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(ChatColor.GREEN+"Tu viens d'activer ton fly !");
        }

        duration = ConsulatCore.INSTANCE.getFlySQL().getDuration(player);

        if(duration == 240 || duration == 180 || duration == 120 || duration == 60 || duration == 30 || duration == 10 ||duration == 5){
            if(duration >= 60){
                Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + " minutes !", 1, 4, 2);
            }else {
                Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !", 1, 4, 2);
            }
        }else if(duration == 3 || duration == 2 || duration == 1) {
            Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !", 1, 4, 2);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
        }

        if(duration == 0){
            ConsulatCore.INSTANCE.getFlySQL().setDuration(player, 300);
            CommandFly.fly.remove(player);
            CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
            player.setAllowFlight(false);
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
            player.sendMessage(ChatColor.RED+"Ton fly est terminé !");
            cancel();
        }
        ConsulatCore.INSTANCE.getFlySQL().updateDuration(player);
        System.out.println(duration+"s");
    }
}
