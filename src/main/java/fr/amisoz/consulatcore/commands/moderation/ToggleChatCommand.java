package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class ToggleChatCommand extends ConsulatCommand {
    
    public ToggleChatCommand(){
        super("consulat.core", "chat", "/chat", 0, Rank.RESPONSABLE);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        ConsulatCore core = ConsulatCore.getInstance();
        if(!core.isChatActivated()){
            sender.getPlayer().performCommand("annonce Le chat est à nouveau disponible.");
        } else {
            sender.getPlayer().performCommand("annonce Le chat est coupé.");
        }
        core.setChat(!core.isChatActivated());
    }
}
