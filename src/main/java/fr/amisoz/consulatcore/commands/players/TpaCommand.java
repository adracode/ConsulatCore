package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaCommand extends ConsulatCommand {
    
    private Map<UUID, UUID> request = new HashMap<>();
    
    public TpaCommand(){
        super("tpa", "/tpa <Joueur> | /tpa accept", 1, Rank.JOUEUR);
        suggest(true, LiteralArgumentBuilder.literal("accept")
                        .then(Arguments.player("joueur", request.keySet())),
                Arguments.player("joueur")
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        //tpa joueur = envoie une requête
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
            if(target.getUUID().equals(request.get(sender.getUUID()))){
                sender.sendMessage(Text.PREFIX + "§cTu as déjà fait une demande de téléportation à " + target.getName());
                return;
            }
            request.put(sender.getUUID(), target.getUUID());
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
                if(!sender.getUUID().equals(request.get(target.getUUID()))){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur ne t'as pas demandé en téléportation.");
                    return;
                }
                if(!target.hasPower(Rank.MECENE)){
                    request.remove(target.getUUID());
                    Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                        try {
                            target.removeMoney(10D);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                                target.getPlayer().teleport(sender.getPlayer());
                                target.sendMessage("§aTu as été téléporté à " + sender.getName() + " pour 10 €.");
                            });
                        } catch(SQLException e){
                            e.printStackTrace();
                        }
                    });
                } else {
                    target.getPlayer().teleport(sender.getPlayer());
                    request.remove(target.getUUID());
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
