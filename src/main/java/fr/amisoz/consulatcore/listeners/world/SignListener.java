package fr.amisoz.consulatcore.listeners.world;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.events.blocks.PlayerInteractSignEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {
    
    @EventHandler
    public void onClickSign(PlayerInteractSignEvent event){
        Player player = event.getPlayer();
        Sign sign = (Sign)event.getBlock().getState();
        String[] lines = sign.getLines();
        if(lines[0].equals("§9[Téléportation]")){
            try {
                int x = Integer.parseInt(lines[1]);
                int y = Integer.parseInt(lines[2]);
                int z = Integer.parseInt(lines[3]);
                Location result = new Location(ConsulatCore.getInstance().getOverworld(), x, y, z);
                player.teleportAsync(result);
                player.sendMessage("§aTu as été téléporté à la zone.");
            } catch(NumberFormatException e){
                player.sendMessage("§cErreur de coordonnées");
            }
        }
    }
    
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
                    event.getBlock().breakNaturally();
                }
            }
        }
    }

}
