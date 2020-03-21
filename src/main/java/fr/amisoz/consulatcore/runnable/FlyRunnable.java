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
            long timeLeft = corePlayer.flyTime -  (System.currentTimeMillis() - startFly)/1000;

            if (corePlayer.flyTime == 300) {
                if (timeLeft == 240 || timeLeft == 180 || timeLeft == 120 || timeLeft == 60 || timeLeft == 30 || timeLeft == 10 || timeLeft == 5) {
                    Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.BLUE + "Tu as encore ton fly pendant " + (timeLeft >= 60 ? timeLeft + " minutes !" : timeLeft + " secondes !"), 1, 4, 2);

                }
            } else if (corePlayer.flyTime == 1500) {
                if (timeLeft == 1200 || timeLeft == 900 || timeLeft == 600 || timeLeft == 300 || timeLeft == 30 || timeLeft == 10 || timeLeft == 5) {
                    Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.BLUE + "Tu as encore ton fly pendant " + (timeLeft >= 60 ? timeLeft + " minutes !" : timeLeft + " secondes !"), 1, 4, 2);
                }
            }

            if (timeLeft == 3 || timeLeft == 2 || timeLeft == 1) {
                Title.send(player, ChatColor.GOLD + "[Fly]", ChatColor.RED + "Tu as encore ton fly pendant " + timeLeft + " secondes !", 1, 4, 2);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1f);
            }

            if (timeLeft <= 0) {
                CoreManagerPlayers.getCorePlayer(player).lastTime = System.currentTimeMillis();
                try {
                    ConsulatCore.INSTANCE.getFlySQL().setLastTime(player, FlyManager.flyMap.get(player));
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Erreur lors de la sauvegarde du fly.");
                }
                FlyManager.flyMap.remove(player);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
                player.sendMessage(ChatColor.RED + "Ton fly est terminé !");
            }
        }
    }
}
