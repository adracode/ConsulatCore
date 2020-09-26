package fr.leconsulat.core.commands.safari;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
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
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.isInModeration()){
            player.sendMessage("§cTu ne peux pas aller sur le safari en mode staff.");
            return;
        }
        if(player.isInCombat()){
            player.sendMessage(Text.IN_COMBAT);
            return;
        }
        SafariServer safari = ConsulatCore.getInstance().getSafari();
        switch(safari.queuePlayer(sender)){
            case IN_QUEUE:
                sender.sendMessage(Text.NOW_IN_QUEUE(sender.getPositionInQueue(), safari.getPlayersInQueue()));
                break;
            case ALREADY_IN_QUEUE:
                sender.sendMessage(Text.IN_QUEUE(sender.getPositionInQueue(), safari.getPlayersInQueue()));
                break;
        }
    }
}
