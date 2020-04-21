package fr.amisoz.consulatcore.listeners.world;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSign(SignChangeEvent event) {
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if (player.hasPower(Rank.RESPONSABLE)){
            String[] lines = event.getLines();
            if(lines[0].equals("[TP]")) {
                event.setLine(0, "§9[Téléportation]");
                try {
                    Integer.parseInt(lines[1]);
                    Integer.parseInt(lines[2]);
                    Integer.parseInt(lines[3]);
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
