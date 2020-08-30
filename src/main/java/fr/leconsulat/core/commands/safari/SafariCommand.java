package fr.leconsulat.core.commands.safari;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.server.SafariServer;
import org.jetbrains.annotations.NotNull;

public class SafariCommand extends ConsulatCommand {
    
    public SafariCommand(){
        super(ConsulatCore.getInstance(), "safari");
        setDescription("Se téléporter au Safari").
                setUsage("/safari - Se TP au Safari").
                setRank(Rank.JOUEUR).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer player, @NotNull String[] args){
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
