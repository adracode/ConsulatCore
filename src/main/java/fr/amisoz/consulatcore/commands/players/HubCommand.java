package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
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
        ConsulatCore.getInstance().getHub().connectPlayer(sender);
    }
}
