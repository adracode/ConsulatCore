package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.text.DecimalFormat;

public class MoneyCommand extends ConsulatCommand {
    
    private DecimalFormat df = new DecimalFormat();
    
    public MoneyCommand(){
        super("money", "/money", 0, Rank.JOUEUR);
        suggest(true);
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        df.setDecimalSeparatorAlwaysShown(true);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage(Text.PREFIX + "Tu as §e" + df.format(((SurvivalPlayer)sender).getMoney()) + " €.");
    }
}
