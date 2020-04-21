package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;

public class AFKRunnable implements Runnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
            if((System.currentTimeMillis() - survivalPlayer.getLastMove()) > 10 * 60 * 1000 && survivalPlayer.getLastMove() != 0){
                player.kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été exclu.\n§cRaison : §4AFK");
            }
        });
    }
}
