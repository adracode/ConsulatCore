package fr.amisoz.consulatcore.listeners.world;

import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSign(SignChangeEvent event) {
        Player player = event.getPlayer();
        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
        if (consulatPlayer.getRank().getRankPower() >= RankEnum.RESPONSABLE.getRankPower()){
            String[] lines = event.getLines();

            if(lines[0].equals("[TP]")) {
                event.setLine(0, "§9[Téléportation]");
                try {
                    int x = Integer.parseInt(lines[1]);
                    int y = Integer.parseInt(lines[2]);
                    int z = Integer.parseInt(lines[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cErreur de coordonnées");
                    event.setLine(0, "§cErreur");
                    event.setLine(1, "§cErreur");
                    event.setLine(2, "§cErreur");
                    event.setLine(3, "§cErreur");
                }
            }
        }
    }

}
