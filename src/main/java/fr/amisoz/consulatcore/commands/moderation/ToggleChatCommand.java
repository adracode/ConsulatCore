package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

public class ToggleChatCommand extends ConsulatCommand {
    
    public ToggleChatCommand(){
        super("consulat.core", "chat", "/chat", 0, Rank.RESPONSABLE);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        ConsulatCore core = ConsulatCore.getInstance();
        core.setChat(!core.isChatActivated());
        if(core.isChatActivated()){
            Bukkit.broadcastMessage(Text.BRODCAST(sender.getName(), "Le chat est à nouveau disponible."));
        } else {
            Bukkit.broadcastMessage(Text.BRODCAST(sender.getName(), "Le chat est coupé."));
        }
    }
}
