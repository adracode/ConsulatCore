package fr.amisoz.consulatcore.commands.safari;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.server.SafariServer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
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
