package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.amisoz.consulatcore.players.SurvivalOffline;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.text.DecimalFormat;
import java.util.SortedSet;

public class BaltopCommand extends ConsulatCommand {
    
    private DecimalFormat formatMoney = new DecimalFormat("###,###,###");
    
    public BaltopCommand(){
        super("baltop", "/baltop", 0, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage("§e========= Baltop =========");
        SortedSet<BaltopManager.MoneyOwner> baltop = BaltopManager.getInstance().getBaltop();
        for(BaltopManager.MoneyOwner moneyOwner : baltop){
            sender.sendMessage("§6" + moneyOwner.getName() + ":§e " + formatMoney.format(moneyOwner.getMoney()));
        }
    }
}
