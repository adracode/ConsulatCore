package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class MoneyCommand extends ConsulatCommand {
    
    public MoneyCommand(){
        super("consulat.core", "money", "/money", 0, Rank.JOUEUR);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage(Text.PREFIX + "Tu as §e" + ConsulatCore.formatMoney(((SurvivalPlayer)sender).getMoney()) + ".");
    }
}
