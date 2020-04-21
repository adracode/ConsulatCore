package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalOffline;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Optional;

public class PayCommand extends ConsulatCommand {
    
    public PayCommand(){
        super("/pay <Joueur> <Montant>", 2, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        double moneyToGive;
        try {
            moneyToGive = Double.parseDouble(args[1]);
        } catch(NumberFormatException exception){
            sender.sendMessage("§c" + getUsage());
            return;
        }
        if(!player.hasMoney(moneyToGive)){
            sender.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent !");
            return;
        }
        if(moneyToGive <= 0){
            sender.sendMessage(Text.PREFIX + "§cTu ne peux pas donner " + moneyToGive + " €.");
            return;
        }
        if(CPlayerManager.getInstance().getPlayerUUID(args[0]) == null){
            sender.sendMessage(Text.PREFIX + "§cCe joueur ne s'est jamais connecté sur le serveur !");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                Optional<SurvivalOffline> resultOffline = SPlayerManager.getInstance().fetchOffline(args[0]);
                if(!resultOffline.isPresent()){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur ne s'est jamais connecté sur le serveur !");
                    return;
                }
                SurvivalOffline offlineTarget = resultOffline.get();
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(offlineTarget.getUUID());
                if(target != null){
                    target.addMoney(moneyToGive);
                    target.getPlayer().sendMessage(Text.PREFIX + "§aTu as reçu §2" + moneyToGive + "€ §ade §2" + player.getName());
                } else {
                    SPlayerManager.getInstance().addMoney(offlineTarget.getUUID(), moneyToGive);
                }
                player.removeMoney(moneyToGive);
                sender.sendMessage(Text.PREFIX + "§aTu as envoyé §2" + moneyToGive + "€ §aà §2" + args[0]);
            } catch(SQLException e){
                sender.sendMessage("§cUne erreur interne est survenue. La transaction a échoué.");
                e.printStackTrace();
            }
        });
    }
}
