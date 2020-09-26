package fr.leconsulat.core.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.jetbrains.annotations.NotNull;

public class BackCommand extends ConsulatCommand {
    
    public BackCommand(){
        super(ConsulatCore.getInstance(), "back");
        setDescription("Revenir en arrière").
                setUsage("/back - Revenir en arrière").
                setRank(Rank.MECENE).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.isInCombat()){
            player.sendMessage(Text.IN_COMBAT);
            return;
        }
        if(player.getOldLocation() == null){
            player.sendMessage(Text.NOT_YET_TELEPORTED);
            return;
        }
        sender.getPlayer().teleportAsync(player.getOldLocation());
        sender.sendMessage(Text.TELEPORTATION);
    }
}
