package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.players.CommandFly;
import fr.amisoz.consulatcore.utils.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by KIZAFOX on 15/03/2020 for ConsulatCore
 */
public class FlyRunnableBoutique extends BukkitRunnable {

    private Player player;
    public static int duration;
    private int durationAtEnd;

    public FlyRunnableBoutique(Player player, int duration, int durationAtEnd) {
        this.player = player;
        FlyRunnableBoutique.duration = duration;
        this.durationAtEnd = durationAtEnd;
    }

    @Override
    public void run() {
        if(!CommandFly.fly25.contains(player)){
            CommandFly.fly25.add(player);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(ChatColor.GREEN+"Tu viens d'activer ton fly !");
        }

        duration = ConsulatCore.INSTANCE.getFlySQL().getDuration(player);

        if(duration == 1200 || duration == 900 || duration == 600 || duration == 300 || duration == 30 || duration == 10 || duration == 5){
            System.out.println("Duration: " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + "s");
            System.out.println("Duration: " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + "min");

            Title.send(player, ChatColor.GOLD+"[Fly]", ChatColor.BLUE+"Tu as encore ton fly pendant " + (duration >= 60 ? ConsulatCore.INSTANCE.getFlySQL().getDuration(player)/60 + " minutes !" : ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !"), 1, 4, 2);
        }else if(duration == 3 || duration == 2 || duration == 1) {
            Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + ConsulatCore.INSTANCE.getFlySQL().getDuration(player) + " secondes !", 1, 4, 2);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
        }

        if(duration == 0){
            ConsulatCore.INSTANCE.getFlySQL().setDuration(player, durationAtEnd);
            CommandFly.fly5.remove(player);
            CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
            player.setAllowFlight(false);
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
            player.sendMessage(ChatColor.RED+"Ton fly est terminé !");
            cancel();
        }
        if(!player.isOnline()){
            ConsulatCore.INSTANCE.getFlySQL().setDuration(player, durationAtEnd);
            CommandFly.fly5.remove(player);
            CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
            player.setAllowFlight(false);
            player.setFlying(false);
            cancel();
        }
        ConsulatCore.INSTANCE.getFlySQL().updateDuration(player);
    }
}
