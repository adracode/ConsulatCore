package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListeners implements Listener {
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player == null){
            event.getPlayer().sendMessage(Text.ERROR);
            event.setCancelled(true);
            return;
        }
        String message = player.chat(event.getMessage());
        if(message == null){
            event.setCancelled(true);
            return;
        }
        event.setMessage(message);
        event.setFormat(player.getDisplayRank() + " %s§7: §f%s");
    }
}
