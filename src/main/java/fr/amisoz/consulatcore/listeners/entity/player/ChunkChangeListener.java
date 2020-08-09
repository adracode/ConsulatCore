package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.events.ClaimChangeEvent;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChunkChangeListener implements Listener {
    
    @EventHandler
    public void onChunkChangeEvent(ClaimChangeEvent event){
        SurvivalPlayer player = event.getPlayer();
        if(player.isFlying()){
            if(!player.canFlyHere(event.getClaimTo())){
                player.disableFly();
                player.sendMessage(Text.FLY_OUTSIDE_CLAIM);
            }
        }
    }
}
