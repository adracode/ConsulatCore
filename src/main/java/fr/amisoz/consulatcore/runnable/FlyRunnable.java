package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.amisoz.consulatcore.utils.Title;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by KIZAFOX on 18/03/2020 for ConsulatCore
 */
public class FlyRunnable implements Runnable {


    @Override
    public void run() {
        for (Map.Entry<Player, Long> fly : FlyManager.flyMap.entrySet()) {
            Player player = fly.getKey();
            CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

            long startFly = FlyManager.flyMap.get(player);
            long timeLeft = corePlayer.timeLeft - (System.currentTimeMillis() - startFly) / 1000;

            long minutes = ((timeLeft / 60) % 60);
            long seconds = timeLeft % 60;

            if(timeLeft % 60 == 0 && timeLeft > 0){
                Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.BLUE + "Tu as encore ton fly pendant " + minutes + " minute" + ((minutes > 1) ? ("s") : ("")), 1, 4, 2);
            }

            if (timeLeft == 30 || timeLeft == 10 || timeLeft == 5 || timeLeft == 3 || timeLeft == 2 || timeLeft == 1) {
                Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + seconds + " seconde" + ((seconds > 1) ? ("s") : ("")), 1, 4, 2);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
            }

            if (timeLeft <= 0) {
                try {
                    ConsulatCore.INSTANCE.getFlySQL().saveFly(player, System.currentTimeMillis(), corePlayer.flyTime);
                    corePlayer.timeLeft = corePlayer.flyTime;
                    corePlayer.lastTime = System.currentTimeMillis();
                } catch (SQLException e) {
                    player.sendMessage(FlyManager.flyPrefix + "Erreur lors de la sauvegarde du fly.");
                }
                FlyManager.flyMap.remove(player);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
                player.sendMessage(FlyManager.flyPrefix + "Ton fly est terminé !");
            }
        }
    }
}
