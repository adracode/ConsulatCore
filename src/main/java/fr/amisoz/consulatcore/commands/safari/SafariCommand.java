package fr.amisoz.consulatcore.commands.safari;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.server.SafariServer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class SafariCommand extends ConsulatCommand {
    
    public SafariCommand(){
        super("safari", "/safari", 0, Rank.JOUEUR);
        setPermission("consulat.core.command.safari");
        suggest(false);
    }
    
    @Override
    public void onCommand(ConsulatPlayer player, String[] args){
        SafariServer safari = ConsulatCore.getInstance().getSafari();
        switch(safari.queuePlayer(player)){
            case IN_QUEUE:
                player.sendMessage("§aTu es desormais dans la queue : " + player.getPositionInQueue() + " / " + safari.getPlayersInQueue());
                break;
            case ALREADY_IN_QUEUE:
                player.sendMessage("§cTu es déjà dans la queue : " + player.getPositionInQueue() + " / " + safari.getPlayersInQueue());
                break;
        }
    }
}
