package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListeners implements Listener {
    
    public ChatListeners(){
    }
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        String message = player.chat(event.getMessage());
        if(message == null){
            event.setCancelled(true);
            return;
        }
        event.setMessage(message);
        event.setFormat(player.getDisplayName() + " %s§7 : §f%s");
    }
}
