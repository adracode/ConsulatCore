package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class BaltopCommand extends ConsulatCommand {
    
    public BaltopCommand(){
        super("consulat.core", "baltop", "/baltop", 0, Rank.ADMIN);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage(Text.BALTOP(BaltopManager.getInstance().getBaltop()));
    }
}
