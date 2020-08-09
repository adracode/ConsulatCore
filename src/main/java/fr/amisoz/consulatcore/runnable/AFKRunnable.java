package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AFKRunnable implements Runnable {

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()){
            SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
            if(survivalPlayer != null){
                if((System.currentTimeMillis() - survivalPlayer.getLastMove()) > 10 * 60 * 1000 && survivalPlayer.getLastMove() != 0 && !survivalPlayer.hasPermission("consulat.core.bypass-afk")){
                    player.kickPlayer(Text.KICK_PLAYER("§4AFK"));
                }
            }
        }
    }
}
