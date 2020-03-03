package fr.amisoz.consulatcore.runnable;

import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import org.bukkit.Bukkit;

public class AFKRunnable implements Runnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
            if((System.currentTimeMillis() - corePlayer.lastMove) > 1000*60*10 && corePlayer.lastMove != 0){
                player.kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été exclu.\n§cRaison : §4AFK");
            }
        });
    }
}
