package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

public class MoneyCommand extends ConsulatCommand {
    
    public MoneyCommand(){
        super(ConsulatCore.getInstance(), "money");
        setDescription("Affiche l'argent que tu possèdes").
                setUsage("/money - Affiche l'argent").
                setRank(Rank.JOUEUR).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        sender.sendMessage(Text.MONEY(((SurvivalPlayer)sender).getMoney()));
    }
}
