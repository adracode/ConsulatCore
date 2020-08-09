package fr.amisoz.consulatcore.commands.safari;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.server.SafariServer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class SafariCommand extends ConsulatCommand {
    
    public SafariCommand(){
        super("consulat.core", "safari", "/safari", 0, Rank.JOUEUR);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer player, String[] args){
        SafariServer safari = ConsulatCore.getInstance().getSafari();
        switch(safari.queuePlayer(player)){
            case IN_QUEUE:
                player.sendMessage(Text.NOW_IN_QUEUE(player.getPositionInQueue(), safari.getPlayersInQueue()));
                break;
            case ALREADY_IN_QUEUE:
                player.sendMessage(Text.IN_QUEUE(player.getPositionInQueue(), safari.getPlayersInQueue()));
                break;
        }
    }
}
