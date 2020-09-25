package fr.leconsulat.core.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.guis.pvp.PVPGui;
import org.jetbrains.annotations.NotNull;

public class PVPCommand extends ConsulatCommand {
    
    public PVPCommand(){
        super(ConsulatCore.getInstance(), "pvp");
        setDescription("Gérer son statut PVP").
                setUsage("/pvp - Gérer son statut PVP").
                setRank(Rank.JOUEUR).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        PVPGui.getPvpGui().open(sender);
    }
}
