package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
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
        if(player.getOldLocation() == null){
            player.sendMessage(Text.NOT_YET_TELEPORTED);
            return;
        }
        sender.getPlayer().teleportAsync(player.getOldLocation());
        sender.sendMessage(Text.TELEPORTATION);
    }
}
