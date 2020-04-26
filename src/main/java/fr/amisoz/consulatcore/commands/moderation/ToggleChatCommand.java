package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;

public class ToggleChatCommand extends ConsulatCommand {
    
    public ToggleChatCommand(){
        super("/chat", 0, Rank.RESPONSABLE);
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
