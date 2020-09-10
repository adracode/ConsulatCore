package fr.leconsulat.core.commands.economy;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalOffline;
import org.jetbrains.annotations.NotNull;

public class BaltopCommand extends ConsulatCommand {
    
    public BaltopCommand(){
        super(ConsulatCore.getInstance(), "baltop");
        setDescription("Affiche le classement d'argent").
                setUsage("/baltop - Affiche le classement").
                setRank(Rank.JOUEUR).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        sender.sendMessage(Text.BALTOP(ConsulatCore.getInstance().getPlayerBaltop().getBaltop(),
                SurvivalOffline::getName,
                SurvivalOffline::getMoney));
    }
}
