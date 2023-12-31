package fr.leconsulat.core.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.jetbrains.annotations.NotNull;

public class HubCommand extends ConsulatCommand {
    
    public HubCommand(){
        super(ConsulatCore.getInstance(), "hub");
        setDescription("Se téléporter au Hub").
                setUsage("/hub - Se TP au Hub").
                setRank(Rank.INVITE).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.isInCombat()){
            player.sendMessage(Text.IN_COMBAT);
            return;
        }
        ConsulatCore.getInstance().getHub().connectPlayer(sender);
    }
}
