package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

public class BaltopCommand extends ConsulatCommand {
    
    public BaltopCommand(){
        super(ConsulatCore.getInstance(), "baltop");
        setDescription("Affiche le classement d'argent").
                setUsage("/baltop - Affiche le classement").
                setRank(Rank.ADMIN).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        sender.sendMessage(Text.BALTOP(BaltopManager.getInstance().getBaltop()));
    }
}
