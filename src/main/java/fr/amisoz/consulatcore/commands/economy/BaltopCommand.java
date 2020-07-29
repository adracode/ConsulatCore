package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.SortedSet;

public class BaltopCommand extends ConsulatCommand {
    
    public BaltopCommand(){
        super("baltop", "/baltop", 0, Rank.JOUEUR);
        suggest(true);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage("§e========= Baltop =========");
        SortedSet<BaltopManager.MoneyOwner> baltop = BaltopManager.getInstance().getBaltop();
        for(BaltopManager.MoneyOwner moneyOwner : baltop){
            sender.sendMessage("§6" + moneyOwner.getName() + ":§e " + ConsulatCore.formatMoney(moneyOwner.getMoney()));
        }
    }
}
