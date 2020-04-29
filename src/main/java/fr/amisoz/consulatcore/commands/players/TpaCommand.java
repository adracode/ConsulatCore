package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TpaCommand extends ConsulatCommand {
    
    private Map<ConsulatPlayer, ConsulatPlayer> request = new HashMap<>();
    
    public TpaCommand(){
        super("tpa", "/tpa <Joueur> | /tpa accept", 1, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(args.length == 1){
            SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
            if(target == null){
                sender.sendMessage(Text.PREFIX + "§cLe joueur n'est pas connecté.");
                return;
            }
            if(!survivalSender.hasMoney(10.0) && !survivalSender.hasPower(Rank.MECENE)){
                sender.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent.");
                return;
            }
            if(target.equals(request.get(sender))){
                sender.sendMessage(Text.PREFIX + "§cTu as déjà fait une demande de téléportation à " + target.getName());
                return;
            }
            request.put(sender, target);
            sender.sendMessage(Text.PREFIX + "§aTu as fait une demande de téléportation à " + target.getName());
            target.sendMessage(Text.PREFIX + "§eTu as reçu une demande de téléportation de " + sender.getName());
            target.sendMessage(Text.PREFIX + "§eFais /tpa accept " + sender.getName());
        } else if(args.length == 2){
            if(args[0].equalsIgnoreCase("accept")){
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
                if(target == null){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur n'est pas connecté.");
                    return;
                }
                if(!(request.get(target) == sender)){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur ne t'as pas demandé en téléportation.");
                    return;
                }
                if(!target.hasPower(Rank.MECENE)){
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            target.removeMoney(10D);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), ()->{
                                target.getPlayer().teleport(sender.getPlayer());
                                request.remove(target);
                                target.sendMessage("§aTu as été téléporté à " + sender.getName() + " pour 10 €.");
                            });
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    });
                } else {
                    target.getPlayer().teleport(sender.getPlayer());
                    request.remove(target);
                    target.sendMessage("§aTu as été téléporté à " + sender.getName() + ".");
                }
            } else {
                sender.sendMessage(Text.PREFIX + "§c/tpa accept <joueur>");
            }
        } else {
            sender.sendMessage("§c" + getUsage());
        }
    }
}
